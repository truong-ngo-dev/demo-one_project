package vn.truongngo.apartcom.one.service.oauth2.domain.session;

import vn.truongngo.apartcom.one.lib.common.domain.service.Repository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends Repository<Oauth2Session, SessionId> {
    Optional<Oauth2Session> findByAuthorizationId(String authorizationId);
    List<Oauth2Session> findActiveByDeviceId(String deviceId);
    List<Oauth2Session> findActiveByUserId(String userId);
    void revokeAllByDeviceId(String deviceId);
    void revokeAllByUserId(String userId);
}
