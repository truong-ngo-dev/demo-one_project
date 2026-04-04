# UC-007: Lock / Unlock user

## Mô tả
Admin khoá hoặc mở khoá tài khoản user.

## Actors
- **Admin**: Thực hiện lock/unlock.

## Trigger
- `POST /api/v1/users/{id}/lock`
- `POST /api/v1/users/{id}/unlock`

## Luồng chính — Lock

1. Tìm user theo `UserId`.
2. Kiểm tra status không phải `LOCKED` hoặc `DELETED`.
3. Gọi `user.lock()`.
4. Persist.
5. Dispatch `UserLockedEvent` → handler notify oauth2 service revoke active sessions.

## Luồng chính — Unlock

1. Tìm user theo `UserId`.
2. Kiểm tra status là `LOCKED`.
3. Gọi `user.unlock()`.
4. Persist.
5. Dispatch `UserUnlockedEvent`.

## Luồng thay thế

### A. User không tồn tại
- Tại bước 1 → trả về `USER_NOT_FOUND`.

### B. Thao tác không hợp lệ với status hiện tại
- Lock user đã `LOCKED` hoặc `DELETED` → trả về `INVALID_STATUS`.
- Unlock user không phải `LOCKED` → trả về `INVALID_STATUS`.

## Output
`204 No Content`

## Điều kiện sau
**Lock:**
- User status = `LOCKED`.
- `UserLockedEvent` đã được dispatch — oauth2 service revoke toàn bộ active sessions.

**Unlock:**
- User status = `ACTIVE`.
- `UserUnlockedEvent` đã được dispatch.

## Ghi chú
- Lock có side effect quan trọng: oauth2 service sẽ revoke toàn bộ active sessions của user — user bị đăng xuất ngay lập tức trên tất cả thiết bị.
- Unlock không revoke session — user vẫn phải đăng nhập lại thủ công.

## Tham khảo
- [Domain: User](../domains/user.md)