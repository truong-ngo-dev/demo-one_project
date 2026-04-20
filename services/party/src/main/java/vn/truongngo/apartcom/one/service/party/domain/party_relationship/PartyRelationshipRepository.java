package vn.truongngo.apartcom.one.service.party.domain.party_relationship;

import vn.truongngo.apartcom.one.lib.common.domain.service.Repository;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;

import java.util.List;

public interface PartyRelationshipRepository extends Repository<PartyRelationship, PartyRelationshipId> {

    List<PartyRelationship> findByFromPartyId(PartyId fromPartyId);

    List<PartyRelationship> findByToPartyId(PartyId toPartyId);

    boolean existsActiveByFromAndTo(PartyId fromPartyId, PartyId toPartyId, PartyRelationshipType type);
}
