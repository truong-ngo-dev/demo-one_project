package vn.truongngo.apartcom.one.service.party.presentation.employment.model;

import vn.truongngo.apartcom.one.service.party.domain.employment.BQLPosition;

import java.time.LocalDate;

public record AssignPositionRequest(
        BQLPosition position,
        String department,
        LocalDate startDate
) {}
