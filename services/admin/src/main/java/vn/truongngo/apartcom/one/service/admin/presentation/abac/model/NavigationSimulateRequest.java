package vn.truongngo.apartcom.one.service.admin.presentation.abac.model;

import vn.truongngo.apartcom.one.service.admin.application.simulate.simulate_policy.SimulatePolicy;

public record NavigationSimulateRequest(
        SimulatePolicy.SimulateSubjectRequest subject,
        String resourceName,
        Long policySetId
) {}
