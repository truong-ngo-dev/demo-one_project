# Prompt: Party Service Phase 2 — Domain Layer (PartyRelationship)

**Vai trò**: Bạn là Senior Backend Engineer implement Domain Layer cho aggregate `PartyRelationship`. Phase 1 (Party, Person, Organization, Household) đã hoàn thành và compile pass. Nhiệm vụ này: tạo toàn bộ domain layer cho `party_relationship/`.

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md
2. Service overview: @services/party/CLAUDE.md
3. Schema & business rules: @docs/development/260416_01_design_party_model/01_party_service.md (Section 3, 6)
4. Implementation plan: @docs/development/260416_01_design_party_model/plan.md (Phase 2 — 2.1 Domain layer)

## Files tham khảo pattern (Phase 1 đã implement)

- Typed ID: `domain/party/PartyId.java`
- AR pattern: `domain/party/Party.java`
- Error code + exception: `domain/party/PartyErrorCode.java`, `domain/party/PartyException.java`
- Repository port: `domain/party/PartyRepository.java`
- Domain event: `domain/party/event/PersonCreatedEvent.java`

Base package: `vn.truongngo.apartcom.one.service.party`

## Context từ Phase 1 (đã implement — dùng trực tiếp)

```
### Phase 1 Domain — packages sẵn có

PartyId       — domain.party.PartyId    (extends AbstractId<String>)
PartyType     — domain.party.PartyType  (PERSON, ORGANIZATION, HOUSEHOLD)
PartyStatus   — domain.party.PartyStatus
OrgType       — domain.organization.OrgType (BQL, TENANT, VENDOR, OTHER)

PartyRepository.findById(PartyId) → Optional<Party>

### libs/common base classes dùng cho Phase 2

AbstractAggregateRoot<ID>    — extend cho AR
AbstractId<String>            — extend cho typed ID
AbstractDomainEvent           — extend cho events
Repository<T, ID>             — extend cho repository port
EventDispatcher               — inject tại application layer
Assert                        — precondition checks

### Error code convention
- Phase 1 dùng codes 20001–20009
- Phase 2 dùng codes 20101–20109 (tránh clash)
```

---

## Nhiệm vụ cụ thể

Package gốc: `domain/party_relationship/`

### 1. Value Object — `PartyRelationshipId`

```java
public class PartyRelationshipId extends AbstractId<String>
```
- Constructor private, factory: `PartyRelationshipId.of(String)`, `PartyRelationshipId.generate()`
- Generate dùng `UUID.randomUUID().toString()`

### 2. Enums

```java
enum PartyRelationshipType  { MEMBER_OF, EMPLOYED_BY }
enum PartyRoleType          { MEMBER, HEAD, EMPLOYEE, EMPLOYER }
enum PartyRelationshipStatus { ACTIVE, ENDED }
```

### 3. Aggregate Root — `PartyRelationship`

Fields:
```
PartyRelationshipId id
PartyId             fromPartyId
PartyId             toPartyId
PartyRelationshipType type
PartyRoleType        fromRole
PartyRoleType        toRole
PartyRelationshipStatus status
LocalDate            startDate
LocalDate            endDate      (nullable)
```

Factory methods:
```java
static PartyRelationship create(PartyId fromPartyId, PartyId toPartyId,
                                PartyRelationshipType type,
                                PartyRoleType fromRole, PartyRoleType toRole,
                                LocalDate startDate)
    // id = generate, status = ACTIVE, endDate = null

static PartyRelationship reconstitute(PartyRelationshipId id, PartyId fromPartyId,
                                      PartyId toPartyId, PartyRelationshipType type,
                                      PartyRoleType fromRole, PartyRoleType toRole,
                                      PartyRelationshipStatus status,
                                      LocalDate startDate, LocalDate endDate)
```

Behaviors:
```java
void end(LocalDate endDate)
    // Guard: status == ENDED → throw PartyRelationshipException.alreadyEnded()
    // Set status = ENDED, this.endDate = endDate
```

### 4. Repository Port — `PartyRelationshipRepository`

```java
public interface PartyRelationshipRepository extends Repository<PartyRelationship, PartyRelationshipId> {
    List<PartyRelationship> findByFromPartyId(PartyId fromPartyId);
    List<PartyRelationship> findByToPartyId(PartyId toPartyId);
    boolean existsActiveByFromAndTo(PartyId fromPartyId, PartyId toPartyId, PartyRelationshipType type);
}
```

> `List<>` là `java.util.List` — không phải Spring Data Page. FindRelationshipsByParty không cần pagination ở Phase 2.

### 5. Error Code — `PartyRelationshipErrorCode`

```java
public enum PartyRelationshipErrorCode implements ErrorCode {
    RELATIONSHIP_NOT_FOUND       ("20101", "Relationship not found",          404),
    RELATIONSHIP_ALREADY_ENDED   ("20102", "Relationship already ended",       422),
    MEMBER_ALREADY_IN_GROUP      ("20103", "Member already in group",          409),
    INVALID_FROM_PARTY_TYPE      ("20104", "From party must be a Person",      422),
    INVALID_TO_PARTY_TYPE        ("20105", "To party must be Household or non-BQL Organization", 422),
}
```

Implement `ErrorCode` từ `libs/common`: `code()`, `defaultMessage()`, `httpStatus()` (trả int).

### 6. Exception — `PartyRelationshipException`

```java
public class PartyRelationshipException extends DomainException {
    // static factories:
    notFound(), alreadyEnded(), memberAlreadyInGroup(), invalidFromPartyType(), invalidToPartyType()
}
```

### 7. Domain Events

Package: `domain/party_relationship/event/`

**MemberAddedEvent**:
```java
// extends AbstractDomainEvent
// aggregateId = relationshipId.getValue()
// Fields: String relationshipId, String personId, String groupId, PartyType groupType
constructor(PartyRelationshipId relId, PartyId personId, PartyId groupId, PartyType groupType)
```

**MemberRemovedEvent**:
```java
// Fields: String relationshipId, String personId, String groupId
constructor(PartyRelationshipId relId, PartyId personId, PartyId groupId)
```

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`docs/development/260416_01_design_party_model/plan.md`** — tick `[x]` tất cả items trong **2.1 Domain layer**
- **`services/party/SERVICE_MAP.md`** — cập nhật section **Domain Layer**: thêm row cho `PartyRelationship`

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` pass, cung cấp:

### INFRASTRUCTURE CONTEXT BLOCK
- Package paths thực tế của tất cả classes
- `PartyRelationship` field list với exact Java types
- `PartyRelationshipRepository` method signatures thực tế
- Error code values thực tế (code string + httpStatus)
- Event constructor signatures

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
