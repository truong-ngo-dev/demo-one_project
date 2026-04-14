package vn.truongngo.apartcom.one.service.admin.application.policy_set.get_policy_set;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetException;

import java.util.List;

public class GetPolicySet {

    public record Query(Long id) {
        public Query {
            Assert.notNull(id, "id is required");
        }
    }

    public record PolicySummary(Long id, String name, String combineAlgorithm) {}

    public record Result(Long id, String name, String scope, String combineAlgorithm,
                         boolean isRoot, String tenantId,
                         long createdAt, long updatedAt,
                         List<PolicySummary> policies) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Result> {

        private final PolicySetRepository policySetRepository;
        private final PolicyRepository policyRepository;

        @Override
        @Transactional(readOnly = true)
        public Result handle(Query query) {
            PolicySetDefinition ps = policySetRepository.findById(PolicySetId.of(query.id()))
                    .orElseThrow(PolicySetException::policySetNotFound);
            List<PolicySummary> policies = policyRepository
                    .findByPolicySetId(ps.getId())
                    .stream()
                    .map(p -> new PolicySummary(p.getId().getValue(), p.getName(),
                            p.getCombineAlgorithm().name()))
                    .toList();
            return new Result(
                    ps.getId().getValue(), ps.getName(), ps.getScope().name(),
                    ps.getCombineAlgorithm().name(), ps.isRoot(), ps.getTenantId(),
                    ps.getCreatedAt(), ps.getUpdatedAt(), policies);
        }
    }
}
