package vn.truongngo.apartcom.one.service.admin.domain.abac.uielement;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ActionId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceId;

/**
 * Aggregate Root for UIElement — maps a frontend button/tab/menu item to a resource:action pair.
 * elementId is immutable after creation (frontend hardcodes this string).
 */
@Getter
public class UIElement {

    private final Long id;
    private final String elementId;
    private final String label;
    private final UIElementType type;
    private final String elementGroup;
    private final int orderIndex;
    private final ResourceId resourceId;
    private final ActionId actionId;

    private UIElement(Long id, String elementId, String label, UIElementType type,
                      String elementGroup, int orderIndex, ResourceId resourceId, ActionId actionId) {
        this.id = id;
        this.elementId = elementId;
        this.label = label;
        this.type = type;
        this.elementGroup = elementGroup;
        this.orderIndex = orderIndex;
        this.resourceId = resourceId;
        this.actionId = actionId;
    }

    public static UIElement create(String elementId, String label, UIElementType type,
                                   String elementGroup, int orderIndex,
                                   ResourceId resourceId, ActionId actionId) {
        Assert.hasText(elementId, "elementId is required");
        Assert.hasText(label, "label is required");
        Assert.notNull(type, "type is required");
        Assert.notNull(resourceId, "resourceId is required");
        Assert.notNull(actionId, "actionId is required");
        return new UIElement(null, elementId, label, type, elementGroup, orderIndex, resourceId, actionId);
    }

    public static UIElement reconstitute(Long id, String elementId, String label, UIElementType type,
                                         String elementGroup, int orderIndex,
                                         ResourceId resourceId, ActionId actionId) {
        return new UIElement(id, elementId, label, type, elementGroup, orderIndex, resourceId, actionId);
    }

    public UIElement update(String label, UIElementType type, String elementGroup,
                            int orderIndex, ResourceId resourceId, ActionId actionId) {
        Assert.hasText(label, "label is required");
        Assert.notNull(type, "type is required");
        Assert.notNull(resourceId, "resourceId is required");
        Assert.notNull(actionId, "actionId is required");
        return new UIElement(this.id, this.elementId, label, type, elementGroup, orderIndex, resourceId, actionId);
    }
}
