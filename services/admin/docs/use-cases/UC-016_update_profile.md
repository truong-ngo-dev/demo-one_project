# UC-016: Update profile

## Mô tả
User cập nhật thông tin cá nhân. Cũng là bước hoàn tất profile lần đầu sau social login khi `requiresProfileCompletion: true`.

## Actors
- **User**: Tự cập nhật thông tin của mình.

## Trigger
`PATCH /api/v1/users/me`

## Input
```json
{
  "username": "john.doe",
  "fullName": "John Doe",
  "phoneNumber": "0901234567"
}
```

| Field         | Bắt buộc | Mô tả                                                                |
|---------------|----------|----------------------------------------------------------------------|
| `username`    | Không    | Chỉ cho đổi nếu `usernameChanged = false` — sau đó immutable         |
| `fullName`    | Không    |                                                                      |
| `phoneNumber` | Không    | Unique trong hệ thống nếu có                                         |

> Ít nhất một field phải có giá trị.

## Luồng chính

1. Lấy `userId` từ Access Token.
2. Tìm user theo `userId`.
3. Nếu request chứa `username`:
    - Kiểm tra `usernameChanged = false` — nếu đã đổi rồi → trả về `USERNAME_ALREADY_CHANGED`.
    - Validate `username` chưa tồn tại.
    - Cập nhật username, set `usernameChanged = true`.
4. Cập nhật các field còn lại nếu có.
5. Persist.

## Luồng thay thế

### A. Username đã được đổi trước đó
- Tại bước 3 → trả về `USERNAME_ALREADY_CHANGED`.

### B. Username đã tồn tại
- Tại bước 3 → trả về `USERNAME_ALREADY_EXISTS`.

### C. PhoneNumber đã tồn tại
- Tại bước 4 → trả về `PHONE_ALREADY_EXISTS`.

## Output
`200 OK`
```json
{
  "id": "uuid",
  "username": "john.doe",
  "fullName": "John Doe",
  "phoneNumber": "0901234567"
}
```

## Điều kiện sau
- Thông tin user đã được cập nhật.
- Nếu username được đổi: `usernameChanged = true`, username trở thành immutable.

## Ghi chú
- `email` không được phép cập nhật — immutable.
- Social login user dùng UC này để hoàn tất profile lần đầu (đổi username từ auto-generated sang username tự chọn).

## Tham khảo
- [Domain: User](../domains/user.md)
- [Glossary](../glossary.md)