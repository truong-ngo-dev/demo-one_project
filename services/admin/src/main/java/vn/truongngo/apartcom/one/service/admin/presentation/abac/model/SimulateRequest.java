package vn.truongngo.apartcom.one.service.admin.presentation.abac.model;

import java.util.List;
import java.util.Map;

public record SimulateRequest(
        SimulateSubjectRequest subject,
        SimulateResourceRequest resource,
        String action,
        Long policySetId
) {
    public record SimulateSubjectRequest(
            String userId,
            List<String> roles,
            Map<String, Object> attributes
    ) {}

    public record SimulateResourceRequest(
            String name,
            Object data
    ) {}
}
