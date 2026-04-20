package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party_relationship;

import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.PartyRelationship;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.PartyRelationshipId;

public class PartyRelationshipMapper {

    public static PartyRelationship toDomain(PartyRelationshipJpaEntity entity) {
        return PartyRelationship.reconstitute(
                PartyRelationshipId.of(entity.getId()),
                PartyId.of(entity.getFromPartyId()),
                PartyId.of(entity.getToPartyId()),
                entity.getType(),
                entity.getFromRole(),
                entity.getToRole(),
                entity.getStatus(),
                entity.getStartDate(),
                entity.getEndDate()
        );
    }

    public static PartyRelationshipJpaEntity toEntity(PartyRelationship rel) {
        PartyRelationshipJpaEntity entity = new PartyRelationshipJpaEntity();
        entity.setId(rel.getId().getValue());
        entity.setFromPartyId(rel.getFromPartyId().getValue());
        entity.setToPartyId(rel.getToPartyId().getValue());
        entity.setType(rel.getType());
        entity.setFromRole(rel.getFromRole());
        entity.setToRole(rel.getToRole());
        entity.setStatus(rel.getStatus());
        entity.setStartDate(rel.getStartDate());
        entity.setEndDate(rel.getEndDate());
        return entity;
    }

    // Only status and endDate change after creation
    public static void updateEntity(PartyRelationshipJpaEntity existing, PartyRelationship rel) {
        existing.setStatus(rel.getStatus());
        existing.setEndDate(rel.getEndDate());
    }
}
