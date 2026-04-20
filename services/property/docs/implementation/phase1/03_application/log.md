# Log: Property Service Phase 1 — Application Layer

## Status: ✅ Completed — `mvn clean compile -DskipTests` PASS

---

## Files tạo mới

| File                                       | Package                                      |
|--------------------------------------------|----------------------------------------------|
| `AssetView.java`                           | `application.fixed_asset`                    |
| `create_building/CreateBuilding.java`      | `application.fixed_asset.create_building`    |
| `create_floor/CreateFloor.java`            | `application.fixed_asset.create_floor`       |
| `create_unit/CreateUnit.java`              | `application.fixed_asset.create_unit`        |
| `create_other_asset/CreateOtherAsset.java` | `application.fixed_asset.create_other_asset` |
| `find_tree/FindAssetTree.java`             | `application.fixed_asset.find_tree`          |
| `find_by_id/FindAssetById.java`            | `application.fixed_asset.find_by_id`         |

## Files sửa

| File                                 | Thay đổi                                                              |
|--------------------------------------|-----------------------------------------------------------------------|
| `domain/fixed_asset/FixedAsset.java` | `create()` nhận explicit `FixedAssetId id` thay vì tự generate nội bộ |

---

## Deviations

1. **`FixedAsset.create()` signature thay đổi** — nhận `FixedAssetId id` làm tham số đầu tiên. Lý do: application layer cần ID trước để tính `path = parentPath + "/" + id.getValue()` — chicken-and-egg nếu domain tự generate.

2. **Package `create_other_asset/`** — đặt tên là `create_other_asset` (không phải `create_other`) để khớp với convention file name.

---

## PRESENTATION CONTEXT BLOCK

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
