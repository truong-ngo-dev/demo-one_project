package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query;

/**
 * Read-side port — admin login activity queries.
 * Bypass domain layer (CQRS read side).
 */
public interface AdminLoginActivityQueryPort {
    /** UC-012: Global — toàn bộ hệ thống, có filter động. */
    AdminLoginActivityPage findAll(AdminLoginActivityFilter filter, int page, int size);

    /** UC-015: User-centric — lịch sử của một userId cụ thể, có phân trang. */
    UserLoginActivityPage findByUserId(String userId, int page, int size);
}
