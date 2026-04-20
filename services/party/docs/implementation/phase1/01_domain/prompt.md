# Prompt: Party Service Phase 1 — Domain Layer

**Vai trò**: Bạn là Senior Backend Engineer implement Domain Layer cho `services/party`. Đây là nền tảng — code phải pure Java, không import Spring, vì 02_infrastructure và 03_application build tiếp trên đây.

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md
2. Service overview: @services/party/CLAUDE.md
3. Aggregate boundaries + schema: @docs/development/260416_01_design_party_model/01_party_service.md
4. Implementation plan: @docs/development/260416_01_design_party_model/plan.md (Phase 1 — 1.1 Domain layer)
5. Domain docs: @services/party/docs/domains/party.md, @services/party/docs/domains/person.md, @services/party/docs/domains/organization.md, @services/party/docs/domains/household.md

## Files tham khảo pattern (từ services/admin)

- Pattern AR + typed ID: `services/admin/src/main/java/.../domain/role/Role.java` và `RoleId.java`
- Pattern domain exception: `services/admin/src/main/java/.../domain/user/UserException.java`
- Pattern error code enum: `services/admin/src/main/java/.../domain/user/UserErrorCode.java`
- Pattern domain event: `services/admin/src/main/java/.../domain/user/event/UserCreatedEvent.java`

Base package: `vn.truongngo.apartcom.one.service.party`

---

## Nhiệm vụ cụ thể

Tất cả file nằm trong `services/party/src/main/java/vn/truongngo/apartcom/one/service/party/domain/`.

1. **Value Objects & Enums**
   - `party/PartyId.java` — typed UUID record (pattern: RoleId.java)
   - `party/PartyType.java` — enum: `PERSON`, `ORGANIZATION`, `HOUSEHOLD`
   - `party/PartyStatus.java` — enum: `ACTIVE`, `INACTIVE`
   - `party/PartyIdentificationType.java` — enum: `CCCD`, `TAX_ID`, `PASSPORT`, `BUSINESS_REG`
   - `organization/OrgType.java` — enum: `BQL`, `TENANT`, `VENDOR`, `OTHER`
   - `person/Gender.java` — enum: `MALE`, `FEMALE`, `OTHER`

2. **Entity: PartyIdentification** (`party/PartyIdentification.java`)
   - Fields: `id` (UUID), `partyId` (PartyId), `type` (PartyIdentificationType), `value` (String), `issuedDate` (LocalDate, nullable)
   - Factory: `create(PartyId partyId, PartyIdentificationType type, String value, LocalDate issuedDate)`
   - Reconstitute: `reconstitute(UUID id, PartyId partyId, ...)`

3. **Aggregate Root: Party** (`party/Party.java`)
   - Fields: `id` (PartyId), `type` (PartyType), `name` (String), `status` (PartyStatus), `identifications` (List\<PartyIdentification\>), `createdAt`, `updatedAt`
   - Factory: `create(PartyType type, String name)` → status=ACTIVE, id=generated UUID
   - Reconstitute: `reconstitute(PartyId id, PartyType type, String name, PartyStatus status, List<PartyIdentification> identifications, Instant createdAt, Instant updatedAt)`
   - Behaviors: `addIdentification(PartyIdentificationType type, String value, LocalDate issuedDate)` — check duplicate type+value trong list hiện tại (throw nếu trùng trong aggregate; uniqueness toàn hệ thống enforce ở infrastructure); `removeIdentification(UUID identificationId)` — throw nếu không tìm thấy; `deactivate()` — throw nếu đã INACTIVE
   - `updatedAt` tự cập nhật sau mỗi mutation

4. **Aggregate Root: Person** (`person/Person.java`)
   - Fields: `id` (PartyId — shared với Party), `firstName` (String), `lastName` (String), `dob` (LocalDate, nullable), `gender` (Gender, nullable)
   - Factory: `create(PartyId id, String firstName, String lastName, LocalDate dob, Gender gender)`
   - Reconstitute: `reconstitute(...)`
   - Behavior: `updateProfile(String firstName, String lastName, LocalDate dob, Gender gender)`

5. **Aggregate Root: Organization** (`organization/Organization.java`)
   - Fields: `id` (PartyId — shared), `orgType` (OrgType), `taxId` (String, nullable), `registrationNo` (String, nullable)
   - Factory: `create(PartyId id, OrgType orgType, String taxId, String registrationNo)`
   - Reconstitute: `reconstitute(...)`
   - Behavior: `updateInfo(String taxId, String registrationNo)` — orgType immutable

6. **Aggregate Root: Household** (`household/Household.java`)
   - Fields: `id` (PartyId — shared), `headPersonId` (PartyId)
   - Factory: `create(PartyId id, PartyId headPersonId)`
   - Reconstitute: `reconstitute(...)`
   - Behavior: `changeHead(PartyId newHeadPersonId)` — Phase 1: chỉ set field, không validate member (Phase 2 mới validate)

7. **Repository ports** (interfaces — domain không biết gì về JPA)
   - `party/PartyRepository.java`
     ```java
     Optional<Party> findById(PartyId id);
     boolean existsByIdentification(PartyIdentificationType type, String value);
     Party save(Party party);
     ```
   - `person/PersonRepository.java`
     ```java
     Optional<Person> findById(PartyId id);
     Person save(Person person);
     ```
   - `organization/OrganizationRepository.java`
     ```java
     Optional<Organization> findById(PartyId id);
     Organization save(Organization org);
     ```
   - `household/HouseholdRepository.java`
     ```java
     Optional<Household> findById(PartyId id);
     Household save(Household household);
     ```

8. **Domain Events** (`party/event/`)
   - `PersonCreatedEvent.java` — fields: `partyId` (PartyId), `name` (String)
   - `OrganizationCreatedEvent.java` — fields: `partyId` (PartyId), `name` (String), `orgType` (OrgType)
   - `HouseholdCreatedEvent.java` — fields: `partyId` (PartyId), `headPersonId` (PartyId)

9. **Exception & Error Codes** (`party/`)
   - `PartyErrorCode.java` — enum với các codes: `PARTY_NOT_FOUND` (404), `PARTY_ALREADY_INACTIVE` (422), `IDENTIFICATION_ALREADY_EXISTS` (409), `IDENTIFICATION_NOT_FOUND` (404), `INVALID_PARTY_STATUS` (422), `PERSON_NOT_FOUND` (404), `ORGANIZATION_NOT_FOUND` (404), `HOUSEHOLD_NOT_FOUND` (404), `HEAD_PERSON_NOT_FOUND` (404)
   - `PartyException.java` — extends `DomainException` từ `libs/common`

**Không implement**: Infrastructure, Application, Presentation, Spring annotations trong domain.

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`docs/development/260416_01_design_party_model/plan.md`** — tick `[x]` tất cả items trong mục **1.1 Domain layer**
- **`services/party/SERVICE_MAP.md`** — cập nhật section **Domain Layer**: điền package path thực tế của từng aggregate/port, xóa "chưa implement"

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi xong và `mvn clean compile -DskipTests` pass, cung cấp:

### INFRASTRUCTURE CONTEXT BLOCK
- Package paths thực tế của tất cả files đã tạo
- Bất kỳ deviation nào so với spec này (tên method khác, field thêm/bỏ)
- PartyErrorCode — full enum values để infrastructure dùng đúng
- Lưu ý quan trọng cho mapper (field nào nullable, field nào immutable)
---

## Output Log
  
Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
