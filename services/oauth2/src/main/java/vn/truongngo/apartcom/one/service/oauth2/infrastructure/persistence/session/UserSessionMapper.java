package vn.truongngo.apartcom.one.service.oauth2.infrastructure.persistence.session;

import vn.truongngo.apartcom.one.lib.shared.domain.user.UserId;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionId;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.SessionStatus;
import vn.truongngo.apartcom.one.service.oauth2.domain.session.Oauth2Session;

import java.time.Instant;

public class UserSessionMapper {

    public static Oauth2Session toDomain(UserSessionJpaEntity entity) {
        return Oauth2Session.reconstitute(
                new SessionId(entity.getId()),
                new UserId(entity.getUserId()),
                entity.getDeviceId(),
                entity.getIdpSessionId(),
                entity.getAuthorizationId(),
                entity.getIpAddress(),
                SessionStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt()
        );
    }

    public static UserSessionJpaEntity toEntity(Oauth2Session session) {
        return UserSessionJpaEntity.builder()
                .id(session.getId().getValueAsString())
                .userId(session.getUserId().getValueAsString())
                .deviceId(session.getDeviceId())
                .idpSessionId(session.getIdpSessionId())
                .authorizationId(session.getAuthorizationId())
                .ipAddress(session.getIpAddress())
                .status(session.getStatus().name())
                .createdAt(session.getCreatedAt())
                .updatedAt(Instant.now())
                .build();
    }
}
