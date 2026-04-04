# UC-009: Lấy lịch sử đăng nhập của mình

## Mô tả
User xem lịch sử các lần đăng nhập của chính mình — bao gồm cả thành công và thất bại. Dùng để phát hiện đăng nhập lạ hoặc truy cập trái phép.

## Actors
- **User**: Xem lịch sử đăng nhập của chính mình.

## Trigger
`GET /api/v1/login-activities/me`

## Điều kiện tiên quyết
- User đã đăng nhập, Access Token hợp lệ.

## Query params

| Param  | Kiểu | Mô tả                      | Default |
|--------|------|----------------------------|---------|
| `page` | int  | Số trang                   | 0       |
| `size` | int  | Số item mỗi trang, max: 50 | 20      |

## Luồng chính

1. Đọc `userId` từ Access Token.
2. Query Login Activity theo `userId`, sắp xếp theo `createdAt` giảm dần.
3. Trả về danh sách phân trang.

## Output

```json
{
  "content": [
    {
      "result": "SUCCESS",
      "ipAddress": "192.168.1.1",
      "userAgent": "Chrome/120 on macOS",
      "provider": "LOCAL",
      "createdAt": "2025-01-01T00:00:00Z",
      "deviceId": "...",
      "deviceName": "..."
    },
    {
      "result": "FAILED_WRONG_PASSWORD",
      "ipAddress": "10.0.0.1",
      "userAgent": "Unknown",
      "provider": "LOCAL",
      "createdAt": "2024-12-31T23:59:00Z",
      "deviceId": "...",
      "deviceName": "..."
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 50,
  "totalPages": 3
}
```

## Ghi chú
- Không trả về `username`, `hashedPassword` hay thông tin nhạy cảm khác.
- Login FAILED với IP lạ hoặc userAgent lạ → UI nên highlight để user chú ý.

## Điều kiện sau
- Không thay đổi trạng thái hệ thống — read-only.

## Tham khảo
- [Domain: Activity](../domains/activity.md)
- [Glossary](../glossary.md)
