package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.record;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.service.oauth2.domain.activity.LoginActivity;
import vn.truongngo.apartcom.one.service.oauth2.domain.activity.LoginActivityFactory;
import vn.truongngo.apartcom.one.service.oauth2.domain.activity.LoginActivityRepository;
import vn.truongngo.apartcom.one.service.oauth2.domain.activity.LoginResult;

/**
 * Phase 1 — ghi LoginActivity khi xác thực thất bại.
 * compositeHash được tính trước bởi caller (DeviceAwareAuthenticationFailureHandler).
 */
@Component
@RequiredArgsConstructor
public class RecordLoginActivity implements CommandHandler<RecordLoginActivity.Command, Void> {

    public record Command(
            String username,
            String compositeHash,
            String userAgent,
            String ipAddress,
            LoginResult result) {}

    private final LoginActivityRepository loginActivityRepository;

    @Override
    public Void handle(Command command) {
        LoginActivity activity = LoginActivityFactory.ofFailure(
                command.username(),
                command.compositeHash(),
                command.ipAddress(),
                command.userAgent(),
                command.result()
        );
        loginActivityRepository.save(activity);
        return null;
    }
}
