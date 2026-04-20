package vn.truongngo.apartcom.one.service.property.application.agreement;

import vn.truongngo.apartcom.one.service.property.domain.agreement.*;

import java.time.LocalDate;

public record AgreementView(
        String id,
        String partyId,
        PartyType partyType,
        String assetId,
        OccupancyAgreementType agreementType,
        OccupancyAgreementStatus status,
        LocalDate startDate,
        LocalDate endDate,
        String contractRef
) {
    public static AgreementView from(OccupancyAgreement agreement) {
        return new AgreementView(
                agreement.getId().getValue(),
                agreement.getPartyId(),
                agreement.getPartyType(),
                agreement.getAssetId(),
                agreement.getAgreementType(),
                agreement.getStatus(),
                agreement.getStartDate(),
                agreement.getEndDate(),
                agreement.getContractRef()
        );
    }
}
