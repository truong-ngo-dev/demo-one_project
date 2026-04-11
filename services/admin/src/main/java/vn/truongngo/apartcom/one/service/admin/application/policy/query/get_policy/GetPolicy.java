package vn.truongngo.apartcom.one.service.admin.application.policy.query.get_policy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.AbacPolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;

import java.util.List;

public class GetPolicy {

    public record Query(Long id) {
        public Query {
            Assert.notNull(id, "id is required");
        }
    }

    public record RuleView(Long id, String name, String description,
                           String targetExpression, String conditionExpression,
                           String effect, int orderIndex) {}

    public record Result(Long id, Long policySetId, String name,
                         String targetExpression, String combineAlgorithm,
                         long createdAt, long updatedAt, List<RuleView> rules) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Result> {

        private final PolicyRepository repository;

        @Override
        @Transactional(readOnly = true)
        public Result handle(Query query) {
            PolicyDefinition policy = repository.findById(PolicyId.of(query.id()))
                    .orElseThrow(AbacPolicyException::policyNotFound);
            List<RuleView> rules = policy.getRules().stream()
                    .map(r -> new RuleView(
                            r.getId().getValue(), r.getName(), r.getDescription(),
                            r.getTargetExpression() != null ? r.getTargetExpression().spElExpression() : null,
                            r.getConditionExpression() != null ? r.getConditionExpression().spElExpression() : null,
                            r.getEffect().name(), r.getOrderIndex()))
                    .toList();
            return new Result(
                    policy.getId().getValue(), policy.getPolicySetId().getValue(),
                    policy.getName(),
                    policy.getTargetExpression() != null ? policy.getTargetExpression().spElExpression() : null,
                    policy.getCombineAlgorithm().name(),
                    policy.getCreatedAt(), policy.getUpdatedAt(), rules);
        }
    }
}
