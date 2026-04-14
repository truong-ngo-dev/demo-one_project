package vn.truongngo.apartcom.one.service.admin.application.policy.delete_preview;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;

public class GetPolicyDeletePreview {

    public record Query(Long policyId) {
        public Query {
            Assert.notNull(policyId, "policyId is required");
        }
    }

    public record Result(int ruleCount) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Result> {

        private final PolicyRepository policyRepository;

        @Override
        @Transactional(readOnly = true)
        public Result handle(Query query) {
            return policyRepository.findById(PolicyId.of(query.policyId()))
                    .map(policy -> new Result(policy.getRules().size()))
                    .orElseThrow(PolicyException::policyNotFound);
        }
    }
}
