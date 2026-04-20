# Log: Party Service Phase 1 — Domain Layer

**Ngày**: 2026-04-19
**Kết quả**: `mvn clean compile -DskipTests` — BUILD SUCCESS

---

## Files đã tạo

### `domain/party/`
| File | Mô tả |
|------|-------|
| `PartyId.java` | Typed UUID, extends `AbstractId<String>` |
| `PartyType.java` | Enum: PERSON, ORGANIZATION, HOUSEHOLD |
| `PartyStatus.java` | Enum: ACTIVE, INACTIVE |
| `PartyIdentificationType.java` | Enum: CCCD, TAX_ID, PASSPORT, BUSINESS_REG |
| `PartyIdentification.java` | Owned class (không implement Entity interface — xem deviation) |
| `Party.java` | AR, mutable style, behaviors: addIdentification, removeIdentification, deactivate |
| `PartyRepository.java` | Port — extends `Repository<Party, PartyId>` + 2 extra methods |
| `PartyErrorCode.java` | Enum implements ErrorCode, codes 20001–20009 |
| `PartyException.java` | Extends DomainException, static factory methods |

### `domain/party/event/`
| File | Fields |
|------|--------|
| `PersonCreatedEvent.java` | `name: String` |
| `OrganizationCreatedEvent.java` | `name: String`, `orgType: OrgType` |
| `HouseholdCreatedEvent.java` | `headPersonId: String` (unwrapped từ PartyId) |

### `domain/person/`
| File | Mô tả |
|------|-------|
| `Gender.java` | Enum: MALE, FEMALE, OTHER |
| `Person.java` | AR, share PartyId, behavior: updateProfile |
| `PersonRepository.java` | Port — extends `Repository<Person, PartyId>` |

### `domain/organization/`
| File | Mô tả |
|------|-------|
| `OrgType.java` | Enum: BQL, TENANT, VENDOR, OTHER |
| `Organization.java` | AR, share PartyId, orgType immutable, behavior: updateInfo |
| `OrganizationRepository.java` | Port — extends `Repository<Organization, PartyId>` |

### `domain/household/`
| File | Mô tả |
|------|-------|
| `Household.java` | AR, share PartyId, behavior: changeHead (Phase 1: no member validation) |
| `HouseholdRepository.java` | Port — extends `Repository<Household, PartyId>` |

### `pom.xml`
- Thêm dependency `vn.truongngo.apartcom.one.lib:common:1.0.0`

---

## Deviations so với spec

| # | Spec | Thực tế | Lý do |
|---|------|---------|-------|
| 1 | `Party save(Party party)` returns Party | `void save(T)` (từ `Repository` base) | Base interface `Repository<T,ID>` định nghĩa `void save()`. Application layer giữ local reference sau khi gọi save. |
| 2 | `PartyIdentification implements Entity` | Không implement Entity | `Entity<T>` yêu cầu `T extends Id<?>` — id của PartyIdentification là `UUID` (raw), không phải typed Id. Follow pattern `SocialConnection` trong admin service. |
| 3 | Prompt không mention `search()` trên PartyRepository | Đã thêm `Page<Party> search(String, PartyType, PartyStatus, Pageable)` | SearchParties use case (UC-007) cần. Import `Page/Pageable` từ Spring Data — follow pattern của admin `RoleRepository`. |

---

## INFRASTRUCTURE CONTEXT BLOCK

### Package paths thực tế
```
vn.truongngo.apartcom.one.service.party.domain.party
  Party, PartyId, PartyType, PartyStatus, PartyIdentificationType
  PartyIdentification, PartyRepository, PartyErrorCode, PartyException

vn.truongngo.apartcom.one.service.party.domain.party.event
  PersonCreatedEvent, OrganizationCreatedEvent, HouseholdCreatedEvent

vn.truongngo.apartcom.one.service.party.domain.person
  Person, PersonRepository, Gender

vn.truongngo.apartcom.one.service.party.domain.organization
  Organization, OrganizationRepository, OrgType

vn.truongngo.apartcom.one.service.party.domain.household
  Household, HouseholdRepository
```

### PartyErrorCode — full enum values
```
PARTY_NOT_FOUND           ("20001", 404)
PARTY_ALREADY_INACTIVE    ("20002", 422)
IDENTIFICATION_ALREADY_EXISTS ("20003", 409)
IDENTIFICATION_NOT_FOUND  ("20004", 404)
INVALID_PARTY_STATUS      ("20005", 422)
PERSON_NOT_FOUND          ("20006", 404)
ORGANIZATION_NOT_FOUND    ("20007", 404)
HOUSEHOLD_NOT_FOUND       ("20008", 404)
HEAD_PERSON_NOT_FOUND     ("20009", 404)
```

### Lưu ý cho mapper (infrastructure)
- `Party.identifications` là `List<PartyIdentification>` — dùng `getIdentifications()` (trả unmodifiable list)
- `PartyIdentification.id` là `UUID` (không phải typed Id)
- `PartyIdentification.issuedDate` là `LocalDate` — nullable
- `Person.dob` là `LocalDate` — nullable
- `Person.gender` là `Gender` enum — nullable
- `Organization.taxId`, `registrationNo` — nullable
- `Party.createdAt`, `updatedAt` là `Instant` (không phải Long như `Auditable`)
- `HouseholdCreatedEvent.headPersonId` là `String` (unwrapped), không phải `PartyId`

### Repository.save() là void
Application layer giữ local reference — không cần return value từ save.

### PartyRepository.search() signature
```java
Page<Party> search(String keyword, PartyType type, PartyStatus status, Pageable pageable);
```
Các params `type` và `status` là nullable — filter chỉ apply khi không null.
