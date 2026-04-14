package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.policy;

import vn.truongngo.apartcom.one.lib.abac.algorithm.CombineAlgorithmName;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Effect;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.ExpressionVO;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleId;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.expression.AbacExpressionJpaEntity;
import vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.rule.RuleJpaEntity;

import java.util.List;

public class PolicyMapper {

    public static PolicyDefinition toDomain(PolicyJpaEntity entity,
                                             AbacExpressionJpaEntity targetExpr,
                                             List<RuleJpaEntity> ruleEntities,
                                             java.util.function.Function<Long, AbacExpressionJpaEntity> exprLoader) {
        ExpressionVO target = targetExpr != null
                ? ExpressionVO.reconstitute(targetExpr.getId(), targetExpr.getSpelExpression()) : null;
        List<RuleDefinition> rules = ruleEntities.stream()
                .map(r -> toRuleDomain(r, exprLoader))
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
                                               java.util.function.Function<Long, AbacExpressionJpaEntity> exprLoader) {
        ExpressionVO target = r.getTargetExpressionId() != null
                ? toExprVO(exprLoader.apply(r.getTargetExpressionId())) : null;
        ExpressionVO condition = r.getConditionExpressionId() != null
                ? toExprVO(exprLoader.apply(r.getConditionExpressionId())) : null;
        return RuleDefinition.reconstitute(
                RuleId.of(r.getId()), PolicyId.of(r.getPolicyId()),
                r.getName(), r.getDescription(),
                target, condition,
                Effect.valueOf(r.getEffect()), r.getOrderIndex(),
                r.getCreatedAt(), r.getUpdatedAt());
    }

    private static ExpressionVO toExprVO(AbacExpressionJpaEntity expr) {
        if (expr == null) return null;
        return ExpressionVO.reconstitute(expr.getId(), expr.getSpelExpression());
    }

    public static PolicyJpaEntity toEntity(PolicyDefinition domain) {
        PolicyJpaEntity entity = new PolicyJpaEntity();
        entity.setId(domain.getId() != null ? domain.getId().getValue() : null);
        entity.setPolicySetId(domain.getPolicySetId().getValue());
        entity.setName(domain.getName());
        entity.setTargetExpressionId(domain.getTargetExpression() != null
                ? domain.getTargetExpression().id() : null);
        entity.setCombineAlgorithm(domain.getCombineAlgorithm().name());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public static RuleJpaEntity toRuleEntity(RuleDefinition domain) {
        RuleJpaEntity entity = new RuleJpaEntity();
        entity.setId(domain.getId() != null ? domain.getId().getValue() : null);
        entity.setPolicyId(domain.getPolicyId().getValue());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setTargetExpressionId(domain.getTargetExpression() != null
                ? domain.getTargetExpression().id() : null);
        entity.setConditionExpressionId(domain.getConditionExpression() != null
                ? domain.getConditionExpression().id() : null);
        entity.setEffect(domain.getEffect().name());
        entity.setOrderIndex(domain.getOrderIndex());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
