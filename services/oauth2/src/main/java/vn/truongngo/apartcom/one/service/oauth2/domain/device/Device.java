package vn.truongngo.apartcom.one.service.oauth2.domain.device;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractAggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;
import vn.truongngo.apartcom.one.lib.shared.domain.user.UserId;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Device extends AbstractAggregateRoot<DeviceId> implements AggregateRoot<DeviceId> {

    private final UserId userId;
    private final DeviceFingerprint fingerprint;

    private DeviceName name;           // system detected, có thể update
    private boolean trusted;
    private DeviceStatus status;

    private final Instant registeredAt;
    private Instant lastSeenAt;
    private String lastIpAddress;

    private Device(
            DeviceId id,
            UserId userId,
            DeviceFingerprint fingerprint,
            DeviceName name,
            String ipAddress) {
        super(id);
        this.userId = userId;
        this.fingerprint = fingerprint;
        this.name = name;
        this.trusted = false;
        this.status = DeviceStatus.ACTIVE;
        this.registeredAt = Instant.now();
        this.lastSeenAt = Instant.now();
        this.lastIpAddress = ipAddress;
    }

    private Device(
            DeviceId id,
            UserId userId,
            DeviceFingerprint fingerprint,
            DeviceName name,
            boolean trusted,
            DeviceStatus status,
            Instant registeredAt,
            Instant lastSeenAt,
            String lastIpAddress) {
        super(id);
        this.userId = userId;
        this.fingerprint = fingerprint;
        this.name = name;
        this.trusted = trusted;
        this.status = status;
        this.registeredAt = registeredAt;
        this.lastSeenAt = lastSeenAt;
        this.lastIpAddress = lastIpAddress;
    }

    /**
     * Tái tạo Device từ dữ liệu persistence (không dispatch event).
     */
    public static Device reconstitute(
            DeviceId id,
            UserId userId,
            DeviceFingerprint fingerprint,
            DeviceName name,
            boolean trusted,
            DeviceStatus status,
            Instant registeredAt,
            Instant lastSeenAt,
            String lastIpAddress) {
        return new Device(id, userId, fingerprint, name, trusted, status, registeredAt, lastSeenAt, lastIpAddress);
    }

    /**
     * Đăng ký thiết bị mới lần đầu đăng nhập.
     */
    public static Device register(
            UserId userId,
            DeviceFingerprint fingerprint,
            DeviceName name,
            String ipAddress) {
        return new Device(new DeviceId(UUID.randomUUID().toString()), userId, fingerprint, name, ipAddress);
    }

    /**
     * Đánh dấu thiết bị là trusted.
     * Chỉ trusted device mới có thể revoke thiết bị khác.
     */
    public void trust() {
        assertActive();
        this.trusted = true;
        registerEvent(new DeviceTrustedEvent(this.getId().getValueAsString(), this.userId.getValueAsString()));
    }

    /**
     * Revoke thiết bị — thường xảy ra khi user đăng xuất thiết bị từ xa.
     * Sau khi revoke, thiết bị không thể dùng để login cho đến khi đăng ký lại.
     */
    public void revoke() {
        assertActive();
        this.trusted = false;
        this.status = DeviceStatus.REVOKED;
        registerEvent(new DeviceRevokedEvent(this.getId().getValueAsString(), this.userId.getValueAsString()));
    }

    /**
     * Cập nhật thông tin khi user đăng nhập lại trên thiết bị này.
     */
    public void recordActivity(String ipAddress) {
        assertActive();
        this.lastSeenAt = Instant.now();
        this.lastIpAddress = ipAddress;
    }

    /**
     * Cập nhật tên thiết bị — system detect lại từ User-Agent mới.
     */
    public void updateName(DeviceName newName) {
        assertActive();
        this.name = newName;
    }

    public boolean isActive() {
        return this.status == DeviceStatus.ACTIVE;
    }

    public boolean canRevokeOthers() {
        return isActive() && isTrusted();
    }

    public boolean belongsTo(UserId userId) {
        return this.userId.equals(userId);
    }

    private void assertActive() {
        if (this.status != DeviceStatus.ACTIVE) throw new DomainException(DeviceErrorCode.DEVICE_NOT_ACTIVE);
    }
}
