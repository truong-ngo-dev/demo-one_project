package vn.truongngo.apartcom.one.service.oauth2.domain.device;

import vn.truongngo.apartcom.one.lib.common.domain.service.Repository;
import vn.truongngo.apartcom.one.lib.shared.domain.user.UserId;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends Repository<Device, DeviceId> {

    List<Device> findActiveByUserId(UserId userId);

    List<Device> findAllByUserId(UserId userId);

    Optional<Device> findByUserIdAndCompositeHash(UserId userId, String compositeHash);
}
