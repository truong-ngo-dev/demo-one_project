# Log: Party Service Phase 1 — Presentation Layer

**Date**: 2026-04-19  
**Status**: ✅ Compile pass — `mvn clean compile -DskipTests` BUILD SUCCESS (54 source files)

---

## Files tạo mới

### Base (`presentation/base/`)

| File | Ghi chú |
|------|---------|
| `ApiResponse.java` | `record ApiResponse<T>(T data)` + static factory |
| `PagedApiResponse.java` | `record PagedApiResponse<T>(List<T> data, Meta meta)` + static factory từ `Page<T>` |
| `ErrorResponse.java` | `record ErrorResponse(ErrorBody error)` |
| `GlobalExceptionHandler.java` | `@RestControllerAdvice` — handles `DomainException`, `Exception` |

### Models (`presentation/party/model/`)

| File | Ghi chú |
|------|---------|
| `CreatePartyRequest.java` | Single record với tất cả fields cho cả 3 party types + `type` field để dispatch |
| `IdentificationRequest.java` | `record IdentificationRequest(PartyIdentificationType type, String value, LocalDate issuedDate)` |
| `AddPartyIdentificationRequest.java` | `record AddPartyIdentificationRequest(PartyIdentificationType type, String value, LocalDate issuedDate)` |

### Controllers

| File | Package | Base path |
|------|---------|-----------|
| `PartyController.java` | `presentation.party` | `/api/v1/parties` |
| `InternalPartyController.java` | `presentation.internal` | `/internal/parties` |

## File sửa đổi

| File | Thay đổi |
|------|----------|
| `PartyException.java` | Thêm `invalidPartyType()` factory method → `INVALID_PARTY_STATUS` (422) |
| `plan.md` | Tick [x] 1.4 Presentation layer; Phase 1 → `[x] Completed` |
| `SERVICE_MAP.md` | Điền đầy đủ Presentation Layer, Internal API, Domain Events |
| `UC-000_index.md` | UC-008, UC-009 → `Implemented` |

## Deviation / Ghi chú

- **Dispatch pattern**: `POST /api/v1/parties` dùng single `CreatePartyRequest` record + manual `switch(request.type())` trong controller — không dùng `@JsonSubTypes` (simpler, less magic)
- **Không có ABAC**: `PartyController` không có `@PreEnforce`/`@ResourceMapping` — party service không depend `libs/abac`, auth qua OAuth2 resource server ở framework level
- **Internal controller path**: `/internal/parties` (không có `/api/v1` prefix) — phù hợp với plan và CLAUDE.md
- **UC-009 Phase 1**: Trả empty list, throw 422 nếu `type == PERSON`. TODO Phase 2: load via PartyRelationship
- **`addIdentification` response**: Trả `{ data: { id: partyId } }` thay vì identificationId (identification id chỉ có trong domain object, không expose riêng trong Phase 1)

---

## Endpoint Inventory (dùng cho integration test)

### Public API — `/api/v1/parties`

| Method | Path | Request Body / Params | Response |
|--------|------|-----------------------|----------|
| `POST` | `/api/v1/parties` | `CreatePartyRequest` (JSON, field `type` required) | `201 { data: { id } }` |
| `GET` | `/api/v1/parties/{id}` | — | `200 { data: PartyView }` |
| `GET` | `/api/v1/parties` | `?keyword&type&status&page&size` | `200 { data: [...], meta: { page, size, total } }` |
| `POST` | `/api/v1/parties/{id}/identifications` | `AddPartyIdentificationRequest` | `201 { data: { id } }` |

**CreatePartyRequest — PERSON example:**
```json
{
  "type": "PERSON",
  "partyName": "Nguyễn Văn A",
  "firstName": "Văn A",
  "lastName": "Nguyễn",
  "dob": "1990-01-15",
  "gender": "MALE",
  "identifications": [
    { "type": "CCCD", "value": "012345678901", "issuedDate": "2020-01-01" }
  ]
}
```

**CreatePartyRequest — ORGANIZATION example:**
```json
{
  "type": "ORGANIZATION",
  "partyName": "BQL Chung Cư A",
  "orgType": "BQL",
  "taxId": "0123456789",
  "registrationNo": "REG-001"
}
```

**CreatePartyRequest — HOUSEHOLD example:**
```json
{
  "type": "HOUSEHOLD",
  "partyName": "Hộ gia đình Nguyễn",
  "headPersonId": "<existing-person-party-id>"
}
```

**PartyView (GET /parties/{id}):**
```json
{
  "data": {
    "id": "uuid",
    "type": "PERSON",
    "name": "Nguyễn Văn A",
    "status": "ACTIVE",
    "identifications": [
      { "id": "uuid", "type": "CCCD", "value": "012345678901", "issuedDate": "2020-01-01" }
    ],
    "subtypeData": { "firstName": "Văn A", "lastName": "Nguyễn", "dob": "1990-01-15", "gender": "MALE" },
    "createdAt": "2026-04-19T...",
    "updatedAt": "2026-04-19T..."
  }
}
```

### Internal API — `/internal/parties`

| Method | Path | Response |
|--------|------|----------|
| `GET` | `/internal/parties/{id}` | `200 { data: { id, type, name, status } }` |
| `GET` | `/internal/parties/{id}/members` | `200 { data: [] }` (Phase 1 stub; 422 nếu type=PERSON) |

### Error Response

```json
{ "error": { "code": "PARTY_NOT_FOUND", "message": "Party not found" } }
```
