package vn.truongngo.apartcom.one.service.oauth2.infrastructure.persistence.device;

import vn.truongngo.apartcom.one.lib.shared.domain.user.UserId;
import vn.truongngo.apartcom.one.service.oauth2.domain.device.*;

public class DeviceMapper {

    public static Device toDomain(DeviceJpaEntity entity) {
        DeviceFingerprint fingerprint = DeviceFingerprint.of(
                entity.getDeviceHash(),
                entity.getUserAgent(),
                entity.getAcceptLanguage()
        );
        DeviceName name = (entity.getDeviceName() != null && entity.getDeviceType() != null)
                ? DeviceName.of(entity.getDeviceName(), DeviceType.valueOf(entity.getDeviceType()))
                : DeviceName.unknown();

        return Device.reconstitute(
                new DeviceId(entity.getId()),
                new UserId(entity.getUserId()),
                fingerprint,
                name,
                entity.isTrusted(),
                DeviceStatus.valueOf(entity.getStatus()),
                entity.getRegisteredAt(),
                entity.getLastSeenAt(),
                entity.getLastIpAddress()
        );
    }

    public static DeviceJpaEntity toEntity(Device device) {
        return DeviceJpaEntity.builder()
                .id(device.getId().getValueAsString())
                .userId(device.getUserId().getValueAsString())
                .deviceHash(device.getFingerprint().getDeviceHash())
                .userAgent(device.getFingerprint().getUserAgent())
                .acceptLanguage(device.getFingerprint().getAcceptLanguage())
                .compositeHash(device.getFingerprint().getCompositeHash())
                .deviceName(device.getName().getValue())
                .deviceType(device.getName().getType().name())
                .trusted(device.isTrusted())
                .status(device.getStatus().name())
                .registeredAt(device.getRegisteredAt())
                .lastSeenAt(device.getLastSeenAt())
                .lastIpAddress(device.getLastIpAddress())
                .build();
    }
}
