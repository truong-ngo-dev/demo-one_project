package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query;

import java.time.Instant;

/**
 * UC-012 Admin response item — đầy đủ hơn LoginActivityView (user self-service):
 * bao gồm id, userId, username để admin có thể drill down và correlate.
 */
public record AdminLoginActivityView(
        String id,
        String userId,       // null nếu login thất bại và username không tồn tại
        String username,
        String result,
        String ipAddress,
        String userAgent,
        String deviceId,     // null nếu login thất bại
        String deviceName,   // null nếu deviceId null
        String provider,
        Instant createdAt
) {}
