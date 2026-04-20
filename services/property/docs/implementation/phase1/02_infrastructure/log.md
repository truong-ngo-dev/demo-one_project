# Log: Property Service Phase 1 — Infrastructure Layer

## Status: ✅ Completed — `mvn clean compile -DskipTests` PASS

---

## Files tạo mới

| File | Package / Path |
|------|----------------|
| `V1__create_fixed_asset_table.sql` | `src/main/resources/db/migration/` |
| `FixedAssetJpaEntity.java` | `infrastructure.persistence.fixed_asset` |
| `FixedAssetJpaRepository.java` | `infrastructure.persistence.fixed_asset` |
| `FixedAssetMapper.java` | `infrastructure.persistence.fixed_asset` |
| `FixedAssetPersistenceAdapter.java` | `infrastructure.adapter.repository.fixed_asset` |

---

## Deviations

Không có deviation so với spec.

---

## APPLICATION CONTEXT BLOCK

### Package paths

```
vn.truongngo.apartcom.one.service.property.infrastructure.persistence.fixed_asset.FixedAssetJpaEntity
vn.truongngo.apartcom.one.service.property.infrastructure.persistence.fixed_asset.FixedAssetJpaRepository
vn.truongngo.apartcom.one.service.property.infrastructure.persistence.fixed_asset.FixedAssetMapper
vn.truongngo.apartcom.one.service.property.infrastructure.adapter.repository.fixed_asset.FixedAssetPersistenceAdapter
```

### JPA query methods trên FixedAssetJpaRepository

```java
// Kế thừa từ JpaRepository:
Optional<FixedAssetJpaEntity> findById(String id);
FixedAssetJpaEntity save(FixedAssetJpaEntity entity);
void deleteById(String id);

// Derived queries thêm:
List<FixedAssetJpaEntity> findAllByPathStartingWith(String pathPrefix);
List<FixedAssetJpaEntity> findAllByParentId(String parentId);
```

### FixedAssetPersistenceAdapter — method signatures

```java
Optional<FixedAsset> findById(FixedAssetId id)         // dùng id.getValue()
void save(FixedAsset asset)                             // upsert: check exists → updateEntity or toEntity
void delete(FixedAssetId id)                           // dùng id.getValue()
List<FixedAsset> findByPathPrefix(String pathPrefix)   // delegate → findAllByPathStartingWith
```

### FixedAssetMapper — updateEntity scope

`updateEntity` chỉ cập nhật các field mutable:
- `name`, `code`, `sequenceNo`, `status`, `updatedAt`

Fields **không** update (immutable): `id`, `parentId`, `path`, `type`, `managingOrgId`, `createdAt`

### Transaction note

`FixedAssetPersistenceAdapter` **không có** `@Transactional` — application layer quản lý transaction boundary.
