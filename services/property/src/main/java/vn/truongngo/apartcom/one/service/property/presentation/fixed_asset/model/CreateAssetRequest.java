package vn.truongngo.apartcom.one.service.property.presentation.fixed_asset.model;

import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetType;

public record CreateAssetRequest(
        FixedAssetType type,
        String parentId,
        String name,
        String code,
        int sequenceNo,
        String managingOrgId
) {}
