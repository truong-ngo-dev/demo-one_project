# UC-011: IAM Overview Dashboard

## Mô tả
Admin xem các chỉ số tổng quan về tình trạng vận hành và an ninh của hệ thống IAM (Identity & Access Management).

## Actors
- **Admin**: Quản trị viên có quyền truy cập dashboard qua Web Gateway.

## Trigger
`GET /api/v1/admin/iam/overview`

## Điều kiện tiên quyết
- Caller phải có authority `ROLE_ADMIN`.

## Luồng chính
1. Hệ thống thực hiện truy vấn thống kê từ các bảng dữ liệu:
    - Đếm tổng số User từ Identity Projection (hoặc gọi qua Admin Service).
    - Đếm tổng số thiết bị đã ghi nhận từ bảng `devices`.
    - Đếm số lượng phiên đang có trạng thái `ACTIVE` từ bảng `oauth_sessions`.
    - Đếm số lượt đăng nhập thất bại trong ngày (tính từ 00:00:00 của ngày hiện tại) từ bảng `login_activities`.
2. Tổng hợp và trả về kết quả.

## Output
```json
{
    "data": {
        "totalUsers": 150,
        "totalDevices": 320,
        "activeSessions": 45,
        "failedLoginsToday": 12
    }
}
```

## Ghi chú Kỹ thuật
- **Package Placement**: `vn.truongngo.apartcom.one.service.oauth2.application.iam_dashboard.overview` (do đa số data liên quan đến thiết bị/phiên).
- Query thống kê có thể tối ưu bằng cách dùng `count` SQL trực tiếp, không cần load Entity.

## Điều kiện sau
- Không thay đổi trạng thái hệ thống — read-only.

## Tham khảo
- [SERVICE_MAP](../../SERVICE_MAP.md)
- [Business Analysis](../../../docs/business_analysis/admin_security_management.md)
