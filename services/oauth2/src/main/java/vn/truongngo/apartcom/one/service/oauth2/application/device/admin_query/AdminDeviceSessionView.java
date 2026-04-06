package vn.truongngo.apartcom.one.service.oauth2.application.device.admin_query;

import java.time.Instant;

/**
 * UC-014 Admin response item — device + active session status cho một User cụ thể.
 * Khác DeviceSessionView (UC-007): không có trường isCurrent — Admin không dùng thiết bị của User.
 */
public record AdminDeviceSessionView(
        String deviceId,
        String deviceName,
        String ipAddress,
        Instant lastSeenAt,
        String sessionId,       // null nếu không có session ACTIVE trên thiết bị này
        String sessionStatus    // "ACTIVE" | null
) {}
