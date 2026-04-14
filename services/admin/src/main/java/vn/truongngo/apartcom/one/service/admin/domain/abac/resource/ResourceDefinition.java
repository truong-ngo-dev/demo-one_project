package vn.truongngo.apartcom.one.service.admin.domain.abac.resource;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root for Resource & Action Catalogue.
 * name is immutable after creation — changing it would break SpEL expressions
 * that reference resource.name == 'resource_name'.
 * Does not extend AbstractAggregateRoot: BIGINT auto-increment id is assigned
 * by the database on first persist (not pre-generated like UUID).
 */
@Getter
public class ResourceDefinition {

    private final ResourceId id;
    private final String name;
    private final String description;
    private final String serviceName;
    private final List<ActionDefinition> actions;
    private final long createdAt;
    private final long updatedAt;

    private ResourceDefinition(ResourceId id, String name, String description,
                                String serviceName, List<ActionDefinition> actions,
                                long createdAt, long updatedAt) {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.serviceName = serviceName;
        this.actions     = new ArrayList<>(actions);
        this.createdAt   = createdAt;
        this.updatedAt   = updatedAt;
    }

    public static ResourceDefinition create(String name, String description, String serviceName) {
        Assert.hasText(name, "name is required");
        Assert.hasText(serviceName, "serviceName is required");
        long now = System.currentTimeMillis();
        return new ResourceDefinition(null, name, description, serviceName, List.of(), now, now);
    }

    public static ResourceDefinition reconstitute(ResourceId id, String name, String description,
                                                   String serviceName, List<ActionDefinition> actions,
                                                   long createdAt, long updatedAt) {
        return new ResourceDefinition(id, name, description, serviceName, actions, createdAt, updatedAt);
    }

    public ResourceDefinition updateMeta(String description, String serviceName) {
        Assert.hasText(serviceName, "serviceName is required");
        return new ResourceDefinition(
                this.id, this.name, description, serviceName,
                this.actions, this.createdAt, System.currentTimeMillis());
    }

    public ResourceDefinition addAction(String name, String description, boolean isStandard) {
        Assert.hasText(name, "action name is required");
        boolean exists = actions.stream()
                .anyMatch(a -> a.getName().equalsIgnoreCase(name));
        if (exists) throw ResourceException.actionNameDuplicate();

        List<ActionDefinition> updated = new ArrayList<>(actions);
        updated.add(ActionDefinition.create(this.id, name, description, isStandard));
        return new ResourceDefinition(
                this.id, this.name, this.description, this.serviceName,
                updated, this.createdAt, System.currentTimeMillis());
    }

    public ResourceDefinition updateAction(ActionId actionId, String description) {
        List<ActionDefinition> updated = actions.stream()
                .map(a -> a.getId() != null && a.getId().equals(actionId)
                        ? a.updateDescription(description)
                        : a)
                .toList();
        boolean found = updated.stream().anyMatch(a -> a.getId() != null && a.getId().equals(actionId));
        if (!found) throw ResourceException.actionNotFound();
        return new ResourceDefinition(
                this.id, this.name, this.description, this.serviceName,
                updated, this.createdAt, System.currentTimeMillis());
    }

    public ResourceDefinition removeAction(ActionId actionId) {
        boolean exists = actions.stream()
                .anyMatch(a -> a.getId() != null && a.getId().equals(actionId));
        if (!exists) throw ResourceException.actionNotFound();
        List<ActionDefinition> updated = actions.stream()
                .filter(a -> !actionId.equals(a.getId()))
                .toList();
        return new ResourceDefinition(
                this.id, this.name, this.description, this.serviceName,
                updated, this.createdAt, System.currentTimeMillis());
    }

    public List<ActionDefinition> getActions() {
        return Collections.unmodifiableList(actions);
    }
}
