package vn.truongngo.apartcom.one.service.party.domain.party;

import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;

public class PartyException extends DomainException {

    public PartyException(PartyErrorCode errorCode) {
        super(errorCode);
    }

    public static PartyException notFound() {
        return new PartyException(PartyErrorCode.PARTY_NOT_FOUND);
    }

    public static PartyException alreadyInactive() {
        return new PartyException(PartyErrorCode.PARTY_ALREADY_INACTIVE);
    }

    public static PartyException identificationAlreadyExists() {
        return new PartyException(PartyErrorCode.IDENTIFICATION_ALREADY_EXISTS);
    }

    public static PartyException identificationNotFound() {
        return new PartyException(PartyErrorCode.IDENTIFICATION_NOT_FOUND);
    }

    public static PartyException personNotFound() {
        return new PartyException(PartyErrorCode.PERSON_NOT_FOUND);
    }

    public static PartyException organizationNotFound() {
        return new PartyException(PartyErrorCode.ORGANIZATION_NOT_FOUND);
    }

    public static PartyException householdNotFound() {
        return new PartyException(PartyErrorCode.HOUSEHOLD_NOT_FOUND);
    }

    public static PartyException headPersonNotFound() {
        return new PartyException(PartyErrorCode.HEAD_PERSON_NOT_FOUND);
    }

    public static PartyException invalidPartyType() {
        return new PartyException(PartyErrorCode.INVALID_PARTY_STATUS);
    }
}
