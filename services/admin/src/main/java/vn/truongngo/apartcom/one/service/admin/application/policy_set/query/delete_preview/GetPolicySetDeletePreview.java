package vn.truongngo.apartcom.one.service.admin.application.policy_set.query.delete_preview;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.AbacPolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicySetRepository;

import java.util.List;

public class GetPolicySetDeletePreview {

    public record Query(Long policySetId) {
        public Query {
            Assert.notNull(policySetId, "policySetId is required");
        }
    }

    public record Result(int policyCount, int ruleCount) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Result> {

        private final PolicySetRepository policySetRepository;
        private final PolicyRepository policyRepository;

        @Override
        @Transactional(readOnly = true)
        public Result handle(Query query) {
            PolicySetId id = PolicySetId.of(query.policySetId());
            policySetRepository.findById(id).orElseThrow(AbacPolicyException::policySetNotFound);
            List<PolicyDefinition> policies = policyRepository.findByPolicySetId(id);
            int ruleCount = policies.stream().mapToInt(p -> p.getRules().size()).sum();
            return new Result(policies.size(), ruleCount);
        }
    }
}
