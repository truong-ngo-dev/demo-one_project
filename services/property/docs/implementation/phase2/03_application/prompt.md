# Prompt: Property Service Phase 2 — Application Layer

**Vai trò**: Bạn là Senior Backend Engineer implement Application Layer Phase 2 cho `services/property`. Domain và Infrastructure (OccupancyAgreement) đã xong. Nhiệm vụ: 6 use case handlers cho OccupancyAgreement lifecycle.

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md
2. Service overview: @services/property/CLAUDE.md
3. Use case index: @services/property/docs/use-cases/UC-000_index.md (UC-007 → UC-012)
4. Implementation plan: @docs/development/260416_01_design_party_model/property_service_plan.md (Phase 2 — 2.3 Application layer)
5. Domain doc: @services/property/docs/domains/occupancy_agreement.md

## Files tham khảo pattern

- Pattern command handler: `services/party/src/main/java/.../application/party/create_person/CreatePerson.java`
- Pattern query handler: `services/party/src/main/java/.../application/party/find_by_id/FindPartyById.java`

Base package: `vn.truongngo.apartcom.one.service.property`

## Context từ 02_infrastructure Phase 2

### JPA query method names (actual)

```java
// OccupancyAgreementJpaRepository
boolean existsByAssetIdAndAgreementTypeAndStatus(
    String assetId, OccupancyAgreementType agreementType, OccupancyAgreementStatus status);

List<OccupancyAgreementJpaEntity> findAllByAssetId(String assetId);
List<OccupancyAgreementJpaEntity> findAllByPartyId(String partyId);
```

### OccupancyAgreementPersistenceAdapter — method mapping

| Domain port method                            | Adapter implementation                                                         |
|-----------------------------------------------|--------------------------------------------------------------------------------|
| `findById(OccupancyAgreementId)`              | `jpaRepository.findById(id.getValue()).map(toDomain)`                          |
| `existsActiveByAssetIdAndType(assetId, type)` | `existsByAssetIdAndAgreementTypeAndStatus(assetId, type, ACTIVE)`              |
| `findByAssetId(assetId)`                      | `findAllByAssetId(assetId)` → stream map toDomain                              |
| `findByPartyId(partyId)`                      | `findAllByPartyId(partyId)` → stream map toDomain                              |
| `save(agreement)`                             | upsert: check exists → `updateEntity` or `toEntity`; returns `toDomain(saved)` |

### OccupancyAgreementMapper — updateEntity mutable fields

Only `status` and `updatedAt` are updated on existing entity. All other fields (`partyId`, `partyType`, `assetId`, `agreementType`, `startDate`, `endDate`, `contractRef`, `createdAt`) are immutable after creation.

---

## Deviations

None — implemented exactly per spec.

---

## Transaction Note

`OccupancyAgreementPersistenceAdapter.save()` is a pure repository method — `@Transactional` boundary is owned by the application layer handlers (handlers are annotated `@Transactional`). The adapter itself has no transaction annotation.

---

## Nhiệm vụ cụ thể

Package gốc: `application/agreement/`

---

### UC-007 — CreateOccupancyAgreement (`create/`)

```
Command: partyId (String), partyType (PartyType), assetId (String),
         agreementType (OccupancyAgreementType), startDate (LocalDate),
         endDate (LocalDate nullable), contractRef (String nullable)
Result:  agreementId (String)
```

**Flow:**
1. Load FixedAsset: `fixedAssetRepository.findById(assetId)` → throw `ASSET_NOT_FOUND`
2. Check I1: nếu `agreementType == OWNERSHIP` → `agreementRepo.existsActiveByAssetIdAndType(assetId, OWNERSHIP)` → throw `OWNERSHIP_ALREADY_EXISTS`
3. Check I2: nếu `agreementType == LEASE` → `agreementRepo.existsActiveByAssetIdAndType(assetId, LEASE)` → throw `LEASE_ALREADY_EXISTS`
4. `OccupancyAgreement.create(partyId, partyType, assetId, asset.getType(), agreementType, startDate, endDate, contractRef)`
   - Domain validate I4/I5/I6/I7 bên trong — throw nếu vi phạm
5. `agreementRepository.save(agreement)`
6. Return `agreementId`

Không emit event khi tạo (status = PENDING).

---

### UC-008 — ActivateAgreement (`activate/`)

```
Command: agreementId (String)
Result:  void
```

**Flow:**
1. `agreementRepository.findById(agreementId)` → throw `AGREEMENT_NOT_FOUND`
2. `agreement.activate()` — domain throw `AGREEMENT_INVALID_STATUS` nếu status != PENDING
3. `agreementRepository.save(agreement)`
4. Publish `OccupancyAgreementActivatedEvent { agreementId, partyId, partyType, assetId, agreementType }`

---

### UC-009 — TerminateAgreement (`terminate/`)

```
Command: agreementId (String)
Result:  void
```

**Flow:**
1. `agreementRepository.findById(agreementId)` → throw `AGREEMENT_NOT_FOUND`
2. `agreement.terminate()` — domain throw `AGREEMENT_INVALID_STATUS` nếu status != ACTIVE
3. `agreementRepository.save(agreement)`
4. Publish `OccupancyAgreementTerminatedEvent { agreementId, partyId, partyType, assetId, agreementType }`

---

### UC-010 — ExpireAgreement (`expire/`)

```
Command: agreementId (String)
Result:  void
```

**Flow:**
1. `agreementRepository.findById(agreementId)` → throw `AGREEMENT_NOT_FOUND`
2. `agreement.expire()` — domain throw `AGREEMENT_INVALID_STATUS` nếu status != ACTIVE
3. `agreementRepository.save(agreement)`
4. Publish `OccupancyAgreementTerminatedEvent` — **dùng cùng event class với UC-009**

---

### UC-011 — FindAgreementsByAsset (`find_by_asset/`)

```
Query:  assetId (String), status (OccupancyAgreementStatus nullable — filter)
Result: List<AgreementView>
```

**Flow:**
1. `agreementRepository.findByAssetId(assetId)` → map sang `AgreementView`
2. Nếu `status != null` → filter in-memory (list nhỏ, không cần query riêng)

---

### UC-012 — FindAgreementsByParty (`find_by_party/`)

```
Query:  partyId (String)
Result: List<AgreementView>
```

**Flow:** `agreementRepository.findByPartyId(partyId)` → map sang `AgreementView`

`AgreementView` record (khai báo 1 lần ở package `application/agreement/`):
```
{ id, partyId, partyType, assetId, agreementType, status, startDate, endDate, contractRef }
```

---

## Event dispatch

Follow pattern của party-service: `EventDispatcher.dispatch(event)` sau khi persist, cùng transaction.

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`docs/development/260416_01_design_party_model/property_service_plan.md`** — tick `[x]` tất cả items trong mục **Phase 2 — 2.3 Application layer**; cập nhật bảng Status: Phase 2 → `[x] Completed`
- **`services/property/SERVICE_MAP.md`** — cập nhật section **Application Layer**: thêm 6 agreement slices
- **`services/property/docs/use-cases/UC-000_index.md`** — cập nhật UC-007 → UC-012 thành `Implemented`

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi xong và `mvn clean compile -DskipTests` pass, cung cấp:

### PRESENTATION CONTEXT BLOCK
- Package paths thực tế của tất cả handlers
- `AgreementView` — full record fields
- Command record fields thực tế của từng use case
- Error codes được throw từ mỗi handler

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
