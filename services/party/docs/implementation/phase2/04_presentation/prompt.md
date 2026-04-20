# Prompt: Party Service Phase 2 — Presentation Layer (PartyRelationship)

**Vai trò**: Bạn là Senior Backend Engineer implement Presentation Layer cho `PartyRelationship`. Application layer Phase 2 đã xong. Nhiệm vụ: `PartyRelationshipController`.

---

## Tài liệu căn cứ

1. Convention bắt buộc: @docs/conventions/ddd-structure.md, @docs/conventions/api-design.md, @docs/conventions/error-handling.md
2. Service overview: @services/party/CLAUDE.md
3. Use case index: @services/party/docs/use-cases/UC-000_index.md (UC-010 → UC-012)
4. Implementation plan: @docs/development/260416_01_design_party_model/plan.md (Phase 2 — 2.4)

## Files tham khảo pattern (Phase 1)

- Controller: `presentation/party/PartyController.java`
- Base classes: `presentation/base/ApiResponse.java`, `PagedApiResponse.java`, `GlobalExceptionHandler.java`
- Request model: `presentation/party/model/CreatePartyRequest.java`

Base package: `vn.truongngo.apartcom.one.service.party`

## Context từ 03_application Phase 2

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

---

## Nhiệm vụ cụ thể

Package: `presentation/party_relationship/`

### 1. Request models (`presentation/party_relationship/model/`)

**`AddMemberRequest`**:
```java
record AddMemberRequest(String personId, String groupId, PartyRoleType fromRole, LocalDate startDate)
// fromRole: MEMBER hoặc HEAD
// startDate: nullable — nếu null, handler dùng LocalDate.now()
```

> Nếu `startDate` là nullable, xử lý default `LocalDate.now()` tại controller trước khi truyền vào Command.

### 2. Controller — `PartyRelationshipController`

Base path: `/api/v1/party-relationships`

| Method | Path | Use case | Response |
|--------|------|----------|----------|
| `POST` | `/party-relationships` | UC-010 AddMember | `201 { data: { id } }` |
| `DELETE` | `/party-relationships/{id}` | UC-011 RemoveMember | `204 No Content` |
| `GET` | `/party-relationships` | UC-012 FindRelationshipsByParty — params: `partyId` (required), `direction` (FROM/TO/BOTH, default BOTH) | `200 { data: [...] }` |

**POST /party-relationships** mapping:
```java
AddMember.Command command = new AddMember.Command(
    request.personId(),
    request.groupId(),
    request.fromRole(),
    request.startDate() != null ? request.startDate() : LocalDate.now()
);
```

**DELETE /party-relationships/{id}** — trả `204 No Content` (không wrap ApiResponse).

**GET /party-relationships** — trả `ApiResponse<List<RelationshipView>>`:
```java
FindRelationshipsByParty.Query query = new FindRelationshipsByParty.Query(partyId, direction != null ? direction : "BOTH");
List<RelationshipView> result = findRelationshipsByPartyHandler.handle(query);
return ResponseEntity.ok(ApiResponse.of(result));
```

### 3. Không cần tạo lại GlobalExceptionHandler hay base classes

`GlobalExceptionHandler` đã handle `DomainException` chung — `PartyRelationshipException extends DomainException` được handle tự động.

---

## Cập nhật tài liệu (thực hiện sau khi compile pass)

- **`plan.md`** — tick `[x]` tất cả items trong **2.4 Presentation layer**; cập nhật bảng Status: Phase 2 → `[x] Completed`
- **`SERVICE_MAP.md`** — cập nhật section **Presentation Layer**: thêm `PartyRelationshipController`; cập nhật **Domain Events Published**: `MemberAddedEvent`, `MemberRemovedEvent` → `✅ Emitted`; cập nhật **Internal API Endpoints** UC-009: Phase 2 stub → real implementation nếu applicable
- **`UC-000_index.md`** — UC-010, UC-011, UC-012 (đã set `Implemented` ở application layer prompt, xác nhận lại)

---

## Output Log

Sau khi hoàn thành tất cả các bước trên, xuất toàn bộ output (files đã tạo/sửa, endpoints thực tế, ghi chú deviation) ra file `log.md` trong cùng thư mục với prompt này.
