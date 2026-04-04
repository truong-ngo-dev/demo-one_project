package vn.truongngo.apartcom.one.service.oauth2.infrastructure.persistence.device;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceJpaRepository extends JpaRepository<DeviceJpaEntity, String> {

    List<DeviceJpaEntity> findByUserId(String userId);

    List<DeviceJpaEntity> findByUserIdAndStatus(String userId, String status);

    Optional<DeviceJpaEntity> findByUserIdAndCompositeHash(String userId, String compositeHash);
}
