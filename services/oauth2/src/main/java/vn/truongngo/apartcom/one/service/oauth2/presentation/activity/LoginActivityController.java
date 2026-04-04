package vn.truongngo.apartcom.one.service.oauth2.presentation.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.list_my_activities.ListMyLoginActivitiesHandler;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.list_my_activities.ListMyLoginActivitiesQuery;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.list_my_activities.LoginActivityPage;

/**
 * UC-012: User xem lịch sử đăng nhập của chính mình.
 */
@RestController
@RequestMapping("/api/v1/login-activities")
@RequiredArgsConstructor
public class LoginActivityController {

    private final ListMyLoginActivitiesHandler listMyLoginActivitiesHandler;

    /**
     * GET /api/v1/login-activities/me?page=0&size=20
     * Trả về lịch sử đăng nhập phân trang, sắp xếp mới nhất trước.
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
}
