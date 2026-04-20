package vn.truongngo.apartcom.one.service.admin.presentation.operator.model;

import java.util.List;

public record AssignOperatorContextRequest(String userId, List<String> roleIds) {}
