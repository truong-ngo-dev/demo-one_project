package vn.truongngo.apartcom.one.service.oauth2.domain.activity;

public class LoginActivityFactory {

    public static LoginActivity ofSuccess(
            String userId,
            String username,
            String compositeHash,
            String deviceId,
            String sessionId,
            String ipAddress,
            String userAgent) {
        return new LoginActivity(
                new LoginActivityId(java.util.UUID.randomUUID().toString()),
                userId,
                username,
                LoginResult.SUCCESS,
                ipAddress,
                userAgent,
                compositeHash,
                deviceId,
                sessionId,
                LoginActivity.LoginProvider.LOCAL,
                java.time.Instant.now()
        );
    }

    public static LoginActivity ofFailure(
            String username,
            String compositeHash,
            String ipAddress,
            String userAgent,
            LoginResult result) {
        return new LoginActivity(
                new LoginActivityId(java.util.UUID.randomUUID().toString()),
                null,
                username,
                result,
                ipAddress,
                userAgent,
                compositeHash,
                null,   // deviceId — null khi login fail
                null,
                LoginActivity.LoginProvider.LOCAL,
                java.time.Instant.now()
        );
    }
}
