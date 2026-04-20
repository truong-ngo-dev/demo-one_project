# Prompt: Property Service Phase 1 — Domain Layer

**Vai trò**: Bạn là Senior Backend Engineer implement Domain Layer cho `services/property`. Đây là nền tảng — code phải pure Java, không import Spring. Phase 1 chỉ bao gồm `FixedAsset` aggregate.

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md
2. Service overview: @services/property/CLAUDE.md
3. Aggregate boundaries + schema: @docs/development/260416_01_design_party_model/02_property_service.md
4. Implementation plan: @docs/development/260416_01_design_party_model/property_service_plan.md (Phase 1 — 1.1 Domain layer)
5. Domain doc: @services/property/docs/domains/fixed_asset.md

## Files tham khảo pattern

- Pattern AR + typed ID: `services/party/src/main/java/.../domain/party/Party.java` và `PartyId.java`
- Pattern domain exception: `services/party/src/main/java/.../domain/party/PartyException.java`
- Pattern error code enum: `services/party/src/main/java/.../domain/party/PartyErrorCode.java`
- Pattern domain event: `services/party/src/main/java/.../domain/party/event/PersonCreatedEvent.java`

Base package: `vn.truongngo.apartcom.one.service.property`

---

## Nhiệm vụ cụ thể

Tất cả file nằm trong `services/property/src/main/java/vn/truongngo/apartcom/one/service/property/domain/`.

### 1. Value Objects & Enums (`fixed_asset/`)

- `FixedAssetId.java` — typed UUID record (pattern: PartyId.java)
- `FixedAssetType.java` — enum: `BUILDING`, `FLOOR`, `RESIDENTIAL_UNIT`, `COMMERCIAL_SPACE`, `COMMON_AREA`, `FACILITY`, `MEETING_ROOM`, `PARKING_SLOT`, `EQUIPMENT`
- `FixedAssetStatus.java` — enum: `ACTIVE`, `INACTIVE`, `UNDER_MAINTENANCE`

### 2. Aggregate Root: FixedAsset (`fixed_asset/FixedAsset.java`)

Fields:
- `id` (FixedAssetId)
- `parentId` (FixedAssetId — nullable, null khi BUILDING)
- `path` (String — materialized path, immutable)
- `type` (FixedAssetType — immutable)
- `name` (String)
- `code` (String — nullable)
- `sequenceNo` (int)
- `status` (FixedAssetStatus)
- `managingOrgId` (String — nullable, chỉ set trên BUILDING)
- `createdAt` (Instant — immutable)
- `updatedAt` (Instant)

Factory methods:
```java
// Dùng cho tất cả type — validate I8 bên trong
static FixedAsset create(FixedAssetType type, String name, String code, int sequenceNo,
                         FixedAssetId parentId, String path, String managingOrgId)
// I8: nếu type=BUILDING → managingOrgId bắt buộc (throw MANAGING_ORG_REQUIRED)
// parentId=null chỉ hợp lệ khi type=BUILDING

static FixedAsset reconstitute(FixedAssetId id, FixedAssetType type, String name, String code,
                                int sequenceNo, FixedAssetId parentId, String path,
                                FixedAssetStatus status, String managingOrgId,
                                Instant createdAt, Instant updatedAt)
```

Behaviors:
- `deactivate()` — throw `ASSET_ALREADY_INACTIVE` nếu status == INACTIVE
- `setUnderMaintenance()` — chỉ hợp lệ khi status == ACTIVE
- `reactivate()` — chuyển về ACTIVE

### 3. Repository port (`fixed_asset/FixedAssetRepository.java`)

```java
Optional<FixedAsset> findById(FixedAssetId id);
List<FixedAsset> findByPathPrefix(String pathPrefix);  // cho tree query
FixedAsset save(FixedAsset asset);
```

### 4. Domain Events (`fixed_asset/event/`)

- `BuildingCreatedEvent.java` — fields: `buildingId` (FixedAssetId), `name` (String), `managingOrgId` (String)
- `UnitCreatedEvent.java` — fields: `unitId` (FixedAssetId), `type` (FixedAssetType), `buildingId` (String), `code` (String)

> `buildingId` trong `UnitCreatedEvent` là String (path segment), không phải FixedAssetId — application layer trích từ path của parent floor.

### 5. Exception & Error Codes (`fixed_asset/`)

`FixedAssetErrorCode.java` — enum với HTTP status:
- `ASSET_NOT_FOUND` (404)
- `ASSET_ALREADY_INACTIVE` (422)
- `MANAGING_ORG_REQUIRED` (422)
- `INVALID_ASSET_STATUS_TRANSITION` (422)

`FixedAssetException.java` — extends `DomainException` từ `libs/common`

**Không implement**: Infrastructure, Application, Presentation, Spring annotations trong domain.

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`docs/development/260416_01_design_party_model/property_service_plan.md`** — tick `[x]` tất cả items trong mục **Phase 1 — 1.1 Domain layer**
- **`services/property/SERVICE_MAP.md`** — cập nhật section **Domain Layer**: điền package path và behaviors thực tế của FixedAsset

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi xong và `mvn clean compile -DskipTests` pass, cung cấp:

### INFRASTRUCTURE CONTEXT BLOCK
- Package paths thực tế của tất cả files đã tạo
- Tên method thực tế trên `FixedAssetRepository` port
- `FixedAssetErrorCode` — full enum values để infrastructure dùng đúng
- Bất kỳ deviation nào so với spec này
- Lưu ý mapper: field nào nullable, field nào immutable

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
