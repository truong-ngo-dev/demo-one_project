package vn.truongngo.apartcom.one.service.party.presentation.party_relationship.model;

import vn.truongngo.apartcom.one.service.party.domain.party_relationship.PartyRoleType;

import java.time.LocalDate;

public record AddMemberRequest(String personId, String groupId, PartyRoleType fromRole, LocalDate startDate) {}
