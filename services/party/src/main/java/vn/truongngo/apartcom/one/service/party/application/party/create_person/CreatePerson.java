package vn.truongngo.apartcom.one.service.party.application.party.create_person;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.domain.party.*;
import vn.truongngo.apartcom.one.service.party.domain.party.event.PersonCreatedEvent;
import vn.truongngo.apartcom.one.service.party.domain.person.Gender;
import vn.truongngo.apartcom.one.service.party.domain.person.Person;
import vn.truongngo.apartcom.one.service.party.domain.person.PersonRepository;

import java.time.LocalDate;
import java.util.List;

public class CreatePerson {

    public record IdentificationInput(PartyIdentificationType type, String value, LocalDate issuedDate) {}

    public record Command(
            String partyName,
            String firstName,
            String lastName,
            LocalDate dob,
            Gender gender,
            List<IdentificationInput> identifications
    ) {
        public Command {
            Assert.hasText(partyName, "partyName is required");
            Assert.hasText(firstName, "firstName is required");
            Assert.hasText(lastName, "lastName is required");
        }
    }

    public record Result(String partyId) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final PartyRepository partyRepository;
        private final PersonRepository personRepository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Result handle(Command command) {
            if (command.identifications() != null) {
                for (IdentificationInput input : command.identifications()) {
                    if (partyRepository.existsByIdentification(input.type(), input.value())) {
                        throw PartyException.identificationAlreadyExists();
                    }
                }
            }

            Party party = Party.create(PartyType.PERSON, command.partyName());
            if (command.identifications() != null) {
                for (IdentificationInput input : command.identifications()) {
                    party.addIdentification(input.type(), input.value(), input.issuedDate());
                }
            }
            partyRepository.save(party);

            Person person = Person.create(party.getId(), command.firstName(), command.lastName(),
                    command.dob(), command.gender());
            personRepository.save(person);

            eventDispatcher.dispatch(new PersonCreatedEvent(party.getId(), party.getName()));

            return new Result(party.getId().getValue());
        }
    }
}
