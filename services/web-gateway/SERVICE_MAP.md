# SERVICE_MAP — web-gateway

> **BFF (Backend For Frontend) & OAuth2 Client.**
> Xử lý authentication flow, quản lý session tập trung và proxy request đến microservices.

---

## 📂 1. Infrastructure Layer (`infrastructure/`)

#### 📦 Configuration
- `configuration/RedisConfiguration`: Bật `@EnableRedisWebSession` để lưu session vào Redis.
- `configuration/RouteConfiguration`: Định nghĩa các route proxy sang `admin-service` và `oauth2-service` sử dụng `tokenRelay()`.

#### 🔐 Security (`infrastructure/security/`)
- `SecurityConfiguration`: Cấu hình chính cho Security WebFilterChain, OAuth2 Login và Logout.
- `Oauth2AuthorizedClientConfiguration`: Đăng ký `WebSessionServerOAuth2AuthorizedClientRepository` để lưu OAuth2 tokens trực tiếp vào WebSession (Redis).
- `SessionMappingAuthenticationSuccessHandler`: **Core Hook** — Sau khi login thành công, bóc `sid` từ JWT và lưu mapping 2 chiều giữa `sid` và `gateway_session_id` vào Redis.
- `WebGatewayLogoutSuccessHandler`: Xử lý logout trả về 202 Accepted + Location để Angular SPA thực hiện điều hướng.
- `WebGatewayOAuth2RedirectStrategy`: Chiến lược redirect tùy chỉnh cho môi trường AJAX/SPA.

---

## 🚀 2. Presentation Layer (`presentation/`)

- `AuthController`:
    - `GET /webgw/auth/login`: Khởi tạo luồng OAuth2.
    - `GET /webgw/auth/session`: Endpoint cho Angular kiểm tra trạng thái session nhanh (200/401).

- `SessionRevokeController`:
    - `POST /webgw/internal/sessions/revoke`: **Internal API** — Nhận lệnh từ `oauth2-service` để xóa session của một thiết bị cụ thể khi bị logout từ xa.

---

## 📄 3. Resources & Configs

- `application.properties`: Chứa cấu hình OAuth2 Client (web-gateway registration), Redis host/port, và URIs của các backend services.

---

## 🔄 Auth & Proxy Flow tóm tắt

1. **Login**: Angular → `/webgw/auth/login` → OAuth2 Flow → `SessionMappingHandler` lưu mapping → Trả `SESSION` cookie.
2. **Proxy**: Angular request `/api/**` → Gateway kiểm tra session → `TokenRelay` lấy JWT từ Redis → Forward đến Backend.
3. **Remote Revoke**: `oauth2-service` → `/webgw/internal/sessions/revoke` → Gateway tìm `session_id` từ mapping → Xóa key trong Redis.
