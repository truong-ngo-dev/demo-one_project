package vn.truongngo.apartcom.one.service.party.domain.party;

import vn.truongngo.apartcom.one.lib.common.domain.exception.ErrorCode;

public enum PartyErrorCode implements ErrorCode {

    PARTY_NOT_FOUND("20001", "Party not found", "error.party.not_found", 404),
    PARTY_ALREADY_INACTIVE("20002", "Party is already inactive", "error.party.already_inactive", 422),
    IDENTIFICATION_ALREADY_EXISTS("20003", "Identification already exists", "error.party.identification_exists", 409),
    IDENTIFICATION_NOT_FOUND("20004", "Identification not found", "error.party.identification_not_found", 404),
    INVALID_PARTY_STATUS("20005", "Invalid status for this operation", "error.party.invalid_status", 422),
    PERSON_NOT_FOUND("20006", "Person not found", "error.party.person_not_found", 404),
    ORGANIZATION_NOT_FOUND("20007", "Organization not found", "error.party.organization_not_found", 404),
    HOUSEHOLD_NOT_FOUND("20008", "Household not found", "error.party.household_not_found", 404),
    HEAD_PERSON_NOT_FOUND("20009", "Head person not found", "error.party.head_person_not_found", 404);

    private final String code;
    private final String defaultMessage;
    private final String messageKey;
    private final int httpStatus;

    PartyErrorCode(String code, String defaultMessage, String messageKey, int httpStatus) {
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
