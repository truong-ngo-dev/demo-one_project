package vn.truongngo.apartcom.one.service.party.presentation.party.model;

import vn.truongngo.apartcom.one.service.party.domain.organization.OrgType;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyType;
import vn.truongngo.apartcom.one.service.party.domain.person.Gender;

import java.time.LocalDate;
import java.util.List;

public record CreatePartyRequest(
        PartyType type,
        String partyName,
        // PERSON fields
        String firstName,
        String lastName,
        LocalDate dob,
        Gender gender,
        // ORGANIZATION fields
        OrgType orgType,
        String taxId,
        String registrationNo,
        // HOUSEHOLD fields
        String headPersonId,
        // shared
        List<IdentificationRequest> identifications
) {}
