# Phase 6.1 Log — Domain Change (linkPartyId)

## Status: COMPLETED
`mvn clean compile -DskipTests` — BUILD SUCCESS

---

## Files changed

| File                             | Action                                                              |
|----------------------------------|---------------------------------------------------------------------|
| `domain/user/User.java`          | `partyId` đổi từ `final` sang non-final; thêm `linkPartyId(String)` |
| `domain/user/UserErrorCode.java` | Thêm 3 error codes mới (10019, 10020, 10021)                        |

---

## PHASE6_DOMAIN_CONTEXT BLOCK

### `User.linkPartyId()` signature
```java
public void linkPartyId(String partyId);
// Assert.hasText → throw PARTY_ID_ALREADY_SET nếu this.partyId != null
// sets this.partyId + this.updatedAt = Instant.now()
```

### UserErrorCode — 3 codes mới
| Enum                   | Code  | HTTP |
|------------------------|-------|------|
| `PARTY_ID_ALREADY_SET` | 10019 | 409  |
| `BUILDING_NOT_FOUND`   | 10020 | 404  |
| `ROLE_SCOPE_MISMATCH`  | 10021 | 422  |

### Deviation
- `partyId` field đổi từ `final` → non-final. Tất cả constructor và `reconstitute()` vẫn assign bình thường — chỉ bỏ `final` keyword. Không ảnh hưởng behavior hiện có.
