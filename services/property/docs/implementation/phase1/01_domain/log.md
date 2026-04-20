# Log: Property Service Phase 1 — Domain Layer

## Status: ✅ Completed — `mvn clean compile -DskipTests` PASS

---

## Files tạo mới

| File | Package |
|------|---------|
| `FixedAssetId.java` | `domain.fixed_asset` |
| `FixedAssetType.java` | `domain.fixed_asset` |
| `FixedAssetStatus.java` | `domain.fixed_asset` |
| `FixedAsset.java` | `domain.fixed_asset` |
| `FixedAssetRepository.java` | `domain.fixed_asset` |
| `FixedAssetErrorCode.java` | `domain.fixed_asset` |
| `FixedAssetException.java` | `domain.fixed_asset` |
| `BuildingCreatedEvent.java` | `domain.fixed_asset.event` |
| `UnitCreatedEvent.java` | `domain.fixed_asset.event` |

## Files sửa

| File | Thay đổi |
|------|---------|
| `services/property/pom.xml` | Thêm dependency `libs/common:1.0.0` (bị thiếu) |

---

## Deviations

1. **`FixedAssetRepository`** — không khai báo lại `findById` và `save` vì đã có sẵn từ `Repository<T,ID>` base interface (`void save`, `Optional<T> findById`). Chỉ thêm `findByPathPrefix`.
2. **`FixedAssetErrorCode`** — thêm `INVALID_ASSET_TYPE_FOR_PARENT` (code `30005`) ngoài spec vì application layer cần để validate loại asset không hợp lệ với parent.

---

## INFRASTRUCTURE CONTEXT BLOCK

### Package paths

```
vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetId
vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetType
vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetStatus
vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAsset
vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetRepository
vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetErrorCode
vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetException
vn.truongngo.apartcom.one.service.property.domain.fixed_asset.event.BuildingCreatedEvent
vn.truongngo.apartcom.one.service.property.domain.fixed_asset.event.UnitCreatedEvent
```

### FixedAssetRepository methods (từ base + override)

```java
// Từ Repository<FixedAsset, FixedAssetId> base:
Optional<FixedAsset> findById(FixedAssetId id);
void save(FixedAsset asset);          // return void — không return domain object
void delete(FixedAssetId id);

// Thêm trong FixedAssetRepository:
List<FixedAsset> findByPathPrefix(String pathPrefix);
```

### FixedAssetErrorCode — full enum

```java
ASSET_NOT_FOUND("30001", 404)
ASSET_ALREADY_INACTIVE("30002", 422)
MANAGING_ORG_REQUIRED("30003", 422)
INVALID_ASSET_STATUS_TRANSITION("30004", 422)
INVALID_ASSET_TYPE_FOR_PARENT("30005", 422)
```

### FixedAssetException — static factory methods

```java
FixedAssetException.notFound()
FixedAssetException.alreadyInactive()
FixedAssetException.managingOrgRequired()
FixedAssetException.invalidStatusTransition()
FixedAssetException.invalidTypeForParent()
```

### FixedAsset.create() — ID generation

`FixedAssetId.generate()` được gọi bên trong `FixedAsset.create()`. Application layer **không** cần tạo ID trước khi gọi `create()`. Tuy nhiên application layer cần biết ID sau khi tạo để tính `path` — dùng `asset.getId().getValue()` sau khi `create()` trả về.

### Mapper notes

| Field                             | Type           | Nullable                                        |
|-----------------------------------|----------------|-------------------------------------------------|
| `parentId`                        | `FixedAssetId` | null khi BUILDING                               |
| `code`                            | `String`       | nullable                                        |
| `managingOrgId`                   | `String`       | nullable (chỉ non-null khi BUILDING)            |
| `id`, `type`, `path`, `createdAt` | —              | immutable — dùng `reconstitute()`, không setter |

### Transaction note

`FixedAssetRepository.save()` là void — adapter không cần return. Application layer quản lý `@Transactional`.
