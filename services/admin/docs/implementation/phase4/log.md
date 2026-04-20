# Phase 4 Log — Event Consumers

## Status: COMPLETED
`mvn clean compile -DskipTests` — BUILD SUCCESS

---

## Files created / modified

| File | Notes |
|------|-------|
| `domain/user/User.java` | Added `revokeRoleContext(Scope, String orgId)` method |
| `domain/user/UserRepository.java` | Added `findByPartyId(String)`, `findAllByActiveRoleContext(Scope, String)` |
| `domain/party/PartyClient.java` | Port interface — `getMembers(partyId)` |
| `infrastructure/persistence/user/UserJpaRepository.java` | Added `findByPartyId` (derived), `findAllByActiveRoleContext` (@Query with status param) |
| `infrastructure/adapter/repository/user/UserPersistenceAdapter.java` | Implemented 2 new methods |
| `infrastructure/adapter/client/PartyClientAdapter.java` | RestClient impl; graceful degradation on failure |
| `application/event/building/BuildingCreatedEventHandler.java` | Upsert BuildingReference |
| `application/event/organization/OrganizationCreatedEventHandler.java` | Upsert OrgReference if orgType == "BQL" |
| `application/event/agreement/OccupancyAgreementActivatedEventHandler.java` | RESIDENT/TENANT context creation; 3 partyType branches |
| `application/event/agreement/OccupancyAgreementTerminatedEventHandler.java` | Revoke contexts; deleteAllByOrgId via Optional injection |
| `application/event/employment/EmploymentTerminatedEventHandler.java` | Revoke OPERATOR context by matching managingOrgId |

## Deviations

- `OccupancyAgreementTerminatedEventHandler` uses `Optional<TenantSubRoleAssignmentRepository>` constructor injection for graceful handling when Phase 2 bean is available — `ifPresent` pattern, no TODO comment needed.
- `EmploymentTerminatedEventHandler` only calls `userRepository.save(user)` when at least one context was revoked (optimization).
- `findAllByActiveRoleContext` JPQL passes `RoleContextStatus.ACTIVE` as a typed enum parameter (not string literal) to avoid type-mismatch at runtime.

---

## PHASE5_CONTEXT BLOCK

### Handler package paths
- `vn.truongngo.apartcom.one.service.admin.application.event.building.BuildingCreatedEventHandler`
- `vn.truongngo.apartcom.one.service.admin.application.event.organization.OrganizationCreatedEventHandler`
- `vn.truongngo.apartcom.one.service.admin.application.event.agreement.OccupancyAgreementActivatedEventHandler`
- `vn.truongngo.apartcom.one.service.admin.application.event.agreement.OccupancyAgreementTerminatedEventHandler`
- `vn.truongngo.apartcom.one.service.admin.application.event.employment.EmploymentTerminatedEventHandler`

### PartyClient interface
```java
package vn.truongngo.apartcom.one.service.admin.domain.party;
public interface PartyClient {
    List<String> getMembers(String partyId);
}
```

### UserRepository new methods
```java
Optional<User> findByPartyId(String partyId);
List<User> findAllByActiveRoleContext(Scope scope, String orgId);
```
