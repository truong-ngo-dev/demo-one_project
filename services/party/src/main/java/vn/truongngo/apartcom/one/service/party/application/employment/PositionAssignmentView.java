package vn.truongngo.apartcom.one.service.party.application.employment;

import java.time.LocalDate;

public record PositionAssignmentView(
        String positionAssignmentId,
        String position,
        String department,
        LocalDate startDate,
        LocalDate endDate
) {}
