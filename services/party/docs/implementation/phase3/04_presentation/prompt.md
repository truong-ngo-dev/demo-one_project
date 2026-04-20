# Prompt: Party Service Phase 3 — Presentation Layer (Employment)

**Vai trò**: Bạn là Senior Backend Engineer implement Presentation Layer cho `Employment`. Application layer Phase 3 đã xong. Nhiệm vụ: `EmploymentController`.

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md, @docs/conventions/api-design.md, @docs/conventions/error-handling.md
2. Service overview: @services/party/CLAUDE.md
3. Use cases: @services/party/docs/use-cases/UC-013_create_employment.md, UC-014, UC-015, UC-016, UC-017
4. Implementation plan: @docs/development/260416_01_design_party_model/plan.md (Phase 3 — 3.4)

## Files tham khảo pattern (Phase 1 & 2)

- Controller: `presentation/party_relationship/PartyRelationshipController.java`
- Base classes: `presentation/base/ApiResponse.java`, `GlobalExceptionHandler.java`
- Request models: `presentation/party_relationship/model/AddMemberRequest.java`

Base package: `vn.truongngo.apartcom.one.service.party`

## Context từ Phase 3 — 03_application

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

**CreateEmployment.Command + Result**:
```java
record Command(String personId, String orgId, EmploymentType employmentType, LocalDate startDate)
record Result(String employmentId)
```

**TerminateEmployment.Command**:
```java
record Command(String employmentId, LocalDate endDate) {}
// returns Void (null)
```

**AssignPosition.Command + Result**:
```java
record Command(String employmentId, BQLPosition position, String department, LocalDate startDate) {}
record Result(String positionAssignmentId) {}
```

**FindEmploymentsByOrg.Query**:
```java
record Query(String orgId, EmploymentStatus status) {}  // status nullable → trả tất cả
```

**FindEmploymentByPerson.Query**:
```java
record Query(String personId, EmploymentStatus status) {}  // status nullable → trả tất cả
```

**Shared view records** (package `application.employment`):
```java
record EmploymentView(
    String employmentId,
    String relationshipId,
    String employeeId,
    String orgId,
    EmploymentType employmentType,
    EmploymentStatus status,
    LocalDate startDate,
    LocalDate endDate,
    List<PositionAssignmentView> positions
) {}

record PositionAssignmentView(
    String positionAssignmentId,
    String position,        // BQLPosition.name()
    String department,
    LocalDate startDate,
    LocalDate endDate
) {}
```

#### Error codes thrown từ mỗi handler

| Handler                  | Errors thrown                                                                                                               |
|--------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| `CreateEmployment`       | `PARTY_NOT_FOUND` (404), `ORGANIZATION_NOT_FOUND` (404), `INVALID_EMPLOYMENT_TARGET` (422), `PERSON_ALREADY_EMPLOYED` (409) |
| `TerminateEmployment`    | `EMPLOYMENT_NOT_FOUND` (404), `EMPLOYMENT_ALREADY_TERMINATED` (422)                                                         |
| `AssignPosition`         | `EMPLOYMENT_NOT_FOUND` (404), `EMPLOYMENT_NOT_ACTIVE` (422)                                                                 |
| `FindEmploymentsByOrg`   | —                                                                                                                           |
| `FindEmploymentByPerson` | —                                                                                                                           |

#### Event dispatch

Pattern không thay đổi so với Phase 1 & 2:
- `EventDispatcher` injected vào `CreateEmployment`, `TerminateEmployment`, `AssignPosition` handlers
- Controller không cần dispatch event

#### Domain types cần biết ở controller

```text
// Từ domain.employment
EmploymentType   — FULL_TIME, PART_TIME, CONTRACT
EmploymentStatus — ACTIVE, TERMINATED
BQLPosition      — MANAGER, DEPUTY_MANAGER, FINANCE, TECHNICAL, SECURITY, RECEPTIONIST, STAFF
```
```

---

## Nhiệm vụ cụ thể

Package: `presentation/employment/`

### 1. Request models (`presentation/employment/model/`)

**`CreateEmploymentRequest`**:
```java
record CreateEmploymentRequest(
    String personId,
    String orgId,
    EmploymentType employmentType,
    LocalDate startDate             // nullable — nếu null, dùng LocalDate.now()
)
```

**`TerminateEmploymentRequest`**:
```java
record TerminateEmploymentRequest(
    LocalDate endDate               // nullable — nếu null, dùng LocalDate.now()
) {}
```

**`AssignPositionRequest`**:
```java
record AssignPositionRequest(
    BQLPosition position,
    String department,              // nullable
    LocalDate startDate             // nullable — nếu null, dùng LocalDate.now()
) {}
```

### 2. Controller — `EmploymentController`

Base path: `/api/v1/employments`

| Method | Path                          | Use case                                                                                                    | Response               |
|--------|-------------------------------|-------------------------------------------------------------------------------------------------------------|------------------------|
| `POST` | `/employments`                | UC-013 CreateEmployment                                                                                     | `201 { data: { id } }` |
| `POST` | `/employments/{id}/terminate` | UC-014 TerminateEmployment                                                                                  | `200 { data: null }`   |
| `POST` | `/employments/{id}/positions` | UC-015 AssignPosition                                                                                       | `201 { data: { id } }` |
| `GET`  | `/employments`                | UC-016 FindEmploymentsByOrg hoặc UC-017 FindEmploymentByPerson — phân biệt bằng `orgId` vs `personId` param | `200 { data: [...] }`  |

#### POST /employments (UC-013)

```text
CreateEmployment.Command command = new CreateEmployment.Command(
    request.personId(),
    request.orgId(),
    request.employmentType(),
    request.startDate() != null ? request.startDate() : LocalDate.now()
);
CreateEmployment.Result result = createEmploymentHandler.handle(command);
return ResponseEntity.status(201).body(ApiResponse.of(new IdResponse(result.employmentId())));
```

#### POST /employments/{id}/terminate (UC-014)

```text
TerminateEmployment.Command command = new TerminateEmployment.Command(
    id,
    request.endDate() != null ? request.endDate() : LocalDate.now()
);
terminateEmploymentHandler.handle(command);
return ResponseEntity.ok(ApiResponse.of(null));
```

#### POST /employments/{id}/positions (UC-015)

```text
AssignPosition.Command command = new AssignPosition.Command(
    id,
    request.position(),
    request.department(),
    request.startDate() != null ? request.startDate() : LocalDate.now()
);
AssignPosition.Result result = assignPositionHandler.handle(command);
return ResponseEntity.status(201).body(ApiResponse.of(new IdResponse(result.positionAssignmentId())));
```

#### GET /employments (UC-016 + UC-017)

```text
// Phân biệt bằng query param: orgId XOR personId
// orgId != null → UC-016; personId != null → UC-017
// status param: optional, parse sang EmploymentStatus enum (null nếu không có)

if (orgId != null) {
    FindEmploymentsByOrg.Query query = new FindEmploymentsByOrg.Query(orgId, status);
    return ResponseEntity.ok(ApiResponse.of(findByOrgHandler.handle(query)));
} else if (personId != null) {
    FindEmploymentByPerson.Query query = new FindEmploymentByPerson.Query(personId, status);
    return ResponseEntity.ok(ApiResponse.of(findByPersonHandler.handle(query)));
} else {
    // cả hai null — trả 400 Bad Request
    throw new IllegalArgumentException("Either orgId or personId is required");
}
```

> `status` param: `@RequestParam(required = false) EmploymentStatus status` — Spring tự convert String → Enum.

#### `IdResponse` record

```java
// Reuse pattern từ PartyController.IdResponse nếu đã extract ra chỗ share, hoặc định nghĩa private record
private record IdResponse(String id) {}
```

### 3. Không cần tạo lại GlobalExceptionHandler hay base classes

`GlobalExceptionHandler` handle `DomainException` chung — `EmploymentException extends DomainException` được handle tự động.

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`plan.md`** — tick `[x]` tất cả items trong **3.4 Presentation layer**; cập nhật bảng Status: Phase 3 → `[x] Completed`
- **`SERVICE_MAP.md`** — cập nhật section **Presentation Layer**: thêm `EmploymentController`; cập nhật **Domain Events Published**: `EmploymentCreatedEvent`, `EmploymentTerminatedEvent`, `PositionAssignedEvent` → `✅ Emitted`
- **`UC-000_index.md`** — UC-013 đến UC-017 → `Implemented`

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, endpoints thực tế, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
