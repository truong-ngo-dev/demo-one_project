# Prompt: Property Service Phase 2 — Domain Layer

**Vai trò**: Bạn là Senior Backend Engineer implement Domain Layer Phase 2 cho `services/property`. Phase 1 (FixedAsset) đã xong. Phase 2: `OccupancyAgreement` aggregate — lifecycle phức tạp hơn, có invariants và events quan trọng.

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md
2. Service overview: @services/property/CLAUDE.md
3. Aggregate boundaries + schema: @docs/development/260416_01_design_party_model/02_property_service.md
4. Implementation plan: @docs/development/260416_01_design_party_model/property_service_plan.md (Phase 2 — 2.1 Domain layer)
5. Domain doc: @services/property/docs/domains/occupancy_agreement.md

## Files tham khảo pattern

- Pattern AR + typed ID: `services/party/src/main/java/.../domain/party/Party.java` và `PartyId.java`
- Pattern domain event: `services/party/src/main/java/.../domain/party/event/PersonCreatedEvent.java`
- Pattern domain exception: `services/party/src/main/java/.../domain/party/PartyException.java`

Base package: `vn.truongngo.apartcom.one.service.property`

---

## Nhiệm vụ cụ thể

Tất cả file nằm trong `services/property/src/main/java/vn/truongngo/apartcom/one/service/property/domain/`.

### 1. Value Objects & Enums (`agreement/`)

- `OccupancyAgreementId.java` — typed UUID record
- `OccupancyAgreementType.java` — enum: `OWNERSHIP`, `LEASE`
- `OccupancyAgreementStatus.java` — enum: `PENDING`, `ACTIVE`, `TERMINATED`, `EXPIRED`
- `PartyType.java` — enum local: `PERSON`, `HOUSEHOLD`, `ORGANIZATION`
  > **Quan trọng**: Đây là enum của property-service, không import từ party-service. Tên package: `domain.agreement`.

### 2. Aggregate Root: OccupancyAgreement (`agreement/OccupancyAgreement.java`)

Fields:
- `id` (OccupancyAgreementId)
- `partyId` (String — ref → party-service, không validate tồn tại)
- `partyType` (PartyType)
- `assetId` (String — ref → fixed_asset)
- `agreementType` (OccupancyAgreementType — immutable)
- `status` (OccupancyAgreementStatus)
- `startDate` (LocalDate)
- `endDate` (LocalDate — nullable, null khi OWNERSHIP)
- `contractRef` (String — nullable)
- `createdAt` (Instant — immutable)
- `updatedAt` (Instant)

Factory + behaviors:

```java
// create() nhận assetType để validate I4/I5/I6/I7 — KHÔNG lưu assetType vào aggregate
static OccupancyAgreement create(String partyId, PartyType partyType, String assetId,
                                  FixedAssetType assetType, OccupancyAgreementType agreementType,
                                  LocalDate startDate, LocalDate endDate, String contractRef)
```

**Validate trong create() (domain invariants):**
- `[I4]` LEASE: `assetType` phải là `RESIDENTIAL_UNIT` hoặc `COMMERCIAL_SPACE`
- `[I5]` OWNERSHIP: `assetType` phải là `RESIDENTIAL_UNIT`
- `[I6]` OWNERSHIP: `endDate` phải null; `partyType` phải là `PERSON`
- `[I7]` LEASE: `endDate` bắt buộc; nếu `assetType=RESIDENTIAL_UNIT` → `partyType` phải là `PERSON` hoặc `HOUSEHOLD`; nếu `assetType=COMMERCIAL_SPACE` → `partyType` phải là `ORGANIZATION`

```java
static OccupancyAgreement reconstitute(OccupancyAgreementId id, String partyId, PartyType partyType,
                                        String assetId, OccupancyAgreementType agreementType,
                                        OccupancyAgreementStatus status, LocalDate startDate,
                                        LocalDate endDate, String contractRef,
                                        Instant createdAt, Instant updatedAt)

void activate()    // PENDING → ACTIVE; throw AGREEMENT_INVALID_STATUS nếu status != PENDING
void terminate()   // ACTIVE → TERMINATED; throw AGREEMENT_INVALID_STATUS nếu status != ACTIVE
void expire()      // ACTIVE → EXPIRED; throw AGREEMENT_INVALID_STATUS nếu status != ACTIVE
```

`activate()`, `terminate()`, `expire()` **không raise event trực tiếp** — application layer raise sau khi save (follow party pattern với EventDispatcher).

### 3. Repository port (`agreement/OccupancyAgreementRepository.java`)

```java
Optional<OccupancyAgreement> findById(OccupancyAgreementId id);
boolean existsActiveByAssetIdAndType(String assetId, OccupancyAgreementType type);  // check I1/I2
List<OccupancyAgreement> findByAssetId(String assetId);
List<OccupancyAgreement> findByPartyId(String partyId);
OccupancyAgreement save(OccupancyAgreement agreement);
```

### 4. Domain Events (`agreement/event/`)

- `OccupancyAgreementActivatedEvent.java`
  - fields: `agreementId` (OccupancyAgreementId), `partyId` (String), `partyType` (PartyType), `assetId` (String), `agreementType` (OccupancyAgreementType)
- `OccupancyAgreementTerminatedEvent.java` — cùng fields
  > Dùng chung cho cả TERMINATED và EXPIRED — consumer dùng status thực tế nếu cần phân biệt.

### 5. Exception & Error Codes (`agreement/`)

`OccupancyAgreementErrorCode.java`:
- `AGREEMENT_NOT_FOUND` (404)
- `AGREEMENT_INVALID_STATUS` (422)
- `OWNERSHIP_ALREADY_EXISTS` (409)
- `LEASE_ALREADY_EXISTS` (409)
- `INVALID_ASSET_TYPE_FOR_LEASE` (422)
- `INVALID_ASSET_TYPE_FOR_OWNERSHIP` (422)
- `INVALID_PARTY_TYPE_FOR_OWNERSHIP` (422)
- `INVALID_PARTY_TYPE_FOR_UNIT` (422)
- `END_DATE_REQUIRED_FOR_LEASE` (422)

`OccupancyAgreementException.java` — extends `DomainException` từ `libs/common`

**Không implement**: Infrastructure, Application, Presentation, Spring annotations trong domain.

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`docs/development/260416_01_design_party_model/property_service_plan.md`** — tick `[x]` tất cả items trong mục **Phase 2 — 2.1 Domain layer**
- **`services/property/SERVICE_MAP.md`** — cập nhật section **Domain Layer**: thêm OccupancyAgreement aggregate

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi xong và `mvn clean compile -DskipTests` pass, cung cấp:

### INFRASTRUCTURE CONTEXT BLOCK
- Package paths thực tế của tất cả files đã tạo
- Method signatures thực tế trên `OccupancyAgreementRepository`
- `OccupancyAgreementErrorCode` — full enum values
- Bất kỳ deviation nào so với spec này
- Lưu ý mapper: field nào nullable, `assetType` không được persist (chỉ dùng trong create)

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
