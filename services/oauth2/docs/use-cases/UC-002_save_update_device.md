# UC-002: Save / Update Device

## Mô tả
Tạo mới hoặc cập nhật thông tin Device sau khi user xác thực thành công.

## Trigger
Được gọi bởi Authentication Success Handler tại Phase 1.

## Đầu vào
- `userId` — từ Admin Service
- Device fingerprint — từ request (user agent, IP, v.v.)

## Luồng chính

1. Tìm Device theo `userId` + device fingerprint.
2. **Đã tồn tại** → cập nhật thông tin (last seen, IP, v.v.).
3. **Chưa tồn tại** → tạo Device mới.
4. Lưu `deviceId` vào HTTP session để Phase 1.5 bridge sang Authorization Record.

## Điều kiện sau
- Device record tồn tại trong DB.
- `deviceId` có trong HTTP session, sẵn sàng cho Phase 1.5.

## Được gọi bởi
- [UC-001: Login](UC-001_login.md) — Phase 1

## Tham khảo
- [Domain: Device](../domains/device.md)
- [Authentication Flow](../flows/001_authentication_flow.md)