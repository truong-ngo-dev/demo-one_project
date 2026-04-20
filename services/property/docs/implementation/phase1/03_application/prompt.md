# Prompt: Property Service Phase 1 — Application Layer

**Vai trò**: Bạn là Senior Backend Engineer implement Application Layer Phase 1 cho `services/property`. Domain và Infrastructure (FixedAsset) đã xong. Nhiệm vụ: 6 use case handlers (CreateBuilding, CreateFloor, CreateUnit, CreateOtherAsset, FindAssetTree, FindAssetById).

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md
2. Service overview: @services/property/CLAUDE.md
3. Use case index: @services/property/docs/use-cases/UC-000_index.md (UC-001 → UC-006)
4. Implementation plan: @docs/development/260416_01_design_party_model/property_service_plan.md (Phase 1 — 1.3 Application layer)
5. Domain doc: @services/property/docs/domains/fixed_asset.md

## Files tham khảo pattern

- Pattern command handler: `services/party/src/main/java/.../application/party/create_person/CreatePerson.java`
- Pattern query handler: `services/party/src/main/java/.../application/party/find_by_id/FindPartyById.java`

Base package: `vn.truongngo.apartcom.one.service.property`

## Context từ 02_infrastructure

### Package paths

```
vn.truongngo.apartcom.one.service.property.infrastructure.persistence.fixed_asset.FixedAssetJpaEntity
vn.truongngo.apartcom.one.service.property.infrastructure.persistence.fixed_asset.FixedAssetJpaRepository
vn.truongngo.apartcom.one.service.property.infrastructure.persistence.fixed_asset.FixedAssetMapper
vn.truongngo.apartcom.one.service.property.infrastructure.adapter.repository.fixed_asset.FixedAssetPersistenceAdapter
```

### JPA query methods trên FixedAssetJpaRepository

```java
// Kế thừa từ JpaRepository:
Optional<FixedAssetJpaEntity> findById(String id);
FixedAssetJpaEntity save(FixedAssetJpaEntity entity);
void deleteById(String id);

// Derived queries thêm:
List<FixedAssetJpaEntity> findAllByPathStartingWith(String pathPrefix);
List<FixedAssetJpaEntity> findAllByParentId(String parentId);
```

### FixedAssetPersistenceAdapter — method signatures

```java
Optional<FixedAsset> findById(FixedAssetId id);         // dùng id.getValue()
void save(FixedAsset asset);                             // upsert: check exists → updateEntity or toEntity
void delete(FixedAssetId id);                           // dùng id.getValue()
List<FixedAsset> findByPathPrefix(String pathPrefix);   // delegate → findAllByPathStartingWith
```

### FixedAssetMapper — updateEntity scope

`updateEntity` chỉ cập nhật các field mutable:
- `name`, `code`, `sequenceNo`, `status`, `updatedAt`

Fields **không** update (immutable): `id`, `parentId`, `path`, `type`, `managingOrgId`, `createdAt`

### Transaction note

`FixedAssetPersistenceAdapter` **không có** `@Transactional` — application layer quản lý transaction boundary.
---

## Nhiệm vụ cụ thể

Package gốc: `application/fixed_asset/`

### Path Computation (dùng ở mọi use case tạo asset)

```
BUILDING  : path = "/" + newId
FLOOR     : path = parent.getPath() + "/" + newId
Other     : path = parent.getPath() + "/" + newId
```

Application layer load parent trước để lấy path. Domain không tính path.

---

### UC-001 — CreateBuilding (`create_building/`)

```
Command: name (String), managingOrgId (String)
Result:  buildingId (String)
```

**Flow:**
1. `FixedAsset.create(BUILDING, name, null, 0, null, "/" + newId, managingOrgId)`
   - Domain validate I8 (managingOrgId bắt buộc)
2. `fixedAssetRepository.save(asset)`
3. Publish `BuildingCreatedEvent { buildingId, name, managingOrgId }`
4. Return `buildingId`

---

### UC-002 — CreateFloor (`create_floor/`)

```
Command: buildingId (String), name (String), code (String nullable), sequenceNo (int)
Result:  floorId (String)
```

**Flow:**
1. `fixedAssetRepository.findById(buildingId)` → throw `ASSET_NOT_FOUND` nếu không tồn tại
2. Validate: `building.getType() == BUILDING` — nếu không → throw `INVALID_ASSET_TYPE_FOR_PARENT`
3. `path = building.getPath() + "/" + newId`
4. `FixedAsset.create(FLOOR, name, code, sequenceNo, buildingId, path, null)`
5. `fixedAssetRepository.save(floor)`
6. Return `floorId`

---

### UC-003 — CreateUnit (`create_unit/`)

```
Command: floorId (String), name (String), code (String nullable), sequenceNo (int),
         type (FixedAssetType — chỉ RESIDENTIAL_UNIT hoặc COMMERCIAL_SPACE)
Result:  unitId (String)
```

**Flow:**
1. Validate command: `type` phải là `RESIDENTIAL_UNIT` hoặc `COMMERCIAL_SPACE` — nếu không → throw `ASSET_NOT_FOUND` (hoặc tạo error code riêng nếu muốn rõ hơn)
2. `fixedAssetRepository.findById(floorId)` → throw `ASSET_NOT_FOUND`
3. Validate: `floor.getType() == FLOOR` → throw `INVALID_ASSET_TYPE_FOR_PARENT`
4. `path = floor.getPath() + "/" + newId`
5. `FixedAsset.create(type, name, code, sequenceNo, floorId, path, null)`
6. `fixedAssetRepository.save(unit)`
7. Publish `UnitCreatedEvent { unitId, type, buildingId (extract từ path segment 1), code }`
8. Return `unitId`

**Extract buildingId từ path:** path của floor là `/buildingId/floorId` → split("/")[1]

---

### UC-004 — CreateOtherAsset (`create_other_asset/`)

```
Command: parentId (String), name (String), code (String nullable), sequenceNo (int),
         type (FixedAssetType — FACILITY | MEETING_ROOM | PARKING_SLOT | COMMON_AREA | EQUIPMENT)
Result:  assetId (String)
```

**Flow:**
1. `fixedAssetRepository.findById(parentId)` → throw `ASSET_NOT_FOUND`
2. `path = parent.getPath() + "/" + newId`
3. `FixedAsset.create(type, name, code, sequenceNo, parentId, path, null)`
4. `fixedAssetRepository.save(asset)`
5. Return `assetId`

Không emit event cho loại này.

---

### UC-005 — FindAssetTree (`find_tree/`)

```
Query:  buildingId (String)
Result: List<AssetView> — toàn bộ asset trong building, flat list (FE tự build tree)
```

**Flow:**
1. `fixedAssetRepository.findByPathPrefix("/" + buildingId)` → trả toàn bộ node trong building
2. Map sang `AssetView`

`AssetView` record: `{ id, parentId, path, type, name, code, sequenceNo, status, managingOrgId }`

---

### UC-006 — FindAssetById (`find_by_id/`)

```
Query:  assetId (String)
Result: AssetView
```

**Flow:**
1. `fixedAssetRepository.findById(assetId)` → throw `ASSET_NOT_FOUND`
2. Map sang `AssetView`

`AssetView` dùng chung với UC-005 — khai báo 1 lần tại package `application/fixed_asset/`.

---

## ID generation

Dùng `UUID.randomUUID().toString()` tại application layer trước khi pass vào `FixedAsset.create()`. Truyền qua `FixedAssetId.of(UUID.randomUUID())` hoặc theo pattern của domain.

## Event dispatch

Follow pattern của party-service: `EventDispatcher.dispatch(event)` gọi sau khi persist, trong cùng transaction. Config bean tại `infrastructure/cross_cutting/config/EventDispatcherConfig`.

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`docs/development/260416_01_design_party_model/property_service_plan.md`** — tick `[x]` tất cả items trong mục **Phase 1 — 1.3 Application layer**
- **`services/property/SERVICE_MAP.md`** — cập nhật section **Application Layer**: liệt kê 6 use case slices
- **`services/property/docs/use-cases/UC-000_index.md`** — cập nhật UC-001 → UC-006 thành `Implemented`

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi xong và `mvn clean compile -DskipTests` pass, cung cấp:

### PRESENTATION CONTEXT BLOCK
- Package paths thực tế của tất cả handlers
- `AssetView` — full record fields
- Command record fields thực tế của từng use case
- Error codes được throw từ mỗi handler

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
