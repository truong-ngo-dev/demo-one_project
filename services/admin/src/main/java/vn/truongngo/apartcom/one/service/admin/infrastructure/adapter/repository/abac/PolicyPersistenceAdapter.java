package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.repository.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.ExpressionVO;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleDefinition;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.expression.AbacExpressionJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.expression.AbacExpressionJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy.PolicyJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy.PolicyJpaRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy.PolicyMapper;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.rule.RuleJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.rule.RuleJpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PolicyPersistenceAdapter implements PolicyRepository {

    private final PolicyJpaRepository policyJpaRepository;
    private final RuleJpaRepository ruleJpaRepository;
    private final AbacExpressionJpaRepository expressionJpaRepository;

    @Override
    public PolicyDefinition save(PolicyDefinition policy) {
        // Upsert target expression
        Long targetExprId = upsertExpression(policy.getTargetExpression());

        // Save policy entity
        PolicyJpaEntity entity = PolicyMapper.toEntity(policy);
        entity.setTargetExpressionId(targetExprId);
        PolicyJpaEntity savedPolicy = policyJpaRepository.save(entity);
        Long policyDbId = savedPolicy.getId();

        // Upsert rules: delete removed, update existing, insert new
        List<RuleJpaEntity> existingRules = policyDbId != null
                ? ruleJpaRepository.findByPolicyIdOrderByOrderIndex(policyDbId)
                : List.of();

        Set<Long> domainRuleIds = policy.getRules().stream()
                .filter(r -> r.getId() != null)
                .map(r -> r.getId().getValue())
                .collect(Collectors.toSet());

        // Delete rules removed from domain
        for (RuleJpaEntity existing : existingRules) {
            if (!domainRuleIds.contains(existing.getId())) {
                ruleJpaRepository.delete(existing);
            }
        }

        // Upsert domain rules
        List<RuleJpaEntity> savedRules = new ArrayList<>();
        for (RuleDefinition rule : policy.getRules()) {
            Long ruleTargetId = upsertExpression(rule.getTargetExpression());
            Long ruleConditionId = upsertExpression(rule.getConditionExpression());
            RuleJpaEntity ruleEntity = PolicyMapper.toRuleEntity(rule);
            ruleEntity.setPolicyId(policyDbId);
            ruleEntity.setTargetExpressionId(ruleTargetId);
            ruleEntity.setConditionExpressionId(ruleConditionId);
            savedRules.add(ruleJpaRepository.save(ruleEntity));
        }

        // Reconstruct domain
        AbacExpressionJpaEntity targetExprEntity = targetExprId != null
                ? expressionJpaRepository.findById(targetExprId).orElse(null) : null;
        return PolicyMapper.toDomain(savedPolicy, targetExprEntity, savedRules,
                id -> expressionJpaRepository.findById(id).orElse(null));
    }

    @Override
    public Optional<PolicyDefinition> findById(PolicyId id) {
        return policyJpaRepository.findById(id.getValue())
                .map(entity -> {
                    List<RuleJpaEntity> rules = ruleJpaRepository
                            .findByPolicyIdOrderByOrderIndex(entity.getId());
                    AbacExpressionJpaEntity target = loadExpr(entity.getTargetExpressionId());
                    return PolicyMapper.toDomain(entity, target, rules,
                            exprId -> expressionJpaRepository.findById(exprId).orElse(null));
                });
    }

    @Override
    public List<PolicyDefinition> findByPolicySetId(PolicySetId policySetId) {
        return policyJpaRepository.findByPolicySetId(policySetId.getValue())
                .stream()
                .map(entity -> {
                    List<RuleJpaEntity> rules = ruleJpaRepository
                            .findByPolicyIdOrderByOrderIndex(entity.getId());
                    AbacExpressionJpaEntity target = loadExpr(entity.getTargetExpressionId());
                    return PolicyMapper.toDomain(entity, target, rules,
                            exprId -> expressionJpaRepository.findById(exprId).orElse(null));
                })
                .toList();
    }

    @Override
    public void delete(PolicyId id) {
        policyJpaRepository.deleteById(id.getValue());
    }

    private AbacExpressionJpaEntity loadExpr(Long id) {
        if (id == null) return null;
        return expressionJpaRepository.findById(id).orElse(null);
    }

    private Long upsertExpression(ExpressionVO expressionVO) {
        if (expressionVO == null) return null;
        AbacExpressionJpaEntity expr = expressionVO.id() != null
                ? expressionJpaRepository.findById(expressionVO.id()).orElse(new AbacExpressionJpaEntity())
                : new AbacExpressionJpaEntity();
        expr.setType(AbacExpressionJpaEntity.ExpressionType.LITERAL);
        expr.setSpelExpression(expressionVO.spElExpression());
        expr.setCombinationType(null);
        expr.setParentId(null);
        return expressionJpaRepository.save(expr).getId();
    }
}
