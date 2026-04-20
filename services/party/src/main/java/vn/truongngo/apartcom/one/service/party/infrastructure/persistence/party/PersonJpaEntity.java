package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.truongngo.apartcom.one.service.party.domain.person.Gender;

import java.time.LocalDate;

@Entity
@Table(name = "person")
@Getter @Setter @NoArgsConstructor
public class PersonJpaEntity {

    @Id
    @Column(name = "party_id", nullable = false, length = 36)
    private String partyId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "dob")
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;
}
