# UC-004: Create OAuth Session

## Mô tả
Tạo OAuth Session sau khi Authorization Record được persist thành công, liên kết user, device, và Authorization Record lại với nhau.

## Trigger
Được gọi bởi Token Issued Handler tại Phase 2, sau khi Authorization Record đã được lưu.

## Đầu vào
- `userId` — từ Authentication context
- `deviceId` — đọc từ Authorization Record attributes (đã bridge ở Phase 1.5)
- Authorization Record id — primary key của Authorization Record vừa được tạo

## Luồng chính

1. Đọc `deviceId` từ Authorization Record attributes.
2. Tạo OAuth Session với `userId`, `deviceId`, và Authorization Record id.
3. Persist vào DB.

## Điều kiện sau
- OAuth Session record tồn tại trong DB.
- OAuth Session liên kết đúng `userId`, `deviceId`, và Authorization Record.

## Ghi chú
- Chỉ được tạo sau khi Authorization Record đã persist thành công — không tạo session trước.
- `sid` claim trong JWT trỏ về primary key của Authorization Record, không phải OAuth Session id.

## Được gọi bởi
- [UC-001: Login](UC-001_login.md) — Phase 2

## Tham khảo
- [Domain: Session](../domains/session.md)
- [Authentication Flow](../flows/001_authentication_flow.md)
- [Glossary](../glossary.md)