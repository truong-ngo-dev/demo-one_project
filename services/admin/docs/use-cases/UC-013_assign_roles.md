# UC-013: Gán role cho user

## Mô tả
Admin gán một hoặc nhiều role cho user. Merge vào danh sách role hiện tại — không replace toàn bộ.

## Actors
- **Admin**: Thực hiện gán role.

## Trigger
`POST /api/v1/users/{userId}/roles`

## Input
```json
{
  "roleIds": ["role-uuid-1", "role-uuid-2"]
}
```

## Luồng chính

1. Tìm user theo `UserId`.
2. Validate tất cả `roleIds` tồn tại.
3. Merge roles vào user — không replace toàn bộ, không duplicate.
4. Persist.

## Luồng thay thế

### A. User không tồn tại
- Tại bước 1 → trả về `USER_NOT_FOUND`.

### B. Một trong các roleId không tồn tại
- Tại bước 2 → trả về `ROLE_NOT_FOUND`.

## Output
`204 No Content`

## Tham khảo
- [Domain: User](../domains/user.md)
- [Domain: Role](../domains/role.md)