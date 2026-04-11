package vn.truongngo.apartcom.one.service.admin.application.policy.query.list_policies;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetId;

import java.util.List;

public class ListPolicies {

    public record Query(Long policySetId) {
        public Query {
            Assert.notNull(policySetId, "policySetId is required");
        }
    }

    public record PolicySummary(Long id, String name, String combineAlgorithm,
                                String targetExpression, long createdAt, long updatedAt) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, List<PolicySummary>> {

        private final PolicyRepository repository;

        @Override
        @Transactional(readOnly = true)
        public List<PolicySummary> handle(Query query) {
            return repository.findByPolicySetId(PolicySetId.of(query.policySetId()))
                    .stream()
                    .map(p -> new PolicySummary(
                            p.getId().getValue(), p.getName(), p.getCombineAlgorithm().name(),
                            p.getTargetExpression() != null ? p.getTargetExpression().spElExpression() : null,
                            p.getCreatedAt(), p.getUpdatedAt()))
                    .toList();
        }
    }
}
