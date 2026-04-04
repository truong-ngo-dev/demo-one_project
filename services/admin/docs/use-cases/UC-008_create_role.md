# UC-008: Tạo role

## Mô tả
Admin tạo role mới trong hệ thống.

## Actors
- **Admin**: Thực hiện tạo role.

## Trigger
`POST /api/v1/roles`

## Input
```json
{
  "name": "MANAGER",
  "description": "Can manage team members"
}
```

| Field         | Bắt buộc | Mô tả                           |
|---------------|----------|---------------------------------|
| `name`        | Có       | Unique, immutable sau khi tạo   |
| `description` | Không    |                                 |

## Luồng chính

1. Validate `name` chưa tồn tại.
2. Tạo `Role`.
3. Persist.

## Luồng thay thế

### A. Role name đã tồn tại
- Tại bước 1 → trả về `ROLE_ALREADY_EXISTS`.

## Output
`201 Created`
```json
{ "id": "uuid" }
```

## Điều kiện sau
- Role tồn tại trong DB.

## Tham khảo
- [Domain: Role](../domains/role.md)