# Log: Party Service Phase 2 — Domain Layer (PartyRelationship)

**Date**: 2026-04-19  
**Status**: ✅ Compile pass — `mvn clean compile -DskipTests` BUILD SUCCESS (64 source files)

---

## Files tạo mới

### `domain/party_relationship/`

| File | Mô tả |
|------|-------|
| `PartyRelationshipId.java` | Typed UUID, extends `AbstractId<String> implements Id<String>` |
| `PartyRelationshipType.java` | Enum: `MEMBER_OF`, `EMPLOYED_BY` |
| `PartyRoleType.java` | Enum: `MEMBER`, `HEAD`, `EMPLOYEE`, `EMPLOYER` |
| `PartyRelationshipStatus.java` | Enum: `ACTIVE`, `ENDED` |
| `PartyRelationship.java` | AR — behavior: `end(LocalDate)` throws `alreadyEnded()` nếu đã ENDED |
| `PartyRelationshipRepository.java` | Port — extends `Repository<PartyRelationship, PartyRelationshipId>` |
| `PartyRelationshipErrorCode.java` | Enum implements `ErrorCode`, codes 20101–20105 |
| `PartyRelationshipException.java` | Extends `DomainException`, 5 static factory methods |

### `domain/party_relationship/event/`

| File | Fields |
|------|--------|
| `MemberAddedEvent.java` | `relationshipId`, `personId`, `groupId`, `groupType` (PartyType) |
| `MemberRemovedEvent.java` | `relationshipId`, `personId`, `groupId` |

## Files cập nhật

- `docs/development/260416_01_design_party_model/plan.md` — tick [x] tất cả mục 2.1
- `services/party/SERVICE_MAP.md` — Domain Layer section thêm PartyRelationship

## Deviation

- Không có deviation — theo spec đúng 100%

---

## INFRASTRUCTURE CONTEXT BLOCK

> Paste block này vào `[INFRASTRUCTURE CONTEXT BLOCK]` trong `02_infrastructure/prompt.md`

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
