package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyIdentificationType;

import java.time.LocalDate;

@Entity
@Table(name = "party_identification",
       uniqueConstraints = @UniqueConstraint(columnNames = {"type", "value"}))
@Getter @Setter @NoArgsConstructor
public class PartyIdentificationJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "party_id", nullable = false, length = 36)
    private String partyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PartyIdentificationType type;

    @Column(name = "value", nullable = false, length = 100)
    private String value;

    @Column(name = "issued_date")
    private LocalDate issuedDate;
}
