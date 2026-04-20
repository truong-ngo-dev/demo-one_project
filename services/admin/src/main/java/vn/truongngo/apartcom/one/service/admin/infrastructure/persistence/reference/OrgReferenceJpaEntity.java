package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "org_reference")
@Getter
@Setter
@NoArgsConstructor
public class OrgReferenceJpaEntity {

    @Id
    @Column(name = "org_id", nullable = false, length = 36)
    private String orgId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "org_type", nullable = false, length = 20)
    private String orgType;

    @Column(name = "cached_at", nullable = false)
    private Instant cachedAt;
}
