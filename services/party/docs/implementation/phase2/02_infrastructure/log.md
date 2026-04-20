# Log: Party Service Phase 2 — Infrastructure Layer (PartyRelationship)

**Date**: 2026-04-19  
**Status**: ✅ Compile pass — `mvn clean compile -DskipTests` BUILD SUCCESS (68 source files)

---

## Files tạo mới

| File | Package |
|------|---------|
| `V2__create_party_relationship_tables.sql` | `src/main/resources/db/migration/` |
| `PartyRelationshipJpaEntity.java` | `infrastructure.persistence.party_relationship` |
| `PartyRelationshipJpaRepository.java` | `infrastructure.persistence.party_relationship` |
| `PartyRelationshipMapper.java` | `infrastructure.persistence.party_relationship` |
| `PartyRelationshipPersistenceAdapter.java` | `infrastructure.adapter.repository.party_relationship` |

## Files cập nhật

- `plan.md` — tick [x] tất cả mục 2.2
- `SERVICE_MAP.md` — Infrastructure Layer thêm Phase 2 classes

## Deviation

- Không có deviation — theo spec đúng 100%
- `updateEntity()` chỉ update `status` và `endDate` (confirmed: tất cả field khác immutable trong domain)

---

## APPLICATION CONTEXT BLOCK

> Paste block này vào `[APPLICATION CONTEXT BLOCK]` trong `03_application/prompt.md`

```
### Phase 2 Infrastructure — packages thực tế

**Base package**: `vn.truongngo.apartcom.one.service.party`

#### Adapter bean (inject qua interface)

| Bean class | Implements | Package |
|-----------|-----------|---------|
| `PartyRelationshipPersistenceAdapter` | `PartyRelationshipRepository` | `infrastructure.adapter.repository.party_relationship` |

Inject tại application layer bằng constructor injection (`@RequiredArgsConstructor`) qua interface:
```java
private final PartyRelationshipRepository partyRelationshipRepository;
```

#### `PartyRelationshipJpaRepository` — method names chính xác

```java
// Derived queries (tên dài — copy chính xác)
List<PartyRelationshipJpaEntity> findByFromPartyId(String fromPartyId)
List<PartyRelationshipJpaEntity> findByToPartyId(String toPartyId)
boolean existsByFromPartyIdAndToPartyIdAndTypeAndStatus(
    String fromPartyId, String toPartyId,
    PartyRelationshipType type, PartyRelationshipStatus status)
```

**Lưu ý**: Application layer KHÔNG gọi JpaRepository trực tiếp — gọi qua `PartyRelationshipRepository` port interface. Adapter xử lý việc unwrap `PartyId.getValue()` → `String`.

#### Domain Repository interface — method signatures để dùng trong handler

```java
// Từ PartyRelationshipRepository (domain port)
Optional<PartyRelationship> findById(PartyRelationshipId id)
void save(PartyRelationship rel)
void delete(PartyRelationshipId id)
List<PartyRelationship> findByFromPartyId(PartyId fromPartyId)   // param = PartyId (domain)
List<PartyRelationship> findByToPartyId(PartyId toPartyId)       // param = PartyId (domain)
boolean existsActiveByFromAndTo(PartyId fromPartyId, PartyId toPartyId, PartyRelationshipType type)
```

#### Upsert pattern confirmation

- `save()`: check `jpaRepository.findById(id)` → nếu tồn tại thì `updateEntity(existing, domain)` rồi `save(existing)`; nếu không thì `save(toEntity(domain))`
- `updateEntity()` chỉ set: `existing.setStatus(...)`, `existing.setEndDate(...)` — an toàn khi gọi sau `rel.end()`

#### EventDispatcher (từ Phase 1 — không thay đổi)

Bean `EventDispatcher` đã có sẵn qua `EventDispatcherConfig` trong `infrastructure.cross_cutting.config`.
Inject bình thường: `private final EventDispatcher eventDispatcher;`
```
