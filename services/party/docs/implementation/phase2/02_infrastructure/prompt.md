# Prompt: Party Service Phase 2 — Infrastructure Layer (PartyRelationship)

**Vai trò**: Bạn là Senior Backend Engineer implement Infrastructure Layer cho aggregate `PartyRelationship`. Domain layer (Phase 2 — 2.1) đã xong. Nhiệm vụ: migration SQL, JPA entity, repository, mapper, persistence adapter.

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md
2. Schema: @docs/development/260416_01_design_party_model/01_party_service.md (Section 3)
3. Implementation plan: @docs/development/260416_01_design_party_model/plan.md (Phase 2 — 2.2)

## Files tham khảo pattern (Phase 1)

- JPA entity pattern: `infrastructure/persistence/party/PartyJpaEntity.java`
- JPA repository pattern: `infrastructure/persistence/party/PartyJpaRepository.java`
- Mapper pattern (static): `infrastructure/persistence/party/PartyMapper.java`
- Adapter pattern (upsert): `infrastructure/adapter/repository/party/PartyPersistenceAdapter.java`
- Migration: `src/main/resources/db/migration/V1__create_party_tables.sql`

Base package: `vn.truongngo.apartcom.one.service.party`

## Context từ 01_domain Phase 2

```
### Phase 2 Domain — packages thực tế

**Base package**: `vn.truongngo.apartcom.one.service.party`

#### Package `domain.party_relationship`

| Class | Ghi chú |
|-------|---------|
| `PartyRelationshipId` | `extends AbstractId<String> implements Id<String>` — factory: `of(String)`, `generate()` |
| `PartyRelationshipType` | enum `MEMBER_OF`, `EMPLOYED_BY` |
| `PartyRoleType` | enum `MEMBER`, `HEAD`, `EMPLOYEE`, `EMPLOYER` |
| `PartyRelationshipStatus` | enum `ACTIVE`, `ENDED` |
| `PartyRelationship` | AR — all fields immutable except `status`, `endDate` |
| `PartyRelationshipRepository` | port — extends `Repository<PartyRelationship, PartyRelationshipId>` |
| `PartyRelationshipErrorCode` | enum implements `ErrorCode` |
| `PartyRelationshipException` | extends `DomainException` |

#### `PartyRelationship` — exact field types

```java
private final PartyRelationshipId id        // from super
private final PartyId             fromPartyId
private final PartyId             toPartyId
private final PartyRelationshipType type
private final PartyRoleType        fromRole
private final PartyRoleType        toRole
private       PartyRelationshipStatus status  // MUTABLE — changes on end()
private final LocalDate            startDate
private       LocalDate            endDate    // MUTABLE, nullable
```

Getters: `getId()`, `getFromPartyId()`, `getToPartyId()`, `getType()`, `getFromRole()`,
`getToRole()`, `getStatus()`, `getStartDate()`, `getEndDate()`

Factory: `PartyRelationship.create(fromPartyId, toPartyId, type, fromRole, toRole, startDate)`
`PartyRelationship.reconstitute(id, fromPartyId, toPartyId, type, fromRole, toRole, status, startDate, endDate)`

#### `PartyRelationshipRepository` — method signatures

```java
Optional<PartyRelationship> findById(PartyRelationshipId id)    // từ Repository base
void save(PartyRelationship rel)                                  // từ Repository base
void delete(PartyRelationshipId id)                              // từ Repository base
List<PartyRelationship> findByFromPartyId(PartyId fromPartyId)
List<PartyRelationship> findByToPartyId(PartyId toPartyId)
boolean existsActiveByFromAndTo(PartyId fromPartyId, PartyId toPartyId, PartyRelationshipType type)
```

#### `PartyRelationshipErrorCode` — full values

```
RELATIONSHIP_NOT_FOUND     ("20101", 404)
RELATIONSHIP_ALREADY_ENDED ("20102", 422)
MEMBER_ALREADY_IN_GROUP    ("20103", 409)
INVALID_FROM_PARTY_TYPE    ("20104", 422)
INVALID_TO_PARTY_TYPE      ("20105", 422)
```

#### Domain Events — constructor signatures

```java
MemberAddedEvent(PartyRelationshipId relId, PartyId personId, PartyId groupId, PartyType groupType)
    // aggregateId = relId.getValue()
    // fields (String): relationshipId, personId, groupId; (PartyType): groupType

MemberRemovedEvent(PartyRelationshipId relId, PartyId personId, PartyId groupId)
    // fields (String): relationshipId, personId, groupId
```

#### Mapper note — mutable vs immutable fields

Khi implement mapper cho persistence adapter:
- `toEntity()` — map tất cả fields (lần insert)
- `updateEntity(existing, domain)` — chỉ update `status` và `endDate` (các field khác immutable sau create)
- `toDomain()` — dùng `PartyRelationship.reconstitute(...)` với tất cả fields

#### PartyId (từ Phase 1 — dùng cho FK columns)

`PartyId.of(String)` — `getValue()` trả `String` (UUID dạng string)
```

---

## Nhiệm vụ cụ thể

### 1. Migration — `V2__create_party_relationship_tables.sql`

```sql
CREATE TABLE party_relationship (
    id              VARCHAR(36) NOT NULL,
    from_party_id   VARCHAR(36) NOT NULL,
    to_party_id     VARCHAR(36) NOT NULL,
    type            ENUM('MEMBER_OF', 'EMPLOYED_BY') NOT NULL,
    from_role       ENUM('MEMBER', 'HEAD', 'EMPLOYEE', 'EMPLOYER') NOT NULL,
    to_role         ENUM('MEMBER', 'HEAD', 'EMPLOYEE', 'EMPLOYER') NOT NULL,
    status          ENUM('ACTIVE', 'ENDED') NOT NULL DEFAULT 'ACTIVE',
    start_date      DATE NOT NULL,
    end_date        DATE,
    PRIMARY KEY (id),
    CONSTRAINT fk_rel_from FOREIGN KEY (from_party_id) REFERENCES party (id),
    CONSTRAINT fk_rel_to   FOREIGN KEY (to_party_id)   REFERENCES party (id)
);
```

> Không có `employment` hay `position_assignment` trong V2 — đó là Phase 3 (V3).

### 2. JPA Entity — `PartyRelationshipJpaEntity`

Package: `infrastructure/persistence/party_relationship/`

```
@Entity @Table(name = "party_relationship")
@Getter @Setter @NoArgsConstructor
Fields: String id, String fromPartyId, String toPartyId,
        @Enumerated(EnumType.STRING) PartyRelationshipType type,
        @Enumerated(EnumType.STRING) PartyRoleType fromRole,
        @Enumerated(EnumType.STRING) PartyRoleType toRole,
        @Enumerated(EnumType.STRING) PartyRelationshipStatus status,
        LocalDate startDate, LocalDate endDate
```

> PK = `id` (không có `@GeneratedValue` — ID tự sinh tại domain layer).

### 3. JPA Repository — `PartyRelationshipJpaRepository`

```java
public interface PartyRelationshipJpaRepository extends JpaRepository<PartyRelationshipJpaEntity, String> {
    List<PartyRelationshipJpaEntity> findByFromPartyId(String fromPartyId);
    List<PartyRelationshipJpaEntity> findByToPartyId(String toPartyId);
    boolean existsByFromPartyIdAndToPartyIdAndTypeAndStatus(
            String fromPartyId, String toPartyId,
            PartyRelationshipType type, PartyRelationshipStatus status);
}
```

### 4. Mapper — `PartyRelationshipMapper`

Static class với 3 methods:
- `toDomain(PartyRelationshipJpaEntity)` → `PartyRelationship`
- `toEntity(PartyRelationship)` → `PartyRelationshipJpaEntity`
- `updateEntity(PartyRelationshipJpaEntity existing, PartyRelationship domain)` → void (chỉ update status và endDate vì các field khác immutable sau create)

> Các fields `fromPartyId`, `toPartyId`, `type`, `fromRole`, `toRole`, `startDate` không thay đổi sau khi create — chỉ `status` và `endDate` thay đổi khi `end()` được gọi.

### 5. Adapter — `PartyRelationshipPersistenceAdapter`

Package: `infrastructure/adapter/repository/party_relationship/`

Implements `PartyRelationshipRepository`. Dùng **upsert pattern** như Phase 1 (check exists → updateEntity hoặc toEntity).

Method `existsActiveByFromAndTo(fromPartyId, toPartyId, type)`:
```java
return jpaRepository.existsByFromPartyIdAndToPartyIdAndTypeAndStatus(
    fromPartyId.getValue(), toPartyId.getValue(), type, PartyRelationshipStatus.ACTIVE);
```

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`plan.md`** — tick `[x]` tất cả items trong **2.2 Infrastructure layer**
- **`SERVICE_MAP.md`** — cập nhật section **Infrastructure Layer**: thêm PartyRelationship persistence classes

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` pass, cung cấp:

### APPLICATION CONTEXT BLOCK
- Package paths thực tế của tất cả infra classes
- `PartyRelationshipJpaRepository` method names chính xác (derived query names dài — quan trọng để application layer biết)
- Adapter bean name (`@Component`)
- Xác nhận upsert pattern hoạt động (status + endDate mutable, các field khác immutable)

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
