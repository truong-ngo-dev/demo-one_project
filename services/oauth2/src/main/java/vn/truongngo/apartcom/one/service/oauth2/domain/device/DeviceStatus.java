package vn.truongngo.apartcom.one.service.oauth2.domain.device;

public enum DeviceStatus {

    /**
     * Thiết bị đang hoạt động bình thường
     */
    ACTIVE,

    /**
     * Thiết bị bị revoke — không thể dùng để login
     * Khi user đăng xuất thiết bị từ màn hình "Thiết bị của tôi"
     */
    REVOKED
}
