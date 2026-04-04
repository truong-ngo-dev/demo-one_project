# UC-014: Gỡ role khỏi user

## Mô tả
Admin gỡ một role khỏi user.

## Actors
- **Admin**: Thực hiện gỡ role.

## Trigger
`DELETE /api/v1/users/{userId}/roles/{roleId}`

## Luồng chính

1. Tìm user theo `UserId`.
2. Kiểm tra user đang có `roleId` đó.
3. Gỡ role.
4. Persist.

## Luồng thay thế

### A. User không tồn tại
- Tại bước 1 → trả về `USER_NOT_FOUND`.

### B. Role không tồn tại hoặc user không có role này
- Tại bước 2 → trả về `ROLE_NOT_FOUND`.

## Output
`204 No Content`

## Tham khảo
- [Domain: User](../domains/user.md)
- [Domain: Role](../domains/role.md)