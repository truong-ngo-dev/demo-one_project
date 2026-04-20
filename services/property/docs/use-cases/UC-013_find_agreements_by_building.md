# UC-013 — Find Agreements By Building

## Mô tả

Query tất cả `OccupancyAgreement` thuộc một building, bao gồm cả agreements ở các floor, unit, và các asset con khác trong cây tài sản của building đó. Operator Portal dùng UC này để hiển thị danh sách chiếm dụng trong tòa nhà.

---

## Actors

- **BQL_MANAGER / OPERATOR** — xem danh sách agreements trong building quản lý

---

## Kỹ thuật: Materialized Path Query

`FixedAsset.path` lưu dạng `/building-id` (building) và `/building-id/floor-id/unit-id` (con cháu).

Để lấy tất cả asset trong 1 building:
1. Load building: `fixedAssetRepository.findById(buildingId)` → lấy `path` (ví dụ `/abc-123`)
2. Find all child assets: `fixedAssetRepository.findByPathPrefix(buildingPath)` — đã có sẵn
3. Gộp: `assetIds = [buildingId] + childAssets.map(id)`
4. Query agreements: `agreementRepository.findByAssetIds(assetIds)` — **cần thêm method mới**

---

## Thay đổi cần thiết

### `OccupancyAgreementRepository` — thêm method

```java
List<OccupancyAgreement> findByAssetIds(List<String> assetIds);
```

### `OccupancyAgreementJpaRepository` — thêm derived query

```java
List<OccupancyAgreementJpaEntity> findAllByAssetIdIn(List<String> assetIds);
```

### `OccupancyAgreementPersistenceAdapter` — implement method mới

```java
public List<OccupancyAgreement> findByAssetIds(List<String> assetIds) {
    return jpaRepository.findAllByAssetIdIn(assetIds)
        .stream().map(mapper::toDomain).toList();
}
```

---

## Flow (Application Handler)

```
Query: buildingId (String), status (OccupancyAgreementStatus nullable)

1. fixedAssetRepository.findById(FixedAssetId.of(buildingId)) → throw ASSET_NOT_FOUND nếu không có
2. String pathPrefix = building.getPath()
3. List<FixedAsset> children = fixedAssetRepository.findByPathPrefix(pathPrefix)
4. List<String> allAssetIds = Stream.concat(Stream.of(buildingId), children.stream().map(a -> a.getId().getValue())).toList()
5. List<OccupancyAgreement> agreements = agreementRepository.findByAssetIds(allAssetIds)
6. Nếu status != null → filter in-memory
7. Map sang AgreementView (dùng cùng AgreementView record từ application/agreement/)
```

---

## API

```
GET /api/v1/agreements?buildingId={buildingId}&status={status}
Response: 200 { data: List<AgreementView> }
```

> `status` là optional query param. AgreementView là record đã tồn tại từ Phase 2.
