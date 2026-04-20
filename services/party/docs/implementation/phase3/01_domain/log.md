# Log: Party Service Phase 3 — Domain Layer (Employment)

**Date**: 2026-04-19  
**Status**: ✅ Compile pass — `mvn clean compile -DskipTests` BUILD SUCCESS

---

## Files tạo mới

| File                             | Package                   |
|----------------------------------|---------------------------|
| `EmploymentId.java`              | `domain.employment`       |
| `EmploymentType.java`            | `domain.employment`       |
| `EmploymentStatus.java`          | `domain.employment`       |
| `BQLPosition.java`               | `domain.employment`       |
| `PositionAssignment.java`        | `domain.employment`       |
| `Employment.java`                | `domain.employment`       |
| `EmploymentRepository.java`      | `domain.employment`       |
| `EmploymentErrorCode.java`       | `domain.employment`       |
| `EmploymentException.java`       | `domain.employment`       |
| `EmploymentCreatedEvent.java`    | `domain.employment.event` |
| `EmploymentTerminatedEvent.java` | `domain.employment.event` |
| `PositionAssignedEvent.java`     | `domain.employment.event` |

## Files cập nhật

- `plan.md` — tick [x] tất cả mục 3.1
- `SERVICE_MAP.md` — Domain Layer thêm Employment + Phase 3 events

## Deviation

- `PositionAssignment.close()` package-private (không phải public) — chỉ gọi từ `Employment.terminate()` trong cùng package, không expose ra ngoài aggregate
- `Employment.getPositions()` trả `Collections.unmodifiableList` — nhất quán với pattern của `Party.getIdentifications()`

---

## INFRASTRUCTURE CONTEXT BLOCK

```
### Phase 3 Domain — packages thực tế

**Base package**: `vn.truongngo.apartcom.one.service.party`

#### Package `domain.employment`

| Class | Ghi chú |
|-------|---------|
| `EmploymentId` | `extends AbstractId<String> implements Id<String>` — factory: `of(String)`, `generate()` |
| `EmploymentType` | enum `FULL_TIME`, `PART_TIME`, `CONTRACT` |
| `EmploymentStatus` | enum `ACTIVE`, `TERMINATED` |
| `BQLPosition` | enum `MANAGER`, `DEPUTY_MANAGER`, `FINANCE`, `TECHNICAL`, `SECURITY`, `RECEPTIONIST`, `STAFF` |
| `PositionAssignment` | Entity owned bởi Employment — không phải AR |
| `Employment` | AR — `extends AbstractAggregateRoot<EmploymentId> implements AggregateRoot<EmploymentId>` |
| `EmploymentRepository` | port — `extends Repository<Employment, EmploymentId>` |
| `EmploymentErrorCode` | enum implements `ErrorCode` |
| `EmploymentException` | extends `DomainException` |

#### `Employment` — exact field types

```java
private final PartyRelationshipId    relationshipId  // immutable
private final PartyId                employeeId      // immutable
private final PartyId                orgId           // immutable
private final EmploymentType         employmentType  // immutable
private       EmploymentStatus       status          // MUTABLE
private final List<PositionAssignment> positions     // MUTABLE — append only (unmodifiable via getter)
private final LocalDate              startDate       // immutable
private       LocalDate              endDate         // MUTABLE, nullable
```

Getters: `getId()`, `getRelationshipId()`, `getEmployeeId()`, `getOrgId()`, `getEmploymentType()`,
`getStatus()`, `getPositions()` (unmodifiable), `getStartDate()`, `getEndDate()`

Factory: `Employment.create(relationshipId, employeeId, orgId, employmentType, startDate)`
`Employment.reconstitute(id, relationshipId, employeeId, orgId, employmentType, status, positions, startDate, endDate)`

#### `PositionAssignment` — exact field types

```java
private final UUID        id;
private final BQLPosition position;
private final String      department; // nullable
private final LocalDate   startDate;
private       LocalDate   endDate;     // MUTABLE — set via package-private close(LocalDate)
```

Getters: `getId()`, `getPosition()`, `getDepartment()`, `getStartDate()`, `getEndDate()`

Factory: `PositionAssignment.create(position, department, startDate)`
`PositionAssignment.reconstitute(id, position, department, startDate, endDate)`

**Lưu ý mapper**: `close()` là package-private — mapper KHÔNG gọi trực tiếp. Khi load từ DB, dùng `reconstitute(...)` với `endDate` đã có.

#### `EmploymentRepository` — method signatures

```java
Optional<Employment> findById(EmploymentId id);                         // từ Repository base
void save(Employment employment);                                             // từ Repository base
void delete(EmploymentId id);                                                // từ Repository base
List<Employment> findByOrgId(PartyId orgId);
List<Employment> findByEmployeeId(PartyId employeeId);
boolean existsActiveByEmployeeIdAndOrgId(PartyId employeeId, PartyId orgId);
```

#### `EmploymentErrorCode` — full values

```
EMPLOYMENT_NOT_FOUND          ("20201", 404)
EMPLOYMENT_ALREADY_TERMINATED ("20202", 422)
EMPLOYMENT_NOT_ACTIVE         ("20203", 422)
INVALID_EMPLOYMENT_TARGET     ("20204", 422)
PERSON_ALREADY_EMPLOYED       ("20205", 409)
```

#### `EmploymentException` — static factories

```java
notFound();              // → EMPLOYMENT_NOT_FOUND
alreadyTerminated();     // → EMPLOYMENT_ALREADY_TERMINATED
notActive();             // → EMPLOYMENT_NOT_ACTIVE
invalidTarget();         // → INVALID_EMPLOYMENT_TARGET
personAlreadyEmployed(); // → PERSON_ALREADY_EMPLOYED
```

#### Domain Events — constructor signatures

```java
EmploymentCreatedEvent(EmploymentId employmentId, PartyId personId, PartyId orgId);
    // aggregateId = employmentId.getValue()
    // String fields: employmentId, personId, orgId

EmploymentTerminatedEvent(EmploymentId employmentId, PartyId personId, PartyId orgId);
    // String fields: employmentId, personId, orgId

PositionAssignedEvent(EmploymentId employmentId, BQLPosition position, String department, LocalDate startDate);
    // aggregateId = employmentId.getValue()
    // String: employmentId, position (BQLPosition.name()), department (nullable); LocalDate: startDate
```

#### Mapper notes — mutable vs immutable

Khi implement mapper cho persistence adapter:
- `toEntity()` — map tất cả fields + positions list (lần insert)
- `updateEntity(existing, domain)` — update: `status`, `endDate`, sync toàn bộ `positions` list
- `toDomain()` — dùng `Employment.reconstitute(...)` với positions được map từ `PositionAssignment.reconstitute(...)`
- `PositionAssignment.endDate` là MUTABLE — phải đọc qua `getEndDate()` khi sync sau terminate
```
