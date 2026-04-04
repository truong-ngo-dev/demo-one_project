@# UC-008: Remote Logout

## Mô tả
User tự thu hồi một session cụ thể của chính mình — thường dùng khi phát hiện đăng nhập lạ hoặc muốn đăng xuất thiết bị từ xa.

## Actors
- **User**: Tự revoke session của chính mình.

## Trigger
`DELETE /api/v1/sessions/me/{sessionId}`

## Điều kiện tiên quyết
- User đã đăng nhập, Access Token hợp lệ.
- Session cần revoke thuộc về chính user đó.
- Session không phải session hiện tại (`isCurrent = false`) — xem UC-007.

## Luồng chính — từ danh sách thiết bị (UC-007)

1. User xem danh sách thiết bị → thấy thiết bị lạ hoặc muốn đăng xuất từ xa.
2. Click "Đăng xuất" trên thiết bị có `isCurrent = false` và `sessionStatus = ACTIVE`.
3. Gọi `DELETE /api/v1/sessions/me/{sessionId}`.

## Luồng chính

1. Đọc `userId` từ Access Token.
2. Tìm OAuth Session theo `sessionId`.
3. Kiểm tra `OAuthSession.userId == current userId`.
4. Xóa Authorization Record tương ứng theo `authorizationId`.
5. Cập nhật OAuth Session status = `REVOKED`.
6. Dispatch `SessionRevokedEvent`.
7. `SessionRevokedEventHandler` thực hiện:
    - Invalidate local IdP session khỏi DB (ngăn silent re-login) qua `idp_session_id`.
    - Notify Web Gateway xóa Redis session: `POST /internal/sessions/revoke { sid }`.

## Luồng thay thế

### A. Session không tồn tại
- Tại bước 2 → trả về `SESSION_NOT_FOUND`.

### B. Session không thuộc về current user
- Tại bước 3 → trả về `UNAUTHORIZED`.

### C. Session đã bị revoke
- Tại bước 3 → trả về `SESSION_ALREADY_REVOKED`.

### D. Revoke session hiện tại
- Tại bước 3: phát hiện `sessionId` khớp với `sid` trong Access Token hiện tại.
- Trả về lỗi `CANNOT_REVOKE_CURRENT_SESSION` — dùng logout thay thế.

## Output
`204 No Content`

## Điều kiện sau
- Authorization Record đã bị xóa — Refresh Token vô hiệu.
- OAuth Session status = `REVOKED`.
- Web Gateway đã xóa Redis session tương ứng.
- Access Token JWT vẫn hợp lệ đến hết TTL.

## Ghi chú
- Device không bị ảnh hưởng — chỉ session bị hủy.
- Notify Gateway là bắt buộc — nếu Gateway không phản hồi, toàn bộ flow bị rollback.
- Chỉ revoke được session của **thiết bị khác** — không thể revoke session hiện tại, dùng logout thay thế.

## Tham khảo
- [UC-007: List My Devices & Sessions](UC-007_list_devices.md)
- [Domain: Session](../domains/session.md)
- [ADR-003: Revocation Strategy](../decisions/003_revocation_strategy.md)
