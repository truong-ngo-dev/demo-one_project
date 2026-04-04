# Use Case Index

## Auth Flow

| ID     | Tên           | Mô tả ngắn                                                   | Trạng thái  | File                                               |
|--------|---------------|--------------------------------------------------------------|-------------|----------------------------------------------------|
| UC-001 | Login         | Authorization Code Flow + PKCE, Angular nhận SESSION cookie  | Implemented | [UC-001_login.md](UC-001_login.md)                 |
| UC-002 | Logout        | Cleanup Redis session, redirect Angular đến oauth2           | Implemented | [UC-002_logout.md](UC-002_logout.md)               |
| UC-006 | Check Session | Kiểm tra session hợp lệ, trả 200/401, không trigger redirect | Implemented | [UC-006_check_session.md](UC-006_check_session.md) |

---

## Session Management

| ID     | Tên                       | Mô tả ngắn                                       | File                                                 |
|--------|---------------------------|--------------------------------------------------|------------------------------------------------------|
| UC-003 | Revoke Session (internal) | Nhận notify từ oauth2, invalidate Spring session | [UC-003_revoke_session.md](UC-003_revoke_session.md) |

---

## Proxy

| ID     | Tên           | Mô tả ngắn                       | Trạng thái  | File                                               |
|--------|---------------|----------------------------------|-------------|----------------------------------------------------|
| UC-004 | Proxy Request | Token Relay đến backend services | Implemented | [UC-004_proxy_request.md](UC-004_proxy_request.md) |

---

## [PLANNED]

| ID     | Tên          | Mô tả ngắn            |
|--------|--------------|-----------------------|
| UC-005 | Social Login | Đăng nhập bằng Google |