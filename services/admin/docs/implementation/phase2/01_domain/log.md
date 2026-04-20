# Phase 2.1 Log — TenantSubRoleAssignment Domain

## Status: COMPLETED
`mvn clean compile -DskipTests` — BUILD SUCCESS

---

## Files created

| File                                                   | Notes                                           |
|--------------------------------------------------------|-------------------------------------------------|
| `domain/tenant/TenantSubRole.java`                     | Enum: TENANT_MANAGER, TENANT_FINANCE, TENANT_HR |
| `domain/tenant/TenantSubRoleAssignmentId.java`         | Typed UUID — `of(String)`, `generate()`         |
| `domain/tenant/TenantSubRoleAssignment.java`           | AR — `create()`, `reconstitute()`               |
| `domain/tenant/TenantSubRoleAssignmentRepository.java` | Port interface                                  |
| `domain/tenant/TenantSubRoleErrorCode.java`            | ErrorCode enum — 4 values                       |
| `domain/tenant/TenantSubRoleException.java`            | DomainException with static factories           |

---

## INFRA_CONTEXT BLOCK

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
