# Domain: User

## Mô tả
Projection của User từ admin-service vào oauth2 bounded context. Đại diện cho danh tính người dùng trong quá trình xác thực — oauth2 không sở hữu hay quản lý User, chỉ resolve và đọc thông tin cần thiết để cấp token.

---

## Trách nhiệm
- Cung cấp thông tin danh tính để Spring Security xây dựng `Authentication` object.
- Mang trạng thái `requiresProfileCompletion` để JWT customizer đưa vào Access Token.

## Không thuộc trách nhiệm
- Không tạo, cập nhật hay xóa user — đó là trách nhiệm của admin-service.
- Không lưu trữ user trong database của oauth2.
- Không quản lý role hay permission — chỉ đọc để embed vào token.

---

## Thuộc tính

| Thuộc tính                  | Mô tả                                                                 |
|-----------------------------|-----------------------------------------------------------------------|
| `id` (`UserId`)             | Định danh user — lấy từ admin-service, dùng làm `sub` claim trong JWT |
| `username`                  | Tên đăng nhập                                                         |
| `passwordHash`              | BCrypt hash — `null` với social-only user                             |
| `status`                    | Trạng thái tài khoản — xem `UserStatus` bên dưới                      |
| `roles`                     | Tập hợp tên role — embed vào Access Token qua `JwtTokenCustomizer`    |
| `requiresProfileCompletion` | `true` nếu user mới tạo qua social login và chưa hoàn thiện profile   |

---

## UserStatus

| Giá trị   | Ý nghĩa                                                      |
|-----------|--------------------------------------------------------------|
| `ACTIVE`  | Tài khoản hoạt động bình thường — được phép đăng nhập        |
| `LOCKED`  | Tài khoản bị khóa — Spring Security throws `LockedException` |
| `PENDING` | Tài khoản chờ xác thực — chưa được phép đăng nhập            |

---

## Factory methods

| Method                        | Dùng khi     | Ghi chú                                                           |
|-------------------------------|--------------|-------------------------------------------------------------------|
| `fromIdentity(...)`           | Form login   | Map từ `UserIdentityResponse` — có đủ status, roles, passwordHash |
| `fromSocialRegistration(...)` | Social login | Map từ `SocialRegisterResponse` — status = ACTIVE, roles = rỗng   |

---

## Port: UserIdentityService

Không phải Repository CRUD chuẩn — User là cross-BC projection, không persist trong oauth2.

| Method                                    | Trả về           | Semantics                                           |
|-------------------------------------------|------------------|-----------------------------------------------------|
| `findByCredentials(usernameOrEmail)`      | `Optional<User>` | Form login — `empty` nếu user không tồn tại         |
| `resolveBySocialIdentity(SocialIdentity)` | `User`           | Social login — admin-service đảm bảo find-or-create |

`resolveBySocialIdentity` luôn trả về `User` (không Optional) — tên phản ánh semantics "luôn resolve được", không phải "tìm kiếm có thể thất bại".

---

## SocialIdentity

Value Object truyền vào `resolveBySocialIdentity`.

| Thuộc tính       | Ý nghĩa                                      |
|------------------|----------------------------------------------|
| `provider`       | Tên provider viết hoa — ví dụ `GOOGLE`       |
| `providerUserId` | ID của user bên phía provider (Google `sub`) |
| `providerEmail`  | Email từ provider                            |

---

## Quan hệ

| Domain / Service | Quan hệ                                                                  |
|------------------|--------------------------------------------------------------------------|
| admin-service    | Nguồn dữ liệu — oauth2 gọi qua `UserIdentityService` (HTTP, ACL pattern) |
| Device           | `Device.userId` reference đến `User.id`                                  |
| Session          | `Oauth2Session.userId` reference đến `User.id`                           |
| LoginActivity    | `LoginActivity.userId` reference đến `User.id`                           |

---

## Tham khảo
- [UC-001: Login](../use-cases/UC-001_login.md)
- [Domain: Device](device.md)
- [Domain: Session](session.md)
