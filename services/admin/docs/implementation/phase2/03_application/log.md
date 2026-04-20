# Phase 2.3 Log — TenantSubRoleAssignment Application

## Status: COMPLETED
`mvn clean compile -DskipTests` — BUILD SUCCESS

---

## Files created

| File                                                              | Notes                                            |
|-------------------------------------------------------------------|--------------------------------------------------|
| `application/tenant/assign_sub_role/AssignSubRole.java`           | B7 + B8 validation, duplicate check              |
| `application/tenant/revoke_sub_role/RevokeSubRole.java`           | Find by orgId then filter, delete by id          |
| `application/tenant/find_sub_roles_by_org/FindSubRolesByOrg.java` | Query + `SubRoleView` record with static factory |

---

## PRESENTATION_CONTEXT BLOCK

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
