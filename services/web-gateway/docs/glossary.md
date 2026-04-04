# Glossary — Web Gateway

Định nghĩa các thuật ngữ riêng của Web Gateway. Cập nhật khi có khái niệm mới.

> Các thuật ngữ chung của hệ thống (token types, service descriptions) xem tại [Global Glossary](../../../docs/glossary.md).

---

## Session & State

### Spring Session
Phiên làm việc do Spring Session Data Redis tự quản lý. Lưu `OAuth2AuthorizedClient` và `SecurityContext`. Angular chỉ giữ `SESSION` cookie — không bao giờ thấy nội dung bên trong.

### OAuth Mapping
Bản ghi custom trong Redis ánh xạ `sid` (từ JWT access token) sang `spring_session_id`.
> Dùng để Web Gateway tìm đúng Spring Session cần invalidate khi nhận notify revoke từ oauth2 service.
> Key format: `webgw:oauth:{sid}`

### SESSION Cookie
Cookie `httpOnly`, `Secure`, `SameSite=Lax` chứa Spring Session id. Là cách duy nhất Angular tương tác với Web Gateway — không chứa token.

---

## Token & Proxy

### Token Relay
Cơ chế Spring Cloud Gateway tự động lấy access token từ `OAuth2AuthorizedClient` và gắn `Authorization: Bearer` vào request trước khi forward đến backend service.
> Không tự implement — dùng `tokenRelay()` filter.

### BFF (Backend For Frontend)
Pattern kiến trúc trong đó Web Gateway đóng vai trò trung gian giữa Angular và backend services, xử lý toàn bộ auth flow thay cho client.