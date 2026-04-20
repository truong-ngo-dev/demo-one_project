# SERVICE_MAP — party-service

> **First entry point cho AI agents.** Đọc file này trước khi dùng bất kỳ công cụ tìm kiếm nào.
> Chi tiết convention xem tại [`docs/conventions/ddd-structure.md`](../../docs/conventions/ddd-structure.md).
> Design decisions xem tại [`docs/development/260416_01_design_party_model/`](../../docs/development/260416_01_design_party_model/).

---

## 📂 1. Domain Layer (`domain/`)

| Aggregate / Concept | Package | Ghi chú |
|---------------------|---------|---------|
| `Party` | `domain.party` | AR + `PartyIdentification` (owned class), `PartyId`, enums, `PartyRepository` port, `PartyErrorCode`, `PartyException` |
| `Person` | `domain.person` | AR riêng share `PartyId`, `PersonRepository` port, `Gender` enum |
| `Organization` | `domain.organization` | AR riêng share `PartyId`, `OrganizationRepository` port, `OrgType` enum |
| `Household` | `domain.household` | AR riêng share `PartyId`, `HouseholdRepository` port |
| Domain Events | `domain.party.event` | `PersonCreatedEvent`, `OrganizationCreatedEvent`, `HouseholdCreatedEvent` |
| `PartyRelationship` | `domain.party_relationship` | AR, `PartyRelationshipId`, enums (Type/RoleType/Status), `PartyRelationshipRepository` port, `PartyRelationshipErrorCode`, `PartyRelationshipException` |
| Domain Events Phase 2 | `domain.party_relationship.event` | `MemberAddedEvent`, `MemberRemovedEvent` |
| `Employment` | `domain.employment` | AR + `PositionAssignment` (owned entity), `EmploymentId`, enums (`EmploymentType`, `EmploymentStatus`, `BQLPosition`), `EmploymentRepository` port, `EmploymentErrorCode`, `EmploymentException` |
| Domain Events Phase 3 | `domain.employment.event` | `EmploymentCreatedEvent`, `EmploymentTerminatedEvent`, `PositionAssignedEvent` |

---

## 🚀 2. Application Layer (`application/`)

### Party Use Cases (`application/party/`)

| Slice | Handler class | Type | Trả về |
|-------|--------------|------|--------|
| `create_person/` | `CreatePerson.Handler` | Command | `CreatePerson.Result(partyId)` |
| `create_organization/` | `CreateOrganization.Handler` | Command | `CreateOrganization.Result(partyId)` |
| `create_household/` | `CreateHousehold.Handler` | Command | `CreateHousehold.Result(partyId)` |
| `add_identification/` | `AddPartyIdentification.Handler` | Command | `Void` |
| `find_by_id/` | `FindPartyById.Handler` | Query | `FindPartyById.PartyView` |
| `search/` | `SearchParties.Handler` | Query | `Page<SearchParties.PartySummaryView>` |

**Event dispatch**: `EventDispatcher.dispatch(event)` gọi trực tiếp trong handler sau khi persist. Config bean: `infrastructure.cross_cutting.config.EventDispatcherConfig`.

### Employment Use Cases (`application/employment/`)

| Slice | Handler class | Type | Trả về |
|-------|--------------|------|--------|
| `create/` | `CreateEmployment.Handler` | Command | `CreateEmployment.Result(employmentId)` |
| `terminate/` | `TerminateEmployment.Handler` | Command | `Void` |
| `assign_position/` | `AssignPosition.Handler` | Command | `AssignPosition.Result(positionAssignmentId)` |
| `find_by_org/` | `FindEmploymentsByOrg.Handler` | Query | `List<EmploymentView>` |
| `find_by_person/` | `FindEmploymentByPerson.Handler` | Query | `List<EmploymentView>` |

**Shared view records** (package `application.employment`): `EmploymentView`, `PositionAssignmentView`

### PartyRelationship Use Cases (`application/party_relationship/`)

| Slice | Handler class | Type | Trả về |
|-------|--------------|------|--------|
| `add_member/` | `AddMember.Handler` | Command | `AddMember.Result(relationshipId)` |
| `remove_member/` | `RemoveMember.Handler` | Command | `Void` |
| `find_by_party/` | `FindRelationshipsByParty.Handler` | Query | `List<FindRelationshipsByParty.RelationshipView>` |

---

## 🔧 3. Infrastructure Layer (`infrastructure/`)

### Persistence (`infrastructure/persistence/party/`)

| Class | Ghi chú |
|-------|---------|
| `PartyJpaEntity` | JPA entity cho bảng `party`; owns `List<PartyIdentificationJpaEntity>` (cascade ALL, orphanRemoval, EAGER) |
| `PartyIdentificationJpaEntity` | JPA entity cho bảng `party_identification` |
| `PersonJpaEntity` | JPA entity cho bảng `person`; PK = `party_id` (FK → party) |
| `OrganizationJpaEntity` | JPA entity cho bảng `organization`; PK = `party_id` |
| `HouseholdJpaEntity` | JPA entity cho bảng `household`; PK = `party_id` |
| `PartyJpaRepository` | Spring Data JPA; derived query `existsByIdentificationsTypeAndIdentificationsValue`, JPQL `search()` |
| `PersonJpaRepository` | Spring Data JPA — `findById(partyId)` |
| `OrganizationJpaRepository` | Spring Data JPA |
| `HouseholdJpaRepository` | Spring Data JPA |
| `PartyMapper` | Static mapper: `toDomain`, `toEntity`, `updateEntity` (upsert pattern) |
| `PersonMapper` | Static mapper |
| `OrganizationMapper` | Static mapper |
| `HouseholdMapper` | Static mapper |

### Adapters (`infrastructure/adapter/repository/party/`)

| Class | Implements |
|-------|-----------|
| `PartyPersistenceAdapter` | `PartyRepository` — upsert: check exists → updateEntity or toEntity |
| `PersonPersistenceAdapter` | `PersonRepository` |
| `OrganizationPersistenceAdapter` | `OrganizationRepository` |
| `HouseholdPersistenceAdapter` | `HouseholdRepository` |

### Persistence Phase 2 (`infrastructure/persistence/party_relationship/`)

| Class | Ghi chú |
|-------|---------|
| `PartyRelationshipJpaEntity` | JPA entity cho bảng `party_relationship`; PK = `id` (no `@GeneratedValue`) |
| `PartyRelationshipJpaRepository` | Spring Data JPA; derived queries: `findByFromPartyId`, `findByToPartyId`, `existsByFromPartyIdAndToPartyIdAndTypeAndStatus` |
| `PartyRelationshipMapper` | Static: `toDomain`, `toEntity`, `updateEntity` (chỉ update `status` + `endDate`) |

### Adapters Phase 2 (`infrastructure/adapter/repository/party_relationship/`)

| Class | Implements |
|-------|-----------|
| `PartyRelationshipPersistenceAdapter` | `PartyRelationshipRepository` — upsert pattern |

### Persistence Phase 3 (`infrastructure/persistence/employment/`)

| Class | Ghi chú |
|-------|---------|
| `EmploymentJpaEntity` | JPA entity cho bảng `employment`; owns `List<PositionAssignmentJpaEntity>` (cascade ALL, orphanRemoval, EAGER, @JoinColumn) |
| `PositionAssignmentJpaEntity` | JPA entity cho bảng `position_assignment`; plain String `employmentId` (FK managed by parent @JoinColumn) |
| `EmploymentJpaRepository` | Spring Data JPA; derived queries: `findByOrgId`, `findByEmployeeId`, `existsByEmployeeIdAndOrgIdAndStatus` |
| `EmploymentMapper` | Static: `toDomain`, `toEntity`, `updateEntity` (update `status`, `endDate`, sync `positions` via clear+add) |

### Adapters Phase 3 (`infrastructure/adapter/repository/employment/`)

| Class | Implements |
|-------|-----------|
| `EmploymentPersistenceAdapter` | `EmploymentRepository` — upsert pattern |

### Migration (`resources/db/migration/`)

| File | Nội dung |
|------|---------|
| `V1__create_party_tables.sql` | Tables: `party`, `person`, `organization`, `household`, `party_identification`; FK constraints; unique index `(type, value)` trên `party_identification` |
| `V2__create_party_relationship_tables.sql` | Table: `party_relationship`; FK → `party(id)` cho cả `from_party_id` và `to_party_id` |
| `V3__create_employment_tables.sql` | Tables: `employment` (UNIQUE `relationship_id`), `position_assignment`; FK → `party_relationship`, `person`, `organization` |

---

## 🌐 4. Presentation Layer (`presentation/`)

### Base (`presentation/base/`)

`ApiResponse<T>`, `PagedApiResponse<T>`, `ErrorResponse`, `GlobalExceptionHandler`

### Public API (`presentation/party/`, `presentation/party_relationship/`)

| Controller | Base path | Endpoints |
|-----------|-----------|-----------|
| `PartyController` | `/api/v1/parties` | `POST /` (UC-001/002/003 dispatch by type), `GET /{id}` (UC-006), `GET /` (UC-007), `POST /{id}/identifications` (UC-004) |
| `PartyRelationshipController` | `/api/v1/party-relationships` | `POST /` (UC-010), `DELETE /{id}` (UC-011), `GET /` (UC-012 — params: `partyId`, `direction`) |
| `EmploymentController` | `/api/v1/employments` | `POST /` (UC-013), `POST /{id}/terminate` (UC-014), `POST /{id}/positions` (UC-015), `GET /` (UC-016/017 — params: `orgId` XOR `personId`, `status`) |

Request model: `CreatePartyRequest` (single record with all fields + `type` for dispatch), `AddPartyIdentificationRequest`, `IdentificationRequest` — trong `presentation/party/model/`

### Internal API (`presentation/internal/`)

| Controller | Base path | Endpoints |
|-----------|-----------|-----------|
| `InternalPartyController` | `/internal/parties` | `GET /{id}` (UC-008), `GET /{id}/members` (UC-009) |

---

## 📡 5. Internal API Endpoints

| Method | Path | Mục đích | Status |
|--------|------|----------|--------|
| `GET` | `/internal/parties/{id}` | Basic info: `{ id, type, name, status }` | ✅ Implemented |
| `GET` | `/internal/parties/{id}/members` | Members của Household/Org (Phase 1: empty list) | ✅ Implemented (stub)

---

## 📤 6. Domain Events Published

| Event | Trigger | Consumer | Status |
|-------|---------|----------|--------|
| `PersonCreatedEvent` | `CreatePerson.Handler` | — | ✅ Emitted |
| `OrganizationCreatedEvent` | `CreateOrganization.Handler` | admin-service (cache BQL org) | ✅ Emitted |
| `HouseholdCreatedEvent` | `CreateHousehold.Handler` | — | ✅ Emitted |
| `MemberAddedEvent` | `AddMember.Handler` | — | ✅ Emitted |
| `MemberRemovedEvent` | `RemoveMember.Handler` | — | ✅ Emitted |
| `EmploymentCreatedEvent` | `CreateEmployment.Handler` | admin-service | ✅ Emitted |
| `EmploymentTerminatedEvent` | `TerminateEmployment.Handler` | admin-service (revoke RoleContext) | ✅ Emitted |
| `PositionAssignedEvent` | `AssignPosition.Handler` | — | ✅ Emitted |
