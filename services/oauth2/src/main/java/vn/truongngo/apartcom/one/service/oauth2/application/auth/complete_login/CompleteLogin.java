package vn.truongngo.apartcom.one.service.oauth2.application.auth.complete_login;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.shared.domain.user.UserId;
import vn.truongngo.apartcom.one.service.oauth2.domain.activity.LoginActivity;
import vn.truongngo.apartcom.one.service.oauth2.domain.activity.LoginActivityFactory;
import vn.truongngo.apartcom.one.service.oauth2.domain.activity.LoginActivityRepository;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionRepository;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.Oauth2Session;

/**
 * Phase 2 — Token Issuance.
 * Tạo OAuthSession + ghi LoginActivity(SUCCESS) trong cùng 1 transaction.
 * Idempotent: bỏ qua nếu session đã tồn tại cho authorizationId này.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompleteLogin implements CommandHandler<CompleteLogin.Command, Void> {

    public record Command(
            String userId,
            String username,
            String deviceId,
            String compositeHash,
            String userAgent,
            String ipAddress,
            String authorizationId,
            String idpSessionId) {}

    private final SessionRepository sessionRepository;
    private final LoginActivityRepository loginActivityRepository;
    private final EventDispatcher eventDispatcher;

    @Override
    @Transactional
    public Void handle(Command command) {
        if (sessionRepository.findByAuthorizationId(command.authorizationId()).isPresent()) {
            log.warn("[CompleteLoginFlow] Session already exists for authorizationId={}, skipping",
                    command.authorizationId());
            return null;
        }

        Oauth2Session session = Oauth2Session.create(
                new UserId(command.userId()),
                command.deviceId(),
                command.idpSessionId(),
                command.authorizationId(),
                command.ipAddress()
        );
        sessionRepository.save(session);

        LoginActivity activity = LoginActivityFactory.ofSuccess(
                command.userId(),
                command.username(),
                command.compositeHash(),
                command.deviceId(),
                session.getId().getValueAsString(),
                command.ipAddress(),
                command.userAgent()
        );
        loginActivityRepository.save(activity);

        eventDispatcher.dispatchAll(session.pullDomainEvents());

        log.debug("[CompleteLoginFlow] Session + Activity created for userId={}, authorizationId={}",
                command.userId(), command.authorizationId());

        return null;
    }
}
