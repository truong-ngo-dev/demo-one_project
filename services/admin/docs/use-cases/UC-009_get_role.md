# UC-009: Tìm role theo ID

## Mô tả
Query thông tin chi tiết của một role theo RoleId.

## Actors
- **Admin**: Xem thông tin role.
- **Internal service**: Lấy thông tin role để phục vụ nghiệp vụ nội bộ.

## Trigger
`GET /api/v1/roles/{id}`

## Luồng chính

1. Tìm role theo `RoleId`.
2. Không tìm thấy → trả về `ROLE_NOT_FOUND`.
3. Trả về `RoleDetail`.

## Output
`200 OK`
```json
{
  "id": "uuid",
  "name": "MANAGER",
  "description": "Can manage team members",
  "createdAt": "2025-01-01T00:00:00Z"
}
```

## Điều kiện sau
- Không thay đổi trạng thái hệ thống — read-only.

## Tham khảo
- [Domain: Role](../domains/role.md)