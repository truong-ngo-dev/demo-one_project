package vn.truongngo.apartcom.one.service.oauth2.application.session.admin_revoke;

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
 * UC-013 — Admin Force Terminate Session.
 * Admin có thể revoke bất kỳ session nào không phụ thuộc ownership.
 * Khác UC-008: không check ownership, không có constraint "cannot revoke current session".
 */
public class AdminRevokeSession {

    /**
     * @param sessionId ID session cần revoke
     * @param adminId   userId của admin thực hiện — dùng cho audit log
     */
    public record Command(String sessionId, String adminId) {}

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

            sessionTerminationService.terminateSession(session);
            sessionRepository.save(session);
            eventDispatcher.dispatchAll(session.pullDomainEvents());

            log.info("[AUDIT] AdminRevokeSession — adminId={} revoked sessionId={} userId={}",
                    command.adminId(),
                    command.sessionId(),
                    session.getUserId().getValueAsString());

            return null;
        }
    }
}
