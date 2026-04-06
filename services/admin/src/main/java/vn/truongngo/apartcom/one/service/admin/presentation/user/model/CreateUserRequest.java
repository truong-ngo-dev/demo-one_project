package vn.truongngo.apartcom.one.service.admin.presentation.user.model;

import java.util.List;

public record CreateUserRequest(String email, String username, String fullName, List<String> roleIds) {}
