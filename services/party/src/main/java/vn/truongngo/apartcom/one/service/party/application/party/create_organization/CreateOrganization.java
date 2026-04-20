package vn.truongngo.apartcom.one.service.party.application.party.create_organization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.domain.organization.Organization;
import vn.truongngo.apartcom.one.service.party.domain.organization.OrganizationRepository;
import vn.truongngo.apartcom.one.service.party.domain.organization.OrgType;
import vn.truongngo.apartcom.one.service.party.domain.party.*;
import vn.truongngo.apartcom.one.service.party.domain.party.event.OrganizationCreatedEvent;

import java.time.LocalDate;
import java.util.List;

public class CreateOrganization {

    public record IdentificationInput(PartyIdentificationType type, String value, LocalDate issuedDate) {}

    public record Command(
            String partyName,
            OrgType orgType,
            String taxId,
            String registrationNo,
            List<IdentificationInput> identifications
    ) {
        public Command {
            Assert.hasText(partyName, "partyName is required");
            Assert.notNull(orgType, "orgType is required");
        }
    }

    public record Result(String partyId) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final PartyRepository partyRepository;
        private final OrganizationRepository organizationRepository;
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

            Party party = Party.create(PartyType.ORGANIZATION, command.partyName());
            if (command.identifications() != null) {
                for (IdentificationInput input : command.identifications()) {
                    party.addIdentification(input.type(), input.value(), input.issuedDate());
                }
            }
            partyRepository.save(party);

            Organization org = Organization.create(party.getId(), command.orgType(),
                    command.taxId(), command.registrationNo());
            organizationRepository.save(org);

            eventDispatcher.dispatch(new OrganizationCreatedEvent(party.getId(), party.getName(), command.orgType()));

            return new Result(party.getId().getValue());
        }
    }
}
