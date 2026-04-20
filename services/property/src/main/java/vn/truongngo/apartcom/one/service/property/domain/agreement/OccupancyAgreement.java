package vn.truongngo.apartcom.one.service.property.domain.agreement;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractAggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetType;

import java.time.Instant;
import java.time.LocalDate;

@Getter
public class OccupancyAgreement extends AbstractAggregateRoot<OccupancyAgreementId> implements AggregateRoot<OccupancyAgreementId> {

    private final String partyId;
    private final PartyType partyType;
    private final String assetId;
    private final OccupancyAgreementType agreementType;
    private OccupancyAgreementStatus status;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String contractRef;
    private final Instant createdAt;
    private Instant updatedAt;

    private OccupancyAgreement(OccupancyAgreementId id, String partyId, PartyType partyType,
                                String assetId, OccupancyAgreementType agreementType,
                                OccupancyAgreementStatus status, LocalDate startDate, LocalDate endDate,
                                String contractRef, Instant createdAt, Instant updatedAt) {
        super(id);
        this.partyId       = partyId;
        this.partyType     = partyType;
        this.assetId       = assetId;
        this.agreementType = agreementType;
        this.status        = status;
        this.startDate     = startDate;
        this.endDate       = endDate;
        this.contractRef   = contractRef;
        this.createdAt     = createdAt;
        this.updatedAt     = updatedAt;
    }

    public static OccupancyAgreement create(String partyId, PartyType partyType, String assetId,
                                             FixedAssetType assetType, OccupancyAgreementType agreementType,
                                             LocalDate startDate, LocalDate endDate, String contractRef) {
        validateInvariants(partyType, assetType, agreementType, endDate);
        Instant now = Instant.now();
        return new OccupancyAgreement(
                OccupancyAgreementId.generate(),
                partyId, partyType, assetId, agreementType,
                OccupancyAgreementStatus.PENDING,
                startDate, endDate, contractRef, now, now
        );
    }

    public static OccupancyAgreement reconstitute(OccupancyAgreementId id, String partyId, PartyType partyType,
                                                   String assetId, OccupancyAgreementType agreementType,
                                                   OccupancyAgreementStatus status, LocalDate startDate,
                                                   LocalDate endDate, String contractRef,
                                                   Instant createdAt, Instant updatedAt) {
        return new OccupancyAgreement(id, partyId, partyType, assetId, agreementType,
                status, startDate, endDate, contractRef, createdAt, updatedAt);
    }

    // ── Behaviors ────────────────────────────────────────────────────────────

    public void activate() {
        if (this.status != OccupancyAgreementStatus.PENDING) throw OccupancyAgreementException.invalidStatus();
        this.status    = OccupancyAgreementStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void terminate() {
        if (this.status != OccupancyAgreementStatus.ACTIVE) throw OccupancyAgreementException.invalidStatus();
        this.status    = OccupancyAgreementStatus.TERMINATED;
        this.updatedAt = Instant.now();
    }

    public void expire() {
        if (this.status != OccupancyAgreementStatus.ACTIVE) throw OccupancyAgreementException.invalidStatus();
        this.status    = OccupancyAgreementStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }

    // ── Domain Invariants I4–I7 ──────────────────────────────────────────────

    private static void validateInvariants(PartyType partyType, FixedAssetType assetType,
                                           OccupancyAgreementType agreementType, LocalDate endDate) {
        if (agreementType == OccupancyAgreementType.OWNERSHIP) {
            // I5: OWNERSHIP only on RESIDENTIAL_UNIT
            if (assetType != FixedAssetType.RESIDENTIAL_UNIT) {
                throw OccupancyAgreementException.invalidAssetTypeForOwnership();
            }
            // I6: OWNERSHIP requires endDate=null and partyType=PERSON
            if (endDate != null) {
                throw new OccupancyAgreementException(OccupancyAgreementErrorCode.AGREEMENT_INVALID_STATUS);
            }
            if (partyType != PartyType.PERSON) {
                throw OccupancyAgreementException.invalidPartyTypeForOwnership();
            }
        } else {
            // LEASE
            // I4: LEASE only on RESIDENTIAL_UNIT or COMMERCIAL_SPACE
            if (assetType != FixedAssetType.RESIDENTIAL_UNIT && assetType != FixedAssetType.COMMERCIAL_SPACE) {
                throw OccupancyAgreementException.invalidAssetTypeForLease();
            }
            // I7: LEASE requires endDate
            if (endDate == null) {
                throw OccupancyAgreementException.endDateRequiredForLease();
            }
            // I7: partyType must match unit type
            if (assetType == FixedAssetType.RESIDENTIAL_UNIT) {
                if (partyType != PartyType.PERSON && partyType != PartyType.HOUSEHOLD) {
                    throw OccupancyAgreementException.invalidPartyTypeForUnit();
                }
            } else {
                // COMMERCIAL_SPACE
                if (partyType != PartyType.ORGANIZATION) {
                    throw OccupancyAgreementException.invalidPartyTypeForUnit();
                }
            }
        }
    }
}
