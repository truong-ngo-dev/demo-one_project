# UC-011: Cập nhật role

## Mô tả
Admin cập nhật description của role. `name` là immutable — không được thay đổi.

## Actors
- **Admin**: Thực hiện cập nhật.

## Trigger
`PATCH /api/v1/roles/{id}`

## Input
```json
{
  "description": "Updated description"
}
```

> `name` là immutable — không được phép có trong request body.

## Luồng chính

1. Tìm role theo `RoleId`.
2. Cập nhật `description`.
3. Persist.

## Luồng thay thế

### A. Role không tồn tại
- Tại bước 1 → trả về `ROLE_NOT_FOUND`.

### B. Request chứa field `name`
- Trả về `ROLE_NAME_IMMUTABLE`.

## Output
`200 OK`
```json
{
  "id": "uuid",
  "name": "MANAGER",
  "description": "Updated description",
  "createdAt": "2025-01-01T00:00:00Z"
}
```

## Tham khảo
- [Domain: Role](../domains/role.md)