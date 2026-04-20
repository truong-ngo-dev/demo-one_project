# Use Case Index

## User Management

| ID     | Tên                               | Mô tả ngắn                                                                     | Trạng thái  | File                                                                  |
|--------|-----------------------------------|--------------------------------------------------------------------------------|-------------|-----------------------------------------------------------------------|
| UC-001 | Admin tạo user                    | Admin tạo tài khoản user mới                                                   | Implemented | [UC-001_admin_create_user.md](UC-001_admin_create_user.md)            |
| UC-002 | User tự đăng ký                   | User tự tạo tài khoản                                                          | Implemented | [UC-002_register.md](UC-002_register.md)                              |
| UC-003 | Xác thực Social Login             | Handler thông tin xác thực social sau khi đăng nhập (internal)                 | Modify      | [UC-003_post_social_auth_handler.md](UC-003_post_social_auth_handler) |
| UC-004 | Tìm user theo ID                  | Query user theo UserId                                                         | Implemented | [UC-004_get_user.md](UC-004_get_user.md)                              |
| UC-005 | Tìm user theo identity (internal) | oauth2 service dùng để xác thực credentials                                    | Implemented | [UC-005_get_identity.md](UC-005_get_identity.md)                      |
| UC-006 | Search user                       | Admin tìm kiếm và filter danh sách user                                        | Implemented | [UC-006_search_user.md](UC-006_search_user.md)                        |
| UC-007 | Lock / Unlock user                | Admin khoá hoặc mở khoá tài khoản                                              | Implemented | [UC-007_lock_unlock_user.md](UC-007_lock_unlock_user.md)              |
| UC-016 | Update profile                    | Cập nhật thông tin cá nhân — bao gồm lần đầu complete profile sau social login | Implemented | [UC-016_update_profile.md](UC-016_update_profile.md)                  |
| UC-017 | Change password                   | User tự đổi hoặc tạo password                                                  | Implemented | [UC-017_change_password.md](UC-017_change_password.md)                |
| UC-018 | Delete user                       | Soft delete tài khoản                                                          |             | [UC-018_delete_user.md](UC-018_delete_user.md)                        |

---

## Role Management

| ID     | Tên               | Mô tả ngắn                                   | Status      | File                                             |
|--------|-------------------|----------------------------------------------|-------------|--------------------------------------------------|
| UC-008 | Tạo role          | Admin tạo role mới                           | Implemented | [UC-008_create_role.md](UC-008_create_role.md)   |
| UC-009 | Tìm role theo ID  | Query role theo RoleId                       | Implemented | [UC-009_get_role.md](UC-009_get_role.md)         |
| UC-010 | Danh sách roles   | Admin xem và tìm kiếm danh sách role         | Implemented | [UC-010_list_roles.md](UC-010_list_roles.md)     |
| UC-011 | Cập nhật role     | Admin cập nhật description của role          | Implemented | [UC-011_update_role.md](UC-011_update_role.md)   |
| UC-012 | Xóa role          | Admin xóa role — kiểm tra không có user dùng | Implemented | [UC-012_delete_role.md](UC-012_delete_role.md)   |
| UC-013 | Gán role cho user | Admin gán một hoặc nhiều role cho user       | Implemented | [UC-013_assign_roles.md](UC-013_assign_roles.md) |
| UC-014 | Gỡ role khỏi user | Admin gỡ role khỏi user                      | Implemented | [UC-014_remove_role.md](UC-014_remove_role.md)   |

---

---

## ABAC Policy Management

| ID     | Tên                         | Mô tả ngắn                                                                   | Trạng thái  | File                                                                         |
|--------|-----------------------------|------------------------------------------------------------------------------|-------------|------------------------------------------------------------------------------|
| UC-019 | Resource & Action Catalogue | CRUD resource definitions + action catalogue                                 | Implemented | [UC-019_resource_action_catalogue.md](UC-019_resource_action_catalogue.md)   |
| UC-020 | Policy Set Management       | CRUD policy sets (root container)                                            | Implemented | [UC-020_policy_set_management.md](UC-020_policy_set_management.md)           |
| UC-021 | Policy Management           | CRUD policies trong policy set                                               | Implemented | [UC-021_policy_management.md](UC-021_policy_management.md)                   |
| UC-022 | Rule Management             | CRUD rules với raw SpEL, reorder                                             | Implemented | [UC-022_rule_management.md](UC-022_rule_management.md)                       |
| UC-023 | UIElement Registry          | CRUD UIElement + batch evaluate visibility                                   | Implemented | [UC-023_ui_element_registry.md](UC-023_ui_element_registry.md)               |
| UC-024 | Policy Simulator            | Mock enforce API — test policy với virtual subject                           | Implemented | [UC-024_policy_simulate.md](UC-024_policy_simulate.md)                       |
| UC-031 | Navigation Simulate         | Evaluate toàn bộ actions của resource cho virtual subject ở navigation level | Implemented | [UC-031_navigation_simulate.md](UC-031_navigation_simulate.md)               |
| UC-032 | Rule Impact Preview         | Phân tích SpEL expression trích roles, attributes, actions bị ảnh hưởng      | Implemented | [UC-032_rule_impact_preview.md](UC-032_rule_impact_preview.md)               |
| UC-033 | Evaluation Trace            | Per-rule trace khi simulate instance mode — hiển thị lý do PERMIT/DENY       | Implemented | [UC-033_evaluation_trace.md](UC-033_evaluation_trace.md)                     |
| UC-034 | Reverse Lookup              | Tìm rules cover resource+action; matchedRuleName trong navigation mode       | Implemented | [UC-034_reverse_lookup.md](UC-034_reverse_lookup.md)                         |
| UC-035 | Admin Change Audit Log      | Ghi thay đổi policy/rule/UIElement; query log theo entity/performer          | Implemented | [UC-035_admin_change_audit_log.md](UC-035_admin_change_audit_log.md)         |
| UC-036 | UIElement Policy Coverage   | Đánh dấu UIElement có/không được cover bởi PERMIT rule; list uncovered       | Implemented | [UC-036_ui_element_policy_coverage.md](UC-036_ui_element_policy_coverage.md) |

---

## ABAC Phase 2 — Enforcement

| ID     | Tên                          | Mô tả ngắn                                                              | Trạng thái | File                                                                 |
|--------|------------------------------|-------------------------------------------------------------------------|------------|----------------------------------------------------------------------|
| UC-025 | PEP — API Enforcement        | Wire PepEngine + @PreEnforce vào controllers; 403 khi DENY              | Planned    | [UC-025_pep_enforcement.md](UC-025_pep_enforcement.md)               |
| UC-026 | Subject Attribute Enrichment | Load user profile attributes vào Subject.attributes cho SpEL conditions | Planned    | [UC-026_subject_enrichment.md](UC-026_subject_enrichment.md)         |
| UC-027 | Expression Composition       | AND/OR kết hợp nhiều SpEL expressions trong 1 rule                      | Planned    | [UC-027_expression_composition.md](UC-027_expression_composition.md) |
| UC-028 | Frontend Route Guard         | Angular guard check evaluate result — chặn URL-typing bypass            | Planned    | [UC-028_frontend_route_guard.md](UC-028_frontend_route_guard.md)     |
| UC-029 | Policy Decision Audit Log    | Ghi PERMIT/DENY decisions + query API cho admin debug/compliance        | Planned    | [UC-029_policy_audit_log.md](UC-029_policy_audit_log.md)             |

---

---

## IAM Context Management

### Tenant Sub-Role Assignment

| ID     | Tên                           | Mô tả ngắn                                                                            | Trạng thái  |
|--------|-------------------------------|---------------------------------------------------------------------------------------|-------------|
| UC-037 | Assign Tenant Sub-Role        | TENANT_ADMIN gán sub-role (MANAGER/FINANCE/HR) cho member trong cùng org              | Implemented |
| UC-038 | Revoke Tenant Sub-Role        | Gỡ sub-role của user trong org                                                        | Implemented |
| UC-039 | Xem sub-roles theo org        | Query danh sách TenantSubRoleAssignment của 1 orgId                                   | Implemented |

### Auth Context Query

| ID     | Tên                           | Mô tả ngắn                                                                            | Trạng thái  |
|--------|-------------------------------|---------------------------------------------------------------------------------------|-------------|
| UC-040 | Get User Auth Contexts        | Trả về list RoleContext ACTIVE của user, kèm displayName từ building/org reference    | Implemented |

### Operator Portal

| ID     | Tên                               | Mô tả ngắn                                                                        | Trạng thái  |
|--------|-----------------------------------|------------------------------------------------------------------------------------|-------------|
| UC-041 | Link Party ID to User             | Gắn partyId vào User — cần trước khi assign OPERATOR context                      | Implemented |
| UC-042 | Assign Operator Context           | SUPER_ADMIN/BQL_MANAGER gán OPERATOR context cho user tại 1 building               | Implemented |
| UC-043 | Revoke Operator Context           | Thu hồi OPERATOR context của user tại 1 building                                  | Implemented |
| UC-044 | Find Operators by Building        | Query danh sách user có OPERATOR context tại 1 building                            | Implemented |
| UC-045 | Assign Roles to Operator Context  | Cập nhật roleIds trong OPERATOR context của user tại building — B6 (scope check)   | Implemented |

---

## [PLANNED]

| ID     | Tên                      | Mô tả ngắn                     |
|--------|--------------------------|--------------------------------|
| UC-015 | Verify email             | Xác thực email sau khi đăng ký |
| UC-030 | MFA config               | Cấu hình xác thực đa yếu tố    |