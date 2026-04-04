# UC-001: Login

## Mô tả
Xử lý OAuth2 Authorization Code Flow + PKCE. Angular không bao giờ thấy token — chỉ nhận `SESSION` cookie sau khi login thành công.

## Actors
- **Angular**: Khởi tạo login flow.
- **Oauth2 Service**: Xác thực credentials, phát hành token.

## Trigger
`GET /auth/login`

## Điều kiện tiên quyết
- Angular chưa có session hợp lệ.

## Luồng chính

1. Web Gateway tạo `code_verifier`, `code_challenge`, `state`.
2. Web Gateway redirect Angular đến oauth2 service Authorization Endpoint.
3. User xác thực tại oauth2 service (Phase 1 + 2 — xem oauth2 UC-001).
4. Oauth2 Service redirect về Web Gateway callback URI kèm `?code=&state=`.
5. Web Gateway verify `state`.
6. Web Gateway gọi Token Endpoint với `code` + `code_verifier`.
7. Web Gateway nhận `access_token`, `refresh_token`, `id_token`.
8. Spring Session tự lưu `OAuth2AuthorizedClient` vào Redis.
9. Custom filter parse `sid` từ JWT access token.
10. Lưu mapping `webgw:oauth:{sid} → spring_session_id` vào Redis.
11. Set `SESSION` cookie (httpOnly, Secure, SameSite=Lax).

## Luồng thay thế

### A. Xác thực thất bại tại oauth2 service
- Tại bước 4: Oauth2 Service redirect về callback với error.
- Web Gateway redirect Angular về login page.

### B. state không hợp lệ
- Tại bước 5: `state` không khớp.
- Web Gateway reject request, redirect Angular về login page.

## Điều kiện sau
- `spring:session:{session_id}` tồn tại trong Redis.
- `webgw:oauth:{sid}` tồn tại trong Redis.
- Angular giữ `SESSION` cookie — không thấy token.

## Ghi chú
- Social Login (Google) dùng cùng flow này, chỉ khác trigger endpoint `/auth/login/google` — xử lý phía oauth2 service.
- Token Relay tự động refresh access token khi hết hạn — Angular không biết.

## Tham khảo
- [Login Flow](../flows/001_login_flow.md)
- [Domain](../domains/domain.md)
- [Glossary](../glossary.md)