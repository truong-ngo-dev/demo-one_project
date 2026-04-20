package vn.truongngo.apartcom.one.service.property.domain.agreement;

import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;

public class OccupancyAgreementException extends DomainException {

    public OccupancyAgreementException(OccupancyAgreementErrorCode errorCode) {
        super(errorCode);
    }

    public static OccupancyAgreementException notFound() {
        return new OccupancyAgreementException(OccupancyAgreementErrorCode.AGREEMENT_NOT_FOUND);
    }

    public static OccupancyAgreementException invalidStatus() {
        return new OccupancyAgreementException(OccupancyAgreementErrorCode.AGREEMENT_INVALID_STATUS);
    }

    public static OccupancyAgreementException ownershipAlreadyExists() {
        return new OccupancyAgreementException(OccupancyAgreementErrorCode.OWNERSHIP_ALREADY_EXISTS);
    }

    public static OccupancyAgreementException leaseAlreadyExists() {
        return new OccupancyAgreementException(OccupancyAgreementErrorCode.LEASE_ALREADY_EXISTS);
    }

    public static OccupancyAgreementException invalidAssetTypeForLease() {
        return new OccupancyAgreementException(OccupancyAgreementErrorCode.INVALID_ASSET_TYPE_FOR_LEASE);
    }

    public static OccupancyAgreementException invalidAssetTypeForOwnership() {
        return new OccupancyAgreementException(OccupancyAgreementErrorCode.INVALID_ASSET_TYPE_FOR_OWNERSHIP);
    }

    public static OccupancyAgreementException invalidPartyTypeForOwnership() {
        return new OccupancyAgreementException(OccupancyAgreementErrorCode.INVALID_PARTY_TYPE_FOR_OWNERSHIP);
    }

    public static OccupancyAgreementException invalidPartyTypeForUnit() {
        return new OccupancyAgreementException(OccupancyAgreementErrorCode.INVALID_PARTY_TYPE_FOR_UNIT);
    }

    public static OccupancyAgreementException endDateRequiredForLease() {
        return new OccupancyAgreementException(OccupancyAgreementErrorCode.END_DATE_REQUIRED_FOR_LEASE);
    }
}
