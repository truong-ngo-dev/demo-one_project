# Log: Party Service Phase 2 — Presentation Layer (PartyRelationship)

**Date**: 2026-04-19  
**Status**: ✅ Compile pass — `mvn clean compile -DskipTests` BUILD SUCCESS (73 source files)

---

## Files tạo mới

| File | Package |
|------|---------|
| `AddMemberRequest.java` | `presentation.party_relationship.model` |
| `PartyRelationshipController.java` | `presentation.party_relationship` |

## Files cập nhật

- `plan.md` — tick [x] 2.4 Presentation layer; Phase 2 Status → `[x] Completed`
- `SERVICE_MAP.md` — Presentation Layer: thêm `PartyRelationshipController`; Domain Events: `MemberAddedEvent`, `MemberRemovedEvent` → `✅ Emitted`

---

## Endpoints thực tế

| Method | Path | UC | Response |
|--------|------|----|----------|
| `POST` | `/api/v1/party-relationships` | UC-010 AddMember | `201 { data: { id } }` |
| `DELETE` | `/api/v1/party-relationships/{id}` | UC-011 RemoveMember | `204 No Content` |
| `GET` | `/api/v1/party-relationships?partyId=&direction=` | UC-012 FindRelationshipsByParty | `200 { data: [...] }` |

---

## Deviation

- `startDate` nullable trong `AddMemberRequest` — default `LocalDate.now()` xử lý tại controller trước khi truyền vào `AddMember.Command`
- Reuse `PartyController.IdResponse` cho POST response thay vì tạo record mới — giảm duplication
- `direction` param default `"BOTH"` tại controller; handler chấp nhận case-insensitive
