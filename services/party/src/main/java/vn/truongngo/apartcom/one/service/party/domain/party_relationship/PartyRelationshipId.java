package vn.truongngo.apartcom.one.service.party.domain.party_relationship;

import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractId;
import vn.truongngo.apartcom.one.lib.common.domain.model.Id;

import java.util.UUID;

public class PartyRelationshipId extends AbstractId<String> implements Id<String> {

    private PartyRelationshipId(String value) {
        super(value);
    }

    public static PartyRelationshipId of(String value) {
        return new PartyRelationshipId(value);
    }

    public static PartyRelationshipId generate() {
        return new PartyRelationshipId(UUID.randomUUID().toString());
    }
}
