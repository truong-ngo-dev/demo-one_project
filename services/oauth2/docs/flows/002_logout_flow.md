# Logout Flow

## Tổng quan

Flow logout được khởi động từ Web Gateway — Gateway tự cleanup Redis session trước, sau đó redirect Browser đến OAuth2 service để revoke Authorization Record.

---

## Flow

```
Browser                  web-gateway              oauth2-service
   │                          │                         │
   │────── POST /logout ─────▶│                         │
   │                   xóa Redis session                │
   │                   build logout URL                 │
   │◀─ redirect (logout URL) ─│                         │
   │                          │                         │
   │────────────── GET /logout (id_token) ─────────────▶│
   │                                                 [Revoke]
   │                                          xóa Authorization Record
   │                                               theo id_token
   │◀────────────── redirect (logged out) ──────────────│
```

**Lưu ý:**
- Web Gateway chủ động xóa Redis session trước — không phụ thuộc vào OAuth2 service.
- Sau khi Authorization Record bị xóa, Refresh Token vô hiệu ngay lập tức. Access Token JWT vẫn hợp lệ đến hết TTL.

---

## Tác động đến domain

| Domain         | Tác động                                                                       |
|----------------|--------------------------------------------------------------------------------|
| Session        | OAuth Session bị xóa hoặc đánh dấu inactive khi Authorization Record bị revoke |
| Device         | Không bị ảnh hưởng — device vẫn còn, chỉ session bị hủy                        |
| Login Activity | Không ghi thêm activity — logout không phải sự kiện cần audit ở tầng này       |

---

## Tham khảo
- [Glossary](../glossary.md)
- [UC-002: Logout](../use-cases/UC-005_logout)
- [ADR-003: Revocation Strategy](../decisions/003_revocation_strategy.md)