package vn.truongngo.apartcom.one.service.admin.application.policy_set.query.list_policy_sets;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetRepository;

public class ListPolicySets {

    public record Query(String keyword, Pageable pageable) {}

    public record PolicySetSummary(Long id, String name, String scope,
                                   String combineAlgorithm, boolean isRoot,
                                   String tenantId, long createdAt, long updatedAt) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Page<PolicySetSummary>> {

        private final PolicySetRepository repository;

        @Override
        @Transactional(readOnly = true)
        public Page<PolicySetSummary> handle(Query query) {
            return repository.findAll(query.keyword(), query.pageable())
                    .map(ps -> new PolicySetSummary(
                            ps.getId().getValue(), ps.getName(), ps.getScope().name(),
                            ps.getCombineAlgorithm().name(), ps.isRoot(),
                            ps.getTenantId(), ps.getCreatedAt(), ps.getUpdatedAt()));
        }
    }
}
