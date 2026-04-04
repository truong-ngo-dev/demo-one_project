# UC-006: Search user

## Mô tả
Admin tìm kiếm và filter danh sách user theo các tiêu chí.

## Actors
- **Admin**: Tìm kiếm user trong hệ thống.

## Trigger
`GET /api/v1/users`

## Query params

| Param     | Kiểu       | Mô tả                                                           | Default          |
|-----------|------------|-----------------------------------------------------------------|------------------|
| `keyword` | String     | Tìm theo email, username, fullName (contains, case-insensitive) | —                |
| `status`  | UserStatus | Filter theo status                                              | —                |
| `roleId`  | UUID       | Filter theo role                                                | —                |
| `page`    | int        | Số trang                                                        | 0                |
| `size`    | int        | Số item mỗi trang, max: 100                                     | 20               |
| `sort`    | String     | Sắp xếp                                                         | `createdAt,desc` |

## Luồng chính

1. Build dynamic query từ các filter có giá trị.
2. Trả về paginated result.

## Output
`200 OK`
```json
{
  "content": [
    {
      "id": "uuid",
      "email": "john@example.com",
      "username": "john.doe",
      "fullName": "John Doe",
      "status": "ACTIVE"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

## Điều kiện sau
- Không thay đổi trạng thái hệ thống — read-only.

## Tham khảo
- [Domain: User](../domains/user.md)