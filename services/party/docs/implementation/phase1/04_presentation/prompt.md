# Prompt: Party Service Phase 1 — Presentation Layer

**Vai trò**: Bạn là Senior Backend Engineer implement Presentation Layer cho `services/party`. Application layer đã xong. Nhiệm vụ này: PartyController (public) + InternalPartyController (internal).

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md, @docs/conventions/api-design.md, @docs/conventions/error-handling.md
2. Service overview: @services/party/CLAUDE.md
3. Use case index: @services/party/docs/use-cases/UC-000_index.md (UC-001 → UC-009)
4. Implementation plan: @docs/development/260416_01_design_party_model/plan.md (Phase 1 — 1.4 Presentation layer)

## Files tham khảo pattern (từ services/admin)

- Pattern controller: `services/admin/src/main/java/.../presentation/role/RoleController.java`
- Pattern request model: `services/admin/src/main/java/.../presentation/role/model/CreateRoleRequest.java`
- Pattern response: follow `ApiResponse` wrapper từ `libs/common`

Base package: `vn.truongngo.apartcom.one.service.party`

## Context từ 03_application (paste PRESENTATION CONTEXT BLOCK từ handoff vào đây)

[PRESENTATION CONTEXT BLOCK]

---

## Nhiệm vụ cụ thể

Package: `presentation/`

### 1. Request/Response models (`presentation/party/model/`)

**Requests:**
- `CreatePersonRequest` — fields map tới `CreatePerson.Command` (partyName, firstName, lastName, dob, gender, identifications)
- `CreateOrganizationRequest` — map tới `CreateOrganization.Command`
- `CreateHouseholdRequest` — map tới `CreateHousehold.Command`
- `AddPartyIdentificationRequest` — map tới `AddPartyIdentification.Command` (type, value, issuedDate)
- `IdentificationRequest` — inner record cho identification list trong create requests

**Views (Response):**
- `PartyResponse` — wrap `PartyView` từ application, dùng cho single-party endpoints
- `PartySummaryResponse` — wrap `PartySummaryView`, dùng cho search result
- `IdentificationResponse` — wrap identification data

Dùng Jackson 3.x (`tools.jackson.databind`). Field name dùng camelCase. Date fields dùng ISO 8601 string.

### 2. PartyController (`presentation/party/PartyController.java`)

Base path: `/api/v1/parties`

| Method | Path | Use case | Response |
|--------|------|----------|----------|
| `POST` | `/parties` | UC-001/002/003 dispatch theo `type` field trong request body | `201 { data: { id } }` |
| `GET` | `/parties/{id}` | UC-006 FindPartyById | `200 { data: PartyResponse }` |
| `GET` | `/parties` | UC-007 SearchParties — params: keyword, type, status, page, size | `200 { data: Page<PartySummaryResponse> }` |
| `POST` | `/parties/{id}/identifications` | UC-004 AddPartyIdentification | `201 { data: { id: UUID } }` |

**Lưu ý dispatch endpoint:**
`POST /parties` nhận body với field `type: PERSON | ORGANIZATION | HOUSEHOLD` để route sang đúng command:
```json
{ "type": "PERSON", "partyName": "...", "firstName": "...", ... }
```
Dùng `@JsonSubTypes` hoặc manual dispatch trong controller — follow pattern của admin service nếu có, hoặc dùng manual `switch(request.type())`.

### 3. InternalPartyController (`presentation/internal/InternalPartyController.java`)

Base path: `/internal/parties`

**Lưu ý bảo mật**: Internal endpoints không đi qua public gateway — không cần auth annotation như public endpoints. Chỉ accessible qua internal network. Xem `services/party/CLAUDE.md` Section 6.

| Method | Path | Mô tả | Response |
|--------|------|--------|----------|
| `GET` | `/internal/parties/{id}` | UC-008 — basic info: id, type, name, status | `200 { data: PartyBasicView }` |
| `GET` | `/internal/parties/{id}/members` | UC-009 — members của Household/Org; nếu Party là PERSON → 422 | `200 { data: List<PartyBasicView> }` |

`PartyBasicView` record: `{ id, type, name, status }` — không expose identification hay subtype details.

UC-009 flow: load Party → check type là HOUSEHOLD hoặc ORGANIZATION → query PartyRelationship (Phase 2). **Phase 1**: trả empty list với comment `// TODO Phase 2: load members via PartyRelationship`. Không throw lỗi.

**Không implement**: UC-005 RemoveIdentification (không có trong plan Phase 1), bất cứ thứ gì liên quan Phase 2/3.

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi xong và `mvn clean compile -DskipTests` pass, cập nhật:

1. **`docs/development/260416_01_design_party_model/plan.md`** — tick `[x]` tất cả items trong mục **1.4 Presentation layer**; đồng thời cập nhật bảng Status cuối file: Phase 1 → `[x] Completed`

2. **`services/party/SERVICE_MAP.md`** — điền đầy đủ tất cả sections: Presentation Layer (controllers + paths), Internal API Endpoints (method/path/status), Domain Events Published (status = emitted)

3. **`services/party/docs/use-cases/UC-000_index.md`** — cập nhật UC-008, UC-009 từ `Not started` thành `Implemented`

4. Cung cấp danh sách endpoints thực tế (method, path, request/response shape) để dùng trong integration test sau.
---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
