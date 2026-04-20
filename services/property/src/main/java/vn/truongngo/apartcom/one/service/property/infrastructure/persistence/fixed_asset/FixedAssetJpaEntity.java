package vn.truongngo.apartcom.one.service.property.infrastructure.persistence.fixed_asset;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetStatus;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetType;

import java.time.Instant;

@Entity
@Table(name = "fixed_asset")
@Getter @Setter @NoArgsConstructor
public class FixedAssetJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "parent_id", length = 36)
    private String parentId;

    @Column(name = "path", nullable = false, length = 500)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private FixedAssetType type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FixedAssetStatus status;

    @Column(name = "managing_org_id", length = 36)
    private String managingOrgId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
