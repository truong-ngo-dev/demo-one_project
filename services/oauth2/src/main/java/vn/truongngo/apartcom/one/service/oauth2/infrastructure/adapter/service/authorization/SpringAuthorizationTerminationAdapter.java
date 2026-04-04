package vn.truongngo.apartcom.one.service.oauth2.infrastructure.adapter.service.authorization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.Oauth2Session;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionErrorCode;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionRepository;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionTerminationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAuthorizationTerminationAdapter implements SessionTerminationService {

    private final OAuth2AuthorizationService oauth2AuthorizationService;
    private final vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionRepository sessionRepository;
    private final JdbcIndexedSessionRepository springSessionRepository;

    @Override
    public Oauth2Session terminateSession(String authorizationId) {
        Oauth2Session session = sessionRepository.findByAuthorizationId(authorizationId)
                .orElseThrow(() -> new DomainException(SessionErrorCode.SESSION_NOT_FOUND));
        doTerminate(session);
        return session;
    }

    @Override
    public void terminateSession(Oauth2Session session) {
        doTerminate(session);
    }

    private void doTerminate(Oauth2Session session) {
        // 1. Revoke domain object (bắn Domain Event)
        session.revoke();

        // 2. Thu hồi OAuth2 Tokens tại Authorization Server
        OAuth2Authorization authorization = oauth2AuthorizationService.findById(session.getAuthorizationId());
        if (authorization != null) {
            oauth2AuthorizationService.remove(authorization);
            log.debug("[SessionTermination] OAuth2 Authorization removed for id={}", session.getAuthorizationId());
        }

        // 3. Invalidate local IdP session (prevent silent re-login)
        if (session.getIdpSessionId() != null) {
            try {
                springSessionRepository.deleteById(session.getIdpSessionId());
                log.debug("[SessionTermination] Local IdP session deleted: {}", session.getIdpSessionId());
            } catch (Exception e) {
                log.warn("[SessionTermination] Failed to delete local session {}: {}", session.getIdpSessionId(), e.getMessage());
            }
        }
    }
}
