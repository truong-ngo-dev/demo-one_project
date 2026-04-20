# Prompt: Admin Service IAM — Phase 6.1: Domain Change (linkPartyId)

**Vai trò**: Bạn là Senior Backend Engineer thêm 1 behavior nhỏ vào `User` aggregate trong `services/admin`.

> **Thứ tự implement**: Phases 1–5 đã xong. Phase 6 là Operator Portal — bắt đầu từ domain change nhỏ này.

**Yêu cầu**: Tất cả phase trước (1–5) đã xong và compile pass.

---

## Tài liệu căn cứ

1. Convention: @docs/conventions/ddd-structure.md
2. Implementation plan: @docs/development/260416_01_design_party_model/admin_iam_plan.md (Phase 6.1)
3. Service overview: @services/admin/CLAUDE.md

## Files cần sửa

- `services/admin/src/main/java/.../domain/user/User.java`
- `services/admin/src/main/java/.../domain/user/UserErrorCode.java`

Base package: `vn.truongngo.apartcom.one.service.admin`

---

## Context hiện tại

### User — fields liên quan

```java
private final String partyId;  // nullable, immutable after set (final)
```

> Lưu ý: `partyId` hiện là `final`. Để `linkPartyId()` work, cần đổi thành non-final và thêm setter nội bộ. Không expose setter public.

### UserErrorCode — codes hiện có

Codes đã dùng: 10001–10018. Code mới phải bắt đầu từ 10019.

---

## Nhiệm vụ cụ thể

### 1. `User.java` — thêm method `linkPartyId`

```java
public void linkPartyId(String partyId) {
    Assert.hasText(partyId, "partyId is required");
    if (this.partyId != null) {
        throw new DomainException(UserErrorCode.PARTY_ID_ALREADY_SET);
    }
    this.partyId = partyId;
    this.updatedAt = Instant.now();
}
```

> **Lưu ý**: `partyId` field phải đổi từ `final` sang non-final để method này work. Kiểm tra lại constructor private và `reconstitute()` — vẫn assign bình thường, chỉ bỏ `final` keyword.

### 2. `UserErrorCode.java` — thêm 2 error codes mới

| Enum                   | Code  | HTTP | Message                                            |
|------------------------|-------|------|----------------------------------------------------|
| `PARTY_ID_ALREADY_SET` | 10019 | 409  | Party ID is already set for this user              |
| `BUILDING_NOT_FOUND`   | 10020 | 404  | Building not found in reference cache              |
| `ROLE_SCOPE_MISMATCH`  | 10021 | 422  | Role scope does not match the target context scope |

> 3 codes này cần cho Phase 6.2 (application layer). Thêm cả 3 ở đây cho gọn.

---

## Không implement

- Application layer, Presentation layer — để Phase 6.2 và 6.3.

---

## Cập nhật tài liệu (sau khi compile pass)

- `docs/development/260416_01_design_party_model/admin_iam_plan.md` — tick `[x]` Phase 6.1

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` pass, cung cấp:

### PHASE6_DOMAIN_CONTEXT BLOCK
- `User.linkPartyId()` — full signature
- `UserErrorCode` — 3 codes mới (enum name, code string, http)
- Deviation nếu có (ví dụ: final → non-final đã xử lý như thế nào)

---

## Output Log

Xuất log ra `log.md` trong cùng thư mục này sau khi hoàn thành.
