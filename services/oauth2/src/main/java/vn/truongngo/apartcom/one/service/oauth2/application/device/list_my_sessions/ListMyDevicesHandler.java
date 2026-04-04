package vn.truongngo.apartcom.one.service.oauth2.application.device.list_my_sessions;

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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * UC-007: User xem danh sách thiết bị + trạng thái session.
 */
@Component
@RequiredArgsConstructor
public class ListMyDevicesHandler implements QueryHandler<ListMyDevicesQuery, List<DeviceSessionView>> {

    private final DeviceRepository deviceRepository;
    private final SessionRepository sessionRepository;

    @Override
    public List<DeviceSessionView> handle(ListMyDevicesQuery query) {
        UserId userId = new UserId(query.userId());

        List<Device> devices = deviceRepository.findAllByUserId(userId);

        // deviceId → active session mapping
        // merge function: giữ session mới nhất nếu 1 device có nhiều ACTIVE session
        Map<String, Oauth2Session> sessionByDeviceId = sessionRepository
                .findActiveByUserId(query.userId())
                .stream()
                .collect(Collectors.toMap(
                        Oauth2Session::getDeviceId,
                        Function.identity(),
                        (existing, replacement) -> existing.getCreatedAt().isAfter(replacement.getCreatedAt())
                                ? existing : replacement));

        // sid claim → hiện tại session đang dùng
        String currentDeviceId = Optional.ofNullable(query.currentSid())
                .flatMap(sessionRepository::findByAuthorizationId)
                .map(Oauth2Session::getDeviceId)
                .orElse(null);

        return devices.stream()
                .map(device -> {
                    String deviceId = device.getId().getValueAsString();
                    Oauth2Session activeSession = sessionByDeviceId.get(deviceId);
                    return new DeviceSessionView(
                            deviceId,
                            device.getName().getValue(),
                            device.getLastIpAddress(),
                            device.getLastSeenAt(),
                            activeSession != null ? activeSession.getId().getValueAsString() : null,
                            activeSession != null ? activeSession.getStatus().name() : null,
                            deviceId.equals(currentDeviceId)
                    );
                })
                .collect(Collectors.toList());
    }
}
