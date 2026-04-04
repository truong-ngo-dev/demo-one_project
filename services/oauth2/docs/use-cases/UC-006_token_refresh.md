# UC-006: Token Refresh

## Mô tả
Web Gateway dùng Refresh Token để tái cấp Access Token mới khi Access Token hiện tại hết hạn.

## Actors
- **Web Gateway**: Phát hiện Access Token hết hạn, khởi tạo refresh flow.
- **OAuth2 service**: Xác thực Refresh Token, phát hành Access Token mới.

## Điều kiện tiên quyết
- Web Gateway đang giữ Refresh Token hợp lệ.
- Authorization Record tương ứng vẫn còn tồn tại (chưa bị revoke).

## Luồng chính

1. Web Gateway gửi Refresh Token đến Token Endpoint.
2. OAuth2 service xác thực Refresh Token — tìm Authorization Record tương ứng.
3. OAuth2 service phát hành Access Token mới.
4. OAuth2 service cập nhật Authorization Record với Access Token mới.
5. Web Gateway nhận Access Token mới.

## Luồng thay thế

### A. Refresh Token không hợp lệ
- Tại bước 2: Refresh Token không tìm được Authorization Record.
- Trả về lỗi — Web Gateway cần yêu cầu user đăng nhập lại.

### B. Authorization Record đã bị revoke (user đã logout)
- Tại bước 2: Authorization Record không tồn tại.
- Trả về lỗi — Web Gateway cần yêu cầu user đăng nhập lại.

### C. Refresh Token hết hạn
- Tại bước 2: Refresh Token quá TTL.
- Trả về lỗi — Web Gateway cần yêu cầu user đăng nhập lại.

## Điều kiện sau
- **Thành công**: Web Gateway có Access Token mới. Authorization Record được cập nhật.
- **Thất bại**: Không có token mới. Web Gateway redirect user về login.

## Ghi chú
- Device không bị ảnh hưởng.
- OAuth Session không thay đổi — chỉ Access Token được tái cấp.
- Login Activity không ghi thêm record khi refresh.
- Refresh Token không được tái cấp trong flow này — Web Gateway giữ nguyên Refresh Token cũ.

## Tham khảo
- [ADR-001: Token Format](../decisions/001_token_format.md)
- [ADR-003: Revocation Strategy](../decisions/003_revocation_strategy.md)
- [Glossary](../glossary.md)