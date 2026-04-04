package vn.truongngo.apartcom.one.service.oauth2.infrastructure.persistence.activity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "login_activities")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginActivityJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "username", length = 200)
    private String username;

    @Column(name = "result", length = 30, nullable = false)
    private String result;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "composite_hash", length = 64)
    private String compositeHash;

    @Column(name = "device_id", length = 36)
    private String deviceId;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "provider", length = 20)
    private String provider;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
