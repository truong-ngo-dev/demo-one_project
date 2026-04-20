package vn.truongngo.apartcom.one.service.property.domain.fixed_asset;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractAggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;

import java.time.Instant;

@Getter
public class FixedAsset extends AbstractAggregateRoot<FixedAssetId> implements AggregateRoot<FixedAssetId> {

    private final FixedAssetType type;
    private String name;
    private String code;
    private int sequenceNo;
    private final FixedAssetId parentId;
    private final String path;
    private FixedAssetStatus status;
    private final String managingOrgId;
    private final Instant createdAt;
    private Instant updatedAt;

    private FixedAsset(FixedAssetId id, FixedAssetType type, String name, String code, int sequenceNo,
                       FixedAssetId parentId, String path, FixedAssetStatus status,
                       String managingOrgId, Instant createdAt, Instant updatedAt) {
        super(id);
        this.type          = type;
        this.name          = name;
        this.code          = code;
        this.sequenceNo    = sequenceNo;
        this.parentId      = parentId;
        this.path          = path;
        this.status        = status;
        this.managingOrgId = managingOrgId;
        this.createdAt     = createdAt;
        this.updatedAt     = updatedAt;
    }

    public static FixedAsset create(FixedAssetId id, FixedAssetType type, String name, String code, int sequenceNo,
                                    FixedAssetId parentId, String path, String managingOrgId) {
        if (type == FixedAssetType.BUILDING && (managingOrgId == null || managingOrgId.isBlank())) {
            throw FixedAssetException.managingOrgRequired();
        }
        Instant now = Instant.now();
        return new FixedAsset(id, type, name, code, sequenceNo,
                              parentId, path, FixedAssetStatus.ACTIVE, managingOrgId, now, now);
    }

    public static FixedAsset reconstitute(FixedAssetId id, FixedAssetType type, String name, String code,
                                          int sequenceNo, FixedAssetId parentId, String path,
                                          FixedAssetStatus status, String managingOrgId,
                                          Instant createdAt, Instant updatedAt) {
        return new FixedAsset(id, type, name, code, sequenceNo, parentId, path,
                              status, managingOrgId, createdAt, updatedAt);
    }

    // ── Behaviors ────────────────────────────────────────────────────────────

    public void deactivate() {
        if (this.status == FixedAssetStatus.INACTIVE) throw FixedAssetException.alreadyInactive();
        this.status    = FixedAssetStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public void setUnderMaintenance() {
        if (this.status != FixedAssetStatus.ACTIVE) throw FixedAssetException.invalidStatusTransition();
        this.status    = FixedAssetStatus.UNDER_MAINTENANCE;
        this.updatedAt = Instant.now();
    }

    public void reactivate() {
        if (this.status == FixedAssetStatus.ACTIVE) throw FixedAssetException.invalidStatusTransition();
        this.status    = FixedAssetStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }
}
