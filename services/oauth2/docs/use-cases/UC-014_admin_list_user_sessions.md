# UC-014: Admin List User Devices & Sessions (Merged View)

## Mô tả
Admin xem danh sách gộp tất cả thiết bị và trạng thái session tương ứng của một User cụ thể. Phục vụ hiển thị tại trang Profile Admin.

## Actors
- **Admin**: Hỗ trợ khách hàng hoặc quản trị viên.

## Trigger
`GET /api/v1/admin/users/{userId}/sessions`

## Điều kiện tiên quyết
- Caller phải có authority `ROLE_ADMIN`.

## Luồng chính
1. Hệ thống xác thực `userId` (target user) có tồn tại.
2. Thực hiện `Left Join` giữa bảng `devices` và bảng `oauth_sessions` (lọc theo `targetUserId` và `status = ACTIVE`).
3. Tổng hợp danh sách: Mỗi thiết bị là một dòng, kèm ID session nếu thiết bị đó đang online.
4. Trả về kết quả.

## Output
```json
[
    {
        "deviceId": "uuid",
        "deviceName": "Chrome on macOS",
        "ipAddress": "192.168.1.1",
        "lastSeenAt": "2025-01-01T00:00:00Z",
        "sessionId": "uuid | null",
        "sessionStatus": "ACTIVE | null"
    }
]
```

## Ghi chú Kịch bản (Admin Revoke)
- Admin có thể nhấn "Đăng xuất" cho bất kỳ thiết bị nào có `sessionStatus = ACTIVE`.
- Action gọi đến endpoint revoke của admin: `DELETE /api/v1/admin/sessions/{sessionId}`.

## Ghi chú Kỹ thuật
- **Package Placement**: `vn.truongngo.apartcom.one.service.oauth2.application.device.admin_query`.
- Khác UC-007: Không có logic `isCurrent` (vì Admin không phải là User đang sử dụng thiết bị đó).

## Điều kiện sau
- Không thay đổi trạng thái hệ thống.

## Tham khảo
- [Domain: Device](../domains/device.md)
- [UC-007: List My Devices (User)](UC-007_list_devices.md)
