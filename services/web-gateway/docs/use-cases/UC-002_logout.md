# UC-002: Logout

## Mô tả
Web Gateway cleanup Redis session trước, sau đó trả `202 + Location` để Angular tự navigate đến Oauth2 Service hoàn tất OIDC RP-Initiated Logout.

## Actors
- **Angular**: Khởi tạo logout.
- **oauth2 service**: Hoàn tất logout phía Authorization Server — Angular navigate đến đó trực tiếp.

## Trigger
`POST /auth/logout`

## Điều kiện tiên quyết
- Angular đang có `SESSION` cookie hợp lệ.

## Luồng chính

1. Web Gateway invalidate Spring Session khỏi Redis.
2. Web Gateway xóa mapping `webgw:oauth:{sid}` khỏi Redis.
3. Web Gateway clear `SESSION` cookie.
4. Web Gateway build logout URL kèm `id_token_hint`.
5. Trả `202 Accepted` + `Location: {logout_url}`.
6. Angular navigate đến logout URL — browser tự đính kèm cookie của oauth2 service.
7. Oauth2 Service clear HTTP session và xóa Authorization Record.
8. Oauth2 Service redirect Angular về login page.

## Luồng thay thế

### A. Session không tồn tại
- Tại bước 1: session không tìm thấy trong Redis.
- Vẫn tiếp tục bước 4-5 — redirect Angular về oauth2 logout URL.

## Điều kiện sau
- `spring:session:{session_id}` đã bị xóa khỏi Redis.
- `webgw:oauth:{sid}` đã bị xóa khỏi Redis.
- `SESSION` cookie đã bị clear.
- Angular đã được redirect về login page.

## Ghi chú
- Trả `202` thay vì `302` vì Angular gọi qua `HttpClient` (XHR) — xem [ADR-001: Logout Redirect Strategy](../decisions/001_logout_redirect_strategy.md).
- Bước 6 quan trọng: chỉ khi Angular navigate bằng `window.location.href` thì browser mới tự đính kèm cookie của oauth2 service — oauth2 service nhờ đó identify đúng HTTP session cần clear.

## Tham khảo
- [Logout Flow](../flows/002_logout_flow.md)
- [ADR-001: Logout Redirect Strategy](../decisions/001_logout_redirect_strategy.md)
- [Domain](../domains/domain.md)
- [Glossary](../glossary.md)