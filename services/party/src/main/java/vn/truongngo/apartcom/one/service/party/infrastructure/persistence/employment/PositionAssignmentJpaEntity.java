package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.employment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.truongngo.apartcom.one.service.party.domain.employment.BQLPosition;

import java.time.LocalDate;

@Entity
@Table(name = "position_assignment")
@Getter @Setter @NoArgsConstructor
public class PositionAssignmentJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "employment_id", nullable = false, length = 36)
    private String employmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false)
    private BQLPosition position;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
