package vn.truongngo.apartcom.one.service.admin.application.ui_element.command.create_ui_element;

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
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElement;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementType;
import vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.audit.AuditHelper;

import java.util.Map;

public class CreateUIElement {

    public record Command(
            String elementId,
            String label,
            String type,
            String elementGroup,
            int orderIndex,
            Long resourceId,
            Long actionId
    ) {
        public Command {
            Assert.hasText(elementId, "elementId is required");
            Assert.hasText(label, "label is required");
            Assert.hasText(type, "type is required");
            Assert.notNull(resourceId, "resourceId is required");
            Assert.notNull(actionId, "actionId is required");
        }
    }

    public record Result(Long id) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final UIElementRepository uiElementRepository;
        private final ResourceDefinitionRepository resourceRepository;
        private final EventDispatcher eventDispatcher;
        private final ObjectMapper objectMapper;

        @Override
        @Transactional
        @SneakyThrows
        public Result handle(Command command) {
            if (uiElementRepository.existsByElementId(command.elementId())) {
                throw AbacException.uiElementIdDuplicate();
            }

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
            UIElement element = UIElement.create(
                    command.elementId(), command.label(), elementType,
                    command.elementGroup(), command.orderIndex(), resourceId, actionId);
            UIElement saved = uiElementRepository.save(element);

            String snapshot = objectMapper.writeValueAsString(Map.of(
                    "elementId", saved.getElementId(),
                    "label", saved.getLabel(),
                    "type", saved.getType().name(),
                    "resourceId", saved.getResourceId().getValue(),
                    "actionId", saved.getActionId().getValue()
            ));
            eventDispatcher.dispatch(new AbacAuditLogEvent(
                    AuditEntityType.UI_ELEMENT, saved.getId(), saved.getElementId(),
                    AuditActionType.CREATED, AuditHelper.currentPerformedBy(), snapshot));

            return new Result(saved.getId());
        }
    }
}
