package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.resource;

import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ActionDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ActionId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceId;

import java.util.List;

public class ResourceDefinitionMapper {

    public static ResourceDefinition toDomain(ResourceDefinitionJpaEntity entity) {
        List<ActionDefinition> actions = entity.getActions().stream()
                .map(a -> ActionDefinition.reconstitute(
                        ActionId.of(a.getId()),
                        ResourceId.of(entity.getId()),
                        a.getName(),
                        a.getDescription(),
                        a.isStandard()))
                .toList();
        return ResourceDefinition.reconstitute(
                ResourceId.of(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                entity.getServiceName(),
                actions,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public static ResourceDefinitionJpaEntity toEntity(ResourceDefinition domain) {
        ResourceDefinitionJpaEntity entity = new ResourceDefinitionJpaEntity();
        entity.setId(domain.getId() != null ? domain.getId().getValue() : null);
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setServiceName(domain.getServiceName());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        List<ActionDefinitionJpaEntity> actionEntities = domain.getActions().stream()
                .map(a -> toActionEntity(a, entity))
                .toList();
        entity.getActions().clear();
        entity.getActions().addAll(actionEntities);

        return entity;
    }

    private static ActionDefinitionJpaEntity toActionEntity(ActionDefinition domain,
                                                             ResourceDefinitionJpaEntity resourceEntity) {
        ActionDefinitionJpaEntity entity = new ActionDefinitionJpaEntity();
        entity.setId(domain.getId() != null ? domain.getId().getValue() : null);
        entity.setResource(resourceEntity);
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setStandard(domain.isStandard());
        return entity;
    }
}
