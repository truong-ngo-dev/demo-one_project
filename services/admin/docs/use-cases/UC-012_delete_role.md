# UC-012: Xóa role

## Mô tả
Admin xóa role khỏi hệ thống. Không được xóa role đang được gán cho user.

## Actors
- **Admin**: Thực hiện xóa.

## Trigger
`DELETE /api/v1/roles/{id}`

## Luồng chính

1. Tìm role theo `RoleId`.
2. Kiểm tra không có user nào đang được gán role này.
3. Hard delete.

## Luồng thay thế

### A. Role không tồn tại
- Tại bước 1 → trả về `ROLE_NOT_FOUND`.

### B. Role đang được gán cho user
- Tại bước 2 → trả về `ROLE_IN_USE`.

## Output
`204 No Content`

## Tham khảo
- [Domain: Role](../domains/role.md)