# Phase 1 Log — Domain Extension + Schema Migration

## Status: COMPLETED
`mvn clean compile -DskipTests` — BUILD SUCCESS

---

## Files created

| File                                                    | Notes                                        |
|---------------------------------------------------------|----------------------------------------------|
| `src/main/resources/db/migration/V9__iam_extension.sql` | Alter roles, user_role_context, users tables |
| `domain/user/RoleContextStatus.java`                    | Enum: ACTIVE, REVOKED                        |
| `domain/user/OrgType.java`                              | Enum: PARTY, FIXED_ASSET                     |

## Files modified

| File                                                            | Changes                                                                                                                       |
|-----------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| `domain/user/RoleContext.java`                                  | Added `status`, `orgType` fields; updated `create()`/`reconstitute()` signatures; added `revoke()`                            |
| `domain/role/Role.java`                                         | Added `scope (Scope)` field; updated `register()`/`reconstitute()` signatures; updated `updateDescription()`                  |
| `domain/user/User.java`                                         | Added `partyId (String nullable)` field; updated private constructor + `reconstitute()`; updated `addRoleContext()` signature |
| `domain/user/UserErrorCode.java`                                | Added `PARTY_ID_REQUIRED` (10017, 422), `ROLE_CONTEXT_ALREADY_REVOKED` (10018, 422)                                           |
| `domain/user/UserFactory.java`                                  | Updated `register()` + `adminCreate()` calls to `addRoleContext(Scope.ADMIN, null, null, roleIds)`                            |
| `application/user/assign_roles/AssignRoles.java`                | Updated `addRoleContext(Scope.ADMIN, null, null, roleIds)`                                                                    |
| `application/role/create/CreateRole.java`                       | Added `scope` to `Command`; updated `Role.register()` call                                                                    |
| `infrastructure/persistence/user/UserRoleContextJpaEntity.java` | Added `status`, `orgType` fields with `@Enumerated(STRING)`                                                                   |
| `infrastructure/persistence/role/RoleJpaEntity.java`            | Added `scope` field with `@Enumerated(STRING)`; removed `unique=true` from `name`                                             |
| `infrastructure/persistence/user/UserJpaEntity.java`            | Added `partyId` field (column `party_id`, nullable)                                                                           |
| `infrastructure/persistence/user/UserMapper.java`               | Updated `toDomain`, `toEntity`, `updateFields`, `buildContextEntity` for new fields                                           |
| `infrastructure/persistence/role/RoleMapper.java`               | Updated `toDomain`/`toEntity` for `scope`                                                                                     |
| `presentation/role/model/CreateRoleRequest.java`                | Added `scope` field                                                                                                           |
| `presentation/role/RoleController.java`                         | Updated `CreateRole.Command` constructor call to include `scope`                                                              |

## Deviations

- Added `ROLE_CONTEXT_ALREADY_REVOKED` error code (not in prompt spec) — used in `RoleContext.revoke()` instead of generic `INVALID_STATUS` for precision.
- `RoleJpaEntity` `name` column: removed `unique = true` (was `@Column(name="name", nullable=false, unique=true)`) because V9 migration replaces it with composite unique key `(name, scope)`.
- `CreateRole.Command` + `CreateRoleRequest` updated with `scope` — required to propagate the new `Role.register(name, description, scope)` signature up through the stack.

---

## PHASE2_CONTEXT BLOCK

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
