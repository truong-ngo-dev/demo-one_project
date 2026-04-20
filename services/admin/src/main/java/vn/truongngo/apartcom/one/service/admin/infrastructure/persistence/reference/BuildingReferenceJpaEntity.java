package vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "building_reference")
@Getter
@Setter
@NoArgsConstructor
public class BuildingReferenceJpaEntity {

    @Id
    @Column(name = "building_id", nullable = false, length = 36)
    private String buildingId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "managing_org_id", length = 36)
    private String managingOrgId;

    @Column(name = "cached_at", nullable = false)
    private Instant cachedAt;
}
