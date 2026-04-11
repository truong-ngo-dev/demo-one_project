package vn.truongngo.apartcom.one.service.admin.application.resource.command.update_resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.AbacException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceId;

public class UpdateResourceDefinition {

    public record Command(Long id, String description, String serviceName) {
        public Command {
            Assert.notNull(id, "id is required");
            Assert.hasText(serviceName, "serviceName is required");
        }
    }

    public record Result(Long id) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final ResourceDefinitionRepository repository;

        @Override
        @Transactional
        public Result handle(Command command) {
            ResourceDefinition resource = repository.findById(ResourceId.of(command.id()))
                    .orElseThrow(AbacException::resourceNotFound);
            ResourceDefinition updated = resource.updateMeta(command.description(), command.serviceName());
            ResourceDefinition saved = repository.save(updated);
            return new Result(saved.getId().getValue());
        }
    }
}
