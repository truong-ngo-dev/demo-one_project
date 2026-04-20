package vn.truongngo.apartcom.one.service.property.domain.fixed_asset;

import vn.truongngo.apartcom.one.lib.common.domain.exception.ErrorCode;

public enum FixedAssetErrorCode implements ErrorCode {

    ASSET_NOT_FOUND("30001", "Fixed asset not found", "error.asset.not_found", 404),
    ASSET_ALREADY_INACTIVE("30002", "Fixed asset is already inactive", "error.asset.already_inactive", 422),
    MANAGING_ORG_REQUIRED("30003", "Managing organization is required for a building", "error.asset.managing_org_required", 422),
    INVALID_ASSET_STATUS_TRANSITION("30004", "Invalid status transition for this asset", "error.asset.invalid_status_transition", 422),
    INVALID_ASSET_TYPE_FOR_PARENT("30005", "Asset type is not valid for the given parent", "error.asset.invalid_type_for_parent", 422);

    private final String code;
    private final String defaultMessage;
    private final String messageKey;
    private final int httpStatus;

    FixedAssetErrorCode(String code, String defaultMessage, String messageKey, int httpStatus) {
        this.code           = code;
        this.defaultMessage = defaultMessage;
        this.messageKey     = messageKey;
        this.httpStatus     = httpStatus;
    }

    @Override public String code()           { return code; }
    @Override public String defaultMessage() { return defaultMessage; }
    @Override public String messageKey()     { return messageKey; }
    @Override public int httpStatus()        { return httpStatus; }
}
