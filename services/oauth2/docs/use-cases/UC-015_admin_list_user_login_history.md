# UC-015: Admin List User Login History

## Mô tả
Admin tra cứu lịch sử đăng nhập chi tiết của một User cụ thể để giải quyết thắc mắc hoặc sự cố cá nhân.

## Actors
- **Admin**: Nhân viên hỗ trợ hoặc quản trị viên.

## Trigger
`GET /api/v1/admin/users/{userId}/login-activities`

## Điều kiện tiên quyết
- Caller phải có authority `ROLE_ADMIN`.

## Luồng chính
1. Hệ thống tiếp nhận `userId` từ đường dẫn.
2. Thực hiện truy vấn bảng `login_activities` lọc theo `userId`.
3. `Left Join` với bảng `devices` để làm giàu thông tin tên thiết bị.
4. Trả về kết quả phân trang, sắp xếp theo thời gian mới nhất.

## Output
```json
{
    "data": [
        {
            "result": "SUCCESS",
            "ipAddress": "1.2.3.4",
            "deviceName": "Chrome on Android",
            "provider": "GOOGLE",
            "createdAt": "2025-04-05T08:00:00Z"
        }
    ],
    "meta": {
        "page": 0,
        "size": 20,
        "total": 150
    }
}
```

## Ghi chú Kỹ thuật
- **Package Placement**: `vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query`.
- Đảm bảo trả về cả thông tin `result` chi tiết (lý do thất bại) để Admin tư vấn cho User.

## Điều kiện sau
- Không thay đổi trạng thái hệ thống.

## Tham khảo
- [Domain: Activity](../domains/activity.md)
- [UC-009: My Login History (User)](UC-009_my_login_history.md)
