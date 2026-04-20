package vn.truongngo.apartcom.one.service.property.domain.fixed_asset;

import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;

public class FixedAssetException extends DomainException {

    public FixedAssetException(FixedAssetErrorCode errorCode) {
        super(errorCode);
    }

    public static FixedAssetException notFound() {
        return new FixedAssetException(FixedAssetErrorCode.ASSET_NOT_FOUND);
    }

    public static FixedAssetException alreadyInactive() {
        return new FixedAssetException(FixedAssetErrorCode.ASSET_ALREADY_INACTIVE);
    }

    public static FixedAssetException managingOrgRequired() {
        return new FixedAssetException(FixedAssetErrorCode.MANAGING_ORG_REQUIRED);
    }

    public static FixedAssetException invalidStatusTransition() {
        return new FixedAssetException(FixedAssetErrorCode.INVALID_ASSET_STATUS_TRANSITION);
    }

    public static FixedAssetException invalidTypeForParent() {
        return new FixedAssetException(FixedAssetErrorCode.INVALID_ASSET_TYPE_FOR_PARENT);
    }
}
