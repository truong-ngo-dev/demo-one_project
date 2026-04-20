# Prompt: Property Service Phase 2 — Presentation Layer

**Vai trò**: Bạn là Senior Backend Engineer implement Presentation Layer Phase 2 cho `services/property`. Application layer (OccupancyAgreement) đã xong. Nhiệm vụ: `OccupancyAgreementController`.

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md, @docs/conventions/api-design.md, @docs/conventions/error-handling.md
2. Service overview: @services/property/CLAUDE.md
3. Use case index: @services/property/docs/use-cases/UC-000_index.md (UC-007 → UC-012)
4. Implementation plan: @docs/development/260416_01_design_party_model/property_service_plan.md (Phase 2 — 2.4 Presentation layer)

## Files tham khảo pattern

- Pattern controller: `services/party/src/main/java/.../presentation/party/PartyController.java`
- Pattern response: follow `ApiResponse` wrapper từ `libs/common`

Base package: `vn.truongngo.apartcom.one.service.property`

## Context từ 03_application Phase 2

### Handler package paths

```
application.agreement.create.CreateOccupancyAgreement.Handler
application.agreement.activate.ActivateAgreement.Handler
application.agreement.terminate.TerminateAgreement.Handler
application.agreement.expire.ExpireAgreement.Handler
application.agreement.find_by_asset.FindAgreementsByAsset.Handler
application.agreement.find_by_party.FindAgreementsByParty.Handler
```

### AgreementView — full record fields

```java
record AgreementView(
    String id,
    String partyId,
    PartyType partyType,
    String assetId,
    OccupancyAgreementType agreementType,
    OccupancyAgreementStatus status,
    LocalDate startDate,
    LocalDate endDate,       // nullable
    String contractRef       // nullable
) {}
```

Static factory: `AgreementView.from(OccupancyAgreement agreement)`

### Command record fields

| Handler                            | Command fields                                                                                                                                                                                      |
|------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CreateOccupancyAgreement.Command` | `partyId (String)`, `partyType (PartyType)`, `assetId (String)`, `agreementType (OccupancyAgreementType)`, `startDate (LocalDate)`, `endDate (LocalDate nullable)`, `contractRef (String nullable)` |
| `ActivateAgreement.Command`        | `agreementId (String)`                                                                                                                                                                              |
| `TerminateAgreement.Command`       | `agreementId (String)`                                                                                                                                                                              |
| `ExpireAgreement.Command`          | `agreementId (String)`                                                                                                                                                                              |
| `FindAgreementsByAsset.Query`      | `assetId (String)`, `status (OccupancyAgreementStatus nullable)`                                                                                                                                    |
| `FindAgreementsByParty.Query`      | `partyId (String)`                                                                                                                                                                                  |

### Result types

| Handler                    | Result                       |
|----------------------------|------------------------------|
| `CreateOccupancyAgreement` | `Result(String agreementId)` |
| `ActivateAgreement`        | `Result()`                   |
| `TerminateAgreement`       | `Result()`                   |
| `ExpireAgreement`          | `Result()`                   |
| `FindAgreementsByAsset`    | `List<AgreementView>`        |
| `FindAgreementsByParty`    | `List<AgreementView>`        |

### Error codes thrown per handler

| Handler                    | Error codes                                                                                 |
|----------------------------|---------------------------------------------------------------------------------------------|
| `CreateOccupancyAgreement` | `ASSET_NOT_FOUND`, `OWNERSHIP_ALREADY_EXISTS`, `LEASE_ALREADY_EXISTS`, + domain I4–I7 codes |
| `ActivateAgreement`        | `AGREEMENT_NOT_FOUND`, `AGREEMENT_INVALID_STATUS`                                           |
| `TerminateAgreement`       | `AGREEMENT_NOT_FOUND`, `AGREEMENT_INVALID_STATUS`                                           |
| `ExpireAgreement`          | `AGREEMENT_NOT_FOUND`, `AGREEMENT_INVALID_STATUS`                                           |
| `FindAgreementsByAsset`    | —                                                                                           |
| `FindAgreementsByParty`    | —                                                                                           |

---

## Deviations

None — implemented exactly per spec.

---

## Nhiệm vụ cụ thể

Package: `presentation/agreement/`

### 1. Request models (`presentation/agreement/model/`)

- `CreateAgreementRequest.java`:
  ```java
  record CreateAgreementRequest(
      String partyId,
      PartyType partyType,
      String assetId,
      OccupancyAgreementType agreementType,
      LocalDate startDate,
      LocalDate endDate,        // nullable
      String contractRef        // nullable
  )
  ```

### 2. OccupancyAgreementController (`presentation/agreement/OccupancyAgreementController.java`)

Base path: `/api/v1/agreements`

| Method | Path                         | Use case   | Body / Params                                        | Response                                |
|--------|------------------------------|------------|------------------------------------------------------|-----------------------------------------|
| `POST` | `/agreements`                | UC-007     | `CreateAgreementRequest`                             | `201 { data: { id } }`                  |
| `POST` | `/agreements/{id}/activate`  | UC-008     | —                                                    | `200 { data: null }`                    |
| `POST` | `/agreements/{id}/terminate` | UC-009     | —                                                    | `200 { data: null }`                    |
| `POST` | `/agreements/{id}/expire`    | UC-010     | —                                                    | `200 { data: null }`                    |
| `GET`  | `/agreements`                | UC-011/012 | params: `assetId` XOR `partyId`; `status` (optional) | `200 { data: List<AgreementResponse> }` |

**GET dispatch logic:**
```text
if (assetId != null) return findByAssetHandler.handle(assetId, status);
if (partyId != null) return findByPartyHandler.handle(partyId);
// nếu cả 2 null → 400 Bad Request
```

### 3. Response model

`AgreementResponse.java` — wrap `AgreementView` từ application:
- fields: `id`, `partyId`, `partyType`, `assetId`, `agreementType`, `status`, `startDate`, `endDate`, `contractRef`
- Dùng Jackson 3.x. Date fields ISO 8601.

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`docs/development/260416_01_design_party_model/property_service_plan.md`** — tick `[x]` Phase 2 — 2.4 Presentation layer; cập nhật Status: Phase 2 → `[x] Completed`
- **`services/property/SERVICE_MAP.md`** — điền đầy đủ tất cả sections:
  - Presentation Layer: thêm OccupancyAgreementController
  - Domain Events Published: cập nhật status của Phase 2 events → Emitted
- **`services/property/docs/use-cases/UC-000_index.md`** — UC-007 → UC-012 → `Implemented`

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
