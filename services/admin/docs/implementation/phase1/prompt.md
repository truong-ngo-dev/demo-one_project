# Prompt: Admin Service IAM — Phase 1: Domain Extension + Schema Migration

**Vai trò**: Bạn là Senior Backend Engineer mở rộng `services/admin` theo thiết kế IAM mới. Phase 1 là **thay đổi additive trên code hiện có** — không xoá, không rewrite, chỉ thêm field + behavior mới. Mọi use case cũ phải compile và chạy sau phase này.

> **Thứ tự implement**: Phase 1 → Phase 3 → Phase 4 → Phase 2 → Phase 5. Phase này là nền tảng — phải làm trước tất cả.

---

## Tài liệu căn cứ

1. Convention: @docs/conventions/ddd-structure.md
2. Design: @docs/development/260416_01_design_party_model/03_admin_iam.md (Section 2–3)
3. Implementation plan: @docs/development/260416_01_design_party_model/admin_iam_plan.md (Phase 1)
4. Service overview: @services/admin/CLAUDE.md

## Context — code hiện có quan trọng

| File                                                            | Ghi chú                                                                                         |
|-----------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| `domain/user/RoleContext.java`                                  | Hiện có: `scope`, `orgId`, `roleIds`. Thiếu: `status`, `orgType`                                |
| `domain/user/User.java`                                         | Hiện có `addRoleContext(Scope, orgId, Set<RoleId>)`. Thiếu: `partyId` field                     |
| `domain/role/Role.java`                                         | Hiện có: `name`, `description`, `auditable`. Thiếu: `scope`                                     |
| `domain/user/UserFactory.java`                                  | Dùng `user.addRoleContext(Scope.ADMIN, null, roleIds)` — phải update sau khi thay đổi signature |
| `application/user/assign_roles/AssignRoles.java`                | Dùng `user.addRoleContext(Scope.ADMIN, null, roleIds)` — phải update                            |
| `infrastructure/persistence/user/UserRoleContextJpaEntity.java` | Thiếu `status`, `org_type` columns                                                              |
| `infrastructure/persistence/role/RoleJpaEntity.java`            | Thiếu `scope` column                                                                            |
| `infrastructure/persistence/user/UserJpaEntity.java`            | Thiếu `party_id` column                                                                         |

**Lưu ý quan trọng về `addRoleContext`**: Sau khi thêm `orgType`, method signature phải là `addRoleContext(Scope, orgId, OrgType nullable, Set<RoleId>)`. Tất cả callers hiện tại (`UserFactory`, `AssignRoles`) pass `null` cho orgType.

---

## Nhiệm vụ cụ thể

Base package: `vn.truongngo.apartcom.one.service.admin`

### 1. Migration V9

File: `src/main/resources/db/migration/V9__iam_extension.sql`

```sql
-- Extend roles table
ALTER TABLE roles ADD COLUMN scope ENUM('ADMIN','OPERATOR','TENANT','RESIDENT') NOT NULL DEFAULT 'ADMIN';
ALTER TABLE roles DROP INDEX name;
ALTER TABLE roles ADD UNIQUE KEY uq_role_name_scope (name, scope);

-- Extend user_role_context table
ALTER TABLE user_role_context
    ADD COLUMN status   ENUM('ACTIVE','REVOKED') NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN org_type ENUM('PARTY','FIXED_ASSET');

-- Extend users table
ALTER TABLE users ADD COLUMN party_id VARCHAR(36);
```

### 2. Domain — new enums (`domain/user/`)

- `RoleContextStatus.java` — enum: `ACTIVE`, `REVOKED`
- `OrgType.java` — enum: `PARTY`, `FIXED_ASSET`

### 3. Domain — extend `RoleContext`

Thêm fields, cập nhật tất cả constructors:
- `status (RoleContextStatus)` — default `ACTIVE` khi `create()`
- `orgType (OrgType nullable)`

Cập nhật signatures:
```java
// create() — orgType nullable
static RoleContext create(Scope scope, String orgId, OrgType orgType, Set<RoleId> roleIds);

// reconstitute() — thêm status, orgType
static RoleContext reconstitute(Long id, Scope scope, String orgId, OrgType orgType,
                                Set<RoleId> roleIds, RoleContextStatus status);

// thêm behavior mới
void revoke();  // ACTIVE → REVOKED; throw nếu status đã là REVOKED
```

`matchesScope(Scope, orgId)` — giữ nguyên, không thay đổi.

### 4. Domain — extend `Role`

Thêm field `scope (Scope)` — immutable:
```java
// Cập nhật register() — thêm scope param
static Role register(String name, String description, Scope scope);

// Cập nhật reconstitute() — thêm scope param
static Role reconstitute(RoleId id, String name, String description, Scope scope, Auditable auditable);
```

### 5. Domain — extend `User`

Thêm field `partyId (String nullable)` — immutable sau khi set.

Cập nhật `reconstitute()` — thêm `partyId` param.

Cập nhật `addRoleContext()` — thêm `OrgType orgType` param:
```java
// Signature mới (orgType nullable)
public void addRoleContext(Scope scope, String orgId, OrgType orgType, Set<RoleId> roleIds);
```

Thêm `UserErrorCode.PARTY_ID_REQUIRED` (422).

### 6. Domain — update callers bị ảnh hưởng

Trong cùng domain package, update:
- `User` constructor (private) — thêm `partyId`

### 7. Application — update callers

- `domain/user/UserFactory.java` — update tất cả calls sang `addRoleContext(...)` để pass `null` cho `orgType`
- `application/user/assign_roles/AssignRoles.java` — update call `addRoleContext` để pass `null` cho `orgType`

### 8. Infrastructure — update JPA entities + mappers

**`UserRoleContextJpaEntity`**: thêm fields `status`, `orgType` với `@Enumerated(STRING)`.

**`RoleJpaEntity`**: thêm field `scope` với `@Enumerated(STRING)`.

**`UserJpaEntity`**: thêm field `partyId` (column `party_id`, nullable).

**`UserMapper`** (hoặc mapper tương ứng): update `toDomain`, `toEntity`, `updateEntity` để map các field mới.

**`RoleMapper`**: update tương tự cho `scope`.

---

## Không implement

- TenantSubRoleAssignment (Phase 2)
- BuildingReference / OrgReference (Phase 3)
- Event consumers (Phase 4)
- Auth context endpoint (Phase 5)

---

## Cập nhật tài liệu (sau khi compile pass)

- `docs/development/260416_01_design_party_model/admin_iam_plan.md` — tick `[x]` tất cả items Phase 1

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` pass, cung cấp:

### PHASE2_CONTEXT BLOCK
- `RoleContext.create()` và `reconstitute()` — full signatures thực tế
- `Role.register()` và `reconstitute()` — full signatures thực tế
- `User.reconstitute()` — full signature thực tế (kể cả partyId position)
- `User.addRoleContext()` — full signature thực tế
- Bất kỳ deviation nào

---

## Output Log

Xuất log ra `log.md` trong cùng thư mục này sau khi hoàn thành.
