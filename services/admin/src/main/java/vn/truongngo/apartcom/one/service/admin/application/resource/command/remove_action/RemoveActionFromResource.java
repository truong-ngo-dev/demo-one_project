package vn.truongngo.apartcom.one.service.admin.application.resource.command.remove_action;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.AbacException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ActionId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceId;

public class RemoveActionFromResource {

    public record Command(Long resourceId, Long actionId) {
        public Command {
            Assert.notNull(resourceId, "resourceId is required");
            Assert.notNull(actionId, "actionId is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final ResourceDefinitionRepository repository;

        @Override
        @Transactional
        public Void handle(Command command) {
            ResourceDefinition resource = repository.findById(ResourceId.of(command.resourceId()))
                    .orElseThrow(AbacException::resourceNotFound);
            // TODO: implement Batch 3 — check UIElement does not reference this action
            ResourceDefinition updated = resource.removeAction(ActionId.of(command.actionId()));
            repository.save(updated);
            return null;
        }
    }
}
