# API Design Convention

> **Dành cho AI agents**: Áp dụng cho tất cả REST endpoints trong hệ thống.
> Mọi controller được sinh ra phải tuân theo convention này.

---

## URL Structure

Có hai loại URL tuỳ ngữ cảnh:

### Internal URL — Controller mapping trong từng service

```
/api/v{version}/{resource}
/api/v{version}/{resource}/{id}
/api/v{version}/{resource}/{id}/{sub-resource}
```

Controllers trong service luôn dùng format này, không có service prefix.

### External URL — Gọi qua Web Gateway (Angular, external client)

```
/api/{service}/v{version}/{resource}
```

Web Gateway dùng service name làm routing prefix, sau đó strip trước khi forward đến service:

| Service | Prefix gateway | Ví dụ external            | Service nhận       |
|---------|----------------|---------------------------|--------------------|
| admin   | `/api/admin`   | `/api/admin/v1/roles`     | `/api/v1/roles`    |
| oauth2  | `/api/oauth2`  | `/api/oauth2/v1/sessions` | `/api/v1/sessions` |

```
✅ GET  /api/admin/v1/users          ← Angular gọi
✅ POST /api/admin/v1/roles
✅ GET  /api/oauth2/v1/sessions
❌ GET  /api/v1/users                ← không dùng khi gọi qua gateway
```

- Dùng **noun, số nhiều** cho resource: `/users`, `/roles`
- Dùng **kebab-case** nếu resource nhiều từ: `/login-activities`
- Không dùng verb trong URL — action thể hiện qua HTTP method

```
✅ POST   /api/v1/users
✅ GET    /api/v1/users/{id}
✅ PUT    /api/v1/users/{id}
✅ DELETE /api/v1/users/{id}
✅ GET    /api/v1/users/{id}/roles
✅ POST   /api/v1/users/{id}/lock          ← exception: action không map được sang CRUD
❌ POST   /api/v1/createUser
❌ GET    /api/v1/getUserById
```

---

## HTTP Method

| Method | Mục đích                   | Idempotent |
|--------|----------------------------|------------|
| GET    | Đọc resource               | ✅          |
| POST   | Tạo mới resource           | ❌          |
| PUT    | Thay thế toàn bộ resource  | ✅          |
| PATCH  | Cập nhật một phần resource | ❌          |
| DELETE | Xoá resource               | ✅          |

---

## Response Format

**Success:**
```json
{
  "data": {}
}
```
```json
{
  "data": [{}],
  "meta": {
    "page": 1,
    "size": 20,
    "total": 100
  }
}
```

**Error:**
```json
{
  "error": {
    "code": "USER_NOT_FOUND",   
    "message": "User not found",
    "details": [ ]              
  }
}
```

---

## HTTP Status Code

| Trường hợp                | Status                      |
|---------------------------|-----------------------------|
| Tạo thành công            | `201 Created`               |
| Đọc / cập nhật thành công | `200 OK`                    |
| Xoá thành công            | `204 No Content`            |
| Không tìm thấy resource   | `404 Not Found`             |
| Validation lỗi            | `400 Bad Request`           |
| Chưa xác thực             | `401 Unauthorized`          |
| Không có quyền            | `403 Forbidden`             |
| Lỗi server                | `500 Internal Server Error` |

---

## Versioning

- Version đặt trong URL path: `/api/v1/`
- Khi breaking change → tạo version mới, **không xoá version cũ** ngay
- Version hiện tại: `v1`

---

## Pagination

Dùng cho tất cả endpoint trả về list:

```
GET /api/v1/users?page=0&size=20&sort=createdAt,desc
```

| Param  | Default          | Mô tả                           |
|--------|------------------|---------------------------------|
| `page` | `0`              | Index trang, bắt đầu từ 0       |
| `size` | `20`             | Số item mỗi trang, tối đa `100` |
| `sort` | `createdAt,desc` | Field sort, chiều sort          |
