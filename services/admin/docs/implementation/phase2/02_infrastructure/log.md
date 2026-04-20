# Phase 2.2 Log — TenantSubRoleAssignment Infrastructure

## Status: COMPLETED
`mvn clean compile -DskipTests` — BUILD SUCCESS

---

## Files created

| File                                                                                      | Notes                                          |
|-------------------------------------------------------------------------------------------|------------------------------------------------|
| `src/main/resources/db/migration/V10__tenant_sub_role.sql`                                | CREATE TABLE tenant_sub_role_assignment        |
| `infrastructure/persistence/tenant/TenantSubRoleAssignmentJpaEntity.java`                 | `@Entity`, no `@GeneratedValue` on id          |
| `infrastructure/persistence/tenant/TenantSubRoleAssignmentJpaRepository.java`             | extends `JpaRepository<..., String>`           |
| `infrastructure/persistence/tenant/TenantSubRoleAssignmentMapper.java`                    | static `toDomain`, `toEntity`                  |
| `infrastructure/adapter/repository/tenant/TenantSubRoleAssignmentPersistenceAdapter.java` | implements `TenantSubRoleAssignmentRepository` |

---

## APPLICATION_CONTEXT BLOCK

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
