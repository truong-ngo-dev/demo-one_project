package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.list_my_activities;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;

/**
 * UC-012: User xem lịch sử đăng nhập của chính mình — phân trang.
 */
@Component
@RequiredArgsConstructor
public class ListMyLoginActivitiesHandler implements QueryHandler<ListMyLoginActivitiesQuery, LoginActivityPage> {

    private final LoginActivityQueryPort queryPort;

    @Override
    public LoginActivityPage handle(ListMyLoginActivitiesQuery query) {
        int safeSize = Math.min(query.size(), 50);
        return queryPort.findByUserId(query.userId(), query.page(), safeSize);
    }
}
