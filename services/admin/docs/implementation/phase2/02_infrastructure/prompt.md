# Prompt: Admin Service IAM — Phase 2.2: TenantSubRoleAssignment Infrastructure

**Vai trò**: Bạn là Senior Backend Engineer implement infrastructure layer cho `TenantSubRoleAssignment` trong `services/admin`. Domain đã xong.

**Yêu cầu**: Phase 2.1 (Domain) đã xong và compile pass.

---

## Tài liệu căn cứ

1. Convention: @docs/conventions/ddd-structure.md
2. Implementation plan: @docs/development/260416_01_design_party_model/admin_iam_plan.md (Phase 2.2–2.3 Infrastructure)
3. Service overview: @services/admin/CLAUDE.md

## Files tham khảo pattern

- Pattern JPA entity: `services/admin/src/main/java/.../infrastructure/persistence/role/RoleJpaEntity.java`
- Pattern mapper: `services/admin/src/main/java/.../infrastructure/persistence/role/RoleMapper.java`

Base package: `vn.truongngo.apartcom.one.service.admin`

---

## Context từ Phase 2.1

### Package
`vn.truongngo.apartcom.one.service.admin.domain.tenant`

### TenantSubRoleAssignment signatures
```java
static TenantSubRoleAssignment create(String userId, String orgId,
                                      TenantSubRole subRole, String assignedBy);
// → id = TenantSubRoleAssignmentId.generate(), assignedAt = Instant.now()

static TenantSubRoleAssignment reconstitute(TenantSubRoleAssignmentId id, String userId,
                                             String orgId, TenantSubRole subRole,
                                             String assignedBy, Instant assignedAt);
```

### TenantSubRoleAssignmentRepository
```java
TenantSubRoleAssignment save(TenantSubRoleAssignment assignment);
void delete(TenantSubRoleAssignmentId id);
List<TenantSubRoleAssignment> findByOrgId(String orgId);
boolean existsByUserIdAndOrgIdAndSubRole(String userId, String orgId, TenantSubRole subRole);
void deleteAllByOrgId(String orgId);
```

### TenantSubRoleErrorCode
```text
SUB_ROLE_ASSIGNMENT_NOT_FOUND  — "32001", 404
SUB_ROLE_ALREADY_ASSIGNED      — "32002", 409
ASSIGNER_NOT_AUTHORIZED        — "32003", 403
TARGET_USER_NOT_TENANT_MEMBER  — "32004", 422
```

### TenantSubRoleException static factories
```text
TenantSubRoleException.notFound()
TenantSubRoleException.alreadyAssigned()
TenantSubRoleException.assignerNotAuthorized()
TenantSubRoleException.targetUserNotTenantMember()
```


---

## Nhiệm vụ cụ thể

### 1. Migration V10

File: `src/main/resources/db/migration/V10__tenant_sub_role.sql`

```sql
CREATE TABLE tenant_sub_role_assignment (
    id          VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36) NOT NULL,
    org_id      VARCHAR(36) NOT NULL,
    sub_role    ENUM('TENANT_MANAGER','TENANT_FINANCE','TENANT_HR') NOT NULL,
    assigned_by VARCHAR(36) NOT NULL,
    assigned_at DATETIME NOT NULL,
    UNIQUE KEY uq_sub_role (user_id, org_id, sub_role),
    FOREIGN KEY (user_id)     REFERENCES users(id),
    FOREIGN KEY (assigned_by) REFERENCES users(id)
);
```

### 2. JPA Entity (`infrastructure/persistence/tenant/`)

**`TenantSubRoleAssignmentJpaEntity.java`**:
- `@Entity @Table(name="tenant_sub_role_assignment")`
- `@Id String id` (no @GeneratedValue)
- `@Enumerated(STRING)` cho `subRole`
- `assigned_at` → `Instant assignedAt`

### 3. JPA Repository (`infrastructure/persistence/tenant/`)

**`TenantSubRoleAssignmentJpaRepository.java`** extends `JpaRepository<TenantSubRoleAssignmentJpaEntity, String>`:
```java
boolean existsByUserIdAndOrgIdAndSubRole(String userId, String orgId, TenantSubRole subRole);
List<TenantSubRoleAssignmentJpaEntity> findAllByOrgId(String orgId);
void deleteAllByOrgId(String orgId);
```

### 4. Mapper (`infrastructure/persistence/tenant/`)

**`TenantSubRoleAssignmentMapper.java`** — static methods:
- `toDomain(entity) → TenantSubRoleAssignment` — dùng `reconstitute()`
- `toEntity(domain) → TenantSubRoleAssignmentJpaEntity`

### 5. Persistence Adapter (`infrastructure/adapter/repository/tenant/`)

**`TenantSubRoleAssignmentPersistenceAdapter.java`** implements `TenantSubRoleAssignmentRepository`:
- `save`: `jpaRepo.save(toEntity(a))` → `toDomain(saved)`
- `delete`: `jpaRepo.deleteById(id.getValue())`
- `findByOrgId`: `findAllByOrgId` → map toDomain
- `existsByUserIdAndOrgIdAndSubRole`: delegate trực tiếp
- `deleteAllByOrgId`: delegate trực tiếp

---

## Cập nhật tài liệu (sau khi compile pass)

- `docs/development/260416_01_design_party_model/admin_iam_plan.md` — tick `[x]` items Phase 2.2 Infrastructure

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` pass, cung cấp:

### APPLICATION_CONTEXT BLOCK
- Package paths thực tế của tất cả files
- JPA query method names thực tế trên `TenantSubRoleAssignmentJpaRepository`
- Deviation (nếu có)

---

## Output Log

Xuất log ra `log.md` trong cùng thư mục này sau khi hoàn thành.
