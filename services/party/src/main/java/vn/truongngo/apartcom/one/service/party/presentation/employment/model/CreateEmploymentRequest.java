package vn.truongngo.apartcom.one.service.party.presentation.employment.model;

import vn.truongngo.apartcom.one.service.party.domain.employment.EmploymentType;

import java.time.LocalDate;

public record CreateEmploymentRequest(
        String personId,
        String orgId,
        EmploymentType employmentType,
        LocalDate startDate
) {}
