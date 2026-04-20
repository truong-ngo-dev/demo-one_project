package vn.truongngo.apartcom.one.service.property.presentation.agreement.model;

import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementType;
import vn.truongngo.apartcom.one.service.property.domain.agreement.PartyType;

import java.time.LocalDate;

public record CreateAgreementRequest(
        String partyId,
        PartyType partyType,
        String assetId,
        OccupancyAgreementType agreementType,
        LocalDate startDate,
        LocalDate endDate,
        String contractRef
) {}
