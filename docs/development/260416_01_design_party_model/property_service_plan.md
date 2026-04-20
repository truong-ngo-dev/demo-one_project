# Implementation Plan — Property Service

## Thông tin service

```
Service name : property-service
Base package : vn.truongngo.apartcom.one.service.property
Stack        : Java 21, Spring Boot 4.x, Maven, MySQL
```

---

## Aggregate Boundaries

| Aggregate Root       | Entities bên trong | Lý do                                                               |
|----------------------|--------------------|---------------------------------------------------------------------|
| `FixedAsset`         | —                  | Self-referencing tree; path tính tại app layer khi tạo             |
| `OccupancyAgreement` | —                  | Standalone AR; lifecycle riêng; enforce invariants I1–I5 tại domain |

**Không có Agreement supertype** — OccupancyAgreement là AR duy nhất cho agreement, không unify.

**Invariant scope:**
- I1, I2, I3 → validate tại application layer (cần query repo để check ACTIVE count)
- I4, I5, I6, I7 → validate tại domain layer (pure logic trên field)

---

## Phase 1 — FixedAsset Aggregate

### 1.1 Domain layer
- [x] `FixedAssetId` (Value Object — typed UUID)
- [x] `FixedAssetType` enum: `BUILDING`, `FLOOR`, `RESIDENTIAL_UNIT`, `COMMERCIAL_SPACE`, `COMMON_AREA`, `FACILITY`, `MEETING_ROOM`, `PARKING_SLOT`, `EQUIPMENT`
- [x] `FixedAssetStatus` enum: `ACTIVE`, `INACTIVE`, `UNDER_MAINTENANCE`
- [x] `FixedAsset` (Aggregate Root); behaviors: `create()`, `reconstitute()`, `deactivate()`, `setUnderMaintenance()`, `reactivate()`
- [x] `FixedAssetRepository` (interface — port)
- [x] `FixedAssetErrorCode`, `FixedAssetException`
- [x] `BuildingCreatedEvent` — payload: `{ buildingId, name, managingOrgId }`
- [x] `UnitCreatedEvent` — payload: `{ unitId, type, buildingId, code }`

### 1.2 Infrastructure layer
- [x] Migration `V1__create_fixed_asset_table.sql`
- [x] `FixedAssetJpaEntity`
- [x] `FixedAssetJpaRepository` — derived queries: `findAllByPathStartingWith`, `findAllByParentId`
- [x] `FixedAssetMapper`
- [x] `FixedAssetPersistenceAdapter` implements `FixedAssetRepository`

### 1.3 Application layer
- [x] `fixed_asset/create_building/CreateBuilding` (Command) — emit `BuildingCreatedEvent`
- [x] `fixed_asset/create_floor/CreateFloor` (Command)
- [x] `fixed_asset/create_unit/CreateUnit` (Command) — emit `UnitCreatedEvent` nếu type là RESIDENTIAL_UNIT / COMMERCIAL_SPACE
- [x] `fixed_asset/create_other_asset/CreateOtherAsset` (Command) — dùng cho FACILITY, MEETING_ROOM, PARKING_SLOT, COMMON_AREA, EQUIPMENT
- [x] `fixed_asset/find_tree/FindAssetTree` (Query) — trả về cây theo `path LIKE '/buildingId/%'`
- [x] `fixed_asset/find_by_id/FindAssetById` (Query)

### 1.4 Presentation layer
- [x] `fixed_asset/FixedAssetController` — base path `/api/v1/assets`
  - `POST /` — tạo asset (dispatch theo type)
  - `GET /{id}` — find by id
  - `GET /` — query cây (param: `buildingId`)

---

## Phase 2 — OccupancyAgreement Aggregate

### 2.1 Domain layer
- [x] `OccupancyAgreementId` (Value Object — typed UUID)
- [x] `OccupancyAgreementType` enum: `OWNERSHIP`, `LEASE`
- [x] `OccupancyAgreementStatus` enum: `PENDING`, `ACTIVE`, `TERMINATED`, `EXPIRED`
- [x] `PartyType` enum (local): `PERSON`, `HOUSEHOLD`, `ORGANIZATION` — reference only, không import từ party-service
- [x] `OccupancyAgreement` (Aggregate Root); behaviors: `create()`, `reconstitute()`, `activate()`, `terminate()`, `expire()`
  - `activate()` → emit `OccupancyAgreementActivatedEvent`
  - `terminate()` → emit `OccupancyAgreementTerminatedEvent`
  - `expire()` → emit `OccupancyAgreementTerminatedEvent`
  - Validate I4, I5, I6, I7 tại domain
- [x] `OccupancyAgreementRepository` (interface — port)
- [x] `OccupancyAgreementErrorCode`, `OccupancyAgreementException`
- [x] `OccupancyAgreementActivatedEvent` — payload: `{ agreementId, partyId, partyType, assetId, agreementType }`
- [x] `OccupancyAgreementTerminatedEvent` — payload: `{ agreementId, partyId, partyType, assetId, agreementType }`

### 2.2 Infrastructure layer
- [x] Migration `V2__create_occupancy_agreement_table.sql`
- [x] `OccupancyAgreementJpaEntity`
- [x] `OccupancyAgreementJpaRepository` — derived queries:
  - `existsByAssetIdAndAgreementTypeAndStatus` (check I1, I2)
  - `findByAssetId`, `findByPartyId`
- [x] `OccupancyAgreementMapper`
- [x] `OccupancyAgreementPersistenceAdapter` implements `OccupancyAgreementRepository`

### 2.3 Application layer
- [x] `agreement/create/CreateOccupancyAgreement` (Command) — validate I1, I2 qua repo; create với status PENDING
- [x] `agreement/activate/ActivateAgreement` (Command) — status → ACTIVE, emit event
- [x] `agreement/terminate/TerminateAgreement` (Command) — status → TERMINATED, emit event
- [x] `agreement/expire/ExpireAgreement` (Command) — status → EXPIRED, emit event (scheduled or manual)
- [x] `agreement/find_by_asset/FindAgreementsByAsset` (Query)
- [x] `agreement/find_by_party/FindAgreementsByParty` (Query)

### 2.4 Presentation layer
- [x] `agreement/OccupancyAgreementController` — base path `/api/v1/agreements`
  - `POST /` — tạo agreement
  - `POST /{id}/activate`
  - `POST /{id}/terminate`
  - `GET /` — query by `assetId` hoặc `partyId`

---

## Business Rules (enforce trong domain vs application)

| Rule | Layer | Ghi chú |
|------|-------|---------|
| I1 — Max 1 ACTIVE OWNERSHIP per asset | Application | Cần query repo |
| I2 — Max 1 ACTIVE LEASE per asset | Application | Cần query repo |
| I3 — OWNERSHIP và LEASE có thể song song | — | Không cần enforce, tự nhiên từ I1+I2 |
| I4 — LEASE chỉ trên RESIDENTIAL_UNIT / COMMERCIAL_SPACE | Domain | Pure field logic |
| I5 — OWNERSHIP chỉ trên RESIDENTIAL_UNIT | Domain | Pure field logic |
| I6 — OWNERSHIP: end_date=null, party_type=PERSON | Domain | Pure field logic |
| I7 — LEASE: end_date bắt buộc, party_type match unit | Domain | Pure field logic |
| I8 — managing_org_id bắt buộc trên BUILDING | Domain | Validate trong `FixedAsset.create()` |

---

## Status

| Phase                            | Status       |
|----------------------------------|--------------|
| Phase 1 — FixedAsset Aggregate   | `[x] Completed` |
| Phase 2 — OccupancyAgreement     | `[x] Completed` |
