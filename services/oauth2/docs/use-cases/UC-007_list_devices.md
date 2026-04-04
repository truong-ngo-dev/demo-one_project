# UC-007: List My Devices & Sessions

## Mô tả
User xem danh sách thiết bị đã đăng nhập của chính mình, kèm trạng thái session hiện tại trên từng thiết bị.

## Actors
- **User**: Xem danh sách devices của chính mình qua Web Gateway.

## Trigger
`GET /api/v1/sessions/me`

## Điều kiện tiên quyết
- User đã đăng nhập, Access Token hợp lệ.

## Luồng chính

1. Đọc `userId` từ `sub` claim của Access Token.
2. Query tất cả Device theo `userId`.
3. Join với OAuth Session (status = `ACTIVE`) để xác định device nào đang có session active.
4. Trả về danh sách.

## Output

```json
[
    {
        "deviceId": "uuid",
        "deviceName": "Chrome on macOS",
        "ipAddress": "192.168.1.1",
        "lastSeenAt": "2025-01-01T00:00:00Z",
        "sessionId": "uuid",
        "sessionStatus": "ACTIVE",
        "isCurrent": true
    }
]
```

| Field           | Mô tả                                                                                  |
|-----------------|----------------------------------------------------------------------------------------|
| `deviceName`    | Tên thiết bị — system detect từ User-Agent                                             |
| `ipAddress`     | IP address lần đăng nhập gần nhất                                                      |
| `lastSeenAt`    | Thời điểm hoạt động gần nhất                                                           |
| `sessionId`     | Null nếu không có session active — thiết bị đã đăng xuất                               |
| `sessionStatus` | `ACTIVE` / null                                                                        |
| `isCurrent`     | `true` nếu là session đang dùng trong request hiện tại — so sánh `sid` từ Access Token |

## Ghi chú
- Device có `isCurrent = true` — UI disable nút "Đăng xuất", hiển thị badge "Thiết bị này".
- Device có `isCurrent = false` và `sessionStatus = ACTIVE` — UI hiển thị nút "Đăng xuất" → gọi UC-008.
- Device có `sessionStatus = null` — thiết bị đã đăng xuất, không có nút action.

## Điều kiện sau
- Không thay đổi trạng thái hệ thống — read-only.

## Tham khảo
- [Domain: Device](../domains/device.md)
- [Domain: Session](../domains/session.md)
- [Glossary](../glossary.md)
