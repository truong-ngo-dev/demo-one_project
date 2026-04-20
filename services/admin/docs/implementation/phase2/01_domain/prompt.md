# Prompt: Admin Service IAM — Phase 2.1: TenantSubRoleAssignment Domain

**Vai trò**: Bạn là Senior Backend Engineer implement domain layer cho `TenantSubRoleAssignment` trong `services/admin`.

> **Thứ tự implement**: Phase 1 → Phase 3 → Phase 4 → **Phase 2** → Phase 5. Phase 1 phải xong trước.

**Yêu cầu**: Phase 1 đã xong và compile pass.

---

## Tài liệu căn cứ

1. Convention: @docs/conventions/ddd-structure.md
2. Design: @docs/development/260416_01_design_party_model/03_admin_iam.md (Section 2.4 và 6)
3. Implementation plan: @docs/development/260416_01_design_party_model/admin_iam_plan.md (Phase 2.1–2.2)
4. Service overview: @services/admin/CLAUDE.md

## Files tham khảo pattern

- Pattern AR + typed ID: `services/admin/src/main/java/.../domain/role/Role.java` và `RoleId.java`
- Pattern exception: `services/admin/src/main/java/.../domain/role/RoleException.java`

Base package: `vn.truongngo.apartcom.one.service.admin`

---

## Context từ Phase 1

### RoleContext signatures
```java
// create — orgType nullable
static RoleContext create(Scope scope, String orgId, OrgType orgType, Set<RoleId> roleIds);

// reconstitute
static RoleContext reconstitute(Long id, Scope scope, String orgId, OrgType orgType,
                                Set<RoleId> roleIds, RoleContextStatus status);

// revoke
void revoke();  // throws ROLE_CONTEXT_ALREADY_REVOKED if already REVOKED
```

### Role signatures
```java
// register
static Role register(String name, String description, Scope scope);

// reconstitute
static Role reconstitute(RoleId id, String name, String description, Scope scope, Auditable auditable);
```

### User signatures
```java
// reconstitute — partyId is last param
static User reconstitute(UserId id, String username, String email, String phoneNumber,
                         String fullName, UserPassword password,
                         Set<SocialConnection> socialConnections, UserStatus status,
                         Set<RoleContext> roleContexts, boolean usernameChanged,
                         Instant lockedAt, Instant createdAt, Instant updatedAt,
                         String partyId);

// addRoleContext — orgType nullable
public void addRoleContext(Scope scope, String orgId, OrgType orgType, Set<RoleId> roleIdsForContext);
```

### RoleContextStatus
```java
package vn.truongngo.apartcom.one.service.admin.domain.user;
public enum RoleContextStatus { ACTIVE, REVOKED }
```

### OrgType
```java
package vn.truongngo.apartcom.one.service.admin.domain.user;
public enum OrgType { PARTY, FIXED_ASSET }
```

---

## Nhiệm vụ cụ thể

Package: `domain/tenant/`

### 1. `TenantSubRole.java` — enum

Values: `TENANT_MANAGER`, `TENANT_FINANCE`, `TENANT_HR`

### 2. `TenantSubRoleAssignmentId.java` — typed UUID

```java
public class TenantSubRoleAssignmentId extends AbstractId<String> implements Id<String> {
    static TenantSubRoleAssignmentId of(String value);
    static TenantSubRoleAssignmentId generate();
}
```

### 3. `TenantSubRoleAssignment.java` — Aggregate Root

Fields:
- `id` (TenantSubRoleAssignmentId)
- `userId` (String — ref → User)
- `orgId` (String — ref → party-service Organization)
- `subRole` (TenantSubRole)
- `assignedBy` (String — ref → User, phải là TENANT_ADMIN của org)
- `assignedAt` (Instant — immutable)

```java
static TenantSubRoleAssignment create(String userId, String orgId,
                                      TenantSubRole subRole, String assignedBy);
// → assignedAt = Instant.now()

static TenantSubRoleAssignment reconstitute(TenantSubRoleAssignmentId id, String userId,
                                             String orgId, TenantSubRole subRole,
                                             String assignedBy, Instant assignedAt);
```

Không có behaviors khác — delete là tương tác qua repo.

### 4. `TenantSubRoleAssignmentRepository.java` — port interface

```java
TenantSubRoleAssignment save(TenantSubRoleAssignment assignment);
void delete(TenantSubRoleAssignmentId id);
List<TenantSubRoleAssignment> findByOrgId(String orgId);
boolean existsByUserIdAndOrgIdAndSubRole(String userId, String orgId, TenantSubRole subRole);
void deleteAllByOrgId(String orgId);
```

### 5. `TenantSubRoleErrorCode.java` — enum implements ErrorCode

| Enum                            | Code  | HTTP | Message                                                      |
|---------------------------------|-------|------|--------------------------------------------------------------|
| `SUB_ROLE_ASSIGNMENT_NOT_FOUND` | 32001 | 404  | Sub-role assignment not found                                |
| `SUB_ROLE_ALREADY_ASSIGNED`     | 32002 | 409  | Sub-role already assigned to this user in the org            |
| `ASSIGNER_NOT_AUTHORIZED`       | 32003 | 403  | Assigner does not have TENANT context for this org           |
| `TARGET_USER_NOT_TENANT_MEMBER` | 32004 | 422  | Target user does not have active TENANT context for this org |

### 6. `TenantSubRoleException.java` — extends DomainException

Static factory methods cho tất cả error codes trên.

**Không implement**: Infrastructure, Application, Presentation, Spring annotations trong domain.

---

## Cập nhật tài liệu (sau khi compile pass)

- `docs/development/260416_01_design_party_model/admin_iam_plan.md` — tick `[x]` items Phase 2.1 Domain layer

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` pass, cung cấp:

### INFRA_CONTEXT BLOCK
- Package path thực tế của tất cả files
- `TenantSubRoleAssignment.create()` và `reconstitute()` — full signatures
- `TenantSubRoleAssignmentRepository` — full method signatures
- `TenantSubRoleErrorCode` — full enum values

---

## Output Log

Xuất log ra `log.md` trong cùng thư mục này sau khi hoàn thành.
