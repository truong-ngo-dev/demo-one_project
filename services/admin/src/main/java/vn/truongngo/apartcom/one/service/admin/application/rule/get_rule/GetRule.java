package vn.truongngo.apartcom.one.service.admin.application.rule.get_rule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.application.expression.ExpressionTreeService;
import vn.truongngo.apartcom.one.service.admin.domain.abac.expression.NamedExpressionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleId;
import vn.truongngo.apartcom.one.service.admin.presentation.abac.model.ExpressionNodeView;

public class GetRule {

    public record Query(Long policyId, Long ruleId) {
        public Query {
            Assert.notNull(policyId, "policyId is required");
            Assert.notNull(ruleId, "ruleId is required");
        }
    }

    public record Result(Long id, Long policyId, String name, String description,
                         ExpressionNodeView targetExpression, ExpressionNodeView conditionExpression,
                         String effect, int orderIndex, long createdAt, long updatedAt) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Result> {

        private final PolicyRepository policyRepository;
        private final ExpressionTreeService expressionTreeService;
        private final NamedExpressionRepository namedExpressionRepository;

        @Override
        @Transactional(readOnly = true)
        public Result handle(Query query) {
            RuleId ruleId = RuleId.of(query.ruleId());
            RuleDefinition rule = policyRepository.findById(PolicyId.of(query.policyId()))
                    .orElseThrow(PolicyException::policyNotFound)
                    .getRules().stream()
                    .filter(r -> ruleId.equals(r.getId()))
                    .findFirst()
                    .orElseThrow(PolicyException::ruleNotFound);
            return toResult(rule, expressionTreeService, namedExpressionRepository);
        }

        public static Result toResult(RuleDefinition rule,
                                       ExpressionTreeService treeService,
                                       NamedExpressionRepository namedRepo) {
            return new Result(
                    rule.getId().getValue(),
                    rule.getPolicyId().getValue(),
                    rule.getName(),
                    rule.getDescription(),
                    ExpressionNodeView.from(rule.getTargetExpression(), treeService, namedRepo),
                    ExpressionNodeView.from(rule.getConditionExpression(), treeService, namedRepo),
                    rule.getEffect().name(),
                    rule.getOrderIndex(),
                    rule.getCreatedAt(),
                    rule.getUpdatedAt());
        }
    }
}
