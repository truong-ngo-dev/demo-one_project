package vn.truongngo.apartcom.one.service.oauth2.presentation.iam_dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.truongngo.apartcom.one.service.oauth2.application.iam_dashboard.overview.IamOverviewData;
import vn.truongngo.apartcom.one.service.oauth2.application.iam_dashboard.overview.IamOverviewHandler;
import vn.truongngo.apartcom.one.service.oauth2.application.iam_dashboard.overview.IamOverviewQuery;

/**
 * UC-011: Admin IAM Overview Dashboard.
 */
@RestController
@RequestMapping("/api/v1/admin/iam")
@RequiredArgsConstructor
public class IamDashboardController {

    private final IamOverviewHandler iamOverviewHandler;

    /**
     * GET /api/v1/admin/iam/overview
     * Trả về các chỉ số KPI tổng quan IAM cho Admin Dashboard.
     * Yêu cầu authority ROLE_ADMIN.
     */
    @GetMapping("/overview")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<IamOverviewResponse> getOverview() {
        IamOverviewData data = iamOverviewHandler.handle(new IamOverviewQuery());
        return ResponseEntity.ok(new IamOverviewResponse(data));
    }

    record IamOverviewResponse(IamOverviewData data) {}
}
