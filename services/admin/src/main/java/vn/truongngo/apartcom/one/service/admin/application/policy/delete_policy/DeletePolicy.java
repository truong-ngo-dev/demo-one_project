package vn.truongngo.apartcom.one.service.admin.application.policy.delete_policy;

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
import vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.audit.AuditHelper;

public class DeletePolicy {

    public record Command(Long id) {
        public Command {
            Assert.notNull(id, "id is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final PolicyRepository repository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Void handle(Command command) {
            PolicyId id = PolicyId.of(command.id());
            PolicyDefinition policy = repository.findById(id)
                    .orElseThrow(PolicyException::policyNotFound);
            repository.delete(id);

            eventDispatcher.dispatch(new AbacAuditLogEvent(
                    AuditEntityType.POLICY, command.id(), policy.getName(),
                    AuditActionType.DELETED, AuditHelper.currentPerformedBy(), null));
            return null;
        }
    }
}
