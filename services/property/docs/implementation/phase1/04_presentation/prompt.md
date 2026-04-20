# Prompt: Property Service Phase 1 — Presentation Layer

**Vai trò**: Bạn là Senior Backend Engineer implement Presentation Layer Phase 1 cho `services/property`. Application layer (FixedAsset) đã xong. Nhiệm vụ: `FixedAssetController`.

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md, @docs/conventions/api-design.md, @docs/conventions/error-handling.md
2. Service overview: @services/property/CLAUDE.md
3. Use case index: @services/property/docs/use-cases/UC-000_index.md (UC-001 → UC-006)
4. Implementation plan: @docs/development/260416_01_design_party_model/property_service_plan.md (Phase 1 — 1.4 Presentation layer)

## Files tham khảo pattern

- Pattern controller: `services/party/src/main/java/.../presentation/party/PartyController.java`
- Pattern request model: `services/party/src/main/java/.../presentation/party/model/`
- Pattern response: follow `ApiResponse` wrapper từ `libs/common`

Base package: `vn.truongngo.apartcom.one.service.property`

## Context từ 03_application

### Package paths

```
vn.truongngo.apartcom.one.service.property.application.fixed_asset.AssetView
vn.truongngo.apartcom.one.service.property.application.fixed_asset.create_building.CreateBuilding
vn.truongngo.apartcom.one.service.property.application.fixed_asset.create_floor.CreateFloor
vn.truongngo.apartcom.one.service.property.application.fixed_asset.create_unit.CreateUnit
vn.truongngo.apartcom.one.service.property.application.fixed_asset.create_other_asset.CreateOtherAsset
vn.truongngo.apartcom.one.service.property.application.fixed_asset.find_tree.FindAssetTree
vn.truongngo.apartcom.one.service.property.application.fixed_asset.find_by_id.FindAssetById
```

### AssetView — full record fields

```java
record AssetView(
    String id,
    String parentId,       // nullable
    String path,
    FixedAssetType type,
    String name,
    String code,           // nullable
    int sequenceNo,
    FixedAssetStatus status,
    String managingOrgId   // nullable, chỉ non-null khi BUILDING
) {}
```

Static factory: `AssetView.from(FixedAsset asset)`

### Command record fields

| Handler                    | Command fields                                                                                              |
|----------------------------|-------------------------------------------------------------------------------------------------------------|
| `CreateBuilding.Command`   | `name (String)`, `managingOrgId (String)`                                                                   |
| `CreateFloor.Command`      | `buildingId (String)`, `name (String)`, `code (String nullable)`, `sequenceNo (int)`                        |
| `CreateUnit.Command`       | `floorId (String)`, `name (String)`, `code (String nullable)`, `sequenceNo (int)`, `type (FixedAssetType)`  |
| `CreateOtherAsset.Command` | `parentId (String)`, `name (String)`, `code (String nullable)`, `sequenceNo (int)`, `type (FixedAssetType)` |
| `FindAssetTree.Query`      | `buildingId (String)`                                                                                       |
| `FindAssetById.Query`      | `assetId (String)`                                                                                          |

### Result types

| Handler            | Result                      |
|--------------------|-----------------------------|
| `CreateBuilding`   | `Result(String buildingId)` |
| `CreateFloor`      | `Result(String floorId)`    |
| `CreateUnit`       | `Result(String unitId)`     |
| `CreateOtherAsset` | `Result(String assetId)`    |
| `FindAssetTree`    | `List<AssetView>`           |
| `FindAssetById`    | `AssetView`                 |

### Error codes thrown per handler

| Handler            | Error codes                                        |
|--------------------|----------------------------------------------------|
| `CreateBuilding`   | `MANAGING_ORG_REQUIRED` (via domain)               |
| `CreateFloor`      | `ASSET_NOT_FOUND`, `INVALID_ASSET_TYPE_FOR_PARENT` |
| `CreateUnit`       | `ASSET_NOT_FOUND`, `INVALID_ASSET_TYPE_FOR_PARENT` |
| `CreateOtherAsset` | `ASSET_NOT_FOUND`                                  |
| `FindAssetById`    | `ASSET_NOT_FOUND`                                  |

### Dispatch pattern

`EventDispatcher.dispatch(event)` gọi trong cùng `@Transactional` sau khi `save()`. Không dùng `@TransactionalEventListener`.

### Type dispatch cho controller

`POST /api/v1/assets` dispatch sang handler theo `request.type()`:
- `BUILDING` → `CreateBuilding.Handler`
- `FLOOR` → `CreateFloor.Handler`
- `RESIDENTIAL_UNIT`, `COMMERCIAL_SPACE` → `CreateUnit.Handler`
- `FACILITY`, `MEETING_ROOM`, `PARKING_SLOT`, `COMMON_AREA`, `EQUIPMENT` → `CreateOtherAsset.Handler`

---

## Nhiệm vụ cụ thể

Package: `presentation/fixed_asset/`

### 1. Request models (`presentation/fixed_asset/model/`)

- `CreateAssetRequest.java` — single request cho tất cả UC-001 → UC-004, dispatch theo field `type`:
  ```java
  record CreateAssetRequest(
      FixedAssetType type,       // bắt buộc
      String parentId,           // nullable, null khi BUILDING
      String name,               // bắt buộc
      String code,               // nullable
      int sequenceNo,            // default 0
      String managingOrgId       // chỉ dùng khi type=BUILDING
  ) {}
  ```

### 2. FixedAssetController (`presentation/fixed_asset/FixedAssetController.java`)

Base path: `/api/v1/assets`

| Method | Path           | Use case                                  | Body / Params                  | Response                            |
|--------|----------------|-------------------------------------------|--------------------------------|-------------------------------------|
| `POST` | `/assets`      | UC-001/002/003/004 — dispatch theo `type` | `CreateAssetRequest`           | `201 { data: { id } }`              |
| `GET`  | `/assets/{id}` | UC-006 FindAssetById                      | —                              | `200 { data: AssetResponse }`       |
| `GET`  | `/assets`      | UC-005 FindAssetTree                      | param: `buildingId` (required) | `200 { data: List<AssetResponse> }` |

**Dispatch logic cho POST:**
```text
return switch (request.type()) {
    case BUILDING          -> createBuildingHandler.handle(...);
    case FLOOR             -> createFloorHandler.handle(...);
    case RESIDENTIAL_UNIT,
         COMMERCIAL_SPACE  -> createUnitHandler.handle(...);
    default                -> createOtherAssetHandler.handle(...);
};
```

### 3. Response model

`AssetResponse.java` — wrap `AssetView` từ application layer:
- fields: `id`, `parentId`, `path`, `type`, `name`, `code`, `sequenceNo`, `status`, `managingOrgId`
- Dùng Jackson 3.x (`tools.jackson.databind`). Field name camelCase.

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`docs/development/260416_01_design_party_model/property_service_plan.md`** — tick `[x]` Phase 1 — 1.4 Presentation layer; cập nhật Status: Phase 1 → `[x] Completed`
- **`services/property/SERVICE_MAP.md`** — điền đầy đủ sections: Presentation (controller + paths), Domain Events Published (status = emitted cho Phase 1)
- **`services/property/docs/use-cases/UC-000_index.md`** — UC-001 → UC-006 → `Implemented`

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
