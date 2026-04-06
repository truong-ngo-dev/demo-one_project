package vn.truongngo.apartcom.one.service.oauth2.presentation.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.truongngo.apartcom.one.service.oauth2.application.device.admin_query.AdminDeviceSessionView;
import vn.truongngo.apartcom.one.service.oauth2.application.device.admin_query.ListUserDeviceSessionsHandler;
import vn.truongngo.apartcom.one.service.oauth2.application.device.admin_query.ListUserDeviceSessionsQuery;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query.ListUserLoginActivitiesHandler;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query.ListUserLoginActivitiesQuery;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query.UserLoginActivityPage;

import java.util.List;

/**
 * UC-014: Admin xem danh sách gộp thiết bị + session ACTIVE của một User.
 * UC-015: Admin xem lịch sử đăng nhập của một User cụ thể.
 *
 * Pattern URL: /api/v1/admin/users/{userId}/...
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserSupportController {

    private final ListUserDeviceSessionsHandler listUserDeviceSessionsHandler;
    private final ListUserLoginActivitiesHandler listUserLoginActivitiesHandler;

    /**
     * UC-014 — GET /api/v1/admin/users/{userId}/sessions
     * Danh sách gộp thiết bị + trạng thái session ACTIVE của User.
     * Dòng có sessionId ≠ null → có thể force terminate qua DELETE /api/v1/sessions/admin/{sessionId}.
     */
    @GetMapping("/{userId}/sessions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AdminDeviceSessionView>> listUserDeviceSessions(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                listUserDeviceSessionsHandler.handle(new ListUserDeviceSessionsQuery(userId)));
    }

    /**
     * UC-015 — GET /api/v1/admin/users/{userId}/login-activities
     * Lịch sử đăng nhập của một User cụ thể, phân trang.
     */
    @GetMapping("/{userId}/login-activities")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserLoginActivityPage> listUserLoginActivities(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                listUserLoginActivitiesHandler.handle(
                        new ListUserLoginActivitiesQuery(userId, page, size)));
    }
}
