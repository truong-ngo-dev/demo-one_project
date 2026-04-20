# Prompt: Property Service Phase 2 — Infrastructure Layer

**Vai trò**: Bạn là Senior Backend Engineer implement Infrastructure Layer Phase 2 cho `services/property`. Phase 1 infrastructure (FixedAsset) đã xong. Nhiệm vụ: migration + JPA entity + adapter cho `OccupancyAgreement`.

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md
2. Service overview: @services/property/CLAUDE.md
3. Schema chi tiết: @docs/development/260416_01_design_party_model/02_property_service.md (Section 3)
4. Implementation plan: @docs/development/260416_01_design_party_model/property_service_plan.md (Phase 2 — 2.2 Infrastructure layer)

## Files tham khảo pattern

- Pattern JPA entity: `services/party/src/main/java/.../infrastructure/persistence/party/PartyJpaEntity.java`
- Pattern mapper: `services/party/src/main/java/.../infrastructure/persistence/party/PartyMapper.java`
- Pattern persistence adapter: `services/party/src/main/java/.../infrastructure/adapter/repository/party/PartyPersistenceAdapter.java`

Base package: `vn.truongngo.apartcom.one.service.property`

## Context từ 01_domain Phase 2

### OccupancyAgreementRepository — method signatures

```java
Optional<OccupancyAgreement> findById(OccupancyAgreementId id);
boolean existsActiveByAssetIdAndType(String assetId, OccupancyAgreementType type);
List<OccupancyAgreement> findByAssetId(String assetId);
List<OccupancyAgreement> findByPartyId(String partyId);
OccupancyAgreement save(OccupancyAgreement agreement);
```

> Note: `OccupancyAgreementRepository` does **not** extend `Repository<T,ID>` from `libs/common` — it declares all methods explicitly, including `save()` returning the aggregate (not void). This is intentional because the upsert adapter needs to return the saved entity.

### OccupancyAgreementErrorCode — full enum

| Enum Value                         | Code  | HTTP |
|------------------------------------|-------|------|
| `AGREEMENT_NOT_FOUND`              | 31001 | 404  |
| `AGREEMENT_INVALID_STATUS`         | 31002 | 422  |
| `OWNERSHIP_ALREADY_EXISTS`         | 31003 | 409  |
| `LEASE_ALREADY_EXISTS`             | 31004 | 409  |
| `INVALID_ASSET_TYPE_FOR_LEASE`     | 31005 | 422  |
| `INVALID_ASSET_TYPE_FOR_OWNERSHIP` | 31006 | 422  |
| `INVALID_PARTY_TYPE_FOR_OWNERSHIP` | 31007 | 422  |
| `INVALID_PARTY_TYPE_FOR_UNIT`      | 31008 | 422  |
| `END_DATE_REQUIRED_FOR_LEASE`      | 31009 | 422  |

### OccupancyAgreement.create() signature

```java
static OccupancyAgreement create(String partyId, PartyType partyType, String assetId,
                                  FixedAssetType assetType, OccupancyAgreementType agreementType,
                                  LocalDate startDate, LocalDate endDate, String contractRef);
```

### OccupancyAgreement.reconstitute() signature

```java
static OccupancyAgreement reconstitute(OccupancyAgreementId id, String partyId, PartyType partyType,
                                        String assetId, OccupancyAgreementType agreementType,
                                        OccupancyAgreementStatus status, LocalDate startDate,
                                        LocalDate endDate, String contractRef,
                                        Instant createdAt, Instant updatedAt);
```

---

## Deviations from Spec

1. **I6 endDate validation**: Spec says `endDate` must be null for OWNERSHIP but doesn't define a dedicated error code. Used `AGREEMENT_INVALID_STATUS` (31002) for this case — semantically the combination of fields is an invalid agreement state.

2. **`activate()`, `terminate()`, `expire()` do not raise events directly**: Per spec, application layer raises events after `save()`. The behaviors only mutate status and `updatedAt`.

3. **`assetType` not stored**: `create()` accepts `FixedAssetType assetType` for invariant validation only — field is not a member of the aggregate.

---

## Nullable Fields

- `endDate` — null for OWNERSHIP agreements
- `contractRef` — optional, always nullable
- `parentId` (not applicable, OccupancyAgreement has no parent)

---

## Nhiệm vụ cụ thể

### 1. Flyway Migration

File: `services/property/src/main/resources/db/migration/V2__create_occupancy_agreement_table.sql`

Schema:
```sql
CREATE TABLE occupancy_agreement (
    id              VARCHAR(36) PRIMARY KEY,
    party_id        VARCHAR(36) NOT NULL,
    party_type      ENUM('PERSON', 'HOUSEHOLD', 'ORGANIZATION') NOT NULL,
    asset_id        VARCHAR(36) NOT NULL,
    agreement_type  ENUM('OWNERSHIP', 'LEASE') NOT NULL,
    status          ENUM('PENDING', 'ACTIVE', 'TERMINATED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    start_date      DATE NOT NULL,
    end_date        DATE,
    contract_ref    VARCHAR(100),
    created_at      DATETIME NOT NULL,
    updated_at      DATETIME NOT NULL,
    FOREIGN KEY (asset_id) REFERENCES fixed_asset(id)
);
```

### 2. JPA Entity

Package: `infrastructure/persistence/agreement/`

`OccupancyAgreementJpaEntity.java`:
- `@Entity @Table(name = "occupancy_agreement")`
- `@Id` là String (không `@GeneratedValue`)
- `@Enumerated(EnumType.STRING)` cho `partyType`, `agreementType`, `status`
- `endDate` nullable, `contractRef` nullable

### 3. JPA Repository

Package: `infrastructure/persistence/agreement/`

`OccupancyAgreementJpaRepository extends JpaRepository<OccupancyAgreementJpaEntity, String>`:
```java
// Check I1/I2 — dùng trong application layer trước khi tạo agreement
boolean existsByAssetIdAndAgreementTypeAndStatus(
    String assetId, OccupancyAgreementType agreementType, OccupancyAgreementStatus status);

List<OccupancyAgreementJpaEntity> findAllByAssetId(String assetId);
List<OccupancyAgreementJpaEntity> findAllByPartyId(String partyId);
```

### 4. Mapper

Package: `infrastructure/persistence/agreement/`

`OccupancyAgreementMapper.java` — static methods:
- `toDomain(OccupancyAgreementJpaEntity entity) → OccupancyAgreement` — dùng `reconstitute()`
- `toEntity(OccupancyAgreement domain) → OccupancyAgreementJpaEntity`
- `endDate` và `contractRef` nullable — handle null
- `OccupancyAgreementId` ↔ String

### 5. Persistence Adapter

Package: `infrastructure/adapter/repository/agreement/`

`OccupancyAgreementPersistenceAdapter.java` implements `OccupancyAgreementRepository` (domain port):
- `findById`: map id → findById → map domain
- `existsActiveByAssetIdAndType(String assetId, OccupancyAgreementType type)`:
  ```java
  return jpaRepo.existsByAssetIdAndAgreementTypeAndStatus(assetId, type, OccupancyAgreementStatus.ACTIVE);
  ```
- `findByAssetId`: `findAllByAssetId` → map list
- `findByPartyId`: `findAllByPartyId` → map list
- `save`: toEntity → jpaRepo.save → toDomain

**Không implement**: Application, Presentation.

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`docs/development/260416_01_design_party_model/property_service_plan.md`** — tick `[x]` tất cả items trong mục **Phase 2 — 2.2 Infrastructure layer**
- **`services/property/SERVICE_MAP.md`** — cập nhật section **Infrastructure Layer**: thêm OccupancyAgreement entity/adapter

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi xong và `mvn clean compile -DskipTests` pass, cung cấp:

### APPLICATION CONTEXT BLOCK
- Package paths thực tế của tất cả files đã tạo
- JPA query method names thực tế
- Deviation so với spec (nếu có)
- Lưu ý transaction scope

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
