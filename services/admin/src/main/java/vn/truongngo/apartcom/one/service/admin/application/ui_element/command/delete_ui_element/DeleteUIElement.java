package vn.truongngo.apartcom.one.service.admin.application.ui_element.command.delete_ui_element;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AbacAuditLogEvent;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditActionType;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditEntityType;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.AbacException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElement;
import vn.truongngo.apartcom.one.service.admin.domain.abac.uielement.UIElementRepository;
import vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.audit.AuditHelper;

public class DeleteUIElement {

    public record Command(Long id) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final UIElementRepository uiElementRepository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Void handle(Command command) {
            UIElement element = uiElementRepository.findById(command.id())
                    .orElseThrow(AbacException::uiElementNotFound);
            uiElementRepository.delete(command.id());
            eventDispatcher.dispatch(new AbacAuditLogEvent(
                    AuditEntityType.UI_ELEMENT, command.id(), element.getElementId(),
                    AuditActionType.DELETED, AuditHelper.currentPerformedBy(), null));
            return null;
        }
    }
}
