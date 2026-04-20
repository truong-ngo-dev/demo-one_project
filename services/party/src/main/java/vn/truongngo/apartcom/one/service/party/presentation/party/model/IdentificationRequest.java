package vn.truongngo.apartcom.one.service.party.presentation.party.model;

import vn.truongngo.apartcom.one.service.party.domain.party.PartyIdentificationType;

import java.time.LocalDate;

public record IdentificationRequest(PartyIdentificationType type, String value, LocalDate issuedDate) {}
