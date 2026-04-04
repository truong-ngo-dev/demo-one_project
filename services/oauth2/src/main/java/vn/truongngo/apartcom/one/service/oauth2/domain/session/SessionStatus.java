package vn.truongngo.apartcom.one.service.oauth2.domain.session;

public enum SessionStatus {
    /**
     * Session đang hoạt động bình thường
     */
    ACTIVE,

    /**
     * Session bị revoke chủ động
     * VD: user đăng xuất thiết bị từ xa
     */
    REVOKED,

    /**
     * Session hết hạn tự nhiên
     * Được cập nhật khi Spring AS token expire
     */
    EXPIRED
}
