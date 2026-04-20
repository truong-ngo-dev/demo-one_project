package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party_relationship;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.PartyRelationshipStatus;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.PartyRelationshipType;

import java.util.List;

public interface PartyRelationshipJpaRepository extends JpaRepository<PartyRelationshipJpaEntity, String> {

    List<PartyRelationshipJpaEntity> findByFromPartyId(String fromPartyId);

    List<PartyRelationshipJpaEntity> findByToPartyId(String toPartyId);

    boolean existsByFromPartyIdAndToPartyIdAndTypeAndStatus(
            String fromPartyId, String toPartyId,
            PartyRelationshipType type, PartyRelationshipStatus status);
}
