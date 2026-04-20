# Log: Party Service Phase 3 — Application Layer (Employment)

**Date**: 2026-04-19  
**Status**: ✅ Compile pass — `mvn clean compile -DskipTests` BUILD SUCCESS

---

## Files tạo mới

| File | Package |
|------|---------|
| `EmploymentView.java` | `application.employment` |
| `PositionAssignmentView.java` | `application.employment` |
| `CreateEmployment.java` | `application.employment.create` |
| `TerminateEmployment.java` | `application.employment.terminate` |
| `AssignPosition.java` | `application.employment.assign_position` |
| `FindEmploymentsByOrg.java` | `application.employment.find_by_org` |
| `FindEmploymentByPerson.java` | `application.employment.find_by_person` |

## Files cập nhật

- `plan.md` — tick [x] tất cả mục 3.3
- `SERVICE_MAP.md` — Application Layer thêm 5 slices Phase 3 + shared views
- `UC-000_index.md` — UC-013 đến UC-017 → `Implemented`

## Deviation

- `EmploymentView` và `PositionAssignmentView` đặt ở package `application.employment` (không inline trong slice) — explicit exception so với convention "không share model giữa 2 use case", justified vì UC-016 và UC-017 trả về cùng view structure và sẽ không diverge
- `FindEmploymentsByOrg` và `FindEmploymentByPerson` mỗi handler có Mapper riêng (static inner class) — mapping logic trùng nhau nhưng giữ tách biệt theo convention; nếu cần refactor sau thì đưa vào `application/employment/service/`

---

## PRESENTATION CONTEXT BLOCK

```
### Phase 3 Application — packages thực tế

**Base package**: `vn.truongngo.apartcom.one.service.party`

#### Handlers

| Handler | Package | Type |
|---------|---------|------|
| `CreateEmployment.Handler` | `application.employment.create` | `CommandHandler<CreateEmployment.Command, CreateEmployment.Result>` |
| `TerminateEmployment.Handler` | `application.employment.terminate` | `CommandHandler<TerminateEmployment.Command, Void>` |
| `AssignPosition.Handler` | `application.employment.assign_position` | `CommandHandler<AssignPosition.Command, AssignPosition.Result>` |
| `FindEmploymentsByOrg.Handler` | `application.employment.find_by_org` | `QueryHandler<FindEmploymentsByOrg.Query, List<EmploymentView>>` |
| `FindEmploymentByPerson.Handler` | `application.employment.find_by_person` | `QueryHandler<FindEmploymentByPerson.Query, List<EmploymentView>>` |

#### Command / Query Records

```java
// CreateEmployment
record Command(String personId, String orgId, EmploymentType employmentType, LocalDate startDate)
record Result(String employmentId)

// TerminateEmployment
record Command(String employmentId, LocalDate endDate)
// returns Void (null)

// AssignPosition
record Command(String employmentId, BQLPosition position, String department, LocalDate startDate)
record Result(String positionAssignmentId)

// FindEmploymentsByOrg
record Query(String orgId, EmploymentStatus status)  // status nullable → trả tất cả

// FindEmploymentByPerson
record Query(String personId, EmploymentStatus status)  // status nullable → trả tất cả
```

#### Shared view records (package `application.employment`)

```java
record EmploymentView(
    String employmentId,
    String relationshipId,
    String employeeId,
    String orgId,
    EmploymentType employmentType,
    EmploymentStatus status,
    LocalDate startDate,
    LocalDate endDate,          // nullable
    List<PositionAssignmentView> positions
)

record PositionAssignmentView(
    String positionAssignmentId,
    String position,            // BQLPosition.name()
    String department,          // nullable
    LocalDate startDate,
    LocalDate endDate           // nullable
)
```

#### Error codes thrown từ mỗi handler

| Handler | Errors thrown |
|---------|--------------|
| `CreateEmployment` | `PARTY_NOT_FOUND` (404), `ORGANIZATION_NOT_FOUND` (404), `INVALID_EMPLOYMENT_TARGET` (422), `PERSON_ALREADY_EMPLOYED` (409) |
| `TerminateEmployment` | `EMPLOYMENT_NOT_FOUND` (404), `EMPLOYMENT_ALREADY_TERMINATED` (422) |
| `AssignPosition` | `EMPLOYMENT_NOT_FOUND` (404), `EMPLOYMENT_NOT_ACTIVE` (422) |
| `FindEmploymentsByOrg` | — |
| `FindEmploymentByPerson` | — |

#### Transaction scope UC-013

`CreateEmployment.Handler.handle()` có `@Transactional` bao gồm:
- `partyRelationshipRepository.save(rel)` — insert party_relationship
- `employmentRepository.save(emp)` — insert employment
→ Cả hai atomic, rollback nếu một trong hai fail
```
