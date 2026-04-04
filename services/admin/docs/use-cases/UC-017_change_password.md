# UC-017: Change password

## Mô tả
User tự đổi password của mình.

## Actors
- **User**: Tự đổi password.

## Trigger
`POST /api/v1/users/me/password`

## Input
```json
{
    "currentPassword": "old_secret",
    "newPassword": "new_secret"
}
```

| Field             | Bắt buộc | Mô tả                                                          |
|-------------------|----------|----------------------------------------------------------------|
| `currentPassword` | Không    | Bắt buộc nếu user đã có password. Bỏ qua nếu social-only user. |
| `newPassword`     | Có       |                                                                |

## Luồng chính

1. Lấy `userId` từ Access Token.
2. Tìm user theo `userId`.
3. **User đã có password**:
    - Kiểm tra `currentPassword` được cung cấp — nếu không → trả về `CURRENT_PASSWORD_REQUIRED`.
    - Verify `currentPassword` khớp với hashed password hiện tại.
    - Kiểm tra `newPassword` khác `currentPassword`.
4. **User chưa có password** (social-only): bỏ qua bước verify.
5. Hash `newPassword`.
6. Gọi `user.changePassword()`.
7. Persist.
8. Dispatch `UserPasswordChangedEvent`.
> [NOTE] Handler nên revoke toàn bộ active sessions sau khi đổi password — chưa implement.

## Luồng thay thế

### A. User đã có password nhưng không cung cấp currentPassword
- Tại bước 3 → trả về `CURRENT_PASSWORD_REQUIRED`.

### B. Current password không khớp
- Tại bước 3 → trả về `INVALID_PASSWORD`.

### C. New password trùng với current password
- Tại bước 3 → không thay đổi gì, trả về `200 OK` với `{ "changed": false, "message": "New password is the same as current password" }`.

## Output
`200 OK`
```text
// Đổi thành công
{ "changed": true }

// Password không thay đổi
{ "changed": false, "message": "New password is the same as current password" }
```

## Điều kiện sau
- Password mới đã được lưu dạng hashed.
- `UserPasswordChangedEvent` đã được dispatch.

## Tham khảo
- [Domain: User](../domains/user.md)