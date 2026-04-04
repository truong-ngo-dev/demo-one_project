package vn.truongngo.apartcom.one.service.oauth2.application.session.remote_revoke;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.Oauth2Session;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionErrorCode;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionId;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionRepository;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionTerminationService;

/**
 * UC-008 — Remote Logout.
 * User chủ động đăng xuất một thiết bị/session từ xa.
 */
public class RemoteRevokeSession {

    public record Command(String sessionId, String currentUserId, String currentSid) {}

    @Slf4j
    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final SessionRepository sessionRepository;
        private final SessionTerminationService sessionTerminationService;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Void handle(Command command) {
            Oauth2Session session = sessionRepository.findById(new SessionId(command.sessionId()))
                    .orElseThrow(() -> new DomainException(SessionErrorCode.SESSION_NOT_FOUND));

            if (!session.getUserId().getValueAsString().equals(command.currentUserId())) {
                throw new DomainException(SessionErrorCode.SESSION_NOT_BELONG_TO_USER);
            }

            if (command.sessionId().equals(command.currentSid())) {
                throw new DomainException(SessionErrorCode.CANNOT_REVOKE_CURRENT_SESSION);
            }

            sessionTerminationService.terminateSession(session);
            sessionRepository.save(session);
            eventDispatcher.dispatchAll(session.pullDomainEvents());

            log.debug("[RemoteRevokeSession] Session revoked remotely — sessionId={}, userId={}",
                    command.sessionId(), command.currentUserId());

            return null;
        }
    }
}
