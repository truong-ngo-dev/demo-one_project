package vn.truongngo.apartcom.one.service.admin.application.resource.get_resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ActionDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceId;

import java.util.List;

public class GetResourceDefinition {

    public record Query(Long id) {
        public Query {
            Assert.notNull(id, "id is required");
        }
    }

    public record ActionView(Long id, String name, String description, boolean isStandard) {}

    public record ResourceView(Long id, String name, String description,
                               String serviceName, List<ActionView> actions) {}

    static class Mapper {
        static ActionView toActionView(ActionDefinition action) {
            return new ActionView(
                    action.getId() != null ? action.getId().getValue() : null,
                    action.getName(),
                    action.getDescription(),
                    action.isStandard());
        }

        static ResourceView toResourceView(ResourceDefinition resource) {
            List<ActionView> actions = resource.getActions().stream()
                    .map(Mapper::toActionView)
                    .toList();
            return new ResourceView(
                    resource.getId() != null ? resource.getId().getValue() : null,
                    resource.getName(),
                    resource.getDescription(),
                    resource.getServiceName(),
                    actions);
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, ResourceView> {

        private final ResourceDefinitionRepository repository;

        @Override
        public ResourceView handle(Query query) {
            return repository.findById(ResourceId.of(query.id()))
                    .map(Mapper::toResourceView)
                    .orElseThrow(ResourceException::resourceNotFound);
        }
    }
}
