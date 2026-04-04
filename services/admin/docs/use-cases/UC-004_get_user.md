# UC-004: Tìm user theo ID

## Mô tả
Query thông tin chi tiết của một user theo UserId.

## Actors
- **Admin**: Xem thông tin user bất kỳ.
- **Internal service**: Lấy thông tin user để phục vụ nghiệp vụ nội bộ.

## Trigger
`GET /api/v1/users/{id}`

## Luồng chính

1. Tìm user theo `UserId`.
2. User có status `DELETED` → trả về `USER_NOT_FOUND`.
3. Trả về `UserDetail`.

## Luồng thay thế

### A. User không tồn tại hoặc đã xóa
- Tại bước 1-2 → trả về `USER_NOT_FOUND`.

## Output
`200 OK`
```json
{
  "id": "uuid",
  "email": "john@example.com",
  "username": "john.doe",
  "fullName": "John Doe",
  "status": "ACTIVE",
  "roles": [{ "id": "uuid", "name": "MANAGER" }],
  "socialConnections": [{ "provider": "GOOGLE", "connectedAt": 1735123456789 }],
  "createdAt": "2025-01-01T00:00:00Z"
}
```

## Điều kiện sau
- Không thay đổi trạng thái hệ thống — read-only.

## Tham khảo
- [Domain: User](../domains/user.md)