package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party_relationship;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.PartyRelationshipStatus;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.PartyRelationshipType;
import vn.truongngo.apartcom.one.service.party.domain.party_relationship.PartyRoleType;

import java.time.LocalDate;

@Entity
@Table(name = "party_relationship")
@Getter @Setter @NoArgsConstructor
public class PartyRelationshipJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "from_party_id", nullable = false, length = 36)
    private String fromPartyId;

    @Column(name = "to_party_id", nullable = false, length = 36)
    private String toPartyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PartyRelationshipType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_role", nullable = false)
    private PartyRoleType fromRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_role", nullable = false)
    private PartyRoleType toRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PartyRelationshipStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
