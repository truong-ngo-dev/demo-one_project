package vn.truongngo.apartcom.one.service.admin.domain.abac.resource;

import lombok.Getter;

/**
 * Entity owned by ResourceDefinition aggregate.
 * name is immutable after creation — changing it would break SpEL expressions
 * that reference action.getAttribute('name') == 'ACTION_NAME'.
 */
@Getter
public class ActionDefinition {

    private final ActionId id;
    private final ResourceId resourceId;
    private final String name;
    private final String description;
    private final boolean isStandard;

    private ActionDefinition(ActionId id, ResourceId resourceId, String name,
                              String description, boolean isStandard) {
        this.id         = id;
        this.resourceId = resourceId;
        this.name       = name;
        this.description = description;
        this.isStandard = isStandard;
    }

    static ActionDefinition create(ResourceId resourceId, String name,
                                   String description, boolean isStandard) {
        return new ActionDefinition(null, resourceId, name, description, isStandard);
    }

    public static ActionDefinition reconstitute(ActionId id, ResourceId resourceId, String name,
                                                String description, boolean isStandard) {
        return new ActionDefinition(id, resourceId, name, description, isStandard);
    }

    ActionDefinition updateDescription(String description) {
        return new ActionDefinition(this.id, this.resourceId, this.name, description, this.isStandard);
    }
}
