# UC-002: User tự đăng ký

## Mô tả
User tự tạo tài khoản bằng email, username và password.

## Actors
- **Anonymous user**: Người chưa có tài khoản.

## Trigger
`POST /api/v1/users/register`

## Input
```json
{
  "email": "john@example.com",
  "username": "john.doe",
  "password": "secret123",
  "fullName": "John Doe"
}
```

| Field      | Bắt buộc | Mô tả                         |
|------------|----------|-------------------------------|
| `email`    | Có       | Unique trong hệ thống         |
| `username` | Có       | Unique, immutable sau khi tạo |
| `password` | Có       |                               |
| `fullName` | Không    |                               |

## Luồng chính

1. Validate `email` chưa tồn tại.
2. Validate `username` chưa tồn tại.
3. Hash password.
4. Tạo `User` với status `ACTIVE`.
5. Persist.
6. Dispatch `UserCreatedEvent` → handler gửi email welcome/verify.

## Luồng thay thế

### A. Email đã tồn tại
- Tại bước 1 → trả về `EMAIL_ALREADY_EXISTS`.

### B. Username đã tồn tại
- Tại bước 2 → trả về `USERNAME_ALREADY_EXISTS`.

## Output
`201 Created`
```json
{ "id": "uuid", "username": "john.doe" }
```

## Điều kiện sau
- User tồn tại trong DB với status `ACTIVE`.
- `UserCreatedEvent` đã được dispatch.

## Ghi chú
- User tự đăng ký không được gán role — role chỉ được gán bởi admin (UC-013).
- Password được hash trước khi lưu — không bao giờ lưu plaintext.

## Tham khảo
- [Domain: User](../domains/user.md)