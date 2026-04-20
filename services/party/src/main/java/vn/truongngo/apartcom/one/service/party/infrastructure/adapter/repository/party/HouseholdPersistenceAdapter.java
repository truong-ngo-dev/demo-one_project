package vn.truongngo.apartcom.one.service.party.infrastructure.adapter.repository.party;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.party.domain.household.Household;
import vn.truongngo.apartcom.one.service.party.domain.household.HouseholdRepository;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party.HouseholdJpaEntity;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party.HouseholdJpaRepository;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party.HouseholdMapper;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HouseholdPersistenceAdapter implements HouseholdRepository {

    private final HouseholdJpaRepository jpaRepository;

    @Override
    public Optional<Household> findById(PartyId id) {
        return jpaRepository.findById(id.getValue()).map(HouseholdMapper::toDomain);
    }

    @Override
    public void save(Household household) {
        Optional<HouseholdJpaEntity> existing = jpaRepository.findById(household.getId().getValue());
        if (existing.isPresent()) {
            HouseholdMapper.updateEntity(existing.get(), household);
            jpaRepository.save(existing.get());
        } else {
            jpaRepository.save(HouseholdMapper.toEntity(household));
        }
    }

    @Override
    public void delete(PartyId id) {
        jpaRepository.deleteById(id.getValue());
    }
}
