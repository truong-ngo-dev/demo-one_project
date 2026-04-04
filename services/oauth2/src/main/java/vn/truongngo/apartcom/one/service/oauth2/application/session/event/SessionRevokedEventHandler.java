package vn.truongngo.apartcom.one.service.oauth2.application.session.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.domain.service.EventHandler;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionRevokedEvent;

/**
 * Event Handler: SessionRevokedEvent → notify các hệ thống bên ngoài.
 * Logic dọn dẹp nội bộ IdP đã được thực hiện tại Domain Service trước đó.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionRevokedEventHandler implements EventHandler<SessionRevokedEvent> {

    private final RevocationNotifier revocationNotifier;

    @Override
    public void handle(SessionRevokedEvent event) {
        String sid = event.getAggregateId(); // authorizationId
        String idpSessionId = event.getIdpSessionId();

        log.debug("[SessionRevokedEventHandler] Handling session revoked — sid={}, idpSessionId={}", sid, idpSessionId);
        
        // Chỉ làm nhiệm vụ điều phối thông báo ra bên ngoài qua Port
        revocationNotifier.notify(sid);
    }

    @Override
    public Class<SessionRevokedEvent> getEventType() {
        return SessionRevokedEvent.class;
    }
}
