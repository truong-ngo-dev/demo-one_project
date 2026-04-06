package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;

/**
 * UC-012: Admin xem toàn bộ lịch sử đăng nhập hệ thống, có filter và phân trang.
 */
@Component
@RequiredArgsConstructor
public class GlobalLoginActivityHandler implements QueryHandler<GlobalLoginActivityQuery, AdminLoginActivityPage> {

    private final AdminLoginActivityQueryPort queryPort;

    @Override
    public AdminLoginActivityPage handle(GlobalLoginActivityQuery query) {
        int safeSize = Math.min(query.size(), 100);
        return queryPort.findAll(query.filter(), query.page(), safeSize);
    }
}
