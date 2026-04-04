# UC-018: Delete user

## Mô tả
Admin soft delete tài khoản user. User bị xóa không thể đăng nhập và không hiển thị trong search.

## Actors
- **Admin**: Thực hiện xóa user.

## Trigger
`DELETE /api/v1/users/{id}`

## Luồng chính

1. Tìm user theo `UserId`.
2. Kiểm tra status không phải `DELETED`.
3. Set status = `DELETED`.
4. Persist.
5. Dispatch `UserDeletedEvent` → handler notify oauth2 service revoke active sessions.

## Luồng thay thế

### A. User không tồn tại hoặc đã bị xóa
- Tại bước 1-2 → trả về `USER_NOT_FOUND`.

## Output
`204 No Content`

## Điều kiện sau
- User status = `DELETED`.
- User không còn xuất hiện trong search (UC-006).
- `GET /api/v1/users/{id}` trả về `USER_NOT_FOUND`.
- oauth2 service revoke toàn bộ active sessions của user.

## Ghi chú
- Soft delete — data vẫn còn trong DB, chỉ đổi status.
- Không hard delete để đảm bảo audit trail và tránh foreign key issues.
- Tương tự lock: có side effect quan trọng là revoke toàn bộ active sessions.

## Tham khảo
- [Domain: User](../domains/user.md)