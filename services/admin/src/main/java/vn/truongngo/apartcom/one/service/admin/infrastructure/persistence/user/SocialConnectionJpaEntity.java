package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "user_social_connections", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "provider"}))
@Getter
@Setter
@NoArgsConstructor
public class SocialConnectionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "social_id", nullable = false)
    private String socialId;

    @Column(name = "email")
    private String email;

    @Column(name = "connected_at", nullable = false)
    private Instant connectedAt;
}
