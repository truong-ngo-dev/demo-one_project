package vn.truongngo.apartcom.one.service.party.application.party.add_identification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.domain.party.*;

import java.time.LocalDate;

public class AddPartyIdentification {

    public record Command(PartyId partyId, PartyIdentificationType type, String value, LocalDate issuedDate) {
        public Command {
            Assert.notNull(partyId, "partyId is required");
            Assert.notNull(type, "type is required");
            Assert.hasText(value, "value is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final PartyRepository partyRepository;

        @Override
        @Transactional
        public Void handle(Command command) {
            Party party = partyRepository.findById(command.partyId())
                    .orElseThrow(PartyException::notFound);

            if (partyRepository.existsByIdentification(command.type(), command.value())) {
                throw PartyException.identificationAlreadyExists();
            }

            party.addIdentification(command.type(), command.value(), command.issuedDate());
            partyRepository.save(party);

            return null;
        }
    }
}
