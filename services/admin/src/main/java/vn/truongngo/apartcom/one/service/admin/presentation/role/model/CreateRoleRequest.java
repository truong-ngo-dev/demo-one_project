package vn.truongngo.apartcom.one.service.admin.presentation.role.model;

import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;

public record CreateRoleRequest(String name, String description, Scope scope) {}
