package vn.truongngo.apartcom.one.service.property.domain.agreement;

import vn.truongngo.apartcom.one.lib.common.domain.exception.ErrorCode;

public enum OccupancyAgreementErrorCode implements ErrorCode {

    AGREEMENT_NOT_FOUND("31001", "Occupancy agreement not found", "error.agreement.not_found", 404),
    AGREEMENT_INVALID_STATUS("31002", "Invalid status transition for this agreement", "error.agreement.invalid_status", 422),
    OWNERSHIP_ALREADY_EXISTS("31003", "An active ownership agreement already exists for this asset", "error.agreement.ownership_already_exists", 409),
    LEASE_ALREADY_EXISTS("31004", "An active lease agreement already exists for this asset", "error.agreement.lease_already_exists", 409),
    INVALID_ASSET_TYPE_FOR_LEASE("31005", "Lease is only valid for RESIDENTIAL_UNIT or COMMERCIAL_SPACE", "error.agreement.invalid_asset_type_for_lease", 422),
    INVALID_ASSET_TYPE_FOR_OWNERSHIP("31006", "Ownership is only valid for RESIDENTIAL_UNIT", "error.agreement.invalid_asset_type_for_ownership", 422),
    INVALID_PARTY_TYPE_FOR_OWNERSHIP("31007", "Ownership requires party type PERSON", "error.agreement.invalid_party_type_for_ownership", 422),
    INVALID_PARTY_TYPE_FOR_UNIT("31008", "Party type does not match the unit type for this lease", "error.agreement.invalid_party_type_for_unit", 422),
    END_DATE_REQUIRED_FOR_LEASE("31009", "End date is required for lease agreements", "error.agreement.end_date_required_for_lease", 422);

    private final String code;
    private final String defaultMessage;
    private final String messageKey;
    private final int httpStatus;

    OccupancyAgreementErrorCode(String code, String defaultMessage, String messageKey, int httpStatus) {
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
