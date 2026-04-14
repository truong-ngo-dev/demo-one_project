package vn.truongngo.apartcom.one.service.admin.presentation.abac.model;

public record CreateUIElementRequest(
        String elementId,
        String label,
        String type,
        String group,
        int orderIndex,
        Long resourceId,
        Long actionId,
        String scope   // optional — defaults to ADMIN
) {}
