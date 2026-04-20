# Log: Property Service Phase 2 — Presentation Layer

## Status: ✅ Completed | `mvn clean compile -DskipTests` PASS

---

## Files Created

| File | Package |
|------|---------|
| `presentation/agreement/model/CreateAgreementRequest.java` | `vn.truongngo.apartcom.one.service.property.presentation.agreement.model` |
| `presentation/agreement/OccupancyAgreementController.java` | `vn.truongngo.apartcom.one.service.property.presentation.agreement` |

---

## Controller Summary

**Base path**: `/api/v1/agreements`

| Method | Path | Use case | Notes |
|--------|------|----------|-------|
| `POST /` | `/api/v1/agreements` | UC-007 | Body: `CreateAgreementRequest` → `201 { data: { id } }` |
| `POST /{id}/activate` | `/api/v1/agreements/{id}/activate` | UC-008 | `200 { data: null }` |
| `POST /{id}/terminate` | `/api/v1/agreements/{id}/terminate` | UC-009 | `200 { data: null }` |
| `POST /{id}/expire` | `/api/v1/agreements/{id}/expire` | UC-010 | `200 { data: null }` |
| `GET /` | `/api/v1/agreements?assetId=&status=` | UC-011 | status optional |
| `GET /` | `/api/v1/agreements?partyId=` | UC-012 | — |

**GET dispatch**: `assetId != null` → FindByAsset; `partyId != null` → FindByParty; both null → `IllegalArgumentException` (400).

---

## Deviations

- No separate `AgreementResponse.java` wrapper — controller returns `AgreementView` directly from application layer (same pattern as `FixedAssetController` returning `AssetView`). All required fields are already present in `AgreementView`.
- `POST /` reuses `FixedAssetController.IdResponse(String id)` inner record as the response body — avoids duplicating a trivial record; both controllers are in the same service.

---

## Docs Updated

- `property_service_plan.md` — Phase 2 — 2.4 ticked `[x]`; Phase 2 Status → `[x] Completed`
- `SERVICE_MAP.md` — Presentation section: added `OccupancyAgreementController`; Domain Events Phase 2 → ✅ Emitted
- `UC-000_index.md` — UC-007 → UC-012 already `Implemented` (set in previous step)
