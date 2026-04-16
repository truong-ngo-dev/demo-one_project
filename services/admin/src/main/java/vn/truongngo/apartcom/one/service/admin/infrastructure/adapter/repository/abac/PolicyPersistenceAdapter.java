package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.repository.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.admin.application.expression.ExpressionTreeService;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.ExpressionNode;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy.PolicyJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy.PolicyJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy.PolicyMapper;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.rule.RuleJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.rule.RuleJpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PolicyPersistenceAdapter implements PolicyRepository {

    private final PolicyJpaRepository policyJpaRepository;
    private final RuleJpaRepository ruleJpaRepository;
    private final ExpressionTreeService expressionTreeService;

    @Override
    public PolicyDefinition save(PolicyDefinition policy) {
        Long policyDbId = policy.getId() != null ? policy.getId().getValue() : null;

        // --- Policy target expression ---
        Long oldPolicyTargetId = null;
        if (policyDbId != null) {
            oldPolicyTargetId = policyJpaRepository.findById(policyDbId)
                    .map(PolicyJpaEntity::getTargetExpressionId).orElse(null);
        }
        Long newPolicyTargetId = expressionTreeService.persist(policy.getTargetExpression(), null);

        // Save policy entity
        PolicyJpaEntity entity = PolicyMapper.toEntity(policy, newPolicyTargetId);
        PolicyJpaEntity savedPolicy = policyJpaRepository.save(entity);
        Long savedPolicyId = savedPolicy.getId();

        // Cleanup old policy target expression tree if changed
        if (oldPolicyTargetId != null && !Objects.equals(oldPolicyTargetId, newPolicyTargetId)) {
            expressionTreeService.deleteTree(oldPolicyTargetId);
        }

        // --- Rules ---
        List<RuleJpaEntity> existingRules = ruleJpaRepository.findByPolicyIdOrderByOrderIndex(savedPolicyId);

        Set<Long> domainRuleIds = policy.getRules().stream()
                .filter(r -> r.getId() != null)
                .map(r -> r.getId().getValue())
                .collect(Collectors.toSet());

        // Delete rules removed from domain (also clean up their expression trees)
        for (RuleJpaEntity existing : existingRules) {
            if (!domainRuleIds.contains(existing.getId())) {
                expressionTreeService.deleteTree(existing.getTargetExpressionId());
                expressionTreeService.deleteTree(existing.getConditionExpressionId());
                ruleJpaRepository.delete(existing);
            }
        }

        // Upsert domain rules
        List<RuleJpaEntity> savedRules = new ArrayList<>();
        for (RuleDefinition rule : policy.getRules()) {
            Long ruleDbId = rule.getId() != null ? rule.getId().getValue() : null;

            // Capture old expression ids before overwriting
            Long oldTargetId = null;
            Long oldConditionId = null;
            if (ruleDbId != null) {
                var existingRule = ruleJpaRepository.findById(ruleDbId);
                if (existingRule.isPresent()) {
                    oldTargetId = existingRule.get().getTargetExpressionId();
                    oldConditionId = existingRule.get().getConditionExpressionId();
                }
            }

            Long ruleTargetId = expressionTreeService.persist(rule.getTargetExpression(), null);
            Long ruleConditionId = expressionTreeService.persist(rule.getConditionExpression(), null);

            RuleJpaEntity ruleEntity = PolicyMapper.toRuleEntity(rule, ruleTargetId, ruleConditionId);
            ruleEntity.setPolicyId(savedPolicyId);
            savedRules.add(ruleJpaRepository.save(ruleEntity));

            // Cleanup old expression trees if replaced
            if (oldTargetId != null && !Objects.equals(oldTargetId, ruleTargetId)) {
                expressionTreeService.deleteTree(oldTargetId);
            }
            if (oldConditionId != null && !Objects.equals(oldConditionId, ruleConditionId)) {
                expressionTreeService.deleteTree(oldConditionId);
            }
        }

        // Reconstruct domain
        return PolicyMapper.toDomain(savedPolicy, savedRules,
                id -> expressionTreeService.loadTree(id));
    }

    @Override
    public Optional<PolicyDefinition> findById(PolicyId id) {
        return policyJpaRepository.findById(id.getValue())
                .map(entity -> {
                    List<RuleJpaEntity> rules = ruleJpaRepository
                            .findByPolicyIdOrderByOrderIndex(entity.getId());
                    return PolicyMapper.toDomain(entity, rules,
                            exprId -> expressionTreeService.loadTree(exprId));
                });
    }

    @Override
    public List<PolicyDefinition> findByPolicySetId(PolicySetId policySetId) {
        return policyJpaRepository.findByPolicySetId(policySetId.getValue())
                .stream()
                .map(entity -> {
                    List<RuleJpaEntity> rules = ruleJpaRepository
                            .findByPolicyIdOrderByOrderIndex(entity.getId());
                    return PolicyMapper.toDomain(entity, rules,
                            exprId -> expressionTreeService.loadTree(exprId));
                })
                .toList();
    }

    @Override
    public void delete(PolicyId id) {
        // Clean up expression trees for all rules and the policy itself before deletion
        policyJpaRepository.findById(id.getValue()).ifPresent(entity -> {
            expressionTreeService.deleteTree(entity.getTargetExpressionId());
            List<RuleJpaEntity> rules = ruleJpaRepository.findByPolicyIdOrderByOrderIndex(id.getValue());
            for (RuleJpaEntity rule : rules) {
                expressionTreeService.deleteTree(rule.getTargetExpressionId());
                expressionTreeService.deleteTree(rule.getConditionExpressionId());
            }
        });
        policyJpaRepository.deleteById(id.getValue());
    }
}
