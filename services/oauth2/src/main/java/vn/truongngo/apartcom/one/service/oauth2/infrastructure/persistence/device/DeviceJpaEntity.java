package vn.truongngo.apartcom.one.service.oauth2.infrastructure.persistence.device;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "devices")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "device_hash", length = 500)
    private String deviceHash;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "accept_language", length = 200)
    private String acceptLanguage;

    @Column(name = "composite_hash", length = 64, nullable = false)
    private String compositeHash;

    @Column(name = "device_name", length = 200)
    private String deviceName;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "trusted", nullable = false)
    private boolean trusted;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "last_ip_address", length = 50)
    private String lastIpAddress;
}
