# UC-005: Tìm user theo identity (internal)

## Mô tả
Tìm user theo email hoặc username để phục vụ xác thực credentials tại oauth2 service. Trả về thông tin identity bao gồm hashed password và roles.

## Actors
- **oauth2 service**: Gọi trong quá trình xác thực username/password.

## Trigger
`GET /api/v1/internal/users/identity?value={emailOrUsername}`

> Endpoint internal — chỉ oauth2 service được gọi, không expose ra public.

## Input

| Param   | Mô tả                          |
|---------|--------------------------------|
| `value` | Email hoặc username của user   |

## Luồng chính

1. Tìm user theo `value` — match email hoặc username.
2. Không tìm thấy hoặc status `DELETED` → trả về `USER_NOT_FOUND`.
3. Status `LOCKED` → trả về `ACCOUNT_LOCKED`.
4. Trả về `UserIdentity`.

## Luồng thay thế

### A. User không tồn tại hoặc đã xóa
- Tại bước 2 → trả về `USER_NOT_FOUND`.

### B. Tài khoản bị khóa
- Tại bước 3 → trả về `ACCOUNT_LOCKED`.

## Output
`200 OK`
```json
{
  "id": "uuid",
  "username": "john.doe",
  "hashedPassword": "$2a$10$...",
  "status": "ACTIVE",
  "roles": [{ "id": "uuid", "name": "MANAGER" }]
}
```

## Điều kiện sau
- Không thay đổi trạng thái hệ thống — read-only.

## Ghi chú
- `hashedPassword` có trong response — **chỉ trả về cho internal caller**, không expose ra public endpoint.
- Social-only user có `hashedPassword: null` — oauth2 service không yêu cầu password khi xác thực social login.
- Status `PENDING` được phép xác thực — không block ở tầng này.

## Tham khảo
- [Domain: User](../domains/user.md)
- [Glossary](../glossary.md)