package vn.truongngo.apartcom.one.service.party.infrastructure.adapter.repository.party;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.party.domain.organization.Organization;
import vn.truongngo.apartcom.one.service.party.domain.organization.OrganizationRepository;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party.OrganizationJpaEntity;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party.OrganizationJpaRepository;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party.OrganizationMapper;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrganizationPersistenceAdapter implements OrganizationRepository {

    private final OrganizationJpaRepository jpaRepository;

    @Override
    public Optional<Organization> findById(PartyId id) {
        return jpaRepository.findById(id.getValue()).map(OrganizationMapper::toDomain);
    }

    @Override
    public void save(Organization org) {
        Optional<OrganizationJpaEntity> existing = jpaRepository.findById(org.getId().getValue());
        if (existing.isPresent()) {
            OrganizationMapper.updateEntity(existing.get(), org);
            jpaRepository.save(existing.get());
        } else {
            jpaRepository.save(OrganizationMapper.toEntity(org));
        }
    }

    @Override
    public void delete(PartyId id) {
        jpaRepository.deleteById(id.getValue());
    }
}
