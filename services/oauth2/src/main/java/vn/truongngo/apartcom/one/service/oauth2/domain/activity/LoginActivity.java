package vn.truongngo.apartcom.one.service.oauth2.domain.activity;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractAggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;

import java.time.Instant;

/**
 * Lịch sử đăng nhập — immutable sau khi tạo, chỉ append.
 */
@Getter
public class LoginActivity extends AbstractAggregateRoot<LoginActivityId> implements AggregateRoot<LoginActivityId> {

    private final String userId;          // nullable — null khi login fail và không tìm thấy user
    private final String username;        // lưu từ request param, dùng khi userId null
    private final LoginResult result;
    private final String ipAddress;
    private final String userAgent;
    private final String compositeHash;   // từ DeviceFingerprint, để correlate với Device
    private final String deviceId;        // nullable — null khi login fail (device chưa được tạo)
    private final String sessionId;       // OAuth2AuthorizationId — nullable khi login fail
    private final LoginProvider provider;
    private final Instant createdAt;

    public enum LoginProvider {
        LOCAL, GOOGLE
    }

    LoginActivity(
            LoginActivityId id,
            String userId,
            String username,
            LoginResult result,
            String ipAddress,
            String userAgent,
            String compositeHash,
            String deviceId,
            String sessionId,
            LoginProvider provider,
            Instant createdAt) {
        super(id);
        this.userId = userId;
        this.username = username;
        this.result = result;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.compositeHash = compositeHash;
        this.deviceId = deviceId;
        this.sessionId = sessionId;
        this.provider = provider;
        this.createdAt = createdAt;
    }

    public boolean isSuccess() {
        return this.result == LoginResult.SUCCESS;
    }
}
