# SERVICE_MAP — property-service

> **First entry point cho AI agents.** Đọc file này trước khi dùng bất kỳ công cụ tìm kiếm nào.
> Chi tiết convention xem tại [`docs/conventions/ddd-structure.md`](../../docs/conventions/ddd-structure.md).
> Design decisions xem tại [`docs/development/260416_01_design_party_model/02_property_service.md`](../../docs/development/260416_01_design_party_model/02_property_service.md).

---

## 📂 1. Domain Layer (`domain/`)

| Aggregate / Concept | Package | Ghi chú |
|---------------------|---------|---------|
| `FixedAsset` | `domain.fixed_asset` | AR; `create()`, `reconstitute()`; behaviors: `deactivate()`, `setUnderMaintenance()`, `reactivate()`; `FixedAssetId`, enums, `FixedAssetRepository` port, `FixedAssetErrorCode`, `FixedAssetException` |
| Domain Events Phase 1 | `domain.fixed_asset.event` | `BuildingCreatedEvent`, `UnitCreatedEvent` |
| `OccupancyAgreement` | `domain.agreement` | AR; `create()` (validates I4–I7, receives `FixedAssetType` but does not store it), `reconstitute()`; behaviors: `activate()`, `terminate()`, `expire()`; `OccupancyAgreementId`, `OccupancyAgreementType`, `OccupancyAgreementStatus`, `PartyType` (local), `OccupancyAgreementRepository` port, `OccupancyAgreementErrorCode`, `OccupancyAgreementException` |
| Domain Events Phase 2 | `domain.agreement.event` | `OccupancyAgreementActivatedEvent`, `OccupancyAgreementTerminatedEvent` |

---

## 🚀 2. Application Layer (`application/`)

### FixedAsset Use Cases (`application/fixed_asset/`)

| Slice | Handler class | Type | Trả về |
|-------|--------------|------|--------|
| `create_building/` | `CreateBuilding.Handler` | Command | `CreateBuilding.Result(buildingId)` |
| `create_floor/` | `CreateFloor.Handler` | Command | `CreateFloor.Result(floorId)` |
| `create_unit/` | `CreateUnit.Handler` | Command | `CreateUnit.Result(unitId)` |
| `create_other_asset/` | `CreateOtherAsset.Handler` | Command | `CreateOtherAsset.Result(assetId)` |
| `find_tree/` | `FindAssetTree.Handler` | Query | `List<AssetView>` |
| `find_by_id/` | `FindAssetById.Handler` | Query | `AssetView` |

**Shared view**: `AssetView` record ở package `application.fixed_asset` — dùng chung cho cả 2 query.

### OccupancyAgreement Use Cases (`application/agreement/`)

| Slice | Handler class | Type | Trả về |
|-------|--------------|------|--------|
| `create/` | `CreateOccupancyAgreement.Handler` | Command | `CreateOccupancyAgreement.Result(agreementId)` |
| `activate/` | `ActivateAgreement.Handler` | Command | `ActivateAgreement.Result()` |
| `terminate/` | `TerminateAgreement.Handler` | Command | `TerminateAgreement.Result()` |
| `expire/` | `ExpireAgreement.Handler` | Command | `ExpireAgreement.Result()` |
| `find_by_asset/` | `FindAgreementsByAsset.Handler` | Query | `List<AgreementView>` |
| `find_by_party/` | `FindAgreementsByParty.Handler` | Query | `List<AgreementView>` |

**Shared view**: `AgreementView` record ở package `application.agreement` — dùng chung cho cả 2 query.

---

## 🔧 3. Infrastructure Layer (`infrastructure/`)

### Persistence (`infrastructure/persistence/fixed_asset/`)

| Class | Ghi chú |
|-------|---------|
| `FixedAssetJpaEntity` | JPA entity cho bảng `fixed_asset`; `parentId` là plain String (nullable); `@Enumerated(STRING)` cho `type` và `status` |
| `FixedAssetJpaRepository` | Spring Data JPA; `findAllByPathStartingWith(String)` (tree query), `findAllByParentId(String)` |
| `FixedAssetMapper` | Static: `toDomain`, `toEntity`, `updateEntity` (chỉ update mutable fields: name, code, sequenceNo, status, updatedAt) |

### Adapters (`infrastructure/adapter/repository/fixed_asset/`)

| Class | Implements |
|-------|-----------|
| `FixedAssetPersistenceAdapter` | `FixedAssetRepository` — upsert pattern (check exists → updateEntity or toEntity) |

### Persistence (`infrastructure/persistence/agreement/`)

| Class | Ghi chú |
|-------|---------|
| `OccupancyAgreementJpaEntity` | JPA entity cho bảng `occupancy_agreement`; `endDate` và `contractRef` nullable; `@Enumerated(STRING)` cho `partyType`, `agreementType`, `status` |
| `OccupancyAgreementJpaRepository` | Spring Data JPA; `existsByAssetIdAndAgreementTypeAndStatus` (I1/I2 check), `findAllByAssetId`, `findAllByPartyId` |
| `OccupancyAgreementMapper` | Static: `toDomain`, `toEntity`, `updateEntity` (chỉ update mutable fields: status, updatedAt) |

### Adapters (`infrastructure/adapter/repository/agreement/`)

| Class | Implements |
|-------|-----------|
| `OccupancyAgreementPersistenceAdapter` | `OccupancyAgreementRepository` — upsert pattern; `save()` returns domain aggregate |

### Migration (`resources/db/migration/`)

| File | Nội dung |
|------|---------|
| `V1__create_fixed_asset_table.sql` | Table `fixed_asset`; self-referencing FK `parent_id → id`; ENUM cho `type` và `status` |
| `V2__create_occupancy_agreement_table.sql` | Table `occupancy_agreement`; FK `asset_id → fixed_asset(id)`; ENUM cho `party_type`, `agreement_type`, `status` |

---

## 🌐 4. Presentation Layer (`presentation/`)

### Base (`presentation/base/`)

`ApiResponse<T>`, `ErrorResponse`, `GlobalExceptionHandler` — xử lý `DomainException` và `IllegalArgumentException`

### Public API (`presentation/fixed_asset/`)

| Controller | Base path | Endpoints |
|-----------|-----------|-----------|
| `FixedAssetController` | `/api/v1/assets` | `POST /` (dispatch by type — UC-001/002/003/004), `GET /{id}` (UC-006), `GET /` (param: `buildingId` — UC-005) |

### Public API (`presentation/agreement/`)

| Controller | Base path | Endpoints |
|-----------|-----------|-----------|
| `OccupancyAgreementController` | `/api/v1/agreements` | `POST /` (UC-007), `POST /{id}/activate` (UC-008), `POST /{id}/terminate` (UC-009), `POST /{id}/expire` (UC-010), `GET /` (params: `assetId` XOR `partyId`, `status?` — UC-011/012) |

---

## 📤 5. Domain Events Published

| Event | Trigger | Consumer | Status |
|-------|---------|----------|--------|
| `BuildingCreated` | Tạo Building | admin-service (cache building ref) | ✅ Emitted |
| `UnitCreated` | Tạo RESIDENTIAL_UNIT / COMMERCIAL_SPACE | — | ✅ Emitted |
| `OccupancyAgreementActivated` | Agreement → ACTIVE | admin-service (tạo RoleContext) | ✅ Emitted |
| `OccupancyAgreementTerminated` | Agreement → TERMINATED / EXPIRED | admin-service (revoke RoleContext) | ✅ Emitted |
