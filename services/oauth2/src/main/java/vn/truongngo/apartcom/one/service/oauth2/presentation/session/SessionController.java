package vn.truongngo.apartcom.one.service.oauth2.presentation.session;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.truongngo.apartcom.one.service.oauth2.application.device.list_my_sessions.DeviceSessionView;
import vn.truongngo.apartcom.one.service.oauth2.application.device.list_my_sessions.ListMyDevicesHandler;
import vn.truongngo.apartcom.one.service.oauth2.application.device.list_my_sessions.ListMyDevicesQuery;
import vn.truongngo.apartcom.one.service.oauth2.application.session.admin_query.ActiveSessionView;
import vn.truongngo.apartcom.one.service.oauth2.application.session.admin_query.ListActiveSessionsHandler;
import vn.truongngo.apartcom.one.service.oauth2.application.session.admin_query.ListActiveSessionsQuery;
import vn.truongngo.apartcom.one.service.oauth2.application.session.admin_revoke.AdminRevokeSession;
import vn.truongngo.apartcom.one.service.oauth2.application.session.remote_revoke.RemoteRevokeSession;

import java.util.List;

/**
 * UC-007: User xem danh sách thiết bị + trạng thái session.
 * UC-008: User đăng xuất từ xa một thiết bị/session cụ thể.
 * UC-013: Admin xem tất cả phiên ACTIVE + force terminate.
 */
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final ListMyDevicesHandler listMyDevicesHandler;
    private final RemoteRevokeSession.Handler remoteRevokeSessionHandler;
    private final ListActiveSessionsHandler listActiveSessionsHandler;
    private final AdminRevokeSession.Handler adminRevokeSessionHandler;

    /**
     * UC-007 — GET /api/v1/sessions/me
     */
    @GetMapping("/me")
    public ResponseEntity<List<DeviceSessionView>> listMyDevices(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        String currentSid = jwt.getClaim("sid");
        return ResponseEntity.ok(listMyDevicesHandler.handle(new ListMyDevicesQuery(userId, currentSid)));
    }

    /**
     * UC-008 — DELETE /api/v1/sessions/me/{sessionId}
     * Đăng xuất từ xa session của chính mình. Không thể revoke session hiện tại.
     */
    @DeleteMapping("/me/{sessionId}")
    public ResponseEntity<Void> revokeSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        String currentSid = jwt.getClaim("sid");
        remoteRevokeSessionHandler.handle(new RemoteRevokeSession.Command(sessionId, userId, currentSid));
        return ResponseEntity.noContent().build();
    }

    /**
     * UC-013 — GET /api/v1/admin/active-sessions
     * Admin xem tất cả phiên ACTIVE toàn hệ thống.
     */
    @GetMapping("/admin/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ActiveSessionsResponse> listActiveSessions() {
        List<ActiveSessionView> sessions = listActiveSessionsHandler.handle(new ListActiveSessionsQuery());
        return ResponseEntity.ok(new ActiveSessionsResponse(sessions));
    }

    /**
     * UC-013 — DELETE /api/v1/admin/sessions/{sessionId}
     * Admin force terminate bất kỳ session nào.
     */
    @DeleteMapping("/admin/{sessionId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> adminForceTerminate(
            @PathVariable String sessionId,
            @AuthenticationPrincipal Jwt jwt) {
        String adminId = jwt.getSubject();
        adminRevokeSessionHandler.handle(new AdminRevokeSession.Command(sessionId, adminId));
        return ResponseEntity.noContent().build();
    }

    record ActiveSessionsResponse(List<ActiveSessionView> data) {}
}
