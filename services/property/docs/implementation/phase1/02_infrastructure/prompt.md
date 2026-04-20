# Prompt: Property Service Phase 1 — Infrastructure Layer

**Vai trò**: Bạn là Senior Backend Engineer implement Infrastructure Layer Phase 1 cho `services/property`. Domain layer (FixedAsset) đã xong. Nhiệm vụ: migration SQL + JPA entity + persistence adapter.

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md
2. Service overview: @services/property/CLAUDE.md
3. Schema chi tiết: @docs/development/260416_01_design_party_model/02_property_service.md (Section 3)
4. Implementation plan: @docs/development/260416_01_design_party_model/property_service_plan.md (Phase 1 — 1.2 Infrastructure layer)

## Files tham khảo pattern

- Pattern JPA entity: `services/party/src/main/java/.../infrastructure/persistence/party/PartyJpaEntity.java`
- Pattern mapper: `services/party/src/main/java/.../infrastructure/persistence/party/PartyMapper.java`
- Pattern persistence adapter: `services/party/src/main/java/.../infrastructure/adapter/repository/party/PartyPersistenceAdapter.java`
- Pattern migration: `services/party/src/main/resources/db/migration/V1__create_party_tables.sql`

Base package: `vn.truongngo.apartcom.one.service.property`

## Context từ 01_domain

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

| Field | Type | Nullable |
|-------|------|---------|
| `parentId` | `FixedAssetId` | null khi BUILDING |
| `code` | `String` | nullable |
| `managingOrgId` | `String` | nullable (chỉ non-null khi BUILDING) |
| `id`, `type`, `path`, `createdAt` | — | immutable — dùng `reconstitute()`, không setter |

### Transaction note

`FixedAssetRepository.save()` là void — adapter không cần return. Application layer quản lý `@Transactional`.

---

## Nhiệm vụ cụ thể

### 1. Flyway Migration

File: `services/property/src/main/resources/db/migration/V1__create_fixed_asset_table.sql`

Schema (xem Section 3 của `02_property_service.md`):
```sql
CREATE TABLE fixed_asset (
    id              VARCHAR(36) PRIMARY KEY,
    parent_id       VARCHAR(36),
    path            VARCHAR(500) NOT NULL,
    type            ENUM(...) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    code            VARCHAR(50),
    sequence_no     INT DEFAULT 0,
    status          ENUM('ACTIVE', 'INACTIVE', 'UNDER_MAINTENANCE') NOT NULL DEFAULT 'ACTIVE',
    managing_org_id VARCHAR(36),
    created_at      DATETIME NOT NULL,
    updated_at      DATETIME NOT NULL,
    FOREIGN KEY (parent_id) REFERENCES fixed_asset(id)
);
```

**Phase 1 chỉ tạo `fixed_asset`** — không tạo `occupancy_agreement`.

### 2. JPA Entity

Package: `infrastructure/persistence/fixed_asset/`

`FixedAssetJpaEntity.java`:
- `@Entity @Table(name = "fixed_asset")`
- `@Id` là String (không `@GeneratedValue` — ID do domain tạo)
- `parent_id` là plain String (nullable) — không dùng `@ManyToOne` để tránh lazy load phức tạp
- `@Enumerated(EnumType.STRING)` cho `type` và `status`
- Tất cả fields nullable theo schema

### 3. JPA Repository

Package: `infrastructure/persistence/fixed_asset/`

`FixedAssetJpaRepository extends JpaRepository<FixedAssetJpaEntity, String>`:
```java
List<FixedAssetJpaEntity> findAllByPathStartingWith(String pathPrefix);
List<FixedAssetJpaEntity> findAllByParentId(String parentId);
```

### 4. Mapper

Package: `infrastructure/persistence/fixed_asset/`

`FixedAssetMapper.java` — static methods:
- `toDomain(FixedAssetJpaEntity entity) → FixedAsset`
- `toEntity(FixedAsset domain) → FixedAssetJpaEntity`
- Dùng `FixedAsset.reconstitute()` khi map về domain
- `FixedAssetId` ↔ `String` (`.value()` và `new FixedAssetId(UUID.fromString(...))`)
- `managingOrgId` và `parentId` nullable — handle null

### 5. Persistence Adapter

Package: `infrastructure/adapter/repository/fixed_asset/`

`FixedAssetPersistenceAdapter.java` implements `FixedAssetRepository` (domain port):
- `findById`: map String id → findById → map to domain (empty Optional nếu không tìm thấy)
- `findByPathPrefix`: `findAllByPathStartingWith(pathPrefix)` → map list
- `save`: `toEntity` → `jpaRepo.save` → `toDomain`

**Không implement**: Application, Presentation, `occupancy_agreement` tables.

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`docs/development/260416_01_design_party_model/property_service_plan.md`** — tick `[x]` tất cả items trong mục **Phase 1 — 1.2 Infrastructure layer**
- **`services/property/SERVICE_MAP.md`** — cập nhật section **Infrastructure Layer**: liệt kê entity/adapter đã tạo

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi xong và `mvn clean compile -DskipTests` pass, cung cấp:

### APPLICATION CONTEXT BLOCK
- Package paths thực tế của tất cả files đã tạo
- JPA query method names thực tế trên `FixedAssetJpaRepository`
- Deviation so với spec (nếu có)
- Lưu ý transaction: adapter có `@Transactional` không, hay để application layer quản lý

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
