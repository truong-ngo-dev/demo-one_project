# Log: Property Service Phase 2 — Domain Layer

## Status: ✅ Completed | `mvn clean compile -DskipTests` PASS

---

## Files Created

| File                                                            | Package                                                             |
|-----------------------------------------------------------------|---------------------------------------------------------------------|
| `domain/agreement/OccupancyAgreementId.java`                    | `vn.truongngo.apartcom.one.service.property.domain.agreement`       |
| `domain/agreement/OccupancyAgreementType.java`                  | `vn.truongngo.apartcom.one.service.property.domain.agreement`       |
| `domain/agreement/OccupancyAgreementStatus.java`                | `vn.truongngo.apartcom.one.service.property.domain.agreement`       |
| `domain/agreement/PartyType.java`                               | `vn.truongngo.apartcom.one.service.property.domain.agreement`       |
| `domain/agreement/OccupancyAgreement.java`                      | `vn.truongngo.apartcom.one.service.property.domain.agreement`       |
| `domain/agreement/OccupancyAgreementRepository.java`            | `vn.truongngo.apartcom.one.service.property.domain.agreement`       |
| `domain/agreement/OccupancyAgreementErrorCode.java`             | `vn.truongngo.apartcom.one.service.property.domain.agreement`       |
| `domain/agreement/OccupancyAgreementException.java`             | `vn.truongngo.apartcom.one.service.property.domain.agreement`       |
| `domain/agreement/event/OccupancyAgreementActivatedEvent.java`  | `vn.truongngo.apartcom.one.service.property.domain.agreement.event` |
| `domain/agreement/event/OccupancyAgreementTerminatedEvent.java` | `vn.truongngo.apartcom.one.service.property.domain.agreement.event` |

---

## INFRASTRUCTURE CONTEXT BLOCK

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
