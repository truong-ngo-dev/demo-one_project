# Log: Party Service Phase 1 — Application Layer

**Date**: 2026-04-19  
**Status**: ✅ Compile pass — `mvn clean compile -DskipTests` BUILD SUCCESS (45 source files)

---

## Files tạo mới

| File | Package |
|------|---------|
| `CreatePerson.java` | `application.party.create_person` |
| `CreateOrganization.java` | `application.party.create_organization` |
| `CreateHousehold.java` | `application.party.create_household` |
| `AddPartyIdentification.java` | `application.party.add_identification` |
| `FindPartyById.java` | `application.party.find_by_id` |
| `SearchParties.java` | `application.party.search` |
| `EventDispatcherConfig.java` | `infrastructure.cross_cutting.config` |

## Files cập nhật

- `docs/development/260416_01_design_party_model/plan.md` — tick [x] tất cả mục 1.3
- `services/party/SERVICE_MAP.md` — Application Layer section
- `services/party/docs/use-cases/UC-000_index.md` — UC-001→UC-004, UC-006, UC-007 → `Implemented`

## Deviation / Ghi chú

- `HouseholdCreatedEvent` constructor nhận `(PartyId partyId, PartyId headPersonId)` — không phải `(PartyId, String name)` như các event khác
- `OrganizationCreatedEvent` constructor nhận `(PartyId partyId, String name, OrgType orgType)` — 3 params
- `AddPartyIdentification.Handler` return type `Void` (null) — không có meaningful result
- `EventDispatcher.dispatch()` gọi synchronously trong `@Transactional` — không có after-commit hook (Phase 1 chưa có external broker)
- `IdentificationInput` được define lại trong từng create command (CreatePerson, CreateOrganization) — vertical slice pattern, không share

---

## PRESENTATION CONTEXT BLOCK

> Paste block này vào `[PRESENTATION CONTEXT BLOCK]` trong `04_presentation/prompt.md`

```
### Application Layer thực tế (từ 03_application)

**Base package**: `vn.truongngo.apartcom.one.service.party`

#### Use Case Handlers

| Handler | Package | CommandHandler / QueryHandler |
|---------|---------|-------------------------------|
| `CreatePerson.Handler` | `application.party.create_person` | `CommandHandler<CreatePerson.Command, CreatePerson.Result>` |
| `CreateOrganization.Handler` | `application.party.create_organization` | `CommandHandler<CreateOrganization.Command, CreateOrganization.Result>` |
| `CreateHousehold.Handler` | `application.party.create_household` | `CommandHandler<CreateHousehold.Command, CreateHousehold.Result>` |
| `AddPartyIdentification.Handler` | `application.party.add_identification` | `CommandHandler<AddPartyIdentification.Command, Void>` |
| `FindPartyById.Handler` | `application.party.find_by_id` | `QueryHandler<FindPartyById.Query, FindPartyById.PartyView>` |
| `SearchParties.Handler` | `application.party.search` | `QueryHandler<SearchParties.Query, Page<SearchParties.PartySummaryView>>` |

#### Command / Query Records

**CreatePerson.Command**:
```java
record Command(String partyName, String firstName, String lastName, LocalDate dob,
               Gender gender, List<IdentificationInput> identifications)
record IdentificationInput(PartyIdentificationType type, String value, LocalDate issuedDate)
record Result(String partyId)
```

**CreateOrganization.Command**:
```java
record Command(String partyName, OrgType orgType, String taxId, String registrationNo,
               List<IdentificationInput> identifications)
record IdentificationInput(PartyIdentificationType type, String value, LocalDate issuedDate)
record Result(String partyId)
```

**CreateHousehold.Command**:
```java
record Command(String partyName, String headPersonId)  // headPersonId = String UUID
record Result(String partyId)
```

**AddPartyIdentification.Command**:
```java
record Command(PartyId partyId, PartyIdentificationType type, String value, LocalDate issuedDate)
// returns Void (null)
```

**FindPartyById.Query + PartyView**:
```java
record Query(String partyId)
record PartyView(
    String id, PartyType type, String name, PartyStatus status,
    List<IdentificationDetail> identifications,
    Object subtypeData,          // PersonData | OrgData | HouseholdData
    Instant createdAt, Instant updatedAt
)
record IdentificationDetail(String id, PartyIdentificationType type, String value, LocalDate issuedDate)
record PersonData(String firstName, String lastName, LocalDate dob, Gender gender)
record OrgData(OrgType orgType, String taxId, String registrationNo)
record HouseholdData(String headPersonId)
```

**SearchParties.Query + PartySummaryView**:
```java
// Use static factory: SearchParties.Query.of(keyword, type, status, page, size)
record Query(String keyword, PartyType type, PartyStatus status, int page, int size)
record PartySummaryView(String id, PartyType type, String name, PartyStatus status, Instant createdAt)
// Returns Page<PartySummaryView>
```

#### Event Dispatch Mechanism

- `EventDispatcher` (từ `libs/common`) injected vào create handlers
- Gọi `eventDispatcher.dispatch(new XxxCreatedEvent(...))` synchronously trong `@Transactional`
- **Controller không cần dispatch event** — handlers đã tự dispatch
- Config bean: `infrastructure.cross_cutting.config.EventDispatcherConfig`

#### Error Codes thrown từ các handlers

| Handler | Errors thrown |
|---------|--------------|
| `CreatePerson` | `IDENTIFICATION_ALREADY_EXISTS` (409) |
| `CreateOrganization` | `IDENTIFICATION_ALREADY_EXISTS` (409) |
| `CreateHousehold` | `HEAD_PERSON_NOT_FOUND` (404) |
| `AddPartyIdentification` | `PARTY_NOT_FOUND` (404), `IDENTIFICATION_ALREADY_EXISTS` (409) |
| `FindPartyById` | `PARTY_NOT_FOUND` (404), `PERSON_NOT_FOUND` (404), `ORGANIZATION_NOT_FOUND` (404), `HOUSEHOLD_NOT_FOUND` (404) |
| `SearchParties` | — |

#### Internal API — chưa implement (sang Presentation layer)

UC-008: `GET /internal/parties/{id}` — trả về basic Party info (id, type, name, status)
UC-009: `GET /internal/parties/{id}/members` — trả về members của Household/Org (Phase 2 — chưa có data)

Gọi `FindPartyById.Handler` cho UC-008, map lại response đơn giản hơn.
UC-009 sẽ chỉ trả về empty hoặc placeholder ở Phase 1 vì PartyRelationship chưa có.
```
