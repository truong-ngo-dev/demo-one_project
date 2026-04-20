# Prompt: Party Service Phase 3 — Application Layer (Employment)

**Vai trò**: Bạn là Senior Backend Engineer implement Application Layer cho `Employment`. Domain và Infrastructure Phase 3 đã xong. Nhiệm vụ: 5 use case handlers (UC-013 đến UC-017).

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md
2. Service overview: @services/party/CLAUDE.md
3. Use cases: @services/party/docs/use-cases/UC-013_create_employment.md, UC-014, UC-015, UC-016, UC-017
4. Business rules: @docs/development/260416_01_design_party_model/01_party_service.md (Section 6)
5. Implementation plan: @docs/development/260416_01_design_party_model/plan.md (Phase 3 — 3.3)

## Files tham khảo pattern

- Command handler (tạo atomic): `application/party/create_person/CreatePerson.java`
- Command handler (load + mutate): `application/party_relationship/remove_member/RemoveMember.java`
- Query handler: `application/party_relationship/find_by_party/FindRelationshipsByParty.java`
- Event dispatch: `application/party_relationship/add_member/AddMember.java`

Base package: `vn.truongngo.apartcom.one.service.party`

## Context từ Phase 3 — 02_infrastructure

```
### Phase 3 Infrastructure — packages thực tế

**Base package**: `vn.truongngo.apartcom.one.service.party`

#### Adapter bean (inject qua interface)

| Bean class | Implements | Package |
|-----------|-----------|---------|
| `EmploymentPersistenceAdapter` | `EmploymentRepository` | `infrastructure.adapter.repository.employment` |

Inject tại application layer bằng constructor injection (`@RequiredArgsConstructor`) qua interface:
```java
private final EmploymentRepository employmentRepository;
```

#### `EmploymentJpaRepository` — method names chính xác

```java
List<EmploymentJpaEntity> findByOrgId(String orgId);
List<EmploymentJpaEntity> findByEmployeeId(String employeeId);
boolean existsByEmployeeIdAndOrgIdAndStatus(String employeeId, String orgId, EmploymentStatus status);
```

**Lưu ý**: Application layer KHÔNG gọi JpaRepository trực tiếp — gọi qua `EmploymentRepository` port.

#### Domain Repository — method signatures

```java
Optional<Employment> findById(EmploymentId id);
void save(Employment employment);
void delete(EmploymentId id);
List<Employment> findByOrgId(PartyId orgId);
List<Employment> findByEmployeeId(PartyId employeeId);
boolean existsActiveByEmployeeIdAndOrgId(PartyId employeeId, PartyId orgId);
```

#### Upsert pattern (positions sync)

- `save()` với employment đã tồn tại: adapter gọi `updateEntity(existing, domain)`
- `updateEntity` update: `status`, `endDate`, và sync toàn bộ `positions` list (clear + re-add)
- Sync positions bằng clear+add là an toàn vì positions chỉ append mới hoặc set endDate (không xóa)
```

## Context từ Phase 1 & 2 (dùng trực tiếp)

```
### Repositories cần inject

PartyRepository              — domain.party (findById)
OrganizationRepository       — domain.organization (findById)
PartyRelationshipRepository  — domain.party_relationship (save, existsActiveByFromAndTo)
EmploymentRepository         — domain.employment (Phase 3)

EventDispatcher              — libs/common (đã có bean trong EventDispatcherConfig)

### Phase 1 domain types

PartyId, PartyType (PERSON, ORGANIZATION, HOUSEHOLD), OrgType (BQL, TENANT, VENDOR, OTHER)
Party.getType() → PartyType
Organization.getOrgType() → OrgType
PartyException.notFound(), PartyException.organizationNotFound()

### Phase 2 domain types

PartyRelationshipId, PartyRelationshipType (EMPLOYED_BY), PartyRoleType (EMPLOYEE, EMPLOYER)
PartyRelationship.create(fromPartyId, toPartyId, type, fromRole, toRole, startDate)
```

---

## Nhiệm vụ cụ thể

Package gốc: `application/employment/`

### 1. UC-013 — CreateEmployment (`create/`)

```
Command: personId (String), orgId (String), employmentType (EmploymentType), startDate (LocalDate)
Result:  employmentId (String)
```

**Flow:**
1. Load `Party fromParty = partyRepository.findById(PartyId.of(personId))` → throw `PartyException.notFound()`
2. Validate `fromParty.getType() == PERSON` → throw `EmploymentException.invalidTarget()` (INVALID_FROM_PARTY_TYPE — reuse message)

   > Dùng `EmploymentException.invalidTarget()` chứ không phải `PartyRelationshipException` — Employment handler tự validate.

3. Load `Organization org = organizationRepository.findById(PartyId.of(orgId))` → throw `PartyException.organizationNotFound()`
4. Validate `org.getOrgType() == BQL` → throw `EmploymentException.invalidTarget()`
5. Check `employmentRepository.existsActiveByEmployeeIdAndOrgId(PartyId.of(personId), PartyId.of(orgId))` → throw `EmploymentException.personAlreadyEmployed()`
6. Tạo `PartyRelationship rel = PartyRelationship.create(PartyId.of(personId), PartyId.of(orgId), EMPLOYED_BY, EMPLOYEE, EMPLOYER, startDate)`
7. `partyRelationshipRepository.save(rel)`
8. Tạo `Employment emp = Employment.create(rel.getId(), PartyId.of(personId), PartyId.of(orgId), employmentType, startDate)`
9. `employmentRepository.save(emp)`
10. `eventDispatcher.dispatch(new EmploymentCreatedEvent(emp.getId(), PartyId.of(personId), PartyId.of(orgId)))`
11. Return `new Result(emp.getId().getValue())`

> Bước 6-9 phải trong `@Transactional` — PartyRelationship và Employment tạo atomic.

### 2. UC-014 — TerminateEmployment (`terminate/`)

```
Command: employmentId (String), endDate (LocalDate)
Result:  Void
```

**Flow:**
1. Load `Employment emp = employmentRepository.findById(EmploymentId.of(employmentId))` → throw `EmploymentException.notFound()`
2. `emp.terminate(endDate)` — domain method throw `alreadyTerminated()` nếu status == TERMINATED
3. `employmentRepository.save(emp)`
4. `eventDispatcher.dispatch(new EmploymentTerminatedEvent(emp.getId(), emp.getEmployeeId(), emp.getOrgId()))`

### 3. UC-015 — AssignPosition (`assign_position/`)

```
Command: employmentId (String), position (BQLPosition), department (String — nullable), startDate (LocalDate)
Result:  positionAssignmentId (String)
```

**Flow:**
1. Load `Employment emp = employmentRepository.findById(EmploymentId.of(employmentId))` → throw `EmploymentException.notFound()`
2. `PositionAssignment pos = emp.assignPosition(position, department, startDate)` — domain method throw `notActive()` nếu status != ACTIVE
3. `employmentRepository.save(emp)`
4. `eventDispatcher.dispatch(new PositionAssignedEvent(emp.getId(), position, department, startDate))`
5. Return `new Result(pos.getId().toString())`

### 4. UC-016 — FindEmploymentsByOrg (`find_by_org/`)

```
Query:  orgId (String), status (EmploymentStatus — nullable, null = trả tất cả)
Result: List<EmploymentView>
```

```java
public record EmploymentView(
    String employmentId,
    String relationshipId,
    String employeeId,
    String orgId,
    EmploymentType employmentType,
    EmploymentStatus status,
    LocalDate startDate,
    LocalDate endDate,               // nullable
    List<PositionAssignmentView> positions
)

public record PositionAssignmentView(
    String positionAssignmentId,
    String position,                 // BQLPosition.name()
    String department,               // nullable
    LocalDate startDate,
    LocalDate endDate                // nullable
)
```

**Flow:**
- `List<Employment> list = employmentRepository.findByOrgId(PartyId.of(orgId))`
- Nếu `status != null`: filter `list.stream().filter(e -> e.getStatus() == status)`
- Map mỗi `Employment` → `EmploymentView` (bao gồm positions)

### 5. UC-017 — FindEmploymentByPerson (`find_by_person/`)

```
Query:  personId (String), status (EmploymentStatus — nullable, null = trả tất cả)
Result: List<EmploymentView>
```

**Flow:**
- `List<Employment> list = employmentRepository.findByEmployeeId(PartyId.of(personId))`
- Nếu `status != null`: filter theo status
- Map → `EmploymentView` (tái dụng record từ UC-016)

> `EmploymentView` và `PositionAssignmentView` là shared records — đặt trong package `application/employment/` (không lặp lại định nghĩa ở mỗi slice). Tham chiếu từ cả hai handlers.

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`plan.md`** — tick `[x]` tất cả items trong **3.3 Application layer**
- **`SERVICE_MAP.md`** — cập nhật section **Application Layer**: thêm 5 slices Phase 3
- **`UC-000_index.md`** — cập nhật UC-013 đến UC-017 → `Implemented`

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` pass, cung cấp:

### PRESENTATION CONTEXT BLOCK
- Package paths thực tế của 5 handlers
- Command / Query record fields chính xác
- `EmploymentView` và `PositionAssignmentView` full records (package path)
- Error codes thrown từ mỗi handler
- Xác nhận transaction scope của UC-013 (PartyRelationship + Employment atomic)

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
