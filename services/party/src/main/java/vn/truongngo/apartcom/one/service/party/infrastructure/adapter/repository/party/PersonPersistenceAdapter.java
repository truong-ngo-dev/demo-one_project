package vn.truongngo.apartcom.one.service.party.infrastructure.adapter.repository.party;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;
import vn.truongngo.apartcom.one.service.party.domain.person.Person;
import vn.truongngo.apartcom.one.service.party.domain.person.PersonRepository;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party.PersonJpaEntity;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party.PersonJpaRepository;
import vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party.PersonMapper;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PersonPersistenceAdapter implements PersonRepository {

    private final PersonJpaRepository jpaRepository;

    @Override
    public Optional<Person> findById(PartyId id) {
        return jpaRepository.findById(id.getValue()).map(PersonMapper::toDomain);
    }

    @Override
    public void save(Person person) {
        Optional<PersonJpaEntity> existing = jpaRepository.findById(person.getId().getValue());
        if (existing.isPresent()) {
            PersonMapper.updateEntity(existing.get(), person);
            jpaRepository.save(existing.get());
        } else {
            jpaRepository.save(PersonMapper.toEntity(person));
        }
    }

    @Override
    public void delete(PartyId id) {
        jpaRepository.deleteById(id.getValue());
    }
}
