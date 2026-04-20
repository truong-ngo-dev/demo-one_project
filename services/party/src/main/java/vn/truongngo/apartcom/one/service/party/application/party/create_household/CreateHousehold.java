package vn.truongngo.apartcom.one.service.party.application.party.create_household;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.domain.household.Household;
import vn.truongngo.apartcom.one.service.party.domain.household.HouseholdRepository;
import vn.truongngo.apartcom.one.service.party.domain.party.*;
import vn.truongngo.apartcom.one.service.party.domain.party.event.HouseholdCreatedEvent;
import vn.truongngo.apartcom.one.service.party.domain.person.PersonRepository;

public class CreateHousehold {

    public record Command(String partyName, String headPersonId) {
        public Command {
            Assert.hasText(partyName, "partyName is required");
            Assert.hasText(headPersonId, "headPersonId is required");
        }
    }

    public record Result(String partyId) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final PartyRepository partyRepository;
        private final PersonRepository personRepository;
        private final HouseholdRepository householdRepository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Result handle(Command command) {
            PartyId headPersonId = PartyId.of(command.headPersonId());
            personRepository.findById(headPersonId)
                    .orElseThrow(PartyException::headPersonNotFound);

            Party party = Party.create(PartyType.HOUSEHOLD, command.partyName());
            partyRepository.save(party);

            Household household = Household.create(party.getId(), headPersonId);
            householdRepository.save(household);

            eventDispatcher.dispatch(new HouseholdCreatedEvent(party.getId(), headPersonId));

            return new Result(party.getId().getValue());
        }
    }
}
