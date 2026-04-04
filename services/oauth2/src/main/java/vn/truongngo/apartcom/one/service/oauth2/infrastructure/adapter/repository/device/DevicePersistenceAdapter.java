package vn.truongngo.apartcom.one.service.oauth2.infrastructure.adapter.repository.device;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.truongngo.apartcom.one.lib.shared.domain.user.UserId;
import vn.truongngo.apartcom.one.service.oauth2.domain.device.*;
import vn.truongngo.apartcom.one.service.oauth2.infrastructure.persistence.device.DeviceJpaRepository;
import vn.truongngo.apartcom.one.service.oauth2.infrastructure.persistence.device.DeviceMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DevicePersistenceAdapter implements DeviceRepository {

    private final DeviceJpaRepository jpaRepository;

    @Override
    public Optional<Device> findById(DeviceId id) {
        return jpaRepository.findById(id.getValueAsString()).map(DeviceMapper::toDomain);
    }

    @Override
    public void save(Device device) {
        jpaRepository.save(DeviceMapper.toEntity(device));
    }

    @Override
    public void delete(DeviceId id) {
        jpaRepository.deleteById(id.getValueAsString());
    }

    @Override
    public List<Device> findAllByUserId(UserId userId) {
        return jpaRepository.findByUserId(userId.getValueAsString())
                .stream()
                .map(DeviceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Device> findActiveByUserId(UserId userId) {
        return jpaRepository.findByUserIdAndStatus(userId.getValueAsString(), "ACTIVE")
                .stream()
                .map(DeviceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Device> findByUserIdAndCompositeHash(UserId userId, String compositeHash) {
        return jpaRepository.findByUserIdAndCompositeHash(
                userId.getValueAsString(),
                compositeHash
        ).map(DeviceMapper::toDomain);
    }
}
