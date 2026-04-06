package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query;

import java.time.Instant;

/**
 * UC-015 Admin response item — lịch sử đăng nhập của một User cụ thể.
 * Đơn giản hơn AdminLoginActivityView (UC-012): bỏ id, userId, username vì đã biết từ path param.
 */
public record UserLoginActivityView(
        String result,
        String ipAddress,
        String deviceName,   // null nếu login thất bại
        String provider,
        Instant createdAt
) {}
