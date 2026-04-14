package vn.truongngo.apartcom.one.service.admin.application.rule.delete_rule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AbacAuditLogEvent;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditActionType;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditEntityType;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleId;
import vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.audit.AuditHelper;

public class DeleteRule {

    public record Command(Long policyId, Long ruleId) {
        public Command {
            Assert.notNull(policyId, "policyId is required");
            Assert.notNull(ruleId, "ruleId is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final PolicyRepository policyRepository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Void handle(Command command) {
            PolicyDefinition policy = policyRepository.findById(PolicyId.of(command.policyId()))
                    .orElseThrow(PolicyException::policyNotFound);
            RuleId ruleId = RuleId.of(command.ruleId());
            RuleDefinition rule = policy.getRules().stream()
                    .filter(r -> ruleId.equals(r.getId()))
                    .findFirst()
                    .orElseThrow(PolicyException::ruleNotFound);

            PolicyDefinition updated = policy.removeRule(ruleId);
            policyRepository.save(updated);

            eventDispatcher.dispatch(new AbacAuditLogEvent(
                    AuditEntityType.RULE, command.ruleId(), rule.getName(),
                    AuditActionType.DELETED, AuditHelper.currentPerformedBy(), null));
            return null;
        }
    }
}
