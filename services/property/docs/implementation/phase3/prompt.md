# Prompt: Property Service Phase 3 — FindAgreementsByBuilding

**Vai trò**: Bạn là Senior Backend Engineer implement UC-013 (FindAgreementsByBuilding) cho `services/property`. UC này cần để Operator Portal trong admin-service hiển thị danh sách chiếm dụng trong 1 tòa nhà.

> Phases 1 và 2 đã xong. Phase 3 là 1 use case query thuần.

**Yêu cầu**: Phase 1 và 2 đã xong và compile pass.

---

## Tài liệu căn cứ

1. Convention: @docs/conventions/ddd-structure.md, @docs/conventions/api-design.md
2. Service overview: @services/property/CLAUDE.md
3. UC-013 detail: @services/property/docs/use-cases/UC-013_find_agreements_by_building.md
4. UC index: @services/property/docs/use-cases/UC-000_index.md

## Files tham khảo pattern

- Pattern query handler: `services/property/src/main/java/.../application/agreement/find_by_asset/FindAgreementsByAsset.java`
- Pattern JPA repository: `services/property/src/main/java/.../infrastructure/persistence/agreement/OccupancyAgreementJpaRepository.java`

Base package: `vn.truongngo.apartcom.one.service.property`

---

## Context hiện có

### FixedAssetRepository (đã tồn tại)

```java
// Đã có — trả về tất cả assets có path bắt đầu bằng pathPrefix
List<FixedAsset> findByPathPrefix(String pathPrefix);
```

### OccupancyAgreementRepository (hiện tại)

```java
Optional<OccupancyAgreement> findById(OccupancyAgreementId id);
boolean existsActiveByAssetIdAndType(String assetId, OccupancyAgreementType type);
List<OccupancyAgreement> findByAssetId(String assetId);
List<OccupancyAgreement> findByPartyId(String partyId);
OccupancyAgreement save(OccupancyAgreement agreement);
```

### OccupancyAgreementJpaRepository (hiện tại)

```java
boolean existsByAssetIdAndAgreementTypeAndStatus(/*...*/);
List<OccupancyAgreementJpaEntity> findAllByAssetId(String assetId);
List<OccupancyAgreementJpaEntity> findAllByPartyId(String partyId);
```

### AgreementView record (đã tồn tại tại `application/agreement/`)

```java
record AgreementView(
    String id, String partyId, String partyType, String assetId,
    String agreementType, String status,
    LocalDate startDate, LocalDate endDate, String contractRef
) {}
```

> Dùng lại — không tạo mới.

### FixedAsset — method liên quan

```java
FixedAssetId getId();
String getPath();   // ví dụ: "/abc-uuid"
```

---

## Nhiệm vụ cụ thể

### Bước 1 — Thêm method vào `OccupancyAgreementRepository`

```java
List<OccupancyAgreement> findByAssetIds(List<String> assetIds);
```

### Bước 2 — Thêm derived query vào `OccupancyAgreementJpaRepository`

```java
List<OccupancyAgreementJpaEntity> findAllByAssetIdIn(List<String> assetIds);
```

### Bước 3 — Implement trong `OccupancyAgreementPersistenceAdapter`

```java
@Override
public List<OccupancyAgreement> findByAssetIds(List<String> assetIds) {
    if (assetIds.isEmpty()) return List.of();
    return jpaRepository.findAllByAssetIdIn(assetIds)
            .stream().map(mapper::toDomain).toList();
}
```

### Bước 4 — Handler `application/agreement/find_by_building/`

**`FindAgreementsByBuilding.java`**:

```
Query:  buildingId (String), status (OccupancyAgreementStatus nullable)
Result: List<AgreementView>
```

**Flow:**
1. `fixedAssetRepository.findById(FixedAssetId.of(buildingId))` → throw `FixedAssetException.notFound()` nếu không có
2. `String pathPrefix = building.getPath()`
3. `List<FixedAsset> children = fixedAssetRepository.findByPathPrefix(pathPrefix)`
4. `List<String> allAssetIds = Stream.concat(Stream.of(buildingId), children.stream().map(a -> a.getId().getValue())).toList()`
5. `List<OccupancyAgreement> agreements = agreementRepository.findByAssetIds(allAssetIds)`
6. Nếu `status != null` → filter in-memory: `agreements.stream().filter(a -> a.getStatus() == status)`
7. Map sang `AgreementView` list

> `AgreementView` đã có — import từ package `application.agreement`.

### Bước 5 — Endpoint

Thêm vào controller hiện có (`presentation/agreement/AgreementController.java`) hoặc tạo mới nếu không phù hợp:

| Method | Path                 | Handler                            | Params                                           | Response                            |
|--------|----------------------|------------------------------------|--------------------------------------------------|-------------------------------------|
| `GET`  | `/api/v1/agreements` | `FindAgreementsByBuilding.Handler` | `?buildingId=` (required), `?status=` (optional) | `200 { data: List<AgreementView> }` |

> **Note**: Nếu `GET /api/v1/agreements` đã được dùng cho use case khác, thêm path `/api/v1/agreements/by-building` thay thế.

---

## Cập nhật tài liệu (sau khi compile pass)

- `services/property/docs/use-cases/UC-000_index.md` — cập nhật UC-013 thành `Implemented`

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` pass, cung cấp:

### PHASE3_CONTEXT BLOCK
- Package path thực tế của `FindAgreementsByBuilding.java`
- Endpoint thực tế (path nếu có deviation)
- `findByAssetIds` — actual method name trong JpaRepository
- Deviation so với spec nếu có

---

## Output Log

Xuất log ra `log.md` trong cùng thư mục này sau khi hoàn thành.
