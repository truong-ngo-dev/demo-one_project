package vn.truongngo.apartcom.one.service.party.domain.party;

import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractId;
import vn.truongngo.apartcom.one.lib.common.domain.model.Id;

import java.util.UUID;

public class PartyId extends AbstractId<String> implements Id<String> {

    private PartyId(String value) {
        super(value);
    }

    public static PartyId of(String value) {
        return new PartyId(value);
    }

    public static PartyId generate() {
        return new PartyId(UUID.randomUUID().toString());
    }
}
