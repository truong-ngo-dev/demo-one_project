package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.abac.uielement;

import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ActionId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElement;

public class UIElementMapper {

    public static UIElement toDomain(UIElementJpaEntity entity) {
        return UIElement.reconstitute(
                entity.getId(),
                entity.getElementId(),
                entity.getLabel(),
                entity.getType(),
                entity.getScope(),
                entity.getElementGroup(),
                entity.getOrderIndex(),
                ResourceId.of(entity.getResourceId()),
                ActionId.of(entity.getActionId())
        );
    }

    public static UIElementJpaEntity toEntity(UIElement domain) {
        UIElementJpaEntity entity = new UIElementJpaEntity();
        entity.setId(domain.getId());
        entity.setElementId(domain.getElementId());
        entity.setLabel(domain.getLabel());
        entity.setType(domain.getType());
        entity.setScope(domain.getScope());
        entity.setElementGroup(domain.getElementGroup());
        entity.setOrderIndex(domain.getOrderIndex());
        entity.setResourceId(domain.getResourceId().getValue());
        entity.setActionId(domain.getActionId().getValue());
        return entity;
    }
}
