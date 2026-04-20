package vn.truongngo.apartcom.one.service.property.domain.agreement.event;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractDomainEvent;
import vn.truongngo.apartcom.one.lib.common.domain.model.DomainEvent;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementId;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementType;
import vn.truongngo.apartcom.one.service.property.domain.agreement.PartyType;

import java.time.Instant;
import java.util.UUID;

@Getter
public class OccupancyAgreementActivatedEvent extends AbstractDomainEvent implements DomainEvent {

    private final String partyId;
    private final PartyType partyType;
    private final String assetId;
    private final OccupancyAgreementType agreementType;

    public OccupancyAgreementActivatedEvent(OccupancyAgreementId agreementId, String partyId,
                                             PartyType partyType, String assetId,
                                             OccupancyAgreementType agreementType) {
        super(UUID.randomUUID().toString(), agreementId.getValue(), Instant.now());
        this.partyId       = partyId;
        this.partyType     = partyType;
        this.assetId       = assetId;
        this.agreementType = agreementType;
    }
}
