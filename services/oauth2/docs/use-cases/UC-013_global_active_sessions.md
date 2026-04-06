# UC-013: Global Active Sessions Management

## Mô tả
Admin xem danh sách tất cả các phiên (`ACTIVE`) đang trực tuyến trên toàn hệ thống và có khả năng ngắt kết nối cưỡng bức.

## Actors
- **Admin**: Quản trị viên hệ thống.

## Trigger
`GET /api/v1/admin/active-sessions`

## Điều kiện tiên quyết
- Caller phải có authority `ROLE_ADMIN`.

## Luồng chính
1. Hệ thống truy vấn bảng `oauth_sessions` lấy các bản ghi có `status = ACTIVE`.
2. `Join` với bảng `devices` để lấy tên thiết bị.
3. Trả về danh sách kèm thông tin định danh User (từ `userId`).

## Output
```json
{
    "data": [
        {
            "sessionId": "uuid",
            "userId": "uuid",
            "username": "admin_fetch_from_identity_projection",
            "deviceName": "Firefox on Linux",
            "ipAddress": "10.0.0.5",
            "createdAt": "2025-04-05T09:30:00Z"
        }
    ]
}
```

## Action đặc biệt: Force Terminate (Admin Revoke)
Admin có thể thực hiện `DELETE` session từ danh sách này.
- **Trigger**: `DELETE /api/v1/admin/sessions/{sessionId}`
- **Nghiệp vụ**: 
    - Gọi logic dọn dẹp tại `SessionTerminationService`.
    - Ghi log Audit kèm `adminId` thực hiện.
    - Phát `SessionRevokedEvent` để đồng bộ Gateway.

## Ghi chú Kỹ thuật
- **Package Placement**: 
    - Query: `vn.truongngo.apartcom.one.service.oauth2.application.session.admin_query`.
    - Revoke: `vn.truongngo.apartcom.one.service.oauth2.application.session.admin_revoke`.

## Tham khảo
- [Domain: Session](../domains/session.md)
- [UC-008: Remote Logout (User)](UC-008_remote_logout.md)
