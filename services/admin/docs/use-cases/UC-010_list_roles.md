# UC-010: Danh sách roles

## Mô tả
Admin xem và tìm kiếm danh sách role.

## Actors
- **Admin**: Xem danh sách role.

## Trigger
`GET /api/v1/roles`

## Query params

| Param     | Kiểu   | Mô tả                                                    | Default     |
|-----------|--------|----------------------------------------------------------|-------------|
| `keyword` | String | Tìm theo name, description (contains, case-insensitive)  | —           |
| `page`    | int    | Số trang                                                 | 0           |
| `size`    | int    | Số item mỗi trang, max: 100                              | 20          |
| `sort`    | String | Sắp xếp                                                  | `name,asc`  |

## Luồng chính

1. Build dynamic query từ các filter có giá trị.
2. Trả về paginated result.

## Output
`200 OK`
```json
{
  "data": [
    { "id": "uuid", "name": "MANAGER", "description": "Can manage team members" }
  ],
  "meta": {
    "page": 0,
    "size": 20,
    "total": 10
  }
}
```

## Điều kiện sau
- Không thay đổi trạng thái hệ thống — read-only.

## Tham khảo
- [Domain: Role](../domains/role.md)