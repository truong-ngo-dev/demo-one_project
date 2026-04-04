# UC-003: Log Login Activity

## Mô tả
Ghi nhận kết quả của một lần thử đăng nhập vào hệ thống. Append-only — không sửa, không xóa.

## Trigger
- **SUCCESS**: Được gọi bởi Token Issued Handler tại Phase 2.
- **FAILED**: Được gọi bởi Authentication Failure Handler tại Phase 1.

## Đầu vào

| Field       | SUCCESS                              | FAILED                        |
|-------------|--------------------------------------|-------------------------------|
| `userId`    | Có                                   | Có — chỉ khi user tồn tại     |
| `deviceId`  | Có                                   | Null                          |
| `sessionId` | Có — định danh của OAuth Session     | Null                          |
| `status`    | SUCCESS                              | FAILED                        |

## Luồng chính

1. Nhận đầu vào từ handler tương ứng.
2. Tạo Login Activity record với status, userId, deviceId, sessionId.
3. Persist vào DB — không update, không delete.

## Điều kiện sau
- Login Activity record tồn tại trong DB.

## Ghi chú
- Nếu username không tồn tại trên hệ thống (Admin Service trả 404) → không ghi activity.
- `sessionId` và `deviceId` chỉ có giá trị ở luồng SUCCESS — ở luồng FAILED không có context này.
- `sessionId` trỏ về định danh của OAuth Session, không phải Authorization Record.

## Được gọi bởi
- [UC-001: Login](UC-001_login.md) — Phase 1 (FAILED), Phase 2 (SUCCESS)

## Tham khảo
- [Domain: Activity](../domains/activity.md)
- [Authentication Flow](../flows/001_authentication_flow.md)