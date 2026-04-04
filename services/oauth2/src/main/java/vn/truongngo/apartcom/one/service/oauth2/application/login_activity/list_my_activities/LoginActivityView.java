package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.list_my_activities;

import java.time.Instant;

/**
 * UC-012 response item — login activity với device name join.
 */
public record LoginActivityView(
        String result,
        String ipAddress,
        String userAgent,
        String provider,
        Instant createdAt,
        String deviceId,       // null nếu login thất bại — device chưa được tạo
        String deviceName      // null nếu deviceId null
) {}
