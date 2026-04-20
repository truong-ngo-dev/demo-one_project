package vn.truongngo.apartcom.one.service.party.infrastructure.adapter.repository.party_relationship;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.*;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party_relationship.PartyRelationshipJpaEntity;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party_relationship.PartyRelationshipJpaRepository;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party_relationship.PartyRelationshipMapper;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PartyRelationshipPersistenceAdapter implements PartyRelationshipRepository {

    private final PartyRelationshipJpaRepository jpaRepository;

    @Override
    public Optional<PartyRelationship> findById(PartyRelationshipId id) {
        return jpaRepository.findById(id.getValue()).map(PartyRelationshipMapper::toDomain);
    }

    @Override
    public void save(PartyRelationship rel) {
        Optional<PartyRelationshipJpaEntity> existing = jpaRepository.findById(rel.getId().getValue());
        if (existing.isPresent()) {
            PartyRelationshipMapper.updateEntity(existing.get(), rel);
            jpaRepository.save(existing.get());
        } else {
            jpaRepository.save(PartyRelationshipMapper.toEntity(rel));
        }
    }

    @Override
    public void delete(PartyRelationshipId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public List<PartyRelationship> findByFromPartyId(PartyId fromPartyId) {
        return jpaRepository.findByFromPartyId(fromPartyId.getValue()).stream()
                .map(PartyRelationshipMapper::toDomain)
                .toList();
    }

    @Override
    public List<PartyRelationship> findByToPartyId(PartyId toPartyId) {
        return jpaRepository.findByToPartyId(toPartyId.getValue()).stream()
                .map(PartyRelationshipMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByFromAndTo(PartyId fromPartyId, PartyId toPartyId,
                                           PartyRelationshipType type) {
        return jpaRepository.existsByFromPartyIdAndToPartyIdAndTypeAndStatus(
                fromPartyId.getValue(), toPartyId.getValue(), type, PartyRelationshipStatus.ACTIVE);
    }
}
