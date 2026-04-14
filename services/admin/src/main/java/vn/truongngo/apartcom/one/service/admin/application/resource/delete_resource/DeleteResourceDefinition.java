package vn.truongngo.apartcom.one.service.admin.application.resource.delete_resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceId;

public class DeleteResourceDefinition {

    public record Command(Long id) {
        public Command {
            Assert.notNull(id, "id is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final ResourceDefinitionRepository repository;

        @Override
        @Transactional
        public Void handle(Command command) {
            ResourceId resourceId = ResourceId.of(command.id());
            if (repository.findById(resourceId).isEmpty()) {
                throw ResourceException.resourceNotFound();
            }
            if (repository.existsByIdWithPolicyRef(resourceId)) {
                throw ResourceException.resourceInUse();
            }
            if (repository.existsByIdWithUIElementRef(resourceId)) {
                throw ResourceException.resourceInUse();
            }
            repository.delete(resourceId);
            return null;
        }
    }
}
