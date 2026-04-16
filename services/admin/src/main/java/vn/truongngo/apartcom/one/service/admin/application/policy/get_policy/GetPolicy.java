package vn.truongngo.apartcom.one.service.admin.application.policy.get_policy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.application.expression.ExpressionTreeService;
import vn.truongngo.apartcom.one.service.admin.application.rule.get_rule.GetRule;
import vn.truongngo.apartcom.one.service.admin.domain.abac.expression.NamedExpressionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.presentation.abac.model.ExpressionNodeView;

import java.util.List;

public class GetPolicy {

    public record Query(Long id) {
        public Query {
            Assert.notNull(id, "id is required");
        }
    }

    public record Result(Long id, Long policySetId, String name,
                         ExpressionNodeView targetExpression, String combineAlgorithm,
                         long createdAt, long updatedAt, List<GetRule.Result> rules) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Result> {

        private final PolicyRepository repository;
        private final ExpressionTreeService expressionTreeService;
        private final NamedExpressionRepository namedExpressionRepository;

        @Override
        @Transactional(readOnly = true)
        public Result handle(Query query) {
            PolicyDefinition policy = repository.findById(PolicyId.of(query.id()))
                    .orElseThrow(PolicyException::policyNotFound);
            List<GetRule.Result> rules = policy.getRules().stream()
                    .map(r -> GetRule.Handler.toResult(r, expressionTreeService, namedExpressionRepository))
                    .toList();
            return new Result(
                    policy.getId().getValue(), policy.getPolicySetId().getValue(),
                    policy.getName(),
                    ExpressionNodeView.from(policy.getTargetExpression(), expressionTreeService, namedExpressionRepository),
                    policy.getCombineAlgorithm().name(),
                    policy.getCreatedAt(), policy.getUpdatedAt(), rules);
        }
    }
}
