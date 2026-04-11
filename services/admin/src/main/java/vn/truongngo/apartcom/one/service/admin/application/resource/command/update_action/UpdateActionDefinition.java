package vn.truongngo.apartcom.one.service.admin.application.resource.command.update_action;

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

public class UpdateActionDefinition {

    public record Command(Long resourceId, Long actionId, String description) {
        public Command {
            Assert.notNull(resourceId, "resourceId is required");
            Assert.notNull(actionId, "actionId is required");
        }
    }

    public record Result(Long actionId) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final ResourceDefinitionRepository repository;

        @Override
        @Transactional
        public Result handle(Command command) {
            ResourceDefinition resource = repository.findById(ResourceId.of(command.resourceId()))
                    .orElseThrow(AbacException::resourceNotFound);
            ResourceDefinition updated = resource.updateAction(
                    ActionId.of(command.actionId()), command.description());
            repository.save(updated);
            return new Result(command.actionId());
        }
    }
}
