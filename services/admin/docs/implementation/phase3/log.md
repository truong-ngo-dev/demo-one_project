# Phase 3 Log — Reference Cache

## Status: COMPLETED
`mvn clean compile -DskipTests` — BUILD SUCCESS

---

## Files created

| File                                                                                   | Notes                                           |
|----------------------------------------------------------------------------------------|-------------------------------------------------|
| `src/main/resources/db/migration/V11__reference_cache.sql`                             | CREATE TABLE building_reference + org_reference |
| `domain/reference/BuildingReference.java`                                              | Plain data holder — `of()`, `reconstitute()`    |
| `domain/reference/BuildingReferenceRepository.java`                                    | Port interface                                  |
| `domain/reference/OrgReference.java`                                                   | Plain data holder — `of()`, `reconstitute()`    |
| `domain/reference/OrgReferenceRepository.java`                                         | Port interface                                  |
| `infrastructure/persistence/reference/BuildingReferenceJpaEntity.java`                 | `@Entity`, `@Id String buildingId`              |
| `infrastructure/persistence/reference/BuildingReferenceJpaRepository.java`             | extends `JpaRepository<..., String>`            |
| `infrastructure/persistence/reference/BuildingReferenceMapper.java`                    | static `toDomain`, `toEntity`                   |
| `infrastructure/persistence/reference/OrgReferenceJpaEntity.java`                      | `@Entity`, `@Id String orgId`                   |
| `infrastructure/persistence/reference/OrgReferenceJpaRepository.java`                  | extends `JpaRepository<..., String>`            |
| `infrastructure/persistence/reference/OrgReferenceMapper.java`                         | static `toDomain`, `toEntity`                   |
| `infrastructure/adapter/repository/reference/BuildingReferencePersistenceAdapter.java` | implements `BuildingReferenceRepository`        |
| `infrastructure/adapter/repository/reference/OrgReferencePersistenceAdapter.java`      | implements `OrgReferenceRepository`             |

## Deviations

- `OrgReferenceRepository` includes `Optional<OrgReference> findById(String orgId)` — Phase 5 prompt requires it for displayName resolution; added proactively per that spec.

---

## PHASE4_CONTEXT BLOCK

### Package paths
- `vn.truongngo.apartcom.one.service.admin.domain.reference.BuildingReferenceRepository`
- `vn.truongngo.apartcom.one.service.admin.domain.reference.OrgReferenceRepository`

### BuildingReferenceRepository — full signatures
```java
void upsert(BuildingReference ref);
boolean existsById(String buildingId);
Optional<BuildingReference> findById(String buildingId);
```

### OrgReferenceRepository — full signatures
```java
void upsert(OrgReference ref);
boolean existsById(String orgId);
Optional<OrgReference> findById(String orgId);
```

### BuildingReference.of() and OrgReference.of()
```java
static BuildingReference of(String buildingId, String name, String managingOrgId)
// cachedAt = Instant.now()

static OrgReference of(String orgId, String name, String orgType)
// cachedAt = Instant.now()
```
