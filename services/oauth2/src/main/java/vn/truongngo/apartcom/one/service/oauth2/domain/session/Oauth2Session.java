package vn.truongngo.apartcom.one.service.oauth2.domain.session;


import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractAggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;
import vn.truongngo.apartcom.one.lib.shared.domain.user.UserId;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Oauth2Session extends AbstractAggregateRoot<SessionId> implements AggregateRoot<SessionId> {

    private final UserId userId;
    private final String deviceId;
    private final String idpSessionId;

    /**
     * Reference đến Spring AS OAuth2Authorization
     * Dùng để revoke token thực sự khi cần
     */
    private final String authorizationId;

    private final String ipAddress;
    private SessionStatus status;
    private final Instant createdAt;

    /*
     * -------------------------------------------------------------------------
     * CONSTRUCTOR
     * -------------------------------------------------------------------------
     */

    private Oauth2Session(
            SessionId id,
            UserId userId,
            String deviceId,
            String idpSessionId,
            String authorizationId,
            String ipAddress) {
        super(id);
        this.userId = userId;
        this.deviceId = deviceId;
        this.idpSessionId = idpSessionId;
        this.authorizationId = authorizationId;
        this.ipAddress = ipAddress;
        this.status = SessionStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    private Oauth2Session(
            SessionId id,
            UserId userId,
            String deviceId,
            String idpSessionId,
            String authorizationId,
            String ipAddress,
            SessionStatus status,
            Instant createdAt) {
        super(id);
        this.userId = userId;
        this.deviceId = deviceId;
        this.idpSessionId = idpSessionId;
        this.authorizationId = authorizationId;
        this.ipAddress = ipAddress;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Oauth2Session reconstitute(
            SessionId id,
            UserId userId,
            String deviceId,
            String idpSessionId,
            String authorizationId,
            String ipAddress,
            SessionStatus status,
            Instant createdAt) {
        return new Oauth2Session(id, userId, deviceId, idpSessionId, authorizationId, ipAddress, status, createdAt);
    }

    /*
     * -------------------------------------------------------------------------
     * FACTORY METHOD
     * -------------------------------------------------------------------------
     */

    public static Oauth2Session create(
            UserId userId,
            String deviceId,
            String idpSessionId,
            String authorizationId,
            String ipAddress) {
        return new Oauth2Session(
                new SessionId(UUID.randomUUID().toString()),
                userId,
                deviceId,
                idpSessionId,
                authorizationId,
                ipAddress
        );
    }

    /*
     * -------------------------------------------------------------------------
     * BUSINESS METHODS
     * -------------------------------------------------------------------------
     */

    /**
     * Revoke session chủ động
     * VD: user đăng xuất thiết bị từ xa hoặc device bị revoke
     */
    public void revoke() {
        assertActive();
        this.status = SessionStatus.REVOKED;
        registerEvent(new SessionRevokedEvent(
                this.authorizationId,
                this.userId.getValueAsString(),
                this.deviceId,
                this.idpSessionId
        ));
    }

    /**
     * Đánh dấu session hết hạn
     * Được gọi khi Spring AS token expire
     */
    public void expire() {
        assertActive();
        this.status = SessionStatus.EXPIRED;
    }

    /*
     * -------------------------------------------------------------------------
     * QUERY METHODS
     * -------------------------------------------------------------------------
     */

    public boolean isActive() {
        return this.status == SessionStatus.ACTIVE;
    }

    /*
     * -------------------------------------------------------------------------
     * GUARD
     * -------------------------------------------------------------------------
     */

    private void assertActive() {
        if (this.status != SessionStatus.ACTIVE) throw new DomainException(SessionErrorCode.SESSION_NOT_ACTIVE);
    }
}