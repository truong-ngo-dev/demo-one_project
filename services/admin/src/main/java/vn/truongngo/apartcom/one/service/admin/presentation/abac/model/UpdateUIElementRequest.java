package vn.truongngo.apartcom.one.service.admin.presentation.abac.model;

public record UpdateUIElementRequest(
        String label,
        String type,
        String group,
        int orderIndex,
        Long resourceId,
        Long actionId
) {}
