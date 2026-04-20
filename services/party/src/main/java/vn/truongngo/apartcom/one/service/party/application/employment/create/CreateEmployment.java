package vn.truongngo.apartcom.one.service.party.application.employment.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.domain.employment.*;
import vn.truongngo.apartcom.one.service.party.domain.employment.event.EmploymentCreatedEvent;
import vn.truongngo.apartcom.one.service.party.domain.organization.OrganizationRepository;
import vn.truongngo.apartcom.one.service.party.domain.organization.OrgType;
import vn.truongngo.apartcom.one.service.party.domain.party.*;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.*;

import java.time.LocalDate;

public class CreateEmployment {

    public record Command(
            String personId,
            String orgId,
            EmploymentType employmentType,
            LocalDate startDate
    ) {
        public Command {
            Assert.hasText(personId, "personId is required");
            Assert.hasText(orgId, "orgId is required");
            Assert.notNull(employmentType, "employmentType is required");
            Assert.notNull(startDate, "startDate is required");
        }
    }

    public record Result(String employmentId) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final PartyRepository partyRepository;
        private final OrganizationRepository organizationRepository;
        private final PartyRelationshipRepository partyRelationshipRepository;
        private final EmploymentRepository employmentRepository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Result handle(Command command) {
            PartyId personId = PartyId.of(command.personId());
            PartyId orgId    = PartyId.of(command.orgId());

            Party fromParty = partyRepository.findById(personId)
                    .orElseThrow(PartyException::notFound);
            if (fromParty.getType() != PartyType.PERSON) {
                throw EmploymentException.invalidTarget();
            }

            OrgType orgType = organizationRepository.findById(orgId)
                    .orElseThrow(PartyException::organizationNotFound)
                    .getOrgType();
            if (orgType != OrgType.BQL) {
                throw EmploymentException.invalidTarget();
            }

            if (employmentRepository.existsActiveByEmployeeIdAndOrgId(personId, orgId)) {
                throw EmploymentException.personAlreadyEmployed();
            }

            PartyRelationship rel = PartyRelationship.create(
                    personId, orgId,
                    PartyRelationshipType.EMPLOYED_BY,
                    PartyRoleType.EMPLOYEE, PartyRoleType.EMPLOYER,
                    command.startDate()
            );
            partyRelationshipRepository.save(rel);

            Employment emp = Employment.create(rel.getId(), personId, orgId,
                    command.employmentType(), command.startDate());
            employmentRepository.save(emp);

            eventDispatcher.dispatch(new EmploymentCreatedEvent(emp.getId(), personId, orgId));

            return new Result(emp.getId().getValue());
        }
    }
}
