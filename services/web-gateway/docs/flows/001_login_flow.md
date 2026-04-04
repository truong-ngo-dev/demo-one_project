# Login Flow

## Tổng quan

Web Gateway xử lý toàn bộ OAuth2 Authorization Code Flow + PKCE. Angular không bao giờ thấy token — chỉ nhận `SESSION` cookie sau khi login thành công.

---

## Flow

```
Angular              web-gateway                oauth2-service           Google
   │                      │                           │                    │
   │── GET /auth/login ──▶│                           │                    │
   │                 tạo code_verifier                │                    │
   │                 tạo code_challenge               │                    │
   │                 tạo state                        │                    │
   │◀── redirect /oauth2/authorize ──────────────────▶│                    │
   │                      │                           │                    │
   │─────────────────────────────────────────────────▶│                    │
   │                      │                    user nhập credentials       │
   │                      │                    xác thực (Phase 1+2)        │
   │                      │                           │                    │
   │◀──────────────── redirect ?code=&state= ─────────│                    │
   │                      │                           │                    │
   │── redirect (code) ──▶│                           │                    │
   │                 verify state                     │                    │
   │                      │──── POST /oauth2/token ──▶│                    │
   │                      │◀─── access_token          │                    │
   │                      │     refresh_token         │                    │
   │                      │     id_token ─────────────│                    │
   │               Spring Session tự lưu              │                    │
   │               OAuth2AuthorizedClient vào Redis   │                    │
   │               parse sid từ JWT                   │                    │
   │               lưu webgw:oauth:{sid} → session_id │                    │
   │◀─Set-Cookie: SESSION─│                           │                    │
```

---

## Social Login (Google)

```
Angular                   web-gateway                oauth2-service           Google
   │                           │                           │                    │
   │── GET /auth/login/google ▶│                           │                    │
   │◀── redirect /oauth2/authorize?idp=google ────────────▶│                    │
   │                           │                           │──── redirect ─────▶│
   │                           │                           │◀── callback ───────│
   │                           │                    xử lý social user           │
   │                           │                    gọi Admin Service           │
   │                           │                           │                    │
   │                           │         [requiresProfileCompletion = true]     │
   │◀────────────────────redirect /complete-profile ───────│                    │
   │                           │                           │                    │
   │                           │        [requiresProfileCompletion = false]     │
   │                           │◀─── tiếp tục như login thường từ bước token ───│
```

---

## Tác động đến Redis

| Key                           | Tác động sau login thành công             |
|-------------------------------|-------------------------------------------|
| `spring:session:{session_id}` | Tạo mới — Spring tự quản lý               |
| `webgw:oauth:{sid}`           | Tạo mới — parse `sid` từ JWT access token |

---

## Tham khảo
- [Glossary](../glossary.md)
- [UC-001: Login](../use-cases/UC-001_login.md)
- [Domain](../domains/domain.md)