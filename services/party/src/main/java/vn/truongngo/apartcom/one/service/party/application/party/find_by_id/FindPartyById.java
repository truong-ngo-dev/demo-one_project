package vn.truongngo.apartcom.one.service.party.application.party.find_by_id;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.party.domain.household.Household;
import vn.truongngo.apartcom.one.service.party.domain.household.HouseholdRepository;
import vn.truongngo.apartcom.one.service.party.domain.organization.OrgType;
import vn.truongngo.apartcom.one.service.party.domain.organization.Organization;
import vn.truongngo.apartcom.one.service.party.domain.organization.OrganizationRepository;
import vn.truongngo.apartcom.one.service.party.domain.party.*;
import vn.truongngo.apartcom.one.service.party.domain.person.Gender;
import vn.truongngo.apartcom.one.service.party.domain.person.Person;
import vn.truongngo.apartcom.one.service.party.domain.person.PersonRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class FindPartyById {

    public record Query(String partyId) {
        public Query {
            Assert.hasText(partyId, "partyId is required");
        }
    }

    public record IdentificationDetail(
            String id,
            PartyIdentificationType type,
            String value,
            LocalDate issuedDate
    ) {}

    public record PersonData(String firstName, String lastName, LocalDate dob, Gender gender) {}

    public record OrgData(OrgType orgType, String taxId, String registrationNo) {}

    public record HouseholdData(String headPersonId) {}

    public record PartyView(
            String id,
            PartyType type,
            String name,
            PartyStatus status,
            List<IdentificationDetail> identifications,
            Object subtypeData,
            Instant createdAt,
            Instant updatedAt
    ) {}

    static class Mapper {
        static IdentificationDetail toIdentificationDetail(PartyIdentification id) {
            return new IdentificationDetail(id.getId().toString(), id.getType(), id.getValue(), id.getIssuedDate());
        }

        static PersonData toPersonData(Person person) {
            return new PersonData(person.getFirstName(), person.getLastName(), person.getDob(), person.getGender());
        }

        static OrgData toOrgData(Organization org) {
            return new OrgData(org.getOrgType(), org.getTaxId(), org.getRegistrationNo());
        }

        static HouseholdData toHouseholdData(Household household) {
            return new HouseholdData(household.getHeadPersonId().getValue());
        }

        static PartyView toView(Party party, Object subtypeData) {
            List<IdentificationDetail> identifications = party.getIdentifications().stream()
                    .map(Mapper::toIdentificationDetail)
                    .toList();
            return new PartyView(
                    party.getId().getValue(),
                    party.getType(),
                    party.getName(),
                    party.getStatus(),
                    identifications,
                    subtypeData,
                    party.getCreatedAt(),
                    party.getUpdatedAt()
            );
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, PartyView> {

        private final PartyRepository partyRepository;
        private final PersonRepository personRepository;
        private final OrganizationRepository organizationRepository;
        private final HouseholdRepository householdRepository;

        @Override
        public PartyView handle(Query query) {
            PartyId partyId = PartyId.of(query.partyId());
            Party party = partyRepository.findById(partyId)
                    .orElseThrow(PartyException::notFound);

            Object subtypeData = switch (party.getType()) {
                case PERSON -> personRepository.findById(partyId)
                        .map(Mapper::toPersonData)
                        .orElseThrow(PartyException::personNotFound);
                case ORGANIZATION -> organizationRepository.findById(partyId)
                        .map(Mapper::toOrgData)
                        .orElseThrow(PartyException::organizationNotFound);
                case HOUSEHOLD -> householdRepository.findById(partyId)
                        .map(Mapper::toHouseholdData)
                        .orElseThrow(PartyException::householdNotFound);
            };

            return Mapper.toView(party, subtypeData);
        }
    }
}
