package vn.truongngo.apartcom.one.service.oauth2.application.device.list_my_sessions;

import java.time.Instant;

/**
 * UC-007 response item — device + session status.
 */
public record DeviceSessionView(
        String deviceId,
        String deviceName,
        String ipAddress,
        Instant lastSeenAt,
        String sessionId,       // null nếu không có session active
        String sessionStatus,   // "ACTIVE" | null
        boolean isCurrent       // true nếu đây là thiết bị đang gọi request
) {}
