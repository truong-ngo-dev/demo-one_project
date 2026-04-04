# Domain: User

## Mô tả
Aggregate Root quản lý identity data của user trong hệ thống. Source of truth cho thông tin user — các service khác lấy data qua API, không truy cập trực tiếp DB.

---

## Trách nhiệm
- Quản lý thông tin identity của user (profile, credentials, roles, social connections).
- Kiểm soát vòng đời tài khoản (status transitions).
- Phát sinh domain events khi có thay đổi quan trọng.

## Không thuộc trách nhiệm
- Không xác thực credentials — đó là trách nhiệm của oauth2 service.
- Không track failed login attempts — oauth2 service đọc `LoginActivity` và gọi API lock khi cần.
- Không quản lý session — đó là trách nhiệm của oauth2 service.

---

## Cấu trúc Aggregate

```
User
├── UserId                    (Value Object — typed UUID)
├── Email                     (Value Object — validated, unique, immutable)
├── Username                  (Value Object — unique, immutable sau khi tạo)
├── PhoneNumber               (Value Object — optional, unique)
├── UserPassword              (Value Object — hashed, null nếu social-only)
├── FullName                  (Value Object — optional)
├── UserStatus                (enum: PENDING, ACTIVE, LOCKED, DELETED)
├── List<SocialConnection>    (Entity — owned by User)
├── List<RoleId>              (Value Object — reference đến Role aggregate)
├── createdAt                 (Instant — immutable)
└── updatedAt                 (Instant — cập nhật mỗi khi aggregate thay đổi)
```

---

## Value Objects

### Email
- Validated format khi khởi tạo.
- Unique trong hệ thống.
- Immutable sau khi tạo.

### Username
- Unique trong hệ thống.
- Immutable sau khi tạo — các service khác có thể dùng để reference.
- Auto-suggest từ phần trước `@` của email khi tạo.
- Pattern: chỉ chấp nhận `[a-z0-9._-]`, không có space.

### PhoneNumber
- Optional.
- Unique trong hệ thống nếu có.

### UserPassword
- Lưu dạng hashed (bcrypt).
- Null nếu user chỉ dùng social login.
- Domain không bao giờ expose raw password.

---

## Entity: SocialConnection

Liên kết giữa User và tài khoản mạng xã hội bên ngoài. Một User có thể có nhiều SocialConnection (mỗi provider một record).

```
SocialConnection
├── SocialConnectionId   (Value Object)
├── Provider             (enum: GOOGLE, GITHUB, ...)
├── ProviderUserId       (String — ID từ IdP)
├── ProviderEmail        (String — email từ IdP)
└── ConnectedAt          (Long — timestamp)
```

> Chưa có nghiệp vụ unlink SocialConnection.

---

## Trạng thái (UserStatus)

```
PENDING  → user mới tạo, chưa verify email hoặc chưa complete profile
ACTIVE   → đang hoạt động bình thường
LOCKED   → bị khoá (do admin hoặc do oauth2 service notify sau N lần login fail)
DELETED  → soft delete
```

### Transition rules

```
PENDING → ACTIVE    [PLANNED — chưa implement, tạm thời tạo xong là ACTIVE]
ACTIVE  → LOCKED    — admin gọi lock API, hoặc oauth2 service notify sau N lần login fail
LOCKED  → ACTIVE    — admin gọi unlock API
*       → DELETED   [PLANNED — chưa có use case]
```

> Hiện tại PENDING → ACTIVE chưa implement — user tạo ra sẽ ở trạng thái `ACTIVE`. Không tự sinh code cho các transition PLANNED.

---

## Hành vi

| Hành vi          | Điều kiện       | Mô tả                                                     |
|------------------|-----------------|-----------------------------------------------------------|
| `register`       | —               | Tạo user mới qua self-registration                        |
| `adminCreate`    | —               | Tạo user mới qua admin                                    |
| `connectSocial`  | —               | Thêm SocialConnection vào user                            |
| `changePassword` | status = ACTIVE | Đổi password                                              |
| `lock`           | status = ACTIVE | Khoá tài khoản — gọi bởi admin hoặc oauth2 service notify |
| `unlock`         | status = LOCKED | Mở khoá tài khoản — gọi bởi admin                         |

---

## Invariants
- `email` và `username` là immutable sau khi tạo.
- `username` chỉ chứa `[a-z0-9._-]`, không có space.
- `password` null khi user chỉ dùng social login — không được yêu cầu password khi xác thực.
- `List<RoleId>` chỉ lưu reference — không giữ Role object.
- Ít nhất một trong `username`, `email`, `phoneNumber` phải có giá trị.

---

## Events

| Event                      | Trigger                              | Handler                                      |
|----------------------------|--------------------------------------|----------------------------------------------|
| `UserCreatedEvent`         | User được tạo lần đầu (bất kể nguồn) | Gửi email welcome/verify                     |
| `UserLockedEvent`          | User bị khoá                         | Notify oauth2 service revoke active sessions |
| `UserUnlockedEvent`        | User được mở khoá                    | Không cần handler hiện tại                   |
| `UserPasswordChangedEvent` | Password được đổi                    | [NOTE] Nên revoke sessions — chưa implement  |
| `SocialConnectedEvent`     | SocialConnection được thêm vào User  | Không cần handler hiện tại                   |

---

## Error Codes

| Code                       | HTTP | Mô tả                                     |
|----------------------------|------|-------------------------------------------|
| `USER_NOT_FOUND`           | 404  |                                           |
| `EMAIL_ALREADY_EXISTS`     | 409  |                                           |
| `USERNAME_ALREADY_EXISTS`  | 409  |                                           |
| `ACCOUNT_LOCKED`           | 423  |                                           |
| `ACCOUNT_DELETED`          | 410  |                                           |
| `INVALID_STATUS`           | 422  | Thao tác không hợp lệ với status hiện tại |
| `SOCIAL_ALREADY_CONNECTED` | 409  | Provider đã được link                     |

---

## Quan hệ

| Domain | Quan hệ                                                   |
|--------|-----------------------------------------------------------|
| Role   | User giữ `List<RoleId>` — không giữ Role object trực tiếp |

---

## Tham khảo
- [Glossary](../glossary.md)
- [UC Index](../use-cases/UC-000_index.md)