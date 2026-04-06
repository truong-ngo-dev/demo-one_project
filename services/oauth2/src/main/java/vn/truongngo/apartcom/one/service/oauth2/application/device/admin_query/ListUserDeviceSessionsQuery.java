package vn.truongngo.apartcom.one.service.oauth2.application.device.admin_query;

/**
 * UC-014: Admin xem danh sách gộp thiết bị + session ACTIVE của một User cụ thể.
 *
 * @param targetUserId userId của User cần xem (từ path param)
 */
public record ListUserDeviceSessionsQuery(String targetUserId) {}
