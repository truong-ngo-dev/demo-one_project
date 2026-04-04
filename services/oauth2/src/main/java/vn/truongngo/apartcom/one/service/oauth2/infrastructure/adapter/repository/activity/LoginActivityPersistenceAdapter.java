package vn.truongngo.apartcom.one.service.oauth2.infrastructure.adapter.repository.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.truongngo.apartcom.one.service.oauth2.domain.activity.LoginActivity;
import vn.truongngo.apartcom.one.service.oauth2.domain.activity.LoginActivityId;
import vn.truongngo.apartcom.one.service.oauth2.domain.activity.LoginActivityRepository;
import vn.truongngo.apartcom.one.service.oauth2.infrastructure.persistence.activity.LoginActivityJpaEntity;
import vn.truongngo.apartcom.one.service.oauth2.infrastructure.persistence.activity.LoginActivityJpaRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LoginActivityPersistenceAdapter implements LoginActivityRepository {

    private final LoginActivityJpaRepository jpaRepository;

    @Override
    public Optional<LoginActivity> findById(LoginActivityId id) {
        throw new UnsupportedOperationException("LoginActivity lookup by ID is not supported");
    }

    @Override
    public void save(LoginActivity activity) {
        LoginActivityJpaEntity entity = LoginActivityJpaEntity.builder()
                .id(activity.getId().getValueAsString())
                .userId(activity.getUserId())
                .username(activity.getUsername())
                .result(activity.getResult().name())
                .ipAddress(activity.getIpAddress())
                .userAgent(activity.getUserAgent())
                .compositeHash(activity.getCompositeHash())
                .deviceId(activity.getDeviceId())
                .sessionId(activity.getSessionId())
                .provider(activity.getProvider() != null ? activity.getProvider().name() : null)
                .createdAt(activity.getCreatedAt())
                .build();
        jpaRepository.save(entity);
    }

    @Override
    public void delete(LoginActivityId id) {
        throw new UnsupportedOperationException("LoginActivity is append-only, cannot be deleted");
    }
}
