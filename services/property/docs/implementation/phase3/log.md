# Phase 3 Log — FindAgreementsByBuilding

## Status: COMPLETED
`mvn clean compile -DskipTests` — BUILD SUCCESS

---

## Files changed

| File | Action |
|------|--------|
| `domain/agreement/OccupancyAgreementRepository.java` | Added `findByAssetIds(List<String>)` |
| `infrastructure/persistence/agreement/OccupancyAgreementJpaRepository.java` | Added `findAllByAssetIdIn(List<String>)` |
| `infrastructure/adapter/repository/agreement/OccupancyAgreementPersistenceAdapter.java` | Implemented `findByAssetIds` |
| `application/agreement/find_by_building/FindAgreementsByBuilding.java` | Created |
| `presentation/agreement/OccupancyAgreementController.java` | Added `buildingId` param to `GET /api/v1/agreements` |

## Endpoint

| Method | Path | Params | Response |
|--------|------|--------|----------|
| `GET` | `/api/v1/agreements` | `?buildingId=` (required), `?status=` (optional) | `200 { data: List<AgreementView> }` |

> Tích hợp vào endpoint hiện có thay vì tạo path mới — `buildingId` được check trước `assetId` và `partyId`.

## PHASE3_CONTEXT BLOCK

- Handler: `vn.truongngo.apartcom.one.service.property.application.agreement.find_by_building.FindAgreementsByBuilding`
- Endpoint: `GET /api/v1/agreements?buildingId={id}&status={status}` (status optional)
- JPA method: `findAllByAssetIdIn(List<String> assetIds)` trong `OccupancyAgreementJpaRepository`

## Deviations

None — implemented exactly per spec.
