package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query;

import java.util.List;

/**
 * UC-012 Admin paginated response.
 */
public record AdminLoginActivityPage(
        List<AdminLoginActivityView> data,
        AdminLoginActivityMeta meta
) {
    public record AdminLoginActivityMeta(int page, int size, long total) {}
}
