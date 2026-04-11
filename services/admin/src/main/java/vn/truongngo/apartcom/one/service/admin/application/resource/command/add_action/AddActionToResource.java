package vn.truongngo.apartcom.one.service.admin.application.resource.command.add_action;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.AbacException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ActionDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceId;

public class AddActionToResource {

    public record Command(Long resourceId, String name, String description, boolean isStandard) {
        public Command {
            Assert.notNull(resourceId, "resourceId is required");
            Assert.hasText(name, "name is required");
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
            ResourceDefinition updated = resource.addAction(
                    command.name(), command.description(), command.isStandard());
            ResourceDefinition saved = repository.save(updated);

            ActionDefinition created = saved.getActions().stream()
                    .filter(a -> a.getName().equalsIgnoreCase(command.name()))
                    .findFirst()
                    .orElseThrow(AbacException::actionNotFound);
            return new Result(created.getId().getValue());
        }
    }
}
