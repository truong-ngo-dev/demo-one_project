package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.employment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.truongngo.apartcom.one.service.party.domain.employment.EmploymentStatus;
import vn.truongngo.apartcom.one.service.party.domain.employment.EmploymentType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employment")
@Getter @Setter @NoArgsConstructor
public class EmploymentJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "relationship_id", nullable = false, length = 36)
    private String relationshipId;

    @Column(name = "employee_id", nullable = false, length = 36)
    private String employeeId;

    @Column(name = "org_id", nullable = false, length = 36)
    private String orgId;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EmploymentStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "employment_id")
    private List<PositionAssignmentJpaEntity> positions = new ArrayList<>();
}
