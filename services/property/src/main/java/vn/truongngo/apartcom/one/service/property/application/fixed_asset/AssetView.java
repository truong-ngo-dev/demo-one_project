package vn.truongngo.apartcom.one.service.property.application.fixed_asset;

import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAsset;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetStatus;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetType;

public record AssetView(
        String id,
        String parentId,
        String path,
        FixedAssetType type,
        String name,
        String code,
        int sequenceNo,
        FixedAssetStatus status,
        String managingOrgId
) {
    public static AssetView from(FixedAsset asset) {
        return new AssetView(
                asset.getId().getValue(),
                asset.getParentId() != null ? asset.getParentId().getValue() : null,
                asset.getPath(),
                asset.getType(),
                asset.getName(),
                asset.getCode(),
                asset.getSequenceNo(),
                asset.getStatus(),
                asset.getManagingOrgId()
        );
    }
}
