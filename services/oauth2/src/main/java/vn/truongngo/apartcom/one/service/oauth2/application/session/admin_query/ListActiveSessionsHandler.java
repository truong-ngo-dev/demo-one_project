package vn.truongngo.apartcom.one.service.oauth2.application.session.admin_query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;

import java.util.List;

/**
 * UC-013: Admin xem danh sách tất cả phiên ACTIVE toàn hệ thống.
 */
@Component
@RequiredArgsConstructor
public class ListActiveSessionsHandler implements QueryHandler<ListActiveSessionsQuery, List<ActiveSessionView>> {

    private final AdminSessionQueryPort queryPort;

    @Override
    public List<ActiveSessionView> handle(ListActiveSessionsQuery query) {
        return queryPort.findAllActive();
    }
}
