package vn.truongngo.apartcom.one.service.oauth2.application.device.admin_query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.shared.domain.user.UserId;
import vn.truongngo.apartcom.one.service.oauth2.domain.device.Device;
import vn.truongngo.apartcom.one.service.oauth2.domain.device.DeviceRepository;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.Oauth2Session;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * UC-014: Admin xem danh sách thiết bị + trạng thái session ACTIVE của một User.
 * Logic giống ListMyDevicesHandler (UC-007) nhưng:
 * - Filter theo targetUserId (không phải sub của caller).
 * - Không có isCurrent — Admin không có session trên thiết bị User.
 */
@Component
@RequiredArgsConstructor
public class ListUserDeviceSessionsHandler
        implements QueryHandler<ListUserDeviceSessionsQuery, List<AdminDeviceSessionView>> {

    private final DeviceRepository deviceRepository;
    private final SessionRepository sessionRepository;

    @Override
    public List<AdminDeviceSessionView> handle(ListUserDeviceSessionsQuery query) {
        UserId userId = new UserId(query.targetUserId());

        List<Device> devices = deviceRepository.findAllByUserId(userId);

        // deviceId → ACTIVE session mapping (giữ session mới nhất nếu có nhiều)
        Map<String, Oauth2Session> sessionByDeviceId = sessionRepository
                .findActiveByUserId(query.targetUserId())
                .stream()
                .collect(Collectors.toMap(
                        Oauth2Session::getDeviceId,
                        Function.identity(),
                        (existing, replacement) -> existing.getCreatedAt().isAfter(replacement.getCreatedAt())
                                ? existing : replacement));

        return devices.stream()
                .map(device -> {
                    String deviceId = device.getId().getValueAsString();
                    Oauth2Session activeSession = sessionByDeviceId.get(deviceId);
                    return new AdminDeviceSessionView(
                            deviceId,
                            device.getName().getValue(),
                            device.getLastIpAddress(),
                            device.getLastSeenAt(),
                            activeSession != null ? activeSession.getId().getValueAsString() : null,
                            activeSession != null ? activeSession.getStatus().name() : null
                    );
                })
                .collect(Collectors.toList());
    }
}
