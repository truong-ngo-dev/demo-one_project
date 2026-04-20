package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party;

import vn.truongngo.apartcom.one.service.party.domain.household.Household;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;

public class HouseholdMapper {

    public static Household toDomain(HouseholdJpaEntity entity) {
        return Household.reconstitute(
                PartyId.of(entity.getPartyId()),
                PartyId.of(entity.getHeadPersonId())
        );
    }

    public static HouseholdJpaEntity toEntity(Household household) {
        HouseholdJpaEntity entity = new HouseholdJpaEntity();
        entity.setPartyId(household.getId().getValue());
        entity.setHeadPersonId(household.getHeadPersonId().getValue());
        return entity;
    }

    public static void updateEntity(HouseholdJpaEntity existing, Household household) {
        existing.setHeadPersonId(household.getHeadPersonId().getValue());
    }
}
