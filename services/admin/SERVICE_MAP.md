# SERVICE_MAP — admin-service

> **First entry point cho AI agents.** Đọc file này trước khi dùng bất kỳ công cụ tìm kiếm nào.
> Chi tiết convention xem tại [`docs/conventions/ddd-structure.md`](../../docs/conventions/ddd-structure.md).

---

## 📂 1. Domain Layer (`domain/`)

- **user**: Người dùng hệ thống — identity, credential, social connections, trạng thái.
    - `Root`: `User` — bảo vệ invariant: chỉ đổi username 1 lần, không thể unlock user không bị lock, không thể thực hiện hành động trên user LOCKED.
    - `Value Objects`: `UserId`, `UserPassword` (hashed), `SocialConnection` (provider + socialId + email)
    - `Enum`: `UserStatus` — ACTIVE, LOCKED, PENDING
    - `Factory`: `UserFactory` — tạo user qua register (self) và admin-create (với roles)
    - `Port`: `UserRepository`
    - `ErrorCode`: `UserErrorCode`
    - `Domain Events`: `UserCreatedEvent`, `UserLockedEvent`, `UserUnlockedEvent`, `UserPasswordChangedEvent`, `SocialConnectedEvent`

- **role**: Vai trò hệ thống — nhãn để nhóm quyền, assign cho user.
    - `Root`: `Role` — immutable name (không đổi sau khi tạo), chỉ update description
    - `Value Objects`: `RoleId`
    - `Port`: `RoleRepository`
    - `ErrorCode`: `RoleErrorCode`

---

## 🚀 2. Application Layer (`application/`)

### user — Commands

- `register/RegisterUser`: UC-002 — User tự đăng ký bằng email + password. Validate email/username unique, hash password, gán default role nếu có. **Command**
- `admin_create/AdminCreateUser`: UC-001 — Admin tạo user, chỉ định roles ngay lúc tạo. **Command**
- `social_register/SocialRegisterUser`: UC-003 — Tạo hoặc tìm user từ social provider (find-or-create). Trả `requiresProfileCompletion=true` nếu user mới. **Command**
- `lock/LockUser`: UC-007a — Admin lock user. Guard: không lock user đã LOCKED. **Command**
- `unlock/UnlockUser`: UC-007b — Admin unlock user. Guard: không unlock user đang ACTIVE. **Command**
- `assign_roles/AssignRoles`: UC-013 — Gán thêm roles cho user (additive, không replace). **Command**
- `remove_role/RemoveRole`: UC-014 — Gỡ một role khỏi user. **Command**
- `update_profile/UpdateProfile`: UC-016 — User cập nhật username (1 lần), fullName, phoneNumber. **Command**
- `change_password/ChangePassword`: UC-017 — User đổi password, verify password hiện tại trước. **Command**

### user — Queries

- `find_by_id/FindUserById`: UC-004 — Tìm user theo ID, trả detail bao gồm roles. **Query**
- `search/SearchUsers`: UC-006 — Tìm kiếm có phân trang với filter keyword/status/roleId. Đi qua `UserRepository.findAll()` với JPQL dynamic query. **Query**
- `get_my_profile/GetMyProfile`: — Lấy profile của user hiện tại theo userId từ JWT. **Query**

### user — Internal (service-to-service)

- `find_by_identity/`: UC-U05 — Tìm user theo email hoặc username (auto-detect), trả `UserView` kèm hashed password + roles. Phục vụ oauth2 service cho form login.
    - `GetUserByIdentityHandler`, `GetUserByIdentityQuery`, `UserView`, `UserViewMapper`
    - *Lưu ý*: slice này dùng standalone classes thay vì outer-class pattern — exception vì là internal handler, không phải use case nghiệp vụ.

### role — Commands

- `create/CreateRole`: UC-008 — Tạo role mới. Validate tên unique. **Command**
- `update/UpdateRole`: UC-011 — Cập nhật description của role. **Command**
- `delete/DeleteRole`: UC-012 — Xóa role. Guard: không xóa nếu có user đang dùng role này. **Command**

### role — Queries

- `find_by_id/FindRoleById`: UC-009 — Tìm role theo ID. **Query**
- `find_all/FindAllRoles`: UC-010 — Danh sách roles có phân trang + filter keyword. **Query**

---

## 🛠️ 3. Infrastructure Layer (`infrastructure/`)

### 📦 Persistence (`persistence/`)

- **Entity mapping**:
    - `User` ↔ `UserJpaEntity` → bảng `users`
        - `UserPassword` → embedded cột `hashed_password`
    - `SocialConnection` ↔ `SocialConnectionJpaEntity` → bảng `social_connections` (FK: `user_id`)
    - `Role` ↔ `RoleJpaEntity` → bảng `roles`
    - `user_roles` → join table (FK: `user_id`, `role_id`)

- **`persistence/user/`**: `UserJpaEntity`, `UserJpaRepository`, `UserMapper`, `SocialConnectionJpaEntity`
- **`persistence/role/`**: `RoleJpaEntity`, `RoleJpaRepository`, `RoleMapper`

### 🔌 Adapters (`adapter/`)

- **`adapter/repository/user/UserPersistenceAdapter`**: implement `UserRepository` — CRUD + findBySocialConnection + existsBy* + searchUsers (delegate tới JPA `@Query`)
- **`adapter/repository/role/RolePersistenceAdapter`**: implement `RoleRepository` — CRUD + findAllByIds + existsByName

### 🔐 Security (`security/`)

- `SecurityConfiguration`: Stateless JWT resource server. Permit: `/api/v1/internal/**` (service-to-service), `POST /api/v1/users/register` (self-registration). Require auth: mọi endpoint còn lại. `PasswordEncoder` bean (BCrypt strength=10).

### ⚙️ Cross-cutting (`cross-cutting/config/`)

- `EventDispatcherConfig`: Wire `EventDispatcher` bean với tất cả `EventHandler` — không chứa logic.
- `DataInitializer`: Seed `ADMIN` role và user `admin@example.com` khi khởi động lần đầu (idempotent).

---

## 🖥️ 4. Presentation Layer (`presentation/`)

### `user/UserController` — `/api/v1/users`

| Method | Path | Use Case | Auth |
|---|---|---|---|
| `POST` | `/register` | UC-002 RegisterUser | Public |
| `POST` | `/` | UC-001 AdminCreateUser | JWT |
| `GET` | `/{id}` | UC-004 FindUserById | JWT |
| `GET` | `/` | UC-006 SearchUsers | JWT |
| `POST` | `/{id}/lock` | UC-007a LockUser | JWT |
| `POST` | `/{id}/unlock` | UC-007b UnlockUser | JWT |
| `POST` | `/{id}/roles` | UC-013 AssignRoles | JWT |
| `DELETE` | `/{id}/roles/{roleId}` | UC-014 RemoveRole | JWT |
| `GET` | `/me` | GetMyProfile | JWT |
| `PATCH` | `/me` | UC-016 UpdateProfile | JWT |
| `POST` | `/me/password` | UC-017 ChangePassword | JWT |

DTOs: `user/model/` — `CreateUserRequest`, `RegisterUserRequest`, `AssignRolesRequest`, `UpdateProfileRequest`, `ChangePasswordRequest`

### `role/RoleController` — `/api/v1/roles`

| Method | Path | Use Case | Auth |
|---|---|---|---|
| `POST` | `/` | UC-008 CreateRole | JWT |
| `GET` | `/{id}` | UC-009 FindRoleById | JWT |
| `GET` | `/` | UC-010 FindAllRoles | JWT |
| `PATCH` | `/{id}` | UC-011 UpdateRole | JWT |
| `DELETE` | `/{id}` | UC-012 DeleteRole | JWT |

DTOs: `role/model/` — `CreateRoleRequest`, `UpdateRoleRequest`

### `internal/InternalUserController` — `/api/v1/internal/users`

| Method | Path | Use Case | Auth |
|---|---|---|---|
| `GET` | `/identity?value=` | UC-U05 GetUserByIdentity | Permit (network-level) |
| `POST` | `/social` | UC-003 SocialRegisterUser | Permit (network-level) |

DTOs: `internal/model/` — `SocialRegisterRequest`

### `base/`

- `ApiResponse<T>`, `PagedApiResponse<T>` — wrapper response chuẩn
- `ErrorResponse` — format lỗi
- `GlobalExceptionHandler` — map `DomainException` → HTTP error

---

## 📄 5. Resources & Configs

- `application.properties` / `application-dev.properties`: Server port, DB datasource, OAuth2 resource server JWT issuer URI.
- `db/migration/`: Flyway migration — schema cho `users`, `roles`, `social_connections`, `user_roles`.
