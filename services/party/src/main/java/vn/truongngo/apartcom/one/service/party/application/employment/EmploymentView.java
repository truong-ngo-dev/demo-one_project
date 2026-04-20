package vn.truongngo.apartcom.one.service.party.application.employment;

import vn.truongngo.apartcom.one.service.party.domain.employment.EmploymentStatus;
import vn.truongngo.apartcom.one.service.party.domain.employment.EmploymentType;

import java.time.LocalDate;
import java.util.List;

public record EmploymentView(
        String employmentId,
        String relationshipId,
        String employeeId,
        String orgId,
        EmploymentType employmentType,
        EmploymentStatus status,
        LocalDate startDate,
        LocalDate endDate,
        List<PositionAssignmentView> positions
) {}
