# Prompt: Party Service Phase 2 — Application Layer (PartyRelationship)

**Vai trò**: Bạn là Senior Backend Engineer implement Application Layer cho `PartyRelationship`. Domain và Infrastructure Phase 2 đã xong. Nhiệm vụ: 3 use case handlers (UC-010, UC-011, UC-012).

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md
2. Service overview: @services/party/CLAUDE.md
3. Use case index: @services/party/docs/use-cases/UC-000_index.md (UC-010 → UC-012)
4. Business rules: @docs/development/260416_01_design_party_model/01_party_service.md (Section 6)
5. Implementation plan: @docs/development/260416_01_design_party_model/plan.md (Phase 2 — 2.3)

## Files tham khảo pattern

- Command handler: `application/party/create_person/CreatePerson.java`
- Query handler: `application/party/find_by_id/FindPartyById.java`
- Event dispatch: pattern trong `CreatePerson.Handler` — inject `EventDispatcher`, call `dispatch()` sau persist

Base package: `vn.truongngo.apartcom.one.service.party`

## Context từ 02_infrastructure Phase 2@

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

## Context từ Phase 1 (dùng trực tiếp — không cần paste lại)

```
Repositories cần inject:
  PartyRepository          — domain.party (findById, existsByIdentification)
  OrganizationRepository   — domain.organization (findById để check orgType)
  PartyRelationshipRepository — domain.party_relationship (Phase 2)

EventDispatcher            — libs/common (đã có bean trong EventDispatcherConfig)

Phase 1 domain types cần dùng:
  PartyId, PartyType, OrgType
  Party.getType() → PartyType
  Organization.getOrgType() → OrgType
  PartyException.notFound(), PartyException.organizationNotFound()
```

---

## Nhiệm vụ cụ thể

Package gốc: `application/party_relationship/`

### 1. UC-010 — AddMember (`add_member/`)

```
Command: personId (String), groupId (String), fromRole (PartyRoleType), startDate (LocalDate)
Result:  relationshipId (String)
```

**Flow:**
1. Load `Party fromParty = partyRepository.findById(PartyId.of(personId))` → throw `PartyException.notFound()`
2. Validate `fromParty.getType() == PERSON` → throw `PartyRelationshipException.invalidFromPartyType()`
3. Load `Party toParty = partyRepository.findById(PartyId.of(groupId))` → throw `PartyException.notFound()`
4. Validate `toParty.getType() == HOUSEHOLD || toParty.getType() == ORGANIZATION` → throw `PartyRelationshipException.invalidToPartyType()`
5. Nếu `toParty.getType() == ORGANIZATION`:
   - Load Organization: `organizationRepository.findById(...)` → throw `PartyException.organizationNotFound()`
   - Validate `org.getOrgType() != BQL` → throw `PartyRelationshipException.invalidToPartyType()` (BQL membership qua Employment, không phải AddMember)
6. Check duplicate: `partyRelationshipRepository.existsActiveByFromAndTo(personId, groupId, MEMBER_OF)` → throw `PartyRelationshipException.memberAlreadyInGroup()`
7. `PartyRelationship rel = PartyRelationship.create(PartyId.of(personId), PartyId.of(groupId), MEMBER_OF, fromRole, PartyRoleType.MEMBER, startDate)`
8. `partyRelationshipRepository.save(rel)`
9. `eventDispatcher.dispatch(new MemberAddedEvent(rel.getId(), PartyId.of(personId), PartyId.of(groupId), toParty.getType()))`
10. Return `new Result(rel.getId().getValue())`

> `fromRole` là `MEMBER` hoặc `HEAD` — caller tự quyết. `toRole` luôn là `MEMBER`.

### 2. UC-011 — RemoveMember (`remove_member/`)

```
Command: relationshipId (String)
Result:  Void
```

**Flow:**
1. Load `PartyRelationship rel = partyRelationshipRepository.findById(PartyRelationshipId.of(relationshipId))` → throw `PartyRelationshipException.notFound()`
2. `rel.end(LocalDate.now())`  ← domain method throw `alreadyEnded()` nếu status == ENDED
3. `partyRelationshipRepository.save(rel)`
4. `eventDispatcher.dispatch(new MemberRemovedEvent(rel.getId(), rel.getFromPartyId(), rel.getToPartyId()))`

### 3. UC-012 — FindRelationshipsByParty (`find_by_party/`)

```
Query:  partyId (String), direction ("FROM" | "TO" | "BOTH")
Result: List<RelationshipView>
```

```java
public record RelationshipView(
    String id,
    String fromPartyId,
    String toPartyId,
    PartyRelationshipType type,
    PartyRoleType fromRole,
    PartyRoleType toRole,
    PartyRelationshipStatus status,
    LocalDate startDate,
    LocalDate endDate
)
```

**Flow:**
- direction = "FROM" → `partyRelationshipRepository.findByFromPartyId(PartyId.of(partyId))`
- direction = "TO"   → `partyRelationshipRepository.findByToPartyId(PartyId.of(partyId))`
- direction = "BOTH" → merge cả hai lists (dedup bằng id nếu cần)
- Map mỗi `PartyRelationship` → `RelationshipView`

> Không cần validate partyId tồn tại — query trả list rỗng nếu không có relationships.

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`plan.md`** — tick `[x]` tất cả items trong **2.3 Application layer**
- **`SERVICE_MAP.md`** — cập nhật section **Application Layer**: thêm 3 slices Phase 2
- **`UC-000_index.md`** — cập nhật UC-010, UC-011, UC-012 → `Implemented`

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` pass, cung cấp:

### PRESENTATION CONTEXT BLOCK
- Package paths thực tế của 3 handlers
- Command / Query record fields chính xác
- `RelationshipView` full record
- Error codes thrown từ mỗi handler
- Event dispatch mechanism (không thay đổi so với Phase 1 — xác nhận)

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, handoff block, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
