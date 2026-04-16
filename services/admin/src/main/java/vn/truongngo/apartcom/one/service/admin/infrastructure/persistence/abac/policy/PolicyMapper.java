package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy;

import vn.truongngo.apartcom.one.lib.abac.algorithm.CombineAlgorithmName;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Effect;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.ExpressionNode;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleId;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.rule.RuleJpaEntity;

import java.util.List;
import java.util.function.Function;

public class PolicyMapper {

    public static PolicyDefinition toDomain(PolicyJpaEntity entity,
                                             List<RuleJpaEntity> ruleEntities,
                                             Function<Long, ExpressionNode> treeLoader) {
        ExpressionNode target = treeLoader.apply(entity.getTargetExpressionId());
        List<RuleDefinition> rules = ruleEntities.stream()
                .map(r -> toRuleDomain(r, treeLoader))
                .toList();
        return PolicyDefinition.reconstitute(
                PolicyId.of(entity.getId()),
                PolicySetId.of(entity.getPolicySetId()),
                entity.getName(),
                target,
                CombineAlgorithmName.valueOf(entity.getCombineAlgorithm()),
                rules,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public static RuleDefinition toRuleDomain(RuleJpaEntity r,
                                               Function<Long, ExpressionNode> treeLoader) {
        ExpressionNode target = treeLoader.apply(r.getTargetExpressionId());
        ExpressionNode condition = treeLoader.apply(r.getConditionExpressionId());
        return RuleDefinition.reconstitute(
                RuleId.of(r.getId()), PolicyId.of(r.getPolicyId()),
                r.getName(), r.getDescription(),
                target, condition,
                Effect.valueOf(r.getEffect()), r.getOrderIndex(),
                r.getCreatedAt(), r.getUpdatedAt());
    }

    public static PolicyJpaEntity toEntity(PolicyDefinition domain, Long targetExpressionId) {
        PolicyJpaEntity entity = new PolicyJpaEntity();
        entity.setId(domain.getId() != null ? domain.getId().getValue() : null);
        entity.setPolicySetId(domain.getPolicySetId().getValue());
        entity.setName(domain.getName());
        entity.setTargetExpressionId(targetExpressionId);
        entity.setCombineAlgorithm(domain.getCombineAlgorithm().name());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public static RuleJpaEntity toRuleEntity(RuleDefinition domain,
                                              Long targetExpressionId,
                                              Long conditionExpressionId) {
        RuleJpaEntity entity = new RuleJpaEntity();
        entity.setId(domain.getId() != null ? domain.getId().getValue() : null);
        entity.setPolicyId(domain.getPolicyId().getValue());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setTargetExpressionId(targetExpressionId);
        entity.setConditionExpressionId(conditionExpressionId);
        entity.setEffect(domain.getEffect().name());
        entity.setOrderIndex(domain.getOrderIndex());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
