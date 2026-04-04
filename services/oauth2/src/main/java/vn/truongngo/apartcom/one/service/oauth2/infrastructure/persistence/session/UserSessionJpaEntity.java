package vn.truongngo.apartcom.one.service.oauth2.infrastructure.persistence.session;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "oauth_sessions")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "device_id", length = 36, nullable = false)
    private String deviceId;

    @Column(name = "idp_session_id", length = 100)
    private String idpSessionId;

    @Column(name = "authorization_id", length = 100, nullable = false, unique = true)
    private String authorizationId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
