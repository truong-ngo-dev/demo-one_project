# UC-005: Logout

## Mô tả
User đăng xuất khỏi hệ thống. Web Gateway chủ động xóa Redis session trước, sau đó redirect Browser đến OAuth2 service để revoke Authorization Record.

## Actors
- **User**: Người dùng thực hiện đăng xuất.
- **Web Gateway**: Khởi tạo flow, xóa Redis session.
- **Browser**: Thực hiện redirect đến OAuth2 service.

## Điều kiện tiên quyết
- User đang có session hợp lệ.
- Web Gateway đang giữ Redis session tương ứng.

## Luồng chính

1. User thực hiện logout trên giao diện.
2. Web Gateway xóa Redis session.
3. Web Gateway build logout URL, redirect Browser.
4. Browser truy cập logout URL tại OAuth2 service.
5. OAuth2 service xóa Authorization Record theo `id_token`.
6. OAuth Session bị xóa hoặc đánh dấu inactive.
7. OAuth2 service redirect Browser về trang logged out.

## Luồng thay thế

### A. Authorization Record không tồn tại
- Tại bước 5: không tìm thấy Authorization Record theo `id_token`.
- Vẫn tiếp tục redirect — coi như đã logout.

### B. `id_token` không hợp lệ hoặc đã hết hạn
- Tại bước 5: không thể xác định Authorization Record.
- Vẫn tiếp tục redirect — Redis session đã bị xóa ở bước 2, user thực tế đã logout khỏi Gateway.

## Điều kiện sau
- Redis session tại Web Gateway đã bị xóa.
- Authorization Record đã bị xóa — Refresh Token vô hiệu.
- OAuth Session inactive.
- Access Token JWT vẫn hợp lệ đến hết TTL.

## Ghi chú
- Device không bị ảnh hưởng — chỉ session bị hủy.
- Login Activity không ghi thêm record khi logout.
- Việc xóa Authorization Record phải tự implement trong custom logout handler.

## Tham khảo
- [Logout Flow](../flows/002_logout_flow.md)
- [ADR-003: Revocation Strategy](../decisions/003_revocation_strategy.md)
- [Glossary](../glossary.md)