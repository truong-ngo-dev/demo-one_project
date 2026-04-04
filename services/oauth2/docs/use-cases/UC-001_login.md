# UC-001: Login

## Mô tả
User đăng nhập vào hệ thống thông qua OAuth2 Authorization Code Flow. Hỗ trợ hai provider: `LOCAL` (username/password) và `GOOGLE` (Social Login — [PLANNED]).

## Actors
- **User**: Người dùng thực hiện đăng nhập.
- **Web Gateway**: Khởi tạo flow, nhận token sau khi hoàn tất.
- **Admin Service**: Cung cấp thông tin identity của user.

## Điều kiện tiên quyết
- User đã có tài khoản trên hệ thống (quản lý bởi Admin Service).
- Client (Web Gateway) đã được đăng ký là OAuth2 client hợp lệ.

## Luồng chính — LOCAL (username/password)

1. User nhập credentials trên giao diện. JS tính `deviceHash` từ canvas, screen, timezone, v.v. và đặt vào hidden input `device_hash`.
2. Web Gateway redirect Browser đến Authorization Endpoint.
3. OAuth2 service xác thực credentials — gọi Admin Service để lấy identity.
4. OAuth2 service tạo hoặc cập nhật Device (dùng `deviceHash` + `userAgent` + `acceptLanguage`), lưu `deviceId` và `compositeHash` vào HTTP session.
5. OAuth2 service trả về authorization code, redirect Browser về Web Gateway.
6. Web Gateway dùng code để đổi lấy token tại Token Endpoint.
7. OAuth2 service bridge `deviceId` từ HTTP session vào Authorization Record.
8. OAuth2 service phát hành token, thêm claim `sid` vào JWT.
9. OAuth2 service tạo OAuth Session, ghi Login Activity (SUCCESS).
10. Web Gateway nhận token response.

## Luồng chính — GOOGLE (Social Login)

1. User click "Login with Google". JS tính `deviceHash` → `POST /login/device-hint` lưu vào HTTP session → redirect đến Google OAuth2.
2. Google xác thực, redirect callback về OAuth2 service.
3. OAuth2 service gọi Admin Service để find-or-create user theo Google account.
4. OAuth2 service đọc `deviceHash` từ HTTP session (`pre_auth_device_hash`), xóa attribute sau khi đọc.
5. OAuth2 service tạo hoặc cập nhật Device, lưu `deviceId` và `compositeHash` vào HTTP session.
6. Tiếp tục từ bước 5 của luồng LOCAL.

## Luồng thay thế

### A. Xác thực thất bại — sai password (LOCAL)
- Tại bước 3: credentials không hợp lệ.
- Nếu username tồn tại trên hệ thống → ghi Login Activity (FAILED) với `compositeHash` từ request.
- Nếu username không tồn tại → không ghi activity.
- Trả về lỗi xác thực, không tiếp tục flow.

### B. Admin Service không phản hồi
- Tại bước 3: Admin Service timeout hoặc lỗi.
- Authentication fail ngay lập tức.
- Không ghi Login Activity — không xác định được user.

## Điều kiện sau
- **Thành công**: Web Gateway có access token, refresh token, id token. OAuth Session được tạo. Device được cập nhật.
- **Thất bại**: Không có token. Login Activity (FAILED) được ghi nếu user tồn tại.

## Sub use cases
- [UC-002: Save / Update Device](UC-002_save_update_device.md)
- [UC-003: Log Login Activity](UC-003_log_activity.md)
- [UC-004: Create OAuth Session](UC-004_create_session.md)

## Tham khảo
- [Authentication Flow](../flows/001_authentication_flow.md)
- [Glossary](../glossary.md)