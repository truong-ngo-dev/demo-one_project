# UC-003: Revoke Session (Internal)

## Mô tả
Nhận notify từ oauth2 service để invalidate Spring Session tương ứng khỏi Redis. Endpoint internal — chỉ oauth2 service được gọi.

## Actors
- **oauth2 service**: Gọi khi user revoke session từ xa (UC-008 phía oauth2).

## Trigger
`POST /webgw/internal/sessions/revoke`

## Input
```json
{ "sid": "{oauth_authorization_id}" }
```

## Điều kiện tiên quyết
- Request đến từ oauth2 service — cần đảm bảo bảo mật endpoint này.

## Luồng chính

1. Nhận `sid` từ oauth2 service.
2. Lookup `webgw:oauth:{sid}` → lấy `spring_session_id`.
3. Invalidate Spring Session tương ứng khỏi Redis.
4. Xóa mapping 2 chiều (`webgw:oauth:{sid}` và `webgw:session:{spring_session_id}`).
5. Trả `200 OK`.

## Luồng thay thế

### A. `sid` không tìm thấy trong mapping
- Tại bước 2: key không tồn tại trong Redis.
- Trả `200 OK` — idempotent, bỏ qua.

## Điều kiện sau
- Spring Session đã bị invalidate.
- Mapping `webgw:oauth:{sid}` đã bị xóa.
- User không thể dùng `SESSION` cookie cũ để truy cập.

## Ghi chú
- Response phải nhanh — oauth2 service đang chờ để quyết định rollback hay không (xem oauth2 UC-008).
- Idempotent — gọi nhiều lần với cùng `sid` đều trả `200 OK`.

## Tham khảo
- [Domain](../domains/domain.md)
- [Glossary](../glossary.md)