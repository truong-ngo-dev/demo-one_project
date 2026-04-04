# Social Registration Flow

## Tổng quan

Khi user đăng nhập bằng social account, oauth2 service gọi Admin Service để tạo hoặc tìm user tương ứng. Admin Service trả về flag `requiresProfileCompletion` để oauth2 service quyết định redirect tiếp theo.

---

## Flow

```
oauth2-service                      admin-service                Angular
       │                                  │                          │
       │── POST /internal/users/social ──▶│                          │
       │   (provider, providerUserId,     │                          │
       │    providerEmail)                │                          │
       │                                  │                          │
       │                    [Tìm User theo providerEmail]            │
       │                                  │                          │
       │              [CÓ] user tồn tại với email này                │
       │                   upsert SocialConnection                   │
       │                   (thêm nếu provider chưa liên kết,         │
       │                    bỏ qua nếu đã liên kết)                  │
       │◀─────── 200 OK (requiresProfileCompletion: false) ──────────│
       │                                  │                          │
       │               [KHÔNG] user chưa tồn tại                     │
       │                   auto-generate username                    │
       │                   tạo User (status = ACTIVE)                │
       │                   tạo SocialConnection                      │
       │                   dispatch UserCreatedEvent                 │
       │                   dispatch SocialConnectedEvent             │
       │◀─────── 201 Created (requiresProfileCompletion: true) ──────│
       │                                  │                          │
       │                                  │                          │
[requiresProfileCompletion: false]        │                          │
       │                   tiếp tục flow login bình thường           │
       │                                  │                          │
[requiresProfileCompletion: true]         │                          │
       │─────────────── redirect /complete-profile ─────────────────▶│
       │                                  │                          │
       │                     user hoàn tất profile                   │
       │                     (UC-016 — PLANNED)                      │
```

**Lưu ý:**
- Admin Service không biết gì về oauth2 flow — chỉ tạo/tìm user và trả kết quả.
- Lookup theo `providerEmail` — email là immutable identifier từ phía provider.
- `providerUserId` lưu trong `SocialConnection` để định danh chính xác account phía provider.
- Upsert SocialConnection cho phép cùng 1 user liên kết nhiều provider (Google, GitHub...) với cùng email.
- `requiresProfileCompletion: true` khi user mới tạo — cần hoàn tất ít nhất `username`.
- Auto-generate username từ phần trước `@` của `providerEmail` + timestamp.

---

## Tham khảo
- [UC-003: Tạo user từ Social Login](../use-cases/UC-003_post_social_auth_handler)
- [Domain: User](../domains/user.md)
- [Glossary](../glossary.md)
