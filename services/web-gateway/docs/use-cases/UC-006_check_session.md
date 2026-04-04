# UC-006: Check Session

## Mô tả
Angular kiểm tra xem SESSION cookie hiện tại có hợp lệ không — không trigger redirect, luôn trả HTTP status.
Dùng để guard route và khởi tạo trạng thái auth của app.

## Actors
- **Angular**: Gọi endpoint để biết có session hợp lệ không.

## Trigger
`GET /webgw/auth/session`

## Điều kiện tiên quyết
- Không có. Endpoint luôn được phép gọi dù chưa xác thực.

## Luồng chính (session hợp lệ)

1. Angular gọi `GET /webgw/auth/session` kèm `SESSION` cookie.
2. Spring Security resolve principal từ session trong Redis.
3. Web Gateway trả `200 OK`.
4. Angular xác nhận đã xác thực.

## Luồng thay thế

### A. Không có session hoặc session đã hết hạn
- Tại bước 2: principal không tồn tại.
- Web Gateway trả `401 Unauthorized`.
- Angular redirect người dùng về login.

## Điều kiện sau
- Không thay đổi trạng thái server.
- Angular biết được trạng thái xác thực để điều hướng.

## Ghi chú
- Endpoint nằm ngoài path `/api/**` → Spring Security không tự redirect khi unauthenticated.
  Controller tự check `exchange.getPrincipal()` để trả 200 hoặc 401.
- Khác với `/api/**`: endpoint này không bao giờ trả `302` hay trigger OAuth2 flow.

## Tham khảo
- [Domain](../domains/domain.md)
- [Glossary](../glossary.md)
