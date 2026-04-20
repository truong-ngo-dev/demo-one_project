# Prompt: Party Service Phase 1 — Application Layer

**Vai trò**: Bạn là Senior Backend Engineer implement Application Layer cho `services/party`. Domain và Infrastructure đã xong. Nhiệm vụ này: 6 use case handlers (CreatePerson, CreateOrganization, CreateHousehold, AddPartyIdentification, FindPartyById, SearchParties).

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md
2. Service overview: @services/party/CLAUDE.md
3. Use case index: @services/party/docs/use-cases/UC-000_index.md (UC-001 → UC-007)
4. Implementation plan: @docs/development/260416_01_design_party_model/plan.md (Phase 1 — 1.3 Application layer)
5. Business rules: @docs/development/260416_01_design_party_model/01_party_service.md (Section 6)

## Files tham khảo pattern (từ services/admin)

- Pattern command handler: `services/admin/src/main/java/.../application/role/create/CreateRole.java`
- Pattern query handler: `services/admin/src/main/java/.../application/role/find_by_id/FindRoleById.java`
- Pattern search/pageable: `services/admin/src/main/java/.../application/user/search/SearchUsers.java`

Base package: `vn.truongngo.apartcom.one.service.party`

## Context từ 02_infrastructure (paste APPLICATION CONTEXT BLOCK từ handoff vào đây)

[APPLICATION CONTEXT BLOCK]

---

## Nhiệm vụ cụ thể

Package gốc: `application/`

### 1. UC-001 — CreatePerson (`party/create_person/`)

```
Command: partyName, firstName, lastName, dob (nullable), gender (nullable),
         identifications (List<IdentificationInput> — nullable)

record IdentificationInput(PartyIdentificationType type, String value, LocalDate issuedDate)
```

**Flow (QUAN TRỌNG — phải atomic trong 1 transaction):**
1. Validate: nếu có identification, check `partyRepository.existsByIdentification(type, value)` cho từng item → throw `IDENTIFICATION_ALREADY_EXISTS` nếu trùng
2. `Party party = Party.create(PERSON, partyName)` → thêm identifications nếu có
3. `partyRepository.save(party)`
4. `Person person = Person.create(party.getId(), firstName, lastName, dob, gender)`
5. `personRepository.save(person)`
6. Publish `PersonCreatedEvent`
7. Return `partyId`

Bước 3 + 5 phải trong cùng `@Transactional`. Publish event SAU khi transaction commit (dùng `TransactionSynchronizationManager` hoặc `@TransactionalEventListener` — follow pattern của admin service).

### 2. UC-002 — CreateOrganization (`party/create_organization/`)

```
Command: partyName, orgType (OrgType), taxId (nullable), registrationNo (nullable),
         identifications (nullable)
```

**Flow** (tương tự CreatePerson nhưng tạo Organization):
1. Validate identifications
2. `Party.create(ORGANIZATION, partyName)` → add identifications
3. `partyRepository.save(party)`
4. `Organization.create(party.getId(), orgType, taxId, registrationNo)`
5. `organizationRepository.save(org)`
6. Publish `OrganizationCreatedEvent`

### 3. UC-003 — CreateHousehold (`party/create_household/`)

```
Command: partyName, headPersonId (PartyId)
```

**Flow:**
1. Validate: `personRepository.findById(headPersonId)` → throw `HEAD_PERSON_NOT_FOUND` nếu không tồn tại
2. `Party.create(HOUSEHOLD, partyName)`
3. `partyRepository.save(party)`
4. `Household.create(party.getId(), headPersonId)`
5. `householdRepository.save(household)`
6. Publish `HouseholdCreatedEvent`

### 4. UC-004 — AddPartyIdentification (`party/add_identification/`)

```
Command: partyId (PartyId), type (PartyIdentificationType), value (String), issuedDate (nullable)
```

**Flow:**
1. `partyRepository.findById(partyId)` → throw `PARTY_NOT_FOUND`
2. `partyRepository.existsByIdentification(type, value)` → throw `IDENTIFICATION_ALREADY_EXISTS`
3. `party.addIdentification(type, value, issuedDate)`
4. `partyRepository.save(party)`

### 5. UC-006 — FindPartyById (`party/find_by_id/`)

```
Query: partyId (PartyId)
Result: PartyView (id, type, name, status, identifications, subtypeData, createdAt, updatedAt)
```

**Flow:**
1. `partyRepository.findById(partyId)` → throw `PARTY_NOT_FOUND`
2. Load subtype data theo `party.getType()`:
   - PERSON → `personRepository.findById(partyId)` → map PersonData
   - ORGANIZATION → `organizationRepository.findById(partyId)` → map OrgData
   - HOUSEHOLD → `householdRepository.findById(partyId)` → map HouseholdData
3. Return `PartyView` với subtype data embedded

`PartyView` là inner record của `FindPartyById`, không phải domain object.

### 6. UC-007 — SearchParties (`party/search/`)

```
Query: keyword (String, nullable), type (PartyType, nullable), status (PartyStatus, nullable),
       page (int), size (int)
Result: Page<PartySummaryView> (id, type, name, status, createdAt)
```

**Flow:** Delegate sang repository với filter + pageable. Không load subtype data.

**Không implement**: UC-005 (RemoveIdentification), UC-008/UC-009 (Internal API — sang Presentation layer).

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`docs/development/260416_01_design_party_model/plan.md`** — tick `[x]` tất cả items trong mục **1.3 Application layer**
- **`services/party/SERVICE_MAP.md`** — cập nhật section **Application Layer**: liệt kê 6 use case slices đã tạo với package path thực tế
- **`services/party/docs/use-cases/UC-000_index.md`** — cập nhật cột Trạng thái của UC-001 → UC-007 từ `Not started` thành `Implemented`

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi xong và `mvn clean compile -DskipTests` pass, cung cấp:

### PRESENTATION CONTEXT BLOCK
- Package paths thực tế của tất cả handlers
- `PartyView` và `PartySummaryView` — full record fields (để controller map request/response đúng)
- Command record fields thực tế của từng use case
- Event publish mechanism đang dùng (để controller không re-publish)
- Error codes được throw từ mỗi handler

### FRONTEND CONTEXT BLOCK
*(Party service không có FE trong Phase 1 — bỏ qua)*
---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
