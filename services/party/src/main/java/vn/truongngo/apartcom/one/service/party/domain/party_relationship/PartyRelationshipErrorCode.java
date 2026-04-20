package vn.truongngo.apartcom.one.service.party.domain.party_relationship;

import vn.truongngo.apartcom.one.lib.common.domain.exception.ErrorCode;

public enum PartyRelationshipErrorCode implements ErrorCode {

    RELATIONSHIP_NOT_FOUND     ("20101", "Relationship not found",                                   "error.relationship.not_found",         404),
    RELATIONSHIP_ALREADY_ENDED ("20102", "Relationship already ended",                               "error.relationship.already_ended",     422),
    MEMBER_ALREADY_IN_GROUP    ("20103", "Member already in group",                                  "error.relationship.member_duplicate",  409),
    INVALID_FROM_PARTY_TYPE    ("20104", "From party must be a Person",                              "error.relationship.invalid_from_type", 422),
    INVALID_TO_PARTY_TYPE      ("20105", "To party must be Household or non-BQL Organization",       "error.relationship.invalid_to_type",   422);

    private final String code;
    private final String defaultMessage;
    private final String messageKey;
    private final int httpStatus;

    PartyRelationshipErrorCode(String code, String defaultMessage, String messageKey, int httpStatus) {
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
