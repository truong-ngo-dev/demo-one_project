package vn.truongngo.apartcom.one.service.party.domain.employment;

import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class PositionAssignment {

    private final UUID id;
    private final BQLPosition position;
    private final String department;
    private final LocalDate startDate;
    private LocalDate endDate;

    private PositionAssignment(UUID id, BQLPosition position, String department,
                               LocalDate startDate, LocalDate endDate) {
        this.id         = id;
        this.position   = position;
        this.department = department;
        this.startDate  = startDate;
        this.endDate    = endDate;
    }

    public static PositionAssignment create(BQLPosition position, String department, LocalDate startDate) {
        return new PositionAssignment(UUID.randomUUID(), position, department, startDate, null);
    }

    public static PositionAssignment reconstitute(UUID id, BQLPosition position, String department,
                                                  LocalDate startDate, LocalDate endDate) {
        return new PositionAssignment(id, position, department, startDate, endDate);
    }

    void close(LocalDate endDate) {
        this.endDate = endDate;
    }
}
