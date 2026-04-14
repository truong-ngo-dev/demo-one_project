package vn.truongngo.apartcom.one.service.admin.application.rule.reorder_rules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;

import java.util.List;

public class ReorderRules {

    public record Command(Long policyId, List<Long> orderedRuleIds) {
        public Command {
            Assert.notNull(policyId, "policyId is required");
            Assert.notNull(orderedRuleIds, "orderedRuleIds is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final PolicyRepository policyRepository;

        @Override
        @Transactional
        public Void handle(Command command) {
            PolicyDefinition policy = policyRepository.findById(PolicyId.of(command.policyId()))
                    .orElseThrow(PolicyException::policyNotFound);
            PolicyDefinition reordered = policy.reorderRules(command.orderedRuleIds());
            policyRepository.save(reordered);
            return null;
        }
    }
}
