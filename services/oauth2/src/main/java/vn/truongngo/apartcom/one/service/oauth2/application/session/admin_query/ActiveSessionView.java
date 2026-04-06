package vn.truongngo.apartcom.one.service.oauth2.application.session.admin_query;

import java.time.Instant;

/**
 * UC-013 Admin response item — phiên ACTIVE toàn hệ thống.
 *
 * username lấy từ login_activities gần nhất (SUCCESS) của cùng userId.
 * Có thể null nếu user chưa từng có SUCCESS activity được ghi nhận.
 */
public record ActiveSessionView(
        String sessionId,
        String userId,
        String username,    // nullable — lấy từ login_activities
        String deviceName,  // nullable — lấy từ devices join
        String ipAddress,
        Instant createdAt
) {}
