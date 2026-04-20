# Log: Party Service Phase 3 — Presentation Layer (Employment)

**Date**: 2026-04-19  
**Status**: ✅ Compile pass — `mvn clean compile -DskipTests` BUILD SUCCESS

---

## Files tạo mới

| File                              | Package                         |
|-----------------------------------|---------------------------------|
| `CreateEmploymentRequest.java`    | `presentation.employment.model` |
| `TerminateEmploymentRequest.java` | `presentation.employment.model` |
| `AssignPositionRequest.java`      | `presentation.employment.model` |
| `EmploymentController.java`       | `presentation.employment`       |

## Files cập nhật

- `plan.md` — tick [x] mục 3.4; Phase 3 Status → `[x] Completed`
- `SERVICE_MAP.md` — Presentation Layer thêm `EmploymentController`; Domain Events Phase 3 → `✅ Emitted`

## Deviation

- `GET /employments` khi cả `orgId` và `personId` đều null → return `400 BadRequest` với `ErrorResponse` trực tiếp (không throw exception) — tránh bị `GlobalExceptionHandler.handleGeneral()` catch và trả 500
- Return type của `findEmployments()` là `ResponseEntity<?>` để accommodate cả `ApiResponse<List<EmploymentView>>` và `ErrorResponse` trong cùng method

---

## Endpoints thực tế

| Method | Path                                 | UC                            | Response                |
|--------|--------------------------------------|-------------------------------|-------------------------|
| `POST` | `/api/v1/employments`                | UC-013 CreateEmployment       | `201 { data: { id } }`  |
| `POST` | `/api/v1/employments/{id}/terminate` | UC-014 TerminateEmployment    | `200 { data: null }`    |
| `POST` | `/api/v1/employments/{id}/positions` | UC-015 AssignPosition         | `201 { data: { id } }`  |
| `GET`  | `/api/v1/employments?orgId=`         | UC-016 FindEmploymentsByOrg   | `200 { data: [...] }`   |
| `GET`  | `/api/v1/employments?personId=`      | UC-017 FindEmploymentByPerson | `200 { data: [...] }`   |
| `GET`  | `/api/v1/employments` (no params)    | —                             | `400 { code, message }` |
