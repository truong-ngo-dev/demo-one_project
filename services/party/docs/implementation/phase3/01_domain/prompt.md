# Prompt: Party Service Phase 3 — Domain Layer (Employment)

**Vai trò**: Bạn là Senior Backend Engineer implement Domain Layer cho aggregate `Employment`. Phase 1 (Party, Person, Organization, Household) và Phase 2 (PartyRelationship) đã hoàn thành và compile pass. Nhiệm vụ này: tạo toàn bộ domain layer cho `employment/`.

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md
2. Service overview: @services/party/CLAUDE.md
3. Schema & business rules: @docs/development/260416_01_design_party_model/01_party_service.md (Section 3, 6)
4. Implementation plan: @docs/development/260416_01_design_party_model/plan.md (Phase 3 — 3.1 Domain layer)
5. Use cases: @services/party/docs/use-cases/UC-013_create_employment.md, UC-014, UC-015

## Files tham khảo pattern (Phase 1 và 2 đã implement)

- Typed ID: `domain/party/PartyId.java`
- AR pattern với Entity bên trong: `domain/party/Party.java` (`PartyIdentification` là entity bên trong)
- Error code + exception: `domain/party_relationship/PartyRelationshipErrorCode.java`, `PartyRelationshipException.java`
- Repository port: `domain/party_relationship/PartyRelationshipRepository.java`
- Domain event: `domain/party_relationship/event/MemberAddedEvent.java`

Base package: `vn.truongngo.apartcom.one.service.party`

## Context từ Phase 1 & 2 (dùng trực tiếp)

```
### Phase 1 & 2 Domain — packages sẵn có

PartyId                  — domain.party.PartyId (extends AbstractId<String>)
PartyType                — domain.party.PartyType (PERSON, ORGANIZATION, HOUSEHOLD)
OrgType                  — domain.organization.OrgType (BQL, TENANT, VENDOR, OTHER)
PartyRelationshipId      — domain.party_relationship.PartyRelationshipId (extends AbstractId<String>)

PartyRepository.findById(PartyId) → Optional<Party>
OrganizationRepository.findById(PartyId) → Optional<Organization>
PartyRelationshipRepository.save(PartyRelationship)

### libs/common base classes

AbstractAggregateRoot<ID>  — extend cho AR
AbstractId<String>          — extend cho typed ID
AbstractDomainEvent         — extend cho events
Repository<T, ID>           — extend cho repository port
Assert                      — precondition checks

### Error code convention
- Phase 1 dùng codes 20001–20009
- Phase 2 dùng codes 20101–20109
- Phase 3 dùng codes 20201–20209 (tránh clash)
```

---

## Nhiệm vụ cụ thể

Package gốc: `domain/employment/`

### 1. Value Object — `EmploymentId`

```java
public class EmploymentId extends AbstractId<String>
```
- Constructor private, factory: `EmploymentId.of(String)`, `EmploymentId.generate()`
- Generate dùng `UUID.randomUUID().toString()`

### 2. Enums

```java
enum EmploymentType   { FULL_TIME, PART_TIME, CONTRACT }
enum EmploymentStatus { ACTIVE, TERMINATED }
enum BQLPosition      { MANAGER, DEPUTY_MANAGER, FINANCE, TECHNICAL, SECURITY, RECEPTIONIST, STAFF }
```

### 3. Entity — `PositionAssignment`

Package: `domain/employment/PositionAssignment.java` (không phải AR — entity owned bởi Employment)

Fields:
```
UUID            id           (generated — không có typed ID)
BQLPosition     position
String          department   (nullable)
LocalDate       startDate
LocalDate       endDate      (nullable, set khi employment terminated)
```

Factory:
```java
static PositionAssignment create(BQLPosition position, String department, LocalDate startDate)
    // id = UUID.randomUUID(), endDate = null

static PositionAssignment reconstitute(UUID id, BQLPosition position, String department,
                                        LocalDate startDate, LocalDate endDate)
```

Behavior:
```java
void close(LocalDate endDate)
    // Set this.endDate = endDate (không có guard — gọi từ Employment.terminate())
```

Getter: `getId()`, `getPosition()`, `getDepartment()`, `getStartDate()`, `getEndDate()`

### 4. Aggregate Root — `Employment`

Fields:
```
EmploymentId           id
PartyRelationshipId    relationshipId     (FK — immutable sau create)
PartyId                employeeId         (immutable sau create)
PartyId                orgId              (immutable sau create)
EmploymentType         employmentType     (immutable sau create)
EmploymentStatus       status             (MUTABLE)
List<PositionAssignment> positions        (MUTABLE — append only)
LocalDate              startDate          (immutable)
LocalDate              endDate            (MUTABLE, nullable)
```

Factory methods:
```java
static Employment create(PartyRelationshipId relationshipId, PartyId employeeId,
                          PartyId orgId, EmploymentType employmentType, LocalDate startDate)
    // id = generate, status = ACTIVE, positions = empty list, endDate = null

static Employment reconstitute(EmploymentId id, PartyRelationshipId relationshipId,
                                 PartyId employeeId, PartyId orgId,
                                 EmploymentType employmentType, EmploymentStatus status,
                                 List<PositionAssignment> positions,
                                 LocalDate startDate, LocalDate endDate)
```

Behaviors:
```java
void terminate(LocalDate endDate)
    // Guard: status == TERMINATED → throw EmploymentException.alreadyTerminated()
    // Set status = TERMINATED, this.endDate = endDate
    // Gọi position.close(endDate) trên mỗi position có endDate == null

PositionAssignment assignPosition(BQLPosition position, String department, LocalDate startDate)
    // Guard: status != ACTIVE → throw EmploymentException.notActive()
    // Tạo PositionAssignment.create(...), add vào this.positions
    // Return PositionAssignment vừa tạo (để application layer lấy id)
```

### 5. Repository Port — `EmploymentRepository`

```java
public interface EmploymentRepository extends Repository<Employment, EmploymentId> {
    List<Employment> findByOrgId(PartyId orgId);
    List<Employment> findByEmployeeId(PartyId employeeId);
    boolean existsActiveByEmployeeIdAndOrgId(PartyId employeeId, PartyId orgId);
}
```

> `List<>` là `java.util.List`. Queries UC-016 và UC-017 filter theo status tại application layer — không cần thêm method có status param vào repository port.

### 6. Error Code — `EmploymentErrorCode`

```java
public enum EmploymentErrorCode implements ErrorCode {
    EMPLOYMENT_NOT_FOUND         ("20201", "Employment not found",               404),
    EMPLOYMENT_ALREADY_TERMINATED("20202", "Employment already terminated",      422),
    EMPLOYMENT_NOT_ACTIVE        ("20203", "Employment is not active",            422),
    INVALID_EMPLOYMENT_TARGET    ("20204", "Target org must be a BQL organization",422),
    PERSON_ALREADY_EMPLOYED      ("20205", "Person already has active employment with this org", 409),
}
```

### 7. Exception — `EmploymentException`

```java
public class EmploymentException extends DomainException {
    // static factories:
    notFound(), alreadyTerminated(), notActive(), invalidTarget(), personAlreadyEmployed()
}
```

### 8. Domain Events

Package: `domain/employment/event/`

**EmploymentCreatedEvent**:
```java
// extends AbstractDomainEvent
// aggregateId = employmentId.getValue()
// Fields: String employmentId, String personId, String orgId
constructor(EmploymentId employmentId, PartyId personId, PartyId orgId)
```

**EmploymentTerminatedEvent**:
```java
// Fields: String employmentId, String personId, String orgId
constructor(EmploymentId employmentId, PartyId personId, PartyId orgId)
```

**PositionAssignedEvent**:
```java
// aggregateId = employmentId.getValue()
// Fields: String employmentId, String position (BQLPosition.name()), String department (nullable), LocalDate startDate
constructor(EmploymentId employmentId, BQLPosition position, String department, LocalDate startDate)
```

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`docs/development/260416_01_design_party_model/plan.md`** — tick `[x]` tất cả items trong **3.1 Domain layer**
- **`services/party/SERVICE_MAP.md`** — cập nhật section **Domain Layer**: thêm row cho `Employment`

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` pass, cung cấp:

### INFRASTRUCTURE CONTEXT BLOCK
- Package paths thực tế của tất cả classes
- `Employment` field list với exact Java types
- `PositionAssignment` field list với exact Java types
- `EmploymentRepository` method signatures thực tế
- Error code values thực tế (code string + httpStatus)
- Event constructor signatures
- Lưu ý mapper: field nào mutable (update khi save), field nào immutable

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
