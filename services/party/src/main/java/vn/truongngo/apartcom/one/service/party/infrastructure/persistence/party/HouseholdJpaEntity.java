package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "household")
@Getter @Setter @NoArgsConstructor
public class HouseholdJpaEntity {

    @Id
    @Column(name = "party_id", nullable = false, length = 36)
    private String partyId;

    @Column(name = "head_person_id", nullable = false, length = 36)
    private String headPersonId;
}
