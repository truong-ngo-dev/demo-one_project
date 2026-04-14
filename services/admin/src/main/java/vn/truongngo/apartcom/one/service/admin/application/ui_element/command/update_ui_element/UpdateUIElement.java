package vn.truongngo.apartcom.one.service.admin.application.ui_element.command.update_ui_element;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AbacAuditLogEvent;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditActionType;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditEntityType;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.AbacException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ActionId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Scope;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElement;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementType;
import vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.audit.AuditHelper;

import java.util.Map;

public class UpdateUIElement {

    public record Command(
            Long id,
            String label,
            String type,
            String elementGroup,
            int orderIndex,
            Long resourceId,
            Long actionId,
            String scope   // nullable — keeps existing scope when null
    ) {
        public Command {
            Assert.notNull(id, "id is required");
            Assert.hasText(label, "label is required");
            Assert.hasText(type, "type is required");
            Assert.notNull(resourceId, "resourceId is required");
            Assert.notNull(actionId, "actionId is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final UIElementRepository uiElementRepository;
        private final ResourceDefinitionRepository resourceRepository;
        private final EventDispatcher eventDispatcher;
        private final ObjectMapper objectMapper;

        @Override
        @Transactional
        @SneakyThrows
        public Void handle(Command command) {
            UIElement element = uiElementRepository.findById(command.id())
                    .orElseThrow(AbacException::uiElementNotFound);

            ResourceId resourceId = ResourceId.of(command.resourceId());
            ResourceDefinition resource = resourceRepository.findById(resourceId)
                    .orElseThrow(AbacException::resourceNotFound);

            ActionId actionId = ActionId.of(command.actionId());
            boolean actionBelongsToResource = resource.getActions().stream()
                    .anyMatch(a -> actionId.equals(a.getId()));
            if (!actionBelongsToResource) {
                throw AbacException.actionNotFound();
            }

            UIElementType elementType = UIElementType.valueOf(command.type());
            Scope elementScope = command.scope() != null ? Scope.valueOf(command.scope()) : element.getScope();
            UIElement updated = element.update(
                    command.label(), elementType, elementScope, command.elementGroup(),
                    command.orderIndex(), resourceId, actionId);
            uiElementRepository.save(updated);

            String snapshot = objectMapper.writeValueAsString(Map.of(
                    "elementId", updated.getElementId(),
                    "label", updated.getLabel(),
                    "type", updated.getType().name(),
                    "resourceId", updated.getResourceId().getValue(),
                    "actionId", updated.getActionId().getValue()
            ));
            eventDispatcher.dispatch(new AbacAuditLogEvent(
                    AuditEntityType.UI_ELEMENT, command.id(), updated.getElementId(),
                    AuditActionType.UPDATED, AuditHelper.currentPerformedBy(), snapshot));
            return null;
        }
    }
}
