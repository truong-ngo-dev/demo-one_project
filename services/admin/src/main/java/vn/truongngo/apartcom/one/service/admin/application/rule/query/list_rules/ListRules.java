package vn.truongngo.apartcom.one.service.admin.application.rule.query.list_rules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.AbacPolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.application.rule.query.get_rule.GetRule;

import java.util.List;

public class ListRules {

    public record Query(Long policyId) {
        public Query {
            Assert.notNull(policyId, "policyId is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, List<GetRule.Result>> {

        private final PolicyRepository policyRepository;

        @Override
        @Transactional(readOnly = true)
        public List<GetRule.Result> handle(Query query) {
            return policyRepository.findById(PolicyId.of(query.policyId()))
                    .orElseThrow(AbacPolicyException::policyNotFound)
                    .getRules()
                    .stream()
                    .map(GetRule.Handler::toResult)
                    .toList();
        }
    }
}
