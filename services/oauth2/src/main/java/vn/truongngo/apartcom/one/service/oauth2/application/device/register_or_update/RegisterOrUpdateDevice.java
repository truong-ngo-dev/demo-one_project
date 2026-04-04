package vn.truongngo.apartcom.one.service.oauth2.application.device.register_or_update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.shared.domain.user.UserId;
import vn.truongngo.apartcom.one.service.oauth2.domain.device.*;

@Component
@RequiredArgsConstructor
public class RegisterOrUpdateDevice implements CommandHandler<RegisterOrUpdateDevice.Command, String> {

    public record Command(
            String userId,
            String deviceHash,
            String userAgent,
            String acceptLanguage,
            String ipAddress) {}

    private final DeviceRepository deviceRepository;
    private final EventDispatcher eventDispatcher;
    private final DeviceNameDetector deviceNameDetector;

    @Override
    public String handle(Command command) {
        UserId userId = new UserId(command.userId());

        DeviceFingerprint fingerprint = DeviceFingerprint.of(
                command.deviceHash(),
                command.userAgent(),
                command.acceptLanguage()
        );

        return deviceRepository
                .findByUserIdAndCompositeHash(userId, fingerprint.getCompositeHash())
                .map(existing -> recordActivity(existing, command.ipAddress()))
                .orElseGet(() -> registerNew(userId, fingerprint, command));
    }

    private String recordActivity(Device device, String ipAddress) {
        device.recordActivity(ipAddress);
        deviceRepository.save(device);
        eventDispatcher.dispatchAll(device.pullDomainEvents());
        return device.getId().getValueAsString();
    }

    private String registerNew(UserId userId, DeviceFingerprint fingerprint, Command command) {
        Device device = Device.register(
                userId,
                fingerprint,
                deviceNameDetector.detect(command.userAgent()),
                command.ipAddress()
        );
        deviceRepository.save(device);
        eventDispatcher.dispatchAll(device.pullDomainEvents());
        return device.getId().getValueAsString();
    }
}
