hie# Logout Flow

## Tổng quan

Angular gọi logout endpoint tại Web Gateway. Web Gateway gọi OIDC logout endpoint tại oauth2 service kèm `id_token_hint`, sau đó cleanup Redis session.

---

## Flow

```
Angular               web-gateway                oauth2-service
   │                       │                           │
   │── POST /auth/logout ─▶│                           │
   │             xóa Spring Session khỏi Redis         │
   │             xóa webgw:oauth:{sid} khỏi Redis      │
   │             clear SESSION cookie                  │
   │             build logout URL (id_token_hint)      │
   │◀── 202 + Location ────│                           │
   │                       │                           │
   │───────── navigate to oauth2 logout URL ──────────▶│
   │                       │                 xóa Authorization Record
   │                       │                 redirect về Angular login
   │◀──────────────────────────────────────────────────│
```

**Lưu ý:**
- Web Gateway trả `202 Accepted` + `Location` header thay vì `302` — Angular SPA tự navigate vì XHR/fetch không handle `302` redirect tự động.
- Sau khi Web Gateway cleanup Redis, session đã bị hủy hoàn toàn về phía Gateway — phần xử lý tại oauth2 là độc lập.

---

## Tác động đến Redis

| Key                           | Tác động sau logout |
|-------------------------------|---------------------|
| `spring:session:{session_id}` | Xóa                 |
| `webgw:oauth:{sid}`           | Xóa                 |

---

## Tham khảo
- [Glossary](../glossary.md)
- [UC-002: Logout](../use-cases/UC-002_logout.md)
- [Domain](../domains/domain.md)