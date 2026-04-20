# Log: Property Service Phase 1 — Presentation Layer

## Status: ✅ Completed | `mvn clean compile -DskipTests` PASS

---

## Files Created

| File                                                     | Package                                                                     |
|----------------------------------------------------------|-----------------------------------------------------------------------------|
| `presentation/base/ApiResponse.java`                     | `vn.truongngo.apartcom.one.service.property.presentation.base`              |
| `presentation/base/ErrorResponse.java`                   | `vn.truongngo.apartcom.one.service.property.presentation.base`              |
| `presentation/base/GlobalExceptionHandler.java`          | `vn.truongngo.apartcom.one.service.property.presentation.base`              |
| `presentation/fixed_asset/model/CreateAssetRequest.java` | `vn.truongngo.apartcom.one.service.property.presentation.fixed_asset.model` |
| `presentation/fixed_asset/FixedAssetController.java`     | `vn.truongngo.apartcom.one.service.property.presentation.fixed_asset`       |

---

## Controller Summary

**Base path**: `/api/v1/assets`

| Method      | Path                         | Use case           | Handler dispatched           |
|-------------|------------------------------|--------------------|------------------------------|
| `POST /`    | `/api/v1/assets`             | UC-001/002/003/004 | dispatch by `request.type()` |
| `GET /{id}` | `/api/v1/assets/{id}`        | UC-006             | `FindAssetById.Handler`      |
| `GET /`     | `/api/v1/assets?buildingId=` | UC-005             | `FindAssetTree.Handler`      |

**POST dispatch:**
- `BUILDING` → `CreateBuilding.Handler`
- `FLOOR` → `CreateFloor.Handler`
- `RESIDENTIAL_UNIT`, `COMMERCIAL_SPACE` → `CreateUnit.Handler`
- `default` (FACILITY, MEETING_ROOM, PARKING_SLOT, COMMON_AREA, EQUIPMENT) → `CreateOtherAsset.Handler`

**POST response**: `201 Created { data: { id } }` — inner `IdResponse(String id)` record defined in controller.

---

## GlobalExceptionHandler

| Exception                  | HTTP status              | Body                                                                        |
|----------------------------|--------------------------|-----------------------------------------------------------------------------|
| `DomainException`          | `errorCode.httpStatus()` | `ErrorResponse.of(errorCode.code(), errorCode.defaultMessage())`            |
| `IllegalArgumentException` | 400                      | `ErrorResponse.of("BAD_REQUEST", ex.getMessage())`                          |
| `Exception`                | 500                      | `ErrorResponse.of("INTERNAL_SERVER_ERROR", "An unexpected error occurred")` |

---

## Deviations

- Spec mentioned an `AssetResponse.java` wrapper record; the controller directly returns `AssetView` from the application layer — no separate response model needed since `AssetView` is already a flat record with all display fields. No mapping gap.
- `ApiResponse<T>` and `ErrorResponse` are service-local in `presentation.base` (not from `libs/common`) — consistent with the service's self-contained presentation layer.

---

## Docs Updated

- `property_service_plan.md` — Phase 1 — 1.4 ticked `[x]`; Phase 1 Status → `[x] Completed`
- `SERVICE_MAP.md` — Presentation section filled; Domain Events Phase 1 → ✅ Emitted
- `UC-000_index.md` — UC-001 → UC-006 → `Implemented`
