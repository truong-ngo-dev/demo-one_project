package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query;

/**
 * UC-015: Admin xem lịch sử đăng nhập của một User cụ thể.
 *
 * @param targetUserId userId cần xem (từ path param)
 * @param page         trang (0-based)
 * @param size         số item mỗi trang, max 50
 */
public record ListUserLoginActivitiesQuery(String targetUserId, int page, int size) {}
