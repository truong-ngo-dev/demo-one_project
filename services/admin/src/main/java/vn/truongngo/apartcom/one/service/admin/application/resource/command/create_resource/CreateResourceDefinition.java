package vn.truongngo.apartcom.one.service.admin.application.resource.command.create_resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.AbacException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;

public class CreateResourceDefinition {

    public record Command(String name, String description, String serviceName) {
        public Command {
            Assert.hasText(name, "name is required");
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
            if (repository.existsByName(command.name())) {
                throw AbacException.resourceNameDuplicate();
            }
            ResourceDefinition resource = ResourceDefinition.create(
                    command.name(), command.description(), command.serviceName());
            ResourceDefinition saved = repository.save(resource);
            return new Result(saved.getId().getValue());
        }
    }
}
