package vn.truongngo.apartcom.one.service.party.domain.household;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractAggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;

@Getter
public class Household extends AbstractAggregateRoot<PartyId> implements AggregateRoot<PartyId> {

    private PartyId headPersonId;

    private Household(PartyId id, PartyId headPersonId) {
        super(id);
        this.headPersonId = headPersonId;
    }

    public static Household create(PartyId id, PartyId headPersonId) {
        Assert.notNull(headPersonId, "headPersonId is required");
        return new Household(id, headPersonId);
    }

    public static Household reconstitute(PartyId id, PartyId headPersonId) {
        return new Household(id, headPersonId);
    }

    /**
     * Phase 1: updates headPersonId without member validation.
     * Phase 2: validate that newHeadPersonId is a MEMBER_OF this Household via PartyRelationship.
     */
    public void changeHead(PartyId newHeadPersonId) {
        Assert.notNull(newHeadPersonId, "newHeadPersonId is required");
        this.headPersonId = newHeadPersonId;
    }
}
