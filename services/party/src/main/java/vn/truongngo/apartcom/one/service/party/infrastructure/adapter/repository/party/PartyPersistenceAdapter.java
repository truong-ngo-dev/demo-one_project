package vn.truongngo.apartcom.one.service.party.infrastructure.adapter.repository.party;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.party.domain.party.*;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party.PartyJpaEntity;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party.PartyJpaRepository;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party.PartyMapper;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PartyPersistenceAdapter implements PartyRepository {

    private final PartyJpaRepository jpaRepository;

    @Override
    public Optional<Party> findById(PartyId id) {
        return jpaRepository.findById(id.getValue()).map(PartyMapper::toDomain);
    }

    @Override
    public void save(Party party) {
        Optional<PartyJpaEntity> existing = jpaRepository.findById(party.getId().getValue());
        if (existing.isPresent()) {
            PartyMapper.updateEntity(existing.get(), party);
            jpaRepository.save(existing.get());
        } else {
            jpaRepository.save(PartyMapper.toEntity(party));
        }
    }

    @Override
    public void delete(PartyId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public boolean existsByIdentification(PartyIdentificationType type, String value) {
        return jpaRepository.existsByIdentificationsTypeAndIdentificationsValue(type, value);
    }

    @Override
    public Page<Party> search(String keyword, PartyType type, PartyStatus status, Pageable pageable) {
        return jpaRepository.search(keyword, type, status, pageable).map(PartyMapper::toDomain);
    }
}
