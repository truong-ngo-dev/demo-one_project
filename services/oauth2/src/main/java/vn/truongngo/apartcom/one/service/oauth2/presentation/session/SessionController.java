package vn.truongngo.apartcom.one.service.oauth2.presentation.session;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
import vn.truongngo.apartcom.one.service.oauth2.application.session.remote_revoke.RemoteRevokeSession;

import java.util.List;

/**
 * UC-007: User xem danh sách thiết bị + trạng thái session.
 * UC-008: User đăng xuất từ xa một thiết bị/session cụ thể.
 */
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final ListMyDevicesHandler listMyDevicesHandler;
    private final RemoteRevokeSession.Handler remoteRevokeSessionHandler;

    /**
     * GET /api/v1/sessions/me
     * Trả về danh sách thiết bị của user hiện tại với trạng thái session.
     */
    @GetMapping("/me")
    public ResponseEntity<List<DeviceSessionView>> listMyDevices(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        String currentSid = jwt.getClaim("sid");
        return ResponseEntity.ok(listMyDevicesHandler.handle(new ListMyDevicesQuery(userId, currentSid)));
    }

    /**
     * DELETE /api/v1/sessions/me/{sessionId}
     * Đăng xuất từ xa một thiết bị/session cụ thể của user hiện tại.
     * Không thể dùng để đăng xuất session hiện tại — dùng /logout chuẩn.
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
}
