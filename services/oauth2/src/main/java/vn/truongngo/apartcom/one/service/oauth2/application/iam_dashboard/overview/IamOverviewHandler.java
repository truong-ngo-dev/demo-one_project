package vn.truongngo.apartcom.one.service.oauth2.application.iam_dashboard.overview;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;

/**
 * UC-011: Admin xem tổng quan IAM — các chỉ số KPI.
 */
@Component
@RequiredArgsConstructor
public class IamOverviewHandler implements QueryHandler<IamOverviewQuery, IamOverviewData> {

    private final IamOverviewQueryPort queryPort;

    @Override
    public IamOverviewData handle(IamOverviewQuery query) {
        return queryPort.query();
    }
}
