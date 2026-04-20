# Prompt: Admin Service IAM — Phase 6.3: Operator Portal Presentation Layer

**Vai trò**: Bạn là Senior Backend Engineer implement controller cho Operator Portal trong `services/admin`.

> **Thứ tự implement**: Phase 6.2 (application layer) phải xong trước.

**Yêu cầu**: Phase 6.2 đã xong và compile pass.

---

## Tài liệu căn cứ

1. Convention: @docs/conventions/api-design.md, @docs/conventions/error-handling.md
2. Implementation plan: @docs/development/260416_01_design_party_model/admin_iam_plan.md (Phase 6.4)
3. Service overview: @services/admin/CLAUDE.md

## Files tham khảo pattern

- Pattern controller: `services/admin/src/main/java/.../presentation/tenant/TenantSubRoleController.java`

Base package: `vn.truongngo.apartcom.one.service.admin`

---

## Context từ Phase 6.2

### Handler classes (dùng đúng package path từ handoff của Phase 6.2)

```text
// Inject vào controller
LinkPartyId.Handler          // UC-041
AssignOperatorContext.Handler // UC-042
RevokeOperatorContext.Handler // UC-043
FindOperatorsByBuilding.Handler // UC-044
AssignRolesToOperatorContext.Handler // UC-045
```

### Request/Response types

```java
// UC-041
record LinkPartyIdRequest(String userId, String partyId) {}

// UC-042
record AssignOperatorContextRequest(String userId, List<String> roleIds) {}
// buildingId từ @PathVariable

// UC-045
record AssignRolesToOperatorRequest(List<String> roleIds) {}
// userId và buildingId từ @PathVariable
```

---

## Nhiệm vụ cụ thể

### `OperatorContextController.java`

Package: `presentation/operator/`
Base path: `/api/v1/operators`

| Method   | Path                            | Handler                                | Request Body / Params          | Response |
|----------|---------------------------------|----------------------------------------|--------------------------------|----------|
| `POST`   | `/link-party`                   | `LinkPartyId.Handler`                  | `LinkPartyIdRequest`           | `200`    |
| `POST`   | `/{buildingId}/assign`          | `AssignOperatorContext.Handler`        | `AssignOperatorContextRequest` | `200`    |
| `DELETE` | `/{buildingId}/revoke/{userId}` | `RevokeOperatorContext.Handler`        | —                              | `200`    |
| `GET`    | `/{buildingId}`                 | `FindOperatorsByBuilding.Handler`      | —                              | `200`    |
| `PUT`    | `/{buildingId}/roles/{userId}`  | `AssignRolesToOperatorContext.Handler` | `AssignRolesToOperatorRequest` | `200`    |

> Tất cả response wrap bằng `ApiResponse.of(...)`. Xem pattern tại `presentation/base/ApiResponse.java`.

---

## Cập nhật tài liệu (sau khi compile pass)

- `docs/development/260416_01_design_party_model/admin_iam_plan.md` — tick `[x]` Phase 6.4; cập nhật Phase 6 Status → `[x] Completed`
- `services/admin/docs/use-cases/UC-000_index.md` — cập nhật UC-041 → UC-045 thành `Implemented`

---

## Output Log

Xuất log ra `log.md` trong cùng thư mục này sau khi hoàn thành.
