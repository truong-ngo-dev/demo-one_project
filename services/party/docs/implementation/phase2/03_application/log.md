# Log: Party Service Phase 2 — Application Layer (PartyRelationship)

**Date**: 2026-04-19  
**Status**: ✅ Compile pass — `mvn clean compile -DskipTests` BUILD SUCCESS (71 source files)

---

## Files tạo mới

| File | Package |
|------|---------|
| `AddMember.java` | `application.party_relationship.add_member` |
| `RemoveMember.java` | `application.party_relationship.remove_member` |
| `FindRelationshipsByParty.java` | `application.party_relationship.find_by_party` |

## Files cập nhật

- `plan.md` — tick [x] tất cả mục 2.3
- `SERVICE_MAP.md` — Application Layer thêm 3 slices Phase 2
- `UC-000_index.md` — UC-010, UC-011, UC-012 → `Implemented`

## Deviation

- `FindRelationshipsByParty.Handler.merge()` dùng `LinkedHashMap` để dedup theo id khi direction=`BOTH`, giữ insertion order (FROM trước, TO sau)
- direction matching dùng `toUpperCase()` — chấp nhận cả `"both"` lẫn `"BOTH"`

---

## PRESENTATION CONTEXT BLOCK

> Paste block này vào `[PRESENTATION CONTEXT BLOCK]` trong `04_presentation/prompt.md`

```
### Phase 2 Application — packages thực tế

**Base package**: `vn.truongngo.apartcom.one.service.party`

#### Handlers

| Handler | Package | Type |
|---------|---------|------|
| `AddMember.Handler` | `application.party_relationship.add_member` | `CommandHandler<AddMember.Command, AddMember.Result>` |
| `RemoveMember.Handler` | `application.party_relationship.remove_member` | `CommandHandler<RemoveMember.Command, Void>` |
| `FindRelationshipsByParty.Handler` | `application.party_relationship.find_by_party` | `QueryHandler<FindRelationshipsByParty.Query, List<RelationshipView>>` |

#### Command / Query Records

**AddMember.Command + Result**:
```java
record Command(String personId, String groupId, PartyRoleType fromRole, LocalDate startDate)
record Result(String relationshipId)
```

**RemoveMember.Command**:
```java
record Command(String relationshipId)
// returns Void (null)
```

**FindRelationshipsByParty.Query + RelationshipView**:
```java
record Query(String partyId, String direction)  // direction: "FROM" | "TO" | "BOTH" (case-insensitive)

record RelationshipView(
    String id,
    String fromPartyId,
    String toPartyId,
    PartyRelationshipType type,
    PartyRoleType fromRole,
    PartyRoleType toRole,
    PartyRelationshipStatus status,
    LocalDate startDate,
    LocalDate endDate             // nullable
)
```

#### Error codes thrown từ mỗi handler

| Handler | Errors thrown |
|---------|--------------|
| `AddMember` | `PARTY_NOT_FOUND` (404), `INVALID_FROM_PARTY_TYPE` (422), `INVALID_TO_PARTY_TYPE` (422), `ORGANIZATION_NOT_FOUND` (404), `MEMBER_ALREADY_IN_GROUP` (409) |
| `RemoveMember` | `RELATIONSHIP_NOT_FOUND` (404), `RELATIONSHIP_ALREADY_ENDED` (422) |
| `FindRelationshipsByParty` | — |

#### Event dispatch

Pattern không thay đổi so với Phase 1:
- `EventDispatcher` injected vào `AddMember.Handler` và `RemoveMember.Handler`
- `eventDispatcher.dispatch(event)` gọi synchronously trong `@Transactional`
- Controller không cần dispatch event

#### Domain types cần biết ở controller

```java
// Từ domain.party_relationship
PartyRoleType   — MEMBER, HEAD, EMPLOYEE, EMPLOYER
PartyRelationshipType   — MEMBER_OF, EMPLOYED_BY
PartyRelationshipStatus — ACTIVE, ENDED
```
```
