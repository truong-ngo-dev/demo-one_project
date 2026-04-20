# Prompt: Admin Service IAM — Phase 2.4: TenantSubRoleAssignment Presentation

**Vai trò**: Bạn là Senior Backend Engineer implement presentation layer cho `TenantSubRoleAssignment` trong `services/admin`.

**Yêu cầu**: Phase 2.1–2.3 đã xong và compile pass.

---

## Tài liệu căn cứ

1. Convention: @docs/conventions/api-design.md, @docs/conventions/error-handling.md
2. Design: @docs/development/260416_01_design_party_model/03_admin_iam.md (Section 5 — TENANT portal)
3. Implementation plan: @docs/development/260416_01_design_party_model/admin_iam_plan.md (Phase 2.5 Presentation)
4. Service overview: @services/admin/CLAUDE.md

## Files tham khảo pattern

- Pattern controller: `services/admin/src/main/java/.../presentation/role/RoleController.java`
- Pattern base: `services/admin/src/main/java/.../presentation/base/ApiResponse.java`

Base package: `vn.truongngo.apartcom.one.service.admin`

---

## Context từ Phase 2.3

### Handler class paths
- `vn.truongngo.apartcom.one.service.admin.application.tenant.assign_sub_role.AssignSubRole.Handler`
- `vn.truongngo.apartcom.one.service.admin.application.tenant.revoke_sub_role.RevokeSubRole.Handler`
- `vn.truongngo.apartcom.one.service.admin.application.tenant.find_sub_roles_by_org.FindSubRolesByOrg.Handler`

### AssignSubRole.Command — full fields
```java
record Command(String userId, String orgId, TenantSubRole subRole, String assignedBy)
```

### SubRoleView — full record fields
```java
record SubRoleView(
    String assignmentId,
    String userId,
    String orgId,
    TenantSubRole subRole,
    String assignedBy,
    Instant assignedAt
) {}
// static factory: SubRoleView.from(TenantSubRoleAssignment)
```

### Error codes thrown per handler
- `AssignSubRole`: `UserException.notFound()` (for both assigner and target), `TenantSubRoleException.assignerNotAuthorized()`, `TenantSubRoleException.targetUserNotTenantMember()`, `TenantSubRoleException.alreadyAssigned()`
- `RevokeSubRole`: `TenantSubRoleException.notFound()`
- `FindSubRolesByOrg`: none


---

## Nhiệm vụ cụ thể

Package: `presentation/tenant/`

### 1. Request model (`presentation/tenant/model/`)

**`AssignSubRoleRequest.java`**:
```java
record AssignSubRoleRequest(String userId, TenantSubRole subRole, String assignedBy)
```
> `orgId` lấy từ `@PathVariable`, không có trong body.

### 2. `TenantSubRoleController.java`

Base path: `/api/v1/tenants/{orgId}/sub-roles`

| Method   | Path                                                   | UC                | Body / Params          | Response                          |
|----------|--------------------------------------------------------|-------------------|------------------------|-----------------------------------|
| `POST`   | `/api/v1/tenants/{orgId}/sub-roles`                    | AssignSubRole     | `AssignSubRoleRequest` | `201 { data: { id } }`            |
| `DELETE` | `/api/v1/tenants/{orgId}/sub-roles/{userId}/{subRole}` | RevokeSubRole     | —                      | `200 { data: null }`              |
| `GET`    | `/api/v1/tenants/{orgId}/sub-roles`                    | FindSubRolesByOrg | —                      | `200 { data: List<SubRoleView> }` |

Dùng `ApiResponse<T>` từ `presentation/base/` của admin service.

---

## Cập nhật tài liệu (sau khi compile pass)

- `docs/development/260416_01_design_party_model/admin_iam_plan.md` — tick `[x]` items Phase 2.5 Presentation; Phase 2 Status → `[x] Completed`

---

## Output Log

Xuất log ra `log.md` trong cùng thư mục này sau khi hoàn thành.
