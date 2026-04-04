package vn.truongngo.apartcom.one.service.oauth2.infrastructure.persistence.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserSessionJpaRepository extends JpaRepository<UserSessionJpaEntity, String> {

    Optional<UserSessionJpaEntity> findByAuthorizationId(String authorizationId);

    List<UserSessionJpaEntity> findByDeviceIdAndStatus(String deviceId, String status);

    @Modifying
    @Transactional
    @Query("UPDATE UserSessionJpaEntity s SET s.status = 'REVOKED' WHERE s.deviceId = :deviceId AND s.status = 'ACTIVE'")
    void revokeAllByDeviceId(@Param("deviceId") String deviceId);

    @Modifying
    @Transactional
    @Query("UPDATE UserSessionJpaEntity s SET s.status = 'REVOKED' WHERE s.userId = :userId AND s.status = 'ACTIVE'")
    void revokeAllByUserId(@Param("userId") String userId);

    List<UserSessionJpaEntity> findByUserIdAndStatus(String userId, String active);
}
