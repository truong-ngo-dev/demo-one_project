package vn.truongngo.apartcom.one.service.oauth2.infrastructure.adapter.repository.session;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionId;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionRepository;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.Oauth2Session;
import vn.truongngo.apartcom.one.service.oauth2.infrastructure.persistence.session.UserSessionJpaRepository;
import vn.truongngo.apartcom.one.service.oauth2.infrastructure.persistence.session.UserSessionMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserSessionPersistenceAdapter implements SessionRepository {

    private final UserSessionJpaRepository jpaRepository;

    @Override
    public Optional<Oauth2Session> findById(SessionId id) {
        return jpaRepository.findById(id.getValueAsString()).map(UserSessionMapper::toDomain);
    }

    @Override
    public void save(Oauth2Session session) {
        jpaRepository.save(UserSessionMapper.toEntity(session));
    }

    @Override
    public void delete(SessionId id) {
        jpaRepository.deleteById(id.getValueAsString());
    }

    @Override
    public Optional<Oauth2Session> findByAuthorizationId(String authorizationId) {
        return jpaRepository.findByAuthorizationId(authorizationId).map(UserSessionMapper::toDomain);
    }

    @Override
    public List<Oauth2Session> findActiveByDeviceId(String deviceId) {
        return jpaRepository.findByDeviceIdAndStatus(deviceId, "ACTIVE")
                .stream()
                .map(UserSessionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Oauth2Session> findActiveByUserId(String userId) {
        return jpaRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .stream()
                .map(UserSessionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void revokeAllByDeviceId(String deviceId) {
        jpaRepository.revokeAllByDeviceId(deviceId);
    }

    @Override
    public void revokeAllByUserId(String userId) {
        jpaRepository.revokeAllByUserId(userId);
    }
}
