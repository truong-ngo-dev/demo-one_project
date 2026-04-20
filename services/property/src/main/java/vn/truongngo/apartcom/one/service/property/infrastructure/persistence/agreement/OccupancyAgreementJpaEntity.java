package vn.truongngo.apartcom.one.service.property.infrastructure.persistence.agreement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementStatus;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementType;
import vn.truongngo.apartcom.one.service.property.domain.agreement.PartyType;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "occupancy_agreement")
@Getter @Setter @NoArgsConstructor
public class OccupancyAgreementJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "party_id", nullable = false, length = 36)
    private String partyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "party_type", nullable = false)
    private PartyType partyType;

    @Column(name = "asset_id", nullable = false, length = 36)
    private String assetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_type", nullable = false)
    private OccupancyAgreementType agreementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OccupancyAgreementStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "contract_ref", length = 100)
    private String contractRef;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
