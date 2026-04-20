# Implementation Plan — Party Service

## Thông tin service

```
Service name : party-service
Base package : vn.truongngo.apartcom.one.service.party
Stack        : Java 21, Spring Boot 4.x, Maven, MySQL
```

---

## Aggregate Boundaries

| Aggregate Root      | Entities bên trong    | Lý do                                              |
|---------------------|-----------------------|----------------------------------------------------|
| `Party`             | `PartyIdentification` | Identification lifecycle gắn với Party             |
| `Person`            | —                     | AR riêng, share ID với Party (composition pattern) |
| `Organization`      | —                     | AR riêng, share ID với Party (composition pattern) |
| `Household`         | —                     | AR riêng, share ID với Party (composition pattern) |
| `PartyRelationship` | —                     | Thin AR — chỉ track kết nối, không có entity con   |
| `Employment`        | `PositionAssignment`  | HR lifecycle riêng, tách khỏi PartyRelationship    |

**Composition + Shared ID:** `Party` được tạo trước, subtype AR (`Person`/`Organization`/`Household`) share cùng ID. Mỗi subtype có Repository và Command riêng. `CreatePerson` tạo cả `Party` lẫn `Person` atomic tại application layer (cùng transaction, cùng DB).

**Lý do tách Employment:** Terminate employment ≠ xóa relationship (audit trail). Employment có PositionAssignment history — lifecycle khác PartyRelationship.

---

## Phase 1 — Party Aggregate

### 1.1 Domain layer
- [x] `PartyId` (Value Object — typed UUID)
- [x] `PartyType` enum: `PERSON`, `ORGANIZATION`, `HOUSEHOLD`
- [x] `OrgType` enum: `BQL`, `TENANT`, `VENDOR`, `OTHER`
- [x] `PartyStatus` enum: `ACTIVE`, `INACTIVE`
- [x] `Party` (Aggregate Root)
- [x] `Person` (AR riêng, share PartyId)
- [x] `Organization` (AR riêng, share PartyId)
- [x] `Household` (AR riêng, share PartyId)
- [x] `PartyIdentification` (owned class trong Party aggregate)
- [x] `PartyIdentificationType` enum: `CCCD`, `TAX_ID`, `PASSPORT`, `BUSINESS_REG`
- [x] `PartyRepository` (interface — port)
- [x] `PersonRepository` (interface — port)
- [x] `OrganizationRepository` (interface — port)
- [x] `HouseholdRepository` (interface — port)
- [x] `PartyErrorCode`, `PartyException`
- [x] `PersonCreatedEvent`, `OrganizationCreatedEvent`, `HouseholdCreatedEvent`

### 1.2 Infrastructure layer
- [x] Migration `V1__create_party_tables.sql`
- [x] `PartyJpaEntity`, `PersonJpaEntity`, `OrganizationJpaEntity`, `HouseholdJpaEntity`
- [x] `PartyIdentificationJpaEntity`
- [x] `PartyJpaRepository`, `PersonJpaRepository`, `OrganizationJpaRepository`, `HouseholdJpaRepository`
- [x] `PartyMapper`, `PersonMapper`, `OrganizationMapper`, `HouseholdMapper`
- [x] `PartyPersistenceAdapter` implements `PartyRepository`
- [x] `PersonPersistenceAdapter` implements `PersonRepository`
- [x] `OrganizationPersistenceAdapter` implements `OrganizationRepository`
- [x] `HouseholdPersistenceAdapter` implements `HouseholdRepository`

### 1.3 Application layer
- [x] `party/create_person/CreatePerson` (Command)
- [x] `party/create_organization/CreateOrganization` (Command)
- [x] `party/create_household/CreateHousehold` (Command)
- [x] `party/add_identification/AddPartyIdentification` (Command)
- [x] `party/find_by_id/FindPartyById` (Query)
- [x] `party/search/SearchParties` (Query)

### 1.4 Presentation layer
- [x] `party/PartyController`
- [x] `internal/InternalPartyController`
  - `GET /internal/parties/{id}` — basic info
  - `GET /internal/parties/{id}/members` — members của Household/Org

---

## Phase 2 — PartyRelationship Aggregate

### 2.1 Domain layer
- [x] `PartyRelationshipId` (Value Object — typed UUID)
- [x] `PartyRelationshipType` enum: `MEMBER_OF`, `EMPLOYED_BY`
- [x] `PartyRoleType` enum: `MEMBER`, `HEAD`, `EMPLOYEE`, `EMPLOYER`
- [x] `PartyRelationshipStatus` enum: `ACTIVE`, `ENDED`
- [x] `PartyRelationship` (Aggregate Root)
- [x] `PartyRelationshipRepository` (interface — port)
- [x] `PartyRelationshipErrorCode`, `PartyRelationshipException`
- [x] `MemberAddedEvent`, `MemberRemovedEvent`

### 2.2 Infrastructure layer
- [x] Migration `V2__create_party_relationship_tables.sql`
- [x] `PartyRelationshipJpaEntity`
- [x] `PartyRelationshipJpaRepository`
- [x] `PartyRelationshipMapper`
- [x] `PartyRelationshipPersistenceAdapter` implements `PartyRelationshipRepository`

### 2.3 Application layer
- [x] `party_relationship/add_member/AddMember` (Command)
- [x] `party_relationship/remove_member/RemoveMember` (Command)
- [x] `party_relationship/find_by_party/FindRelationshipsByParty` (Query)

### 2.4 Presentation layer
- [x] `party_relationship/PartyRelationshipController`

---

## Phase 3 — Employment Aggregate

### 3.1 Domain layer
- [x] `EmploymentId` (Value Object — typed UUID)
- [x] `EmploymentType` enum: `FULL_TIME`, `PART_TIME`, `CONTRACT`
- [x] `EmploymentStatus` enum: `ACTIVE`, `TERMINATED`
- [x] `BQLPosition` enum: `MANAGER`, `DEPUTY_MANAGER`, `FINANCE`, `TECHNICAL`, `SECURITY`, `RECEPTIONIST`, `STAFF`
- [x] `Employment` (Aggregate Root)
- [x] `PositionAssignment` (Entity trong Employment aggregate)
- [x] `EmploymentRepository` (interface — port)
- [x] `EmploymentErrorCode`, `EmploymentException`
- [x] `EmploymentCreatedEvent`, `EmploymentTerminatedEvent`
- [x] `PositionAssignedEvent`

### 3.2 Infrastructure layer
- [x] Migration `V3__create_employment_tables.sql`
- [x] `EmploymentJpaEntity`, `PositionAssignmentJpaEntity`
- [x] `EmploymentJpaRepository`
- [x] `EmploymentMapper`
- [x] `EmploymentPersistenceAdapter` implements `EmploymentRepository`

### 3.3 Application layer
- [x] `employment/create/CreateEmployment` (Command)
- [x] `employment/terminate/TerminateEmployment` (Command)
- [x] `employment/assign_position/AssignPosition` (Command)
- [x] `employment/find_by_org/FindEmploymentsByOrg` (Query)
- [x] `employment/find_by_person/FindEmploymentByPerson` (Query)

### 3.4 Presentation layer
- [x] `employment/EmploymentController`

---

## Business Rules (enforce trong domain)

1. `Household.headPersonId` phải là member của Household đó
2. 1 Person chỉ có 1 active `Employment` với BQL Org tại 1 thời điểm
3. `Employment` chỉ tạo khi `PartyRelationship.type = EMPLOYED_BY` và `toParty.orgType = BQL` — validate tại application layer (không phải domain, vì cần load Party khác)
4. `PartyIdentification.value` unique theo `type`
5. `PositionAssignment` chỉ được tạo trên `Employment` có `status = ACTIVE`

---

## Status

| Phase                                 | Status            |
|---------------------------------------|-------------------|
| Phase 1 — Party Aggregate             | `[x] Completed`   |
| Phase 2 — PartyRelationship Aggregate | `[x] Completed`   |
| Phase 3 — Employment Aggregate        | `[x] Completed`   |
