package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query;

import java.util.List;

/**
 * UC-015 paginated response — lịch sử đăng nhập của một User cụ thể.
 */
public record UserLoginActivityPage(
        List<UserLoginActivityView> data,
        UserLoginActivityMeta meta
) {
    public record UserLoginActivityMeta(int page, int size, long total) {}
}
