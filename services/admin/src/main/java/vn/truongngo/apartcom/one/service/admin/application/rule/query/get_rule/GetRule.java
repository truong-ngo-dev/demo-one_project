package vn.truongngo.apartcom.one.service.admin.application.rule.query.get_rule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.AbacPolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleId;

public class GetRule {

    public record Query(Long policyId, Long ruleId) {
        public Query {
            Assert.notNull(policyId, "policyId is required");
            Assert.notNull(ruleId, "ruleId is required");
        }
    }

    public record Result(Long id, Long policyId, String name, String description,
                         String targetExpression, String conditionExpression,
                         String effect, int orderIndex, long createdAt, long updatedAt) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Result> {

        private final PolicyRepository policyRepository;

        @Override
        @Transactional(readOnly = true)
        public Result handle(Query query) {
            RuleId ruleId = RuleId.of(query.ruleId());
            RuleDefinition rule = policyRepository.findById(PolicyId.of(query.policyId()))
                    .orElseThrow(AbacPolicyException::policyNotFound)
                    .getRules().stream()
                    .filter(r -> ruleId.equals(r.getId()))
                    .findFirst()
                    .orElseThrow(AbacPolicyException::ruleNotFound);
            return toResult(rule);
        }

        public static Result toResult(RuleDefinition rule) {
            return new Result(
                    rule.getId().getValue(),
                    rule.getPolicyId().getValue(),
                    rule.getName(),
                    rule.getDescription(),
                    rule.getTargetExpression() != null ? rule.getTargetExpression().spElExpression() : null,
                    rule.getConditionExpression() != null ? rule.getConditionExpression().spElExpression() : null,
                    rule.getEffect().name(),
                    rule.getOrderIndex(),
                    rule.getCreatedAt(),
                    rule.getUpdatedAt());
        }
    }
}
