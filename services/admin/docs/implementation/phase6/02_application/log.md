# Phase 6.2 Log — Operator Portal Application Layer

## Status: COMPLETED
`mvn clean compile -DskipTests` — BUILD SUCCESS

---

## Files created

| File                                                                              | UC     |
|-----------------------------------------------------------------------------------|--------|
| `application/operator/link_party_id/LinkPartyId.java`                             | UC-041 |
| `application/operator/assign_operator_context/AssignOperatorContext.java`         | UC-042 |
| `application/operator/revoke_operator_context/RevokeOperatorContext.java`         | UC-043 |
| `application/operator/find_operators_by_building/FindOperatorsByBuilding.java`    | UC-044 |
| `application/operator/assign_roles_to_operator/AssignRolesToOperatorContext.java` | UC-045 |

---

## PHASE6_APP_CONTEXT BLOCK

### Package paths
```
vn.truongngo.apartcom.one.service.admin.application.operator.link_party_id.LinkPartyId
vn.truongngo.apartcom.one.service.admin.application.operator.assign_operator_context.AssignOperatorContext
vn.truongngo.apartcom.one.service.admin.application.operator.revoke_operator_context.RevokeOperatorContext
vn.truongngo.apartcom.one.service.admin.application.operator.find_operators_by_building.FindOperatorsByBuilding
vn.truongngo.apartcom.one.service.admin.application.operator.assign_roles_to_operator.AssignRolesToOperatorContext
```

### OperatorView record (FindOperatorsByBuilding)
```java
record OperatorView(String userId, String partyId, String buildingId, List<String> roleIds, RoleContextStatus status)
```

### Command record fields
| Use case                            | Command fields                             |
|-------------------------------------|--------------------------------------------|
| UC-041 LinkPartyId                  | `userId, partyId`                          |
| UC-042 AssignOperatorContext        | `userId, buildingId, List<String> roleIds` |
| UC-043 RevokeOperatorContext        | `userId, buildingId`                       |
| UC-044 FindOperatorsByBuilding      | `buildingId` (Query)                       |
| UC-045 AssignRolesToOperatorContext | `userId, buildingId, List<String> roleIds` |

### Deviation
- UC-045 replace logic: `getRoleIdsForScope()` để lấy current set → `removeRoleFromContext()` từng cái cũ → `assignRoleToContext()` từng cái mới. Không cần `replaceRolesInContext()` trên User.
