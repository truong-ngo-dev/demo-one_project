# UC-001: Admin tạo user

## Mô tả
Admin tạo tài khoản user mới cho hệ thống. User được tạo với password default và ít nhất một role.

## Actors
- **Admin**: Người thực hiện tạo user.

## Trigger
`POST /api/v1/users`

## Điều kiện tiên quyết
- Caller có quyền admin.
- Ít nhất một `roleId` hợp lệ được cung cấp.

## Input
```json
{
  "email": "john@example.com",
  "username": "john.doe",
  "fullName": "John Doe",
  "roleIds": ["role-uuid-1"]
}
```

| Field      | Bắt buộc | Mô tả                         |
|------------|----------|-------------------------------|
| `email`    | Có       | Unique trong hệ thống         |
| `username` | Có       | Unique, immutable sau khi tạo |
| `fullName` | Không    |                               |
| `roleIds`  | Có       | Ít nhất 1 role                |

## Luồng chính

1. Validate `email` chưa tồn tại.
2. Validate `username` chưa tồn tại.
3. Validate tất cả `roleIds` tồn tại.
4. Tạo password default.
5. Tạo `User` với status `ACTIVE`.
6. Gán roles.
7. Persist.
8. Dispatch `UserCreatedEvent` → handler gửi email thông báo.

## Luồng thay thế

### A. Email đã tồn tại
- Tại bước 1 → trả về `EMAIL_ALREADY_EXISTS`.

### B. Username đã tồn tại
- Tại bước 2 → trả về `USERNAME_ALREADY_EXISTS`.

### C. RoleId không tồn tại
- Tại bước 3 → trả về `ROLE_NOT_FOUND`.

## Output
`201 Created`
```json
{ "id": "uuid" }
```

## Điều kiện sau
- User tồn tại trong DB với status `ACTIVE`.
- `UserCreatedEvent` đã được dispatch.

## Tham khảo
- [Domain: User](../domains/user.md)
- [Domain: Role](../domains/role.md)