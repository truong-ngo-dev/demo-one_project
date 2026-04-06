package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;

/**
 * UC-015: Admin xem lịch sử đăng nhập của một User cụ thể, có phân trang.
 */
@Component
@RequiredArgsConstructor
public class ListUserLoginActivitiesHandler
        implements QueryHandler<ListUserLoginActivitiesQuery, UserLoginActivityPage> {

    private final AdminLoginActivityQueryPort queryPort;

    @Override
    public UserLoginActivityPage handle(ListUserLoginActivitiesQuery query) {
        int safeSize = Math.min(query.size(), 50);
        return queryPort.findByUserId(query.targetUserId(), query.page(), safeSize);
    }
}
