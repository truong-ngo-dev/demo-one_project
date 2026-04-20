package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party;

import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;
import vn.truongngo.apartcom.one.service.party.domain.person.Person;

public class PersonMapper {

    public static Person toDomain(PersonJpaEntity entity) {
        return Person.reconstitute(
                PartyId.of(entity.getPartyId()),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getDob(),
                entity.getGender()
        );
    }

    public static PersonJpaEntity toEntity(Person person) {
        PersonJpaEntity entity = new PersonJpaEntity();
        entity.setPartyId(person.getId().getValue());
        entity.setFirstName(person.getFirstName());
        entity.setLastName(person.getLastName());
        entity.setDob(person.getDob());
        entity.setGender(person.getGender());
        return entity;
    }

    public static void updateEntity(PersonJpaEntity existing, Person person) {
        existing.setFirstName(person.getFirstName());
        existing.setLastName(person.getLastName());
        existing.setDob(person.getDob());
        existing.setGender(person.getGender());
    }
}
