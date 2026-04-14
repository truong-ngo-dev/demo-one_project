package vn.truongngo.apartcom.one.service.admin.application.policy_set.delete_policy_set;

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
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetException;
import vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.audit.AuditHelper;

public class DeletePolicySet {

    public record Command(Long id) {
        public Command {
            Assert.notNull(id, "id is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final PolicySetRepository repository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Void handle(Command command) {
            PolicySetId id = PolicySetId.of(command.id());
            PolicySetDefinition policySet = repository.findById(id)
                    .orElseThrow(PolicySetException::policySetNotFound);
            repository.delete(id);
            eventDispatcher.dispatch(new AbacAuditLogEvent(
                    AuditEntityType.POLICY_SET, command.id(), policySet.getName(),
                    AuditActionType.DELETED, AuditHelper.currentPerformedBy(), null));
            return null;
        }
    }
}
