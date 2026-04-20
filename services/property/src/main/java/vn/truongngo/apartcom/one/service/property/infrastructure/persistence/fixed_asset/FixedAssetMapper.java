package vn.truongngo.apartcom.one.service.property.infrastructure.persistence.fixed_asset;

import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAsset;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetId;

public class FixedAssetMapper {

    public static FixedAsset toDomain(FixedAssetJpaEntity entity) {
        return FixedAsset.reconstitute(
                FixedAssetId.of(entity.getId()),
                entity.getType(),
                entity.getName(),
                entity.getCode(),
                entity.getSequenceNo(),
                entity.getParentId() != null ? FixedAssetId.of(entity.getParentId()) : null,
                entity.getPath(),
                entity.getStatus(),
                entity.getManagingOrgId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static FixedAssetJpaEntity toEntity(FixedAsset domain) {
        FixedAssetJpaEntity entity = new FixedAssetJpaEntity();
        entity.setId(domain.getId().getValue());
        entity.setParentId(domain.getParentId() != null ? domain.getParentId().getValue() : null);
        entity.setPath(domain.getPath());
        entity.setType(domain.getType());
        entity.setName(domain.getName());
        entity.setCode(domain.getCode());
        entity.setSequenceNo(domain.getSequenceNo());
        entity.setStatus(domain.getStatus());
        entity.setManagingOrgId(domain.getManagingOrgId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public static void updateEntity(FixedAssetJpaEntity existing, FixedAsset domain) {
        existing.setName(domain.getName());
        existing.setCode(domain.getCode());
        existing.setSequenceNo(domain.getSequenceNo());
        existing.setStatus(domain.getStatus());
        existing.setUpdatedAt(domain.getUpdatedAt());
    }
}
