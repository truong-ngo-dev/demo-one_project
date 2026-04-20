# Prompt: Admin Service IAM — Phase 2.3: TenantSubRoleAssignment Application

**Vai trò**: Bạn là Senior Backend Engineer implement application layer cho `TenantSubRoleAssignment` trong `services/admin`.

**Yêu cầu**: Phase 2.1 (Domain) và 2.2 (Infrastructure) đã xong và compile pass.

---

## Tài liệu căn cứ

1. Convention: @docs/conventions/ddd-structure.md
2. Design: @docs/development/260416_01_design_party_model/03_admin_iam.md (Section 5 — TENANT portal, Section 6 — B7/B8)
3. Implementation plan: @docs/development/260416_01_design_party_model/admin_iam_plan.md (Phase 2.4 Application)
4. Service overview: @services/admin/CLAUDE.md

## Files tham khảo pattern

- Pattern command handler: `services/admin/src/main/java/.../application/user/assign_roles/AssignRoles.java`
- Pattern query handler: `services/admin/src/main/java/.../application/user/find_by_id/FindUserById.java`

Base package: `vn.truongngo.apartcom.one.service.admin`

---

## Context từ Phase 2.2

### Package paths
- `vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.tenant.TenantSubRoleAssignmentJpaEntity`
- `vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.tenant.TenantSubRoleAssignmentJpaRepository`
- `vn.truongngo.apartcom.one.service.admin.infrastructure.persistence.tenant.TenantSubRoleAssignmentMapper`
- `vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.repository.tenant.TenantSubRoleAssignmentPersistenceAdapter`

### JPA query methods on TenantSubRoleAssignmentJpaRepository
```java
boolean existsByUserIdAndOrgIdAndSubRole(String userId, String orgId, TenantSubRole subRole);
List<TenantSubRoleAssignmentJpaEntity> findAllByOrgId(String orgId);
void deleteAllByOrgId(String orgId);
```

### Deviations
None.

## Context từ Phase 1

- `User.getRoleContexts()` — trả về `Set<RoleContext>`
- `RoleContext.matchesScope(Scope, orgId)` — check scope + orgId
- `RoleContext.getStatus()` — trả về `RoleContextStatus` (ACTIVE / REVOKED)
- `Scope.TENANT` — scope dùng cho kiểm tra B7/B8
- `UserRepository.findById(UserId)` — load user

---

## Nhiệm vụ cụ thể

Package gốc: `application/tenant/`

---

### UC — AssignSubRole (`assign_sub_role/`)

```
Command: userId (String), orgId (String), subRole (TenantSubRole), assignedBy (String)
Result:  assignmentId (String)
```

**Flow:**
1. Load `assigner = userRepo.findById(assignedBy)` → throw `UserException.notFound()`
2. **B7**: Kiểm tra assigner có active TENANT RoleContext cho orgId → `assigner.getRoleContexts().stream().anyMatch(ctx -> ctx.matchesScope(TENANT, orgId) && ctx.getStatus() == ACTIVE)` → nếu không: throw `TenantSubRoleException.assignerNotAuthorized()`
3. Load `targetUser = userRepo.findById(userId)` → throw `UserException.notFound()`
4. **B8**: Kiểm tra targetUser có active TENANT RoleContext cho orgId → nếu không: throw `TenantSubRoleException.targetUserNotTenantMember()`
5. **Duplicate check**: `subRoleRepo.existsByUserIdAndOrgIdAndSubRole(userId, orgId, subRole)` → nếu true: throw `TenantSubRoleException.alreadyAssigned()`
6. `TenantSubRoleAssignment.create(userId, orgId, subRole, assignedBy)`
7. `subRoleRepo.save(assignment)`
8. Return `assignmentId`

### UC — RevokeSubRole (`revoke_sub_role/`)

```
Command: userId (String), orgId (String), subRole (TenantSubRole)
Result:  void (Result record rỗng)
```

**Flow:**
1. Tìm assignment: `subRoleRepo.findByOrgId(orgId)` → filter by userId + subRole → nếu không tìm thấy: throw `TenantSubRoleException.notFound()`
2. `subRoleRepo.delete(assignment.getId())`

### UC — FindSubRolesByOrg (`find_sub_roles_by_org/`)

```
Query:  orgId (String)
Result: List<SubRoleView>
```

**`SubRoleView` record** (khai báo trong package `application/tenant/`):
```java
record SubRoleView(String assignmentId, String userId, String orgId,
                   TenantSubRole subRole, String assignedBy, Instant assignedAt)
```

Static factory: `SubRoleView.from(TenantSubRoleAssignment)`

**Flow:** `subRoleRepo.findByOrgId(orgId)` → map sang `SubRoleView`

---

## Cập nhật tài liệu (sau khi compile pass)

- `docs/development/260416_01_design_party_model/admin_iam_plan.md` — tick `[x]` items Phase 2.4 Application

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` pass, cung cấp:

### PRESENTATION_CONTEXT BLOCK
- Package paths của 3 handler classes
- `AssignSubRole.Command` — full fields
- `SubRoleView` — full record fields
- Error codes được throw từ mỗi handler

---

## Output Log

Xuất log ra `log.md` trong cùng thư mục này sau khi hoàn thành.
