package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query;

/**
 * UC-012: Admin xem toàn bộ lịch sử đăng nhập hệ thống.
 *
 * @param filter   các tham số lọc (ip, result, username)
 * @param page     trang (0-based), default 0
 * @param size     số item mỗi trang, default 20, max 100
 */
public record GlobalLoginActivityQuery(AdminLoginActivityFilter filter, int page, int size) {}
