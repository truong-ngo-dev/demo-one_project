package vn.truongngo.apartcom.one.service.oauth2.application.session.revoke;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.Oauth2Session;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionRepository;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionTerminationService;

/**
 * UC-005 — Logout.
 * Xóa Authorization Record và revoke Oauth2Session tương ứng.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevokeSession implements CommandHandler<RevokeSession.Command, Void> {

    public record Command(String idToken) {}

    private final AuthorizationLookupPort authorizationLookupPort;
    private final SessionTerminationService sessionTerminationService;
    private final SessionRepository sessionRepository;
    private final EventDispatcher eventDispatcher;

    @Override
    @Transactional
    public Void handle(Command command) {
        String authorizationId = authorizationLookupPort
                .findAuthorizationId(command.idToken(), AuthorizationTokenType.ID_TOKEN)
                .orElse(null);

        if (authorizationId == null) {
            log.warn("[RevokeSession] Authorization Record not found — logout continues");
            return null;
        }

        Oauth2Session session = sessionTerminationService.terminateSession(authorizationId);
        sessionRepository.save(session);
        eventDispatcher.dispatchAll(session.pullDomainEvents());

        log.debug("[RevokeSession] Session revoked for authorizationId={}", authorizationId);
        return null;
    }
}
