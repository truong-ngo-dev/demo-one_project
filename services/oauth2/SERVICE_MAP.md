# SERVICE_MAP — oauth2-service

> **First entry point cho AI agents.** Đọc file này trước khi dùng bất kỳ công cụ tìm kiếm nào.
> Chi tiết convention xem tại [`docs/conventions/ddd-structure.md`](../../docs/conventions/ddd-structure.md).

---

## 📂 1. Domain Layer (`domain/`)

- **device**: Quản lý thiết bị của user — nhận diện, tin tưởng, thu hồi.
    - `Root`: `Device` — bảo vệ invariant trạng thái thiết bị (ACTIVE/REVOKED), kiểm soát việc trust/revoke
    - `Port`: `DeviceRepository`
    - `ErrorCode`: `DeviceErrorCode` (8 mã — 01001–01008)
    - `Domain Service`: `DeviceNameDetector` — interface parse User-Agent thành tên thiết bị đọc được
    - `Domain Events`: `DeviceTrustedEvent`, `DeviceRevokedEvent`

- **session**: Quản lý OAuth2 session tương ứng với Authorization của Spring Authorization Server.
    - `Root`: `Oauth2Session` — ánh xạ 1-1 với Spring `OAuth2Authorization`, bảo vệ trạng thái ACTIVE/REVOKED/EXPIRED
    - `Port`: `SessionRepository`
    - `Domain Service`: `SessionTerminationService` — interface hủy Authorization Record theo `authorizationId`; adapter dùng Spring AS `OAuth2AuthorizationService`
    - `ErrorCode`: `SessionErrorCode` (5 mã — 02001–02005)
    - `Domain Events`: `SessionRevokedEvent`

- **activity**: Ghi nhận lịch sử đăng nhập — append-only, không sửa/xoá.
    - `Root`: `LoginActivity` — immutable, tạo qua Factory
    - `Port`: `LoginActivityRepository` (append-only — `save` duy nhất có hiệu lực)
    - `ErrorCode`: `LoginActivityErrorCode` (1 mã — 03001)
    - `Domain Service`: `LoginActivityFactory` — tạo activity từ kết quả login thành công / thất bại

- **user**: Projection của User từ admin-service vào oauth2 BC — read-only, cross-BC reference.
    - `User` — immutable, không phải AggregateRoot, không dispatch event. Factory: `fromIdentity()` (form login), `fromSocialRegistration()` (social login)
    - `SocialIdentity` — Value Object: provider, providerUserId, providerEmail
    - `UserStatus` — enum: ACTIVE, LOCKED, PENDING
    - `Port`: `UserIdentityService` — `findByCredentials(String): Optional<User>` (form login), `resolveBySocialIdentity(SocialIdentity): User` (social login — find-or-create)

---

## 🚀 2. Application Layer (`application/`)

- **device** — Commands:
    - `register_or_update/`: Đăng ký thiết bị mới hoặc cập nhật last-seen nếu đã tồn tại. — **Command**

- **device** — Queries:
    - `list_my_sessions/`: Trả danh sách thiết bị + trạng thái session của user hiện tại. — **Query**

- **session** — Commands:
    - `revoke/`: Thu hồi session khi logout — lookup Authorization Record theo id_token qua `AuthorizationLookupPort`, gọi `SessionTerminationService`, đánh dấu session REVOKED. — **Command**
    - `remote_revoke/`: Đăng xuất từ xa một session cụ thể của chính user — kiểm tra ownership, không cho revoke session hiện tại, gọi `SessionTerminationService` theo `authorizationId`. — **Command**

- **session** — Ports (Application):
    - `event/RevocationNotifier`: Port thông báo cho các hệ thống bên ngoài (Gateway) khi session bị thu hồi.

- **session** — Events:
    - `event/SessionRevokedEventHandler`: Lắng nghe `SessionRevokedEvent` → gọi `RevocationNotifier.notify(sid)`.

- **login_activity** — Commands:
    - `record/`: Ghi một login activity (thành công hoặc thất bại). — **Command**

- **login_activity** — Queries:
    - `list_my_activities/`: Trả danh sách lịch sử đăng nhập có phân trang. Dùng `LoginActivityQueryPort` (define tại slice này) — read side bypass domain, LEFT JOIN với `devices`. — **Query**

- **auth** — Commands:
    - `complete_login/`: Phase 2 hoàn tất login — liên kết `OAuth2Authorization` với device và session sau khi access token được phát. — **Command**

---

## 🛠️ 3. Infrastructure Layer (`infrastructure/`)

#### 📦 Persistence
- **Cơ chế**: MySQL + Spring Data JPA
- **Entity mapping**:
    - `Device` ↔ `DeviceJpaEntity` → bảng `devices`
        - `DeviceFingerprint` → embedded trong `devices` (các cột: `device_hash`, `user_agent`, `accept_language`, `composite_hash`)
    - `Oauth2Session` ↔ `UserSessionJpaEntity` → bảng `oauth_sessions`
    - `LoginActivity` ↔ `LoginActivityJpaEntity` → bảng `login_activities`

#### 🔌 Adapters
- `adapter/repository/device/`
    - `DevicePersistenceAdapter`: implement `DeviceRepository`
- `adapter/repository/session/`
    - `UserSessionPersistenceAdapter`: implement `SessionRepository`
- `adapter/repository/activity/`
    - `LoginActivityPersistenceAdapter`: implement `LoginActivityRepository`
- `adapter/repository/user/`
    - `UserIdentityServiceAdapter`: implement `UserIdentityService` — map `UserIdentityResponse` → `User.fromIdentity()`, `SocialRegisterResponse` → `User.fromSocialRegistration()`; dùng `AdminServiceClient` từ `api/http/internal/admin/`
- `adapter/query/activity/`
    - `LoginActivityQueryService`: implement `LoginActivityQueryPort` — JdbcTemplate, LEFT JOIN `login_activities` với `devices` để enrich tên thiết bị
- `adapter/service/device/`
    - `YauaaDeviceAdapter`: implement `DeviceNameDetector` — parse User-Agent bằng thư viện YAUAA
- `adapter/service/authorization/`
    - `SpringAuthorizationTerminationAdapter`: implement `SessionTerminationService` — dọn dẹp toàn diện tại IdP: hủy OAuth2 Authorization (tokens) + xóa local Spring Session.
    - `SpringAuthorizationLookupAdapter`: implement `AuthorizationLookupPort` — dùng `OAuth2AuthorizationService.findByToken()`
- `adapter/api/http/`
    - `WebGatewayRevocationAdapter`: implement `RevocationNotifier` — gọi `WebGatewayClient` để notify Gateway.

#### 🌐 Outbound Clients — `api/`
- `api/http/internal/admin/`
    - `AdminServiceClientConfig`: `@Configuration` — khai báo `RestClient` bean trỏ đến Admin Service (`${app.admin-service.base-url}`)
    - `AdminServiceClient`: gom tất cả outbound call sang Admin Service
        - `getUserIdentity(usernameOrEmail)` → `GET /api/v1/internal/users/identity` — phục vụ `OAuth2UserDetailsService`
        - `registerSocialUser(provider, providerUserId, providerEmail)` → `POST /api/v1/internal/users/social` — phục vụ `SocialLoginOidcUserService`
    - *DTOs dùng từ `libs/shared` (shared contract) — không tạo `dto/` riêng*
- `api/http/internal/webgateway/`
    - `WebGatewayClientConfig`: `@Configuration` — khai báo `RestClient` bean trỏ đến Web Gateway (`${app.gateway-service.base-url}`)
    - `WebGatewayClient`: gom tất cả outbound call sang Web Gateway
        - `notifyRevocation(sid)` → `POST /webgw/internal/sessions/revoke` — phục vụ `SessionRevokedEventHandler` (UC-008)

#### 🔐 Security (`infrastructure/security/`)
*Toàn bộ OAuth2 / Spring Authorization Server config — không implement domain Port.*

- `SecurityConfiguration`: Định nghĩa 3 filter chain theo thứ tự ưu tiên — Authorization Server (Order 1), API Resource Server (Order 2), Default form/social login (Order 3). Cấu hình CORS và endpoint login.
- `handler/`
    - `DeviceAwareAuthenticationSuccessHandler`: **Phase 1** — sau form/social login thành công, gọi `RegisterOrUpdateDevice` và lưu device info vào HTTP session để Phase 1.5 dùng.
    - `DeviceAwareAuthenticationFailureHandler`: **Phase 1 thất bại** — gọi `RecordLoginActivity` với kết quả thất bại.
    - `AuthorizationRevokingLogoutSuccessHandler`: Logout — gọi `RevokeSession` để xoá `OAuth2Authorization` và revoke session.
- `model/`
    - `DeviceAwareWebAuthenticationDetails`: Capture `device_hash` từ form param, `userAgent`, `acceptLanguage`, IP (X-Forwarded-For aware) tại thời điểm login.
- `service/`
    - `OAuth2UserDetailsService`: Form login — dùng `UserIdentityService.findByCredentials()`, trả `UserDetails` với roles.
    - `SocialLoginOidcUserService`: Social login — dùng `UserIdentityService.resolveBySocialIdentity()`, thêm claim `requires_profile_completion`.
- `oauth2/`
    - `OAuth2AuthorizationServerConfig`: Cấu hình Spring Authorization Server — OIDC, token settings, logout handler.
    - `OAuth2ClientConfiguration`: Cấu hình OAuth2 client (social login providers).
    - `AuditingOAuth2AuthorizationService`: Wrapper quanh `JdbcOAuth2AuthorizationService` — **Phase 1.5** gắn device info vào `OAuth2Authorization` khi Authorization Code được phát; **Phase 2** gọi `CompleteLogin` khi Access Token được phát.
    - `JwtTokenCustomizer`: Thêm `sid`, `roles` vào Access Token; copy claims third-party vào ID Token; thêm `kid` header cho tất cả token.
    - `DeviceAwareWebAuthenticationDetailsMixin`: Jackson mixin để serialize/deserialize `DeviceAwareWebAuthenticationDetails` trong `OAuth2Authorization`.
- `key/`
    - `KeyConfiguration`: Sinh RSA key pair khi khởi động, inject `NimbusJwtEncoder` và `JWKSource`.
    - `RsaKeyPairRepository`: Interface lưu trữ key pair.
    - `JdbcRsaKeyPairRepository`: Implement `RsaKeyPairRepository` — lưu/load key pair qua JDBC.
    - `RsaKeyPairGenerator`: Sinh RSA key pair mới.
    - `RsaKeyPairGenerationEvent`: Spring event kích hoạt khi cần sinh key mới.
    - `RsaKeyPairJWKSource`: Nimbus `JWKSource` đọc từ `RsaKeyPairRepository` — expose JWK endpoint.
    - `RsaKeyPairRowMapper`: JDBC `RowMapper` cho key pair.
    - `RsaPublicKeyConverter`: Encode/decode public key PEM để lưu DB.
    - `RsaPrivateKeyConverter`: Encode/decode private key PEM (encrypted) để lưu DB.

#### ⚙️ Cross-cutting
- `cross-cutting/config/`
    - `EventDispatcherConfig`: Wire `EventDispatcher` bean với tất cả `EventHandler` — không chứa logic.
- `cross-cutting/utils/`
    - `IpAddressExtractor`: Utility extract IP từ request — X-Forwarded-For aware.

---

## 🖥️ 4. Presentation Layer (`presentation/`)

- `LoginController` (`/login`):
    - `GET /login` — Trả login page (MVC, không phải REST)
    - `POST /login/device-hint` — Nhận `deviceHash` từ client, lưu vào HTTP session trước Phase 1; trả 204

- `session/SessionController` (`/api/v1/sessions`):
    - `GET /api/v1/sessions/me` — Danh sách thiết bị + session của user hiện tại (Bearer JWT required)
    - `DELETE /api/v1/sessions/me/{sessionId}` — Đăng xuất từ xa một session cụ thể; 204 No Content (Bearer JWT required)

- `activity/LoginActivityController` (`/api/v1/login-activities`):
    - `GET /api/v1/login-activities/me` — Lịch sử đăng nhập có phân trang (`page`, `size`) (Bearer JWT required)

---

## 📄 5. Resources & Configs

- `application.properties` / `application-dev.properties` / `application-prod.properties`: Server port, DB, Admin Service base URL (`app.admin-service.base-url`), RSA key config, OAuth2 client registration.
- `db/migration/`: Flyway migration — schema cho `devices`, `oauth_sessions`, `login_activities`, `oauth2_authorization`, `oauth2_registered_client`, `rsa_key_pairs`.

---

## 🔄 Login Flow tóm tắt

```
Phase 1   → DeviceAwareAuthenticationSuccessHandler  → RegisterOrUpdateDevice (Command)
Phase 1.5 → AuditingOAuth2AuthorizationService       → gắn device_id vào Authorization
Phase 2   → AuditingOAuth2AuthorizationService       → CompleteLogin (Command) → tạo OauthSession, RecordLoginActivity
Logout    → AuthorizationRevokingLogoutSuccessHandler → RevokeSession (Command)
```

> Chi tiết flow xem: [`docs/flows/001_authentication_flow.md`](docs/flows/001_authentication_flow.md), [`docs/flows/002_logout_flow.md`](docs/flows/002_logout_flow.md)
