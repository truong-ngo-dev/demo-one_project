-0 00# UC-012: Global Login Activity Log

## Mô tả
Admin tra cứu toàn bộ lịch sử đăng nhập hệ thống để giám sát an ninh và phát hiện các mẫu tấn công.

## Actors
- **Admin**: Quản trị viên an ninh.

## Trigger
`GET /api/v1/admin/login-activities`

## Tham số Input (Query Params)
- `ip`: Filter theo địa chỉ IP.
- `result`: Filter theo kết quả (`SUCCESS`, `FAILED_WRONG_PASSWORD`, v.v.).
- `username`: Filter theo username người dùng nhập vào.
- `page`: Số trang (mặc định 0).
- `size`: Số bản ghi mỗi trang (mặc định 20, tối đa 100).

## Điều kiện tiên quyết
- Caller phải có authority `ROLE_ADMIN`.

## Luồng chính
1. Hệ thống tiếp nhận các tham số filter từ request.
2. Thực hiện truy vấn bảng `login_activities`.
3. `Left Join` với bảng `devices` để lấy thông tin `deviceName` (nếu có).
4. Sắp xếp theo `createdAt` giảm dần (mới nhất lên đầu).
5. Trả về kết quả phân trang.

## Output
```json
{
    "data": [
        {
            "id": "uuid",
            "userId": "uuid | null",
            "username": "john.doe",
            "result": "FAILED_WRONG_PASSWORD",
            "ipAddress": "192.168.1.50",
            "userAgent": "Mozilla/5.0...",
            "deviceName": "Chrome on Windows | null",
            "provider": "LOCAL",
            "createdAt": "2025-04-05T10:00:00Z"
        }
    ],
    "meta": {
        "page": 0,
        "size": 20,
        "total": 5400
    }
}
```

## Ghi chú Kỹ thuật
- **Package Placement**: `vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query`.
- Dữ liệu trả về cho Admin bao gồm cả `username` và `userId` (khác UC-009 chỉ cho chính user xem).

## Điều kiện sau
- Không thay đổi trạng thái hệ thống — read-only.

## Tham khảo
- [Domain: Activity](../domains/activity.md)
- [SERVICE_MAP](../../SERVICE_MAP.md)
