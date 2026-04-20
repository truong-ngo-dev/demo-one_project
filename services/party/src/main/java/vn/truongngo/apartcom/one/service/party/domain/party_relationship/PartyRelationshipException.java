package vn.truongngo.apartcom.one.service.party.domain.party_relationship;

import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;

public class PartyRelationshipException extends DomainException {

    public PartyRelationshipException(PartyRelationshipErrorCode errorCode) {
        super(errorCode);
    }

    public static PartyRelationshipException notFound() {
        return new PartyRelationshipException(PartyRelationshipErrorCode.RELATIONSHIP_NOT_FOUND);
    }

    public static PartyRelationshipException alreadyEnded() {
        return new PartyRelationshipException(PartyRelationshipErrorCode.RELATIONSHIP_ALREADY_ENDED);
    }

    public static PartyRelationshipException memberAlreadyInGroup() {
        return new PartyRelationshipException(PartyRelationshipErrorCode.MEMBER_ALREADY_IN_GROUP);
    }

    public static PartyRelationshipException invalidFromPartyType() {
        return new PartyRelationshipException(PartyRelationshipErrorCode.INVALID_FROM_PARTY_TYPE);
    }

    public static PartyRelationshipException invalidToPartyType() {
        return new PartyRelationshipException(PartyRelationshipErrorCode.INVALID_TO_PARTY_TYPE);
    }
}
