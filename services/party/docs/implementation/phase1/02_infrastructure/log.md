# Log: Party Service Phase 1 — Infrastructure Layer

**Date**: 2026-04-19  
**Status**: ✅ Compile pass — `mvn clean compile -DskipTests` BUILD SUCCESS (38 source files)

---

## Files tạo mới

### Migration

| File | Path |
|------|------|
| `V1__create_party_tables.sql` | `src/main/resources/db/migration/` |

Tables: `party`, `person`, `organization`, `household`, `party_identification`

### JPA Entities (`infrastructure/persistence/party/`)

| File | Ghi chú |
|------|---------|
| `PartyJpaEntity.java` | `@OneToMany(cascade=ALL, orphanRemoval=true, fetch=EAGER)` với `PartyIdentificationJpaEntity`; `@JoinColumn(name="party_id")` |
| `PartyIdentificationJpaEntity.java` | PK = `id` (VARCHAR 36); FK → party |
| `PersonJpaEntity.java` | PK = `party_id` (không có `@GeneratedValue`) |
| `OrganizationJpaEntity.java` | PK = `party_id` |
| `HouseholdJpaEntity.java` | PK = `party_id`; field `headPersonId` (VARCHAR 36) |

### JPA Repositories (`infrastructure/persistence/party/`)

| File | Ghi chú |
|------|---------|
| `PartyJpaRepository.java` | Derived query `existsByIdentificationsTypeAndIdentificationsValue`; JPQL `search()` với nullable params |
| `PersonJpaRepository.java` | `JpaRepository<PersonJpaEntity, String>` |
| `OrganizationJpaRepository.java` | `JpaRepository<OrganizationJpaEntity, String>` |
| `HouseholdJpaRepository.java` | `JpaRepository<HouseholdJpaEntity, String>` |

### Mappers (`infrastructure/persistence/party/`)

| File | Pattern |
|------|---------|
| `PartyMapper.java` | Static: `toDomain`, `toEntity`, `updateEntity` (handles identification list sync) |
| `PersonMapper.java` | Static: `toDomain`, `toEntity`, `updateEntity` |
| `OrganizationMapper.java` | Static: `toDomain`, `toEntity`, `updateEntity` |
| `HouseholdMapper.java` | Static: `toDomain`, `toEntity`, `updateEntity` |

### Adapters (`infrastructure/adapter/repository/party/`)

| File | Implements |
|------|-----------|
| `PartyPersistenceAdapter.java` | `PartyRepository` |
| `PersonPersistenceAdapter.java` | `PersonRepository` |
| `OrganizationPersistenceAdapter.java` | `OrganizationRepository` |
| `HouseholdPersistenceAdapter.java` | `HouseholdRepository` |

Tất cả dùng **upsert pattern**: `findById` → nếu tồn tại thì `updateEntity` rồi `save(existing)`, nếu không thì `save(toEntity(...))`.

## Files cập nhật

- `docs/development/260416_01_design_party_model/plan.md` — tick [x] tất cả mục 1.2
- `services/party/SERVICE_MAP.md` — Infrastructure Layer section

## Deviation / Ghi chú

- `PartyIdentification` không implements `Entity<T>` (plain class) — UUID id không phải typed Id
- `Instant` timestamps (không phải `Long`) → JPA column `DATETIME(6)`, không cần `@Temporal`
- `@OneToMany` không có `mappedBy` — dùng `@JoinColumn` trực tiếp (unidirectional)

---

## APPLICATION CONTEXT BLOCK

> Paste block này vào `[APPLICATION CONTEXT BLOCK]` trong `03_application/prompt.md`

```
### Infrastructure thực tế (từ 02_infrastructure)

**Base package**: `vn.truongngo.apartcom.one.service.party`

#### Repository ports (domain interfaces)

| Interface | Package | Phương thức đặc biệt |
|-----------|---------|----------------------|
| `PartyRepository` | `domain.party` | `existsByIdentification(type, value)`, `search(keyword, type, status, pageable)` → `Page<Party>` |
| `PersonRepository` | `domain.person` | chỉ có `findById`, `save`, `delete` |
| `OrganizationRepository` | `domain.organization` | chỉ có `findById`, `save`, `delete` |
| `HouseholdRepository` | `domain.household` | chỉ có `findById`, `save`, `delete` |

#### Adapter beans (Spring @Component, inject bằng constructor)

| Bean | Package |
|------|---------|
| `PartyPersistenceAdapter` | `infrastructure.adapter.repository.party` |
| `PersonPersistenceAdapter` | `infrastructure.adapter.repository.party` |
| `OrganizationPersistenceAdapter` | `infrastructure.adapter.repository.party` |
| `HouseholdPersistenceAdapter` | `infrastructure.adapter.repository.party` |

#### Domain AR signatures quan trọng

**Party** (`domain.party.Party`):
- `Party.create(PartyType type, String name)` → Party (id tự sinh, status=ACTIVE, createdAt=updatedAt=now)
- `Party.reconstitute(PartyId, PartyType, String name, PartyStatus, List<PartyIdentification>, Instant createdAt, Instant updatedAt)` → Party
- `party.addIdentification(PartyIdentificationType, String value, LocalDate issuedDate)` — throws `PartyException.identificationAlreadyExists()` nếu trùng
- `party.removeIdentification(UUID identificationId)` — throws `PartyException.identificationNotFound()`
- `party.deactivate()` — throws `PartyException.alreadyInactive()`
- `party.getId()` → `PartyId` | `party.getType()` → `PartyType` | `party.getName()` | `party.getStatus()` | `party.getIdentifications()` → unmodifiable list | `party.getCreatedAt()` | `party.getUpdatedAt()`

**PartyIdentification** (`domain.party.PartyIdentification`):
- Fields (via `@Getter`): `UUID id`, `PartyId partyId`, `PartyIdentificationType type`, `String value`, `LocalDate issuedDate`
- `PartyIdentification.create(PartyId, PartyIdentificationType, String value, LocalDate)` → tạo mới với random UUID

**Person** (`domain.person.Person`):
- `Person.create(PartyId id, String firstName, String lastName, LocalDate dob, Gender gender)` → Person
- `Person.reconstitute(PartyId, firstName, lastName, dob, gender)` → Person
- Fields: `firstName`, `lastName`, `dob`, `gender`

**Organization** (`domain.organization.Organization`):
- `Organization.create(PartyId id, OrgType orgType, String taxId, String registrationNo)` → Organization
- Fields: `orgType` (immutable), `taxId`, `registrationNo`

**Household** (`domain.household.Household`):
- `Household.create(PartyId id, PartyId headPersonId)` → Household
- Fields: `headPersonId`

#### Domain Events (`domain.party.event`)

| Event class | Constructor |
|-------------|-------------|
| `PersonCreatedEvent` | `(PartyId partyId, String name)` |
| `OrganizationCreatedEvent` | `(PartyId partyId, String name)` |
| `HouseholdCreatedEvent` | `(PartyId partyId, String name)` |

Tất cả extend `AbstractDomainEvent implements DomainEvent` từ `libs/common`.
Dispatch bằng `EventDispatcher` từ `libs/common` — inject vào handler và gọi `eventDispatcher.dispatch(event)`.
Kiểm tra pattern từ `services/admin` để biết cách dispatch sau transaction.

#### Error codes (`domain.party.PartyErrorCode`)

| Code | HTTP | Factory method trên `PartyException` |
|------|------|--------------------------------------|
| `PARTY_NOT_FOUND` (20001) | 404 | `PartyException.partyNotFound()` |
| `PARTY_ALREADY_INACTIVE` (20002) | 422 | `PartyException.alreadyInactive()` |
| `IDENTIFICATION_ALREADY_EXISTS` (20003) | 409 | `PartyException.identificationAlreadyExists()` |
| `IDENTIFICATION_NOT_FOUND` (20004) | 404 | `PartyException.identificationNotFound()` |
| `INVALID_PARTY_STATUS` (20005) | 422 | — |
| `PERSON_NOT_FOUND` (20006) | 404 | `PartyException.personNotFound()` |
| `ORGANIZATION_NOT_FOUND` (20007) | 404 | `PartyException.organizationNotFound()` |
| `HOUSEHOLD_NOT_FOUND` (20008) | 404 | `PartyException.householdNotFound()` |
| `HEAD_PERSON_NOT_FOUND` (20009) | 404 | `PartyException.headPersonNotFound()` |

#### Schema (MySQL)

```sql
party(id VARCHAR(36) PK, type ENUM, name, status ENUM DEFAULT 'ACTIVE', created_at DATETIME(6), updated_at DATETIME(6))
person(party_id VARCHAR(36) PK → party.id, first_name, last_name, dob DATE, gender ENUM nullable)
organization(party_id PK → party.id, org_type ENUM, tax_id nullable, registration_no nullable)
household(party_id PK → party.id, head_person_id → person.party_id)
party_identification(id VARCHAR(36) PK, party_id → party.id, type ENUM, value VARCHAR(100), issued_date DATE; UNIQUE(type,value))
```
```
