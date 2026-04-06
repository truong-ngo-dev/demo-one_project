package vn.truongngo.apartcom.one.service.oauth2.presentation.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query.AdminLoginActivityFilter;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query.AdminLoginActivityPage;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query.GlobalLoginActivityHandler;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query.GlobalLoginActivityQuery;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.list_my_activities.ListMyLoginActivitiesHandler;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.list_my_activities.ListMyLoginActivitiesQuery;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.list_my_activities.LoginActivityPage;

/**
 * UC-009: User xem lịch sử đăng nhập của chính mình.
 * UC-012: Admin xem toàn bộ lịch sử đăng nhập hệ thống.
 */
@RestController
@RequestMapping("/api/v1/login-activities")
@RequiredArgsConstructor
public class LoginActivityController {

    private final ListMyLoginActivitiesHandler listMyLoginActivitiesHandler;
    private final GlobalLoginActivityHandler globalLoginActivityHandler;

    /**
     * UC-009 — GET /api/v1/login-activities/me
     */
    @GetMapping("/me")
    public ResponseEntity<LoginActivityPage> listMyActivities(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String userId = jwt.getSubject();
        return ResponseEntity.ok(listMyLoginActivitiesHandler.handle(
                new ListMyLoginActivitiesQuery(userId, page, size)));
    }

    /**
     * UC-012 — GET /api/v1/admin/login-activities
     * Admin xem toàn bộ lịch sử đăng nhập, có filter và phân trang.
     */
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AdminLoginActivityPage> listAllActivities(
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        AdminLoginActivityFilter filter = new AdminLoginActivityFilter(ip, result, username);
        return ResponseEntity.ok(globalLoginActivityHandler.handle(
                new GlobalLoginActivityQuery(filter, page, size)));
    }
}
