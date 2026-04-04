# [001] BFF Login Flow — Cơ chế hoạt động

**Trạng thái**: ✅ Đã chốt
**Áp dụng cho**: `services/web-gateway`, `services/oauth2`

---

## Các thành phần

```
services/oauth2      # oauth2 service (Spring Authorization Server)
services/web-gateway # web-gateway service (Spring Cloud Gateway)
web/                 # Angular frontend
```

`registrationId` = `web-gateway`

> Các domain như `auth.server.com`, `bff.server.com`, `web.app.com` trong tài liệu gốc
> chỉ là ví dụ minh họa — không phải tên thực tế của hệ thống.

---

## Storage tổng quan

| Nơi lưu             | Phía                  | Nội dung                                |
|---------------------|-----------------------|-----------------------------------------|
| Redis (web-gateway) | `spring:session:{id}` | OAuth2AuthorizedClient, SecurityContext |
| Redis (web-gateway) | `webgw:oauth:{sid}`   | spring_session_id                       |
| DB (oauth2 service) | `SPRING_SESSION`      | Auth session — principal, authenticated |
| DB (oauth2 service) | `OAuth2Authorization` | access_token, refresh_token, id_token   |

---

## Web-gateway Session Lifecycle

| Thời điểm                         | Hành động                                      | Session chứa                               |
|-----------------------------------|------------------------------------------------|--------------------------------------------|
| Truy cập lần đầu chưa có session  | Tạo mới                                        | state, nonce, code_verifier, saved_request |
| Sau exchange token thành công     | Tạo mới (rotate — session fixation protection) | OAuth2AuthorizedClient, SecurityContext    |
| Đang dùng bình thường             | Giữ nguyên                                     | OAuth2AuthorizedClient, SecurityContext    |
| Token hết hạn, refresh thành công | Giữ nguyên, update token                       | OAuth2AuthorizedClient mới                 |
| Refresh token hết hạn             | Xóa                                            | —                                          |
| Logout                            | Xóa                                            | —                                          |

**Session fixation protection**: Sau khi exchange token thành công, Spring Security tạo session mới
và invalidate session cũ — tránh attacker dùng session id cũ để chiếm quyền truy cập.
Session mới copy `saved_request` từ session cũ để redirect về đúng endpoint sau login.

---

## Auth Session Lifecycle (oauth2 service)

| Thời điểm                               | Hành động                              | Session chứa                                        |
|-----------------------------------------|----------------------------------------|-----------------------------------------------------|
| Nhận authorize request, chưa có session | Tạo mới                                | authorize_context                                   |
| User submit credentials thành công      | Tạo mới (rotate)                       | authorize_context + SecurityContext (authenticated) |
| Sau issue authorization_code            | Giữ nguyên                             | SecurityContext (authenticated)                     |
| Sau cấp token                           | Giữ nguyên                             | SecurityContext (authenticated)                     |
| Silent re-authentication                | Giữ nguyên, thêm authorize_context mới | SecurityContext + authorize_context mới             |
| Logout                                  | Xóa                                    | —                                                   |

**auth_session** là session do oauth2 service tự quản lý (lưu trong `SPRING_SESSION` table), độc lập với web-gateway session:
- Lifetime dài hơn token — đây là lý do silent re-authentication hoạt động
- Mỗi browser/client có auth session riêng
- `OAuth2Authorization` và auth session tồn tại độc lập — `OAuth2Authorization` bị xóa khi logout/revoke, auth session vẫn còn

---

## 1. User nhấn đăng nhập

- Angular redirect đến `/oauth2/authorization/web-gateway`
- Web-gateway tìm client config theo `registrationId = web-gateway`
- Generate `state`, `nonce`, `code_verifier` (PKCE), `code_challenge`
- Tạo authorize endpoint:

```
https://oauth2-service/oauth2/authorize
  ?response_type=code
  &client_id=...
  &redirect_uri=...
  &state=...
  &nonce=...
  &code_challenge=...
  &code_challenge_method=S256
```

- Tạo web-gateway session mới, lưu vào Redis:
    - `state`, `nonce`, `code_verifier` — để verify callback
    - `saved_request` — request bị chặn (nếu có)
- Response `302` + set cookie `SESSION` (httpOnly, Secure)
- Angular lưu cookie → redirect đến authorize endpoint

---

## 2. oauth2 service nhận authorize request

**auth session** là session do oauth2 service tự quản lý:
- Lưu trong `SPRING_SESSION` table của oauth2 service DB
- Cookie tên `SESSION` (httpOnly, Secure) — độc lập với web-gateway session cookie
- Lifetime dài hơn token — duy trì trạng thái "đã đăng nhập với oauth2 service"

**Chưa có auth session (hoặc đã expire):**
- Tạo auth session mới, lưu `authorize_context`:
    - `state`, `nonce`, `code_challenge`, `code_challenge_method`
    - `client_id`, `redirect_uri`
- Trả trang login `200` + set auth session cookie
- User nhập credentials → oauth2 service verify
- Tạo auth session mới (rotate — session fixation protection):
    - Copy `authorize_context` từ session cũ
    - Set `SecurityContext { Authentication: { principal, authenticated: true } }`
- Issue `authorization_code`, lưu vào DB kèm:
    - `code_challenge`, `code_challenge_method`, `client_id`, `redirect_uri`
    - Expiry (thường 30-60 giây)
- Response `302` + set auth session cookie mới
  → `{web-gateway}/login/oauth2/code/web-gateway?code=...&state=...`

**Đã có auth session còn hiệu lực (silent re-authentication):**
- Thấy `SecurityContext.authenticated = true` → không hiện login form
- Lưu `authorize_context` mới vào auth session hiện tại (không tạo session mới)
- Issue `authorization_code` ngay
- Response `302` → `{web-gateway}/login/oauth2/code/web-gateway?code=...&state=...`

---

## 3. Web-gateway nhận callback

- Parse `registrationId`, `state`, `code` từ redirect URI
- Lấy `state`, `nonce`, `code_verifier` từ web-gateway session → verify
- Nếu `state` không khớp → báo lỗi, hủy flow
- Gọi token endpoint:

```
POST /oauth2/token
grant_type=authorization_code
&code=...
&redirect_uri=...
&client_id=...
&client_secret=...
&code_verifier=...
```

---

## 4. oauth2 service xử lý token exchange

- Verify `client_id`, `client_secret`
- Truy vấn `authorization_code` từ DB — kiểm tra còn hạn, chưa dùng, đúng client
- PKCE: băm `code_verifier` với `code_challenge_method` → so sánh với `code_challenge`
- Issue `access_token` (JWT), `id_token` (OIDC JWT), `refresh_token` (Opaque)
- Lưu `OAuth2Authorization` vào DB:

```
OAuth2Authorization {
    id,                  // = sid, sẽ được nhúng vào JWT claims
    registrationId,      // = web-gateway
    principalName,
    access_token,        // JWT
    refresh_token,       // Opaque
    id_token,            // JWT
    ...
}
```

---

## 5. Web-gateway nhận token response

- Verify chữ ký JWT bằng public key của oauth2 service
- Kiểm tra `iss`, `aud`
- Kiểm tra `nonce` — so với nonce trong web-gateway session → nếu không khớp: Replay Attack → hủy flow
- Tạo web-gateway session mới (rotate — session fixation protection):
    - Copy `saved_request` từ session cũ
    - Lưu `OAuth2AuthorizedClient { access_token, refresh_token, id_token }`
    - Lưu `SecurityContext { Authentication: { principal, authenticated: true } }`
- Session cũ bị invalidate và xóa khỏi Redis
- Parse `sid` từ JWT access token claims
- Lưu mapping `webgw:oauth:{sid} → {spring_session_id}` vào Redis
- Set cookie `SESSION` mới (httpOnly, Secure)
- Redirect về `saved_request` hoặc `defaultSuccessUrl`

---

## Lưu ý quan trọng

- Cookie cấu hình: `HttpOnly`, `Secure`, `SameSite` (tùy nhu cầu)
- `registrationId` chỉ có nghĩa với client — oauth2 service không biết đến nó
- oauth2 service đóng vai trò OAuth2 Client với Google — web-gateway không cần config social trực tiếp
- Auth session và web-gateway session dùng cùng tên cookie `SESSION` nhưng là 2 server khác nhau — không conflict
- SSO thực sự (multiple clients) cần cơ chế riêng — auth session hiện tại chỉ đủ cho silent re-authentication trong cùng 1 client
- Nếu nhúng login form trong iframe: cần reverse proxy same-site + subdomain + `X-Frame-Options`