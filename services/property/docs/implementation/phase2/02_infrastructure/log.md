# Log: Property Service Phase 2 — Infrastructure Layer

## Status: ✅ Completed | `mvn clean compile -DskipTests` PASS

---

## Files Created

| File                                                                                    | Package                                                                                  |
|-----------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------|
| `resources/db/migration/V2__create_occupancy_agreement_table.sql`                       | —                                                                                        |
| `infrastructure/persistence/agreement/OccupancyAgreementJpaEntity.java`                 | `vn.truongngo.apartcom.one.service.property.infrastructure.persistence.agreement`        |
| `infrastructure/persistence/agreement/OccupancyAgreementJpaRepository.java`             | `vn.truongngo.apartcom.one.service.property.infrastructure.persistence.agreement`        |
| `infrastructure/persistence/agreement/OccupancyAgreementMapper.java`                    | `vn.truongngo.apartcom.one.service.property.infrastructure.persistence.agreement`        |
| `infrastructure/adapter/repository/agreement/OccupancyAgreementPersistenceAdapter.java` | `vn.truongngo.apartcom.one.service.property.infrastructure.adapter.repository.agreement` |

---

## APPLICATION CONTEXT BLOCK

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
