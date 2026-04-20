package vn.truongngo.apartcom.one.service.admin.presentation.tenant.model;

import vn.truongngo.apartcom.one.service.admin.domain.tenant.TenantSubRole;

public record AssignSubRoleRequest(String userId, TenantSubRole subRole, String assignedBy) {}
