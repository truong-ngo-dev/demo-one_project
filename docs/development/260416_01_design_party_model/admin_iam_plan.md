# Implementation Plan — Admin Service IAM Extension

## Thông tin service

```
Service name : admin-service
Base package : vn.truongngo.apartcom.one.service.admin
Stack        : Java 21, Spring Boot 4.x, Maven, MySQL
Next migration: V9
```

---

## Bối cảnh

Admin service đã có sẵn: User, Role, RoleContext (child entity của User), ABAC engine (Policy/Rule/PolicySet/Resource/UIElement). Plan này implement các phần còn thiếu theo design `03_admin_iam.md`:

| Đã có                                             | Thiếu                                                    |
|---------------------------------------------------|----------------------------------------------------------|
| User AR (profile, password, social, lock/unlock)  | `party_id` field trên User                               |
| Role AR (name, description)                       | `scope` field trên Role; unique key (name, scope)        |
| RoleContext child entity (scope, orgId, roleIds)  | `status` (ACTIVE/REVOKED), `orgType` (PARTY/FIXED_ASSET) |
| AssignRoles, RemoveRole, AdminCreateUser handlers | TenantSubRoleAssignment                                  |
| —                                                 | BuildingReference + OrgReference (reference cache)       |
| —                                                 | Event consumers (5 handlers)                             |
| —                                                 | GetUserContexts endpoint                                 |

---

## Schema gap summary (so với design doc)

| Table                        | Gap                                                                               | Migration |
|------------------------------|-----------------------------------------------------------------------------------|-----------|
| `roles`                      | Thiếu `scope` column; unique key hiện là `(name)`, phải đổi thành `(name, scope)` | V9        |
| `user_role_context`          | Thiếu `status` ENUM(ACTIVE, REVOKED) và `org_type` ENUM(PARTY, FIXED_ASSET)       | V9        |
| `users`                      | Thiếu `party_id` VARCHAR(36) nullable                                             | V9        |
| `tenant_sub_role_assignment` | Table chưa tồn tại                                                                | V10       |
| `building_reference`         | Table chưa tồn tại                                                                | V11       |
| `org_reference`              | Table chưa tồn tại                                                                | V11       |

---

## Phase 1 — Domain Extension + Schema Migration

> Thay đổi additive trên code hiện có. Mọi use case cũ phải compile và chạy được sau phase này.

### 1.1 Migration V9 — Alter existing tables

- [x] `ALTER TABLE roles ADD COLUMN scope ENUM('ADMIN','OPERATOR','TENANT','RESIDENT') NOT NULL DEFAULT 'ADMIN'`
- [x] `ALTER TABLE roles DROP INDEX name; ALTER TABLE roles ADD UNIQUE KEY uq_role_name_scope (name, scope)`
- [x] `ALTER TABLE user_role_context ADD COLUMN status ENUM('ACTIVE','REVOKED') NOT NULL DEFAULT 'ACTIVE'`
- [x] `ALTER TABLE user_role_context ADD COLUMN org_type ENUM('PARTY','FIXED_ASSET')`
- [x] `ALTER TABLE users ADD COLUMN party_id VARCHAR(36)`

### 1.2 Domain changes

- [x] `RoleContextStatus` enum: `ACTIVE`, `REVOKED` — package `domain.user`
- [x] `OrgType` enum: `PARTY`, `FIXED_ASSET` — package `domain.user`
- [x] `RoleContext`: add `status (RoleContextStatus)` + `orgType (OrgType nullable)` fields; add `revoke()` behavior (ACTIVE → REVOKED); update `create()` + `reconstitute()` signatures
- [x] `Role`: add `scope (Scope)` field — immutable after creation; update `register()` + `reconstitute()` signatures
- [x] `User`: add `partyId (String nullable)` field; update `reconstitute()` signature; thêm `assignRoleContext(scope, orgId, orgType, roleIds)` overload mới có orgType
- [x] `UserErrorCode`: thêm `PARTY_ID_REQUIRED` (422) — khi assign RoleContext scope != ADMIN mà partyId null

### 1.3 Infrastructure changes

- [x] `UserRoleContextJpaEntity`: add `status`, `org_type` columns; update mapper
- [x] `RoleJpaEntity`: add `scope` column; update mapper
- [x] `UserJpaEntity`: add `party_id` column; update mapper
- [x] `UserMapper`: update `toDomain`, `toEntity`, `updateEntity` cho các field mới

---

## Phase 2 — TenantSubRoleAssignment

### 2.1 Migration V10

- [ ] Tạo bảng `tenant_sub_role_assignment`:
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

### 2.2 Domain layer

- [x] `TenantSubRole` enum: `TENANT_MANAGER`, `TENANT_FINANCE`, `TENANT_HR` — package `domain.tenant`
- [x] `TenantSubRoleAssignmentId` (typed UUID) — package `domain.tenant`
- [x] `TenantSubRoleAssignment` AR:
  - fields: `id`, `userId (String)`, `orgId (String)`, `subRole (TenantSubRole)`, `assignedBy (String)`, `assignedAt (Instant)`
  - factory: `create(userId, orgId, subRole, assignedBy)`
  - reconstitute
- [x] `TenantSubRoleAssignmentRepository` port:
  - `save(TenantSubRoleAssignment)`
  - `delete(TenantSubRoleAssignmentId)`
  - `findByOrgId(String orgId) → List`
  - `existsByUserIdAndOrgIdAndSubRole(userId, orgId, subRole) → boolean`
  - `deleteAllByOrgId(String orgId)`
- [x] `TenantSubRoleErrorCode`, `TenantSubRoleException`

### 2.3 Infrastructure layer

- [x] `TenantSubRoleAssignmentJpaEntity`
- [x] `TenantSubRoleAssignmentJpaRepository`
- [x] `TenantSubRoleAssignmentMapper`
- [x] `TenantSubRoleAssignmentPersistenceAdapter`

### 2.4 Application layer (`application/tenant/`)

- [x] `assign_sub_role/AssignSubRole` (Command: userId, orgId, subRole, assignedBy)
  - Validate: `assignedBy` phải là User có RoleContext `{scope=TENANT, orgId}` — B7
  - Validate: `userId` phải là User có active RoleContext `{scope=TENANT, orgId}` — B8
  - Validate không duplicate: `existsByUserIdAndOrgIdAndSubRole`
- [x] `revoke_sub_role/RevokeSubRole` (Command: userId, orgId, subRole)
- [x] `find_sub_roles_by_org/FindSubRolesByOrg` (Query: orgId) → `List<SubRoleView>`

### 2.5 Presentation layer

- [x] `presentation/tenant/TenantSubRoleController` — base path `/api/v1/tenants/{orgId}/sub-roles`
  - `POST /` — UC assign sub-role
  - `DELETE /{userId}/{subRole}` — UC revoke sub-role
  - `GET /` — UC find by org

---

## Phase 3 — Reference Cache

### 3.1 Migration V11

- [x] Tạo bảng `building_reference`:
  ```sql
  CREATE TABLE building_reference (
      building_id     VARCHAR(36) PRIMARY KEY,
      name            VARCHAR(255) NOT NULL,
      managing_org_id VARCHAR(36),
      cached_at       DATETIME NOT NULL
  );
  ```
- [x] Tạo bảng `org_reference`:
  ```sql
  CREATE TABLE org_reference (
      org_id    VARCHAR(36) PRIMARY KEY,
      name      VARCHAR(255) NOT NULL,
      org_type  VARCHAR(20) NOT NULL,
      cached_at DATETIME NOT NULL
  );
  ```

### 3.2 Domain layer

- [x] `BuildingReference` (data holder — không phải AR, không cần typed ID):
  - fields: `buildingId (String)`, `name (String)`, `managingOrgId (String nullable)`, `cachedAt (Instant)`
  - `BuildingReferenceRepository` port: `upsert(BuildingReference)`, `existsById(String)`, `findById(String)`
- [x] `OrgReference` (data holder):
  - fields: `orgId (String)`, `name (String)`, `orgType (String)`, `cachedAt (Instant)`
  - `OrgReferenceRepository` port: `upsert(OrgReference)`, `existsById(String)`, `findById(String)`

### 3.3 Infrastructure layer

- [x] `BuildingReferenceJpaEntity`, `BuildingReferenceJpaRepository`, `BuildingReferenceMapper`, `BuildingReferencePersistenceAdapter`
- [x] `OrgReferenceJpaEntity`, `OrgReferenceJpaRepository`, `OrgReferenceMapper`, `OrgReferencePersistenceAdapter`

---

## Phase 4 — Event Consumers

> Package: `application/event/` cho tất cả consumers.
> Pattern: `@EventHandler` từ `libs/common`.

### 4.1 BuildingCreatedEventHandler

- [x] Consume `BuildingCreatedEvent { buildingId, name, managingOrgId }` từ property-service
- [x] Flow: upsert `BuildingReference`

### 4.2 OrganizationCreatedEventHandler

- [x] Consume `OrganizationCreatedEvent { partyId, name, orgType }` từ party-service
- [x] Flow: `IF orgType == BQL` → upsert `OrgReference`

### 4.3 OccupancyAgreementActivatedEventHandler

- [x] Consume `OccupancyAgreementActivatedEvent { agreementId, partyId, partyType, assetId, agreementType }` từ property-service
- [x] Flow phức tạp — xem design doc Section 4.3:
  - `OWNERSHIP + PERSON` hoặc `LEASE + PERSON` → tìm User(`partyId`), tạo RoleContext `{RESIDENT, orgId=assetId, orgType=FIXED_ASSET}`
  - `LEASE + HOUSEHOLD` → query party-service `GET /internal/parties/{partyId}/members`, mỗi member: tạo RoleContext `{RESIDENT, orgId=assetId}`
  - `LEASE + ORGANIZATION` → query party-service members, member đầu tiên (TENANT_ADMIN): tạo RoleContext `{TENANT, orgId=partyId, orgType=PARTY}`
- [x] Phụ thuộc: `UserRepository`, `BuildingReferenceRepository`, HTTP client sang party-service (internal endpoint)
- [x] Xử lý gracefully nếu User chưa tồn tại (party chưa register) — log warning, không throw

### 4.4 OccupancyAgreementTerminatedEventHandler

- [x] Consume `OccupancyAgreementTerminatedEvent { agreementId, partyId, partyType, assetId, agreementType }` từ property-service
- [x] Flow:
  - `OWNERSHIP` hoặc `LEASE + PERSON/HOUSEHOLD` → revoke tất cả RoleContext `{scope=RESIDENT, orgId=assetId}`
  - `LEASE + ORGANIZATION` → revoke tất cả RoleContext `{scope=TENANT, orgId=partyId}`; xoá tất cả `TenantSubRoleAssignment WHERE org_id=partyId`

### 4.5 EmploymentTerminatedEventHandler

- [x] Consume `EmploymentTerminatedEvent { employmentId, personId, orgId }` từ party-service
- [x] Flow: tìm User(`party_id=personId`) → revoke RoleContext `{scope=OPERATOR}` liên quan đến building của orgId

---

## Phase 5 — Auth Context Query

### 5.1 Application layer

- [x] `auth/get_contexts/GetUserContexts` (Query: userId):
  - Load User → lấy `roleContexts` filter status=ACTIVE
  - Với mỗi context: resolve `displayName` từ `BuildingReference` (scope=OPERATOR/RESIDENT) hoặc `OrgReference` (scope=TENANT)
  - Return `List<ContextView>` — fields: `{ contextId, scope, orgId, orgType, displayName, roles[] }`

### 5.2 Presentation layer

- [x] `presentation/auth/AuthContextController` — base path `/api/v1/auth`
  - `GET /contexts?userId=` — GetUserContexts
  > Note: Token issuance khi switch context thuộc trách nhiệm của oauth2-service, không implement ở đây.

---

## Business Rules enforcement

| Rule | Phase | Enforce tại |
|------|-------|-------------|
| B1 — 1 active RoleContext per (scope, orgId) | Phase 1 | Đã có trong `User.addRoleContext()` |
| B2 — scope != ADMIN → partyId required | Phase 1 | Application layer khi assign RoleContext |
| B3 — scope = OPERATOR → orgId phải có trong `building_reference` | Phase 3+4 | Application layer: check `BuildingReferenceRepository.existsById` |
| B4/B5 — RESIDENT/TENANT chỉ tạo qua event | Phase 4 | Application layer: handler từ chối manual assign cho 2 scope này |
| B6 — Role chỉ attach vào context cùng scope | Phase 1 | Application layer `AssignRoles` — check `role.scope == context.scope` |
| B7 — TenantSubRole.assignedBy phải là TENANT_ADMIN của org | Phase 2 | Application layer `AssignSubRole` |
| B8 — TenantSubRole chỉ assign cho member có TENANT context | Phase 2 | Application layer `AssignSubRole` |

---

## Dependencies giữa các phase

```
Phase 1 (domain ext) → Phase 2, Phase 3, Phase 4, Phase 5 đều phụ thuộc
Phase 3 (ref cache)  → Phase 4 (event consumers dùng reference cache)
Phase 2 + Phase 4    → Phase 5 (contexts cần đủ data)
```

Thứ tự implement bắt buộc: **1 → 3 → 4 → 2 → 5**

---

## Phase 6 — Operator Portal

> Cho phép SUPER_ADMIN / BQL_MANAGER gán/thu OPERATOR context thủ công, và query danh sách operator của 1 tòa nhà.

### 6.1 Domain change

- [x] `User.linkPartyId(String partyId)` — set `partyId` nếu hiện tại null; throw `PARTY_ID_ALREADY_SET` nếu đã có giá trị khác

### 6.2 Schema

- [x] Không cần migration mới — `party_id` đã có từ V9

### 6.3 Application layer (`application/operator/`)

- [x] `link_party_id/LinkPartyId` (Command: userId, partyId)
  - Validate: user tồn tại
  - Gọi `user.linkPartyId(partyId)`
- [x] `assign_operator_context/AssignOperatorContext` (Command: userId, buildingId, roleIds)
  - B2: user phải có `partyId` (non-null)
  - B3: `buildingId` phải có trong `building_reference`
  - B6: tất cả roleIds phải có `scope = OPERATOR`
  - Gọi `user.addRoleContext(OPERATOR, buildingId, FIXED_ASSET, roleIds)`
- [x] `revoke_operator_context/RevokeOperatorContext` (Command: userId, buildingId)
  - Tìm RoleContext `{scope=OPERATOR, orgId=buildingId}`, gọi `ctx.revoke()`
- [x] `find_operators_by_building/FindOperatorsByBuilding` (Query: buildingId) → `List<OperatorView>`
  - `OperatorView(String userId, String partyId, List<String> roleIds, RoleContextStatus status)`
  - Dùng `userRepository.findAllByActiveRoleContext(OPERATOR, buildingId)`
- [x] `assign_roles_to_operator/AssignRolesToOperatorContext` (Command: userId, buildingId, roleIds)
  - Tìm RoleContext `{scope=OPERATOR, orgId=buildingId}`, cập nhật roleIds
  - B6: tất cả roleIds phải có `scope = OPERATOR`

### 6.4 Presentation layer (`presentation/operator/`)

- [x] `OperatorContextController` — base path `/api/v1/operators`
  - `POST /link-party` — LinkPartyId
  - `POST /{buildingId}/assign` — AssignOperatorContext
  - `DELETE /{buildingId}/revoke/{userId}` — RevokeOperatorContext
  - `GET /{buildingId}` — FindOperatorsByBuilding
  - `PUT /{buildingId}/roles/{userId}` — AssignRolesToOperatorContext

### 6.5 Property service dependency

- [x] UC-013: `FindAgreementsByBuilding` — query `occupancy_agreement WHERE asset_id` prefix-match trên materialized path của building → phục vụ Operator Portal hiển thị danh sách unit/agreement

---

## Status

| Phase | Status |
|-------|--------|
| Phase 1 — Domain Extension + Schema Migration | `[x] Completed` |
| Phase 2 — TenantSubRoleAssignment | `[x] Completed` |
| Phase 3 — Reference Cache | `[x] Completed` |
| Phase 4 — Event Consumers | `[x] Completed` |
| Phase 5 — Auth Context Query | `[x] Completed` |
| Phase 6 — Operator Portal | `[x] Completed` |
