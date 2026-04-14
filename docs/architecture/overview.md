 # System Architecture Overview

> **Dành cho AI agents**: Đây là tài liệu entry point mô tả kiến trúc tổng thể.
> Khi cần chi tiết về một service, đọc tiếp `services/<tên>/CLAUDE.md`.
> Các tính năng đánh dấu `[PLANNED]` chưa được implement — không tự ý sinh code cho các phần này.

---

## 1. Mục tiêu hệ thống

`one-project` là nền tảng identity & access management (IAM) phục vụ
các hệ thống nội bộ. Cung cấp xác thực tập trung, quản trị người dùng,
và kiểm soát phân quyền theo mô hình ABAC.

---

## 2. Kiến trúc tổng thể

```
                     ┌──────────────────────────────────┐
                     │         web (Frontend)           │
                     │         Angular (TBD version)    │
                     └───────────────┬──────────────────┘
                                     │ HTTPS (session cookie only)
                     ┌───────────────▼──────────────────┐
                     │          Web gateway             │
                     │         Java + Spring            │
                     │  OAuth2 Client + Request Proxy   │
                     └──────┬───────────────┬───────────┘
                            │               │
             ┌──────────────▼───┐   ┌───────▼──────────────┐
             │  oauth2 service  │   │    admin service     │
             │  Spring Auth Srv │   │    (TBD stack)       │
             └──────────────────┘   └──────────────────────┘
                     │                        │
             ┌───────▼────────────────────────▼───────┐
             │           Shared Infrastructure        │
             │  MySQL (mỗi service DB riêng)          │
             │  Redis (Web gateway session store)     │
             └────────────────────────────────────────┘

    [PLANNED — thêm sau]
    ┌──────────────────────┐
    │  party-mgmt service  │  + các service khác
    └──────────────────────┘
```

---

## 3. Các thành phần

### 3.1 `web/` — Frontend

| Thuộc tính | Giá trị                                                 |
|------------|---------------------------------------------------------|
| Framework  | Angular (TBD version)                                   |
| Auth       | Không lưu token — chỉ giữ session cookie từ Web gateway |
| Giao tiếp  | Chỉ gọi Web gateway, không gọi thẳng services           |

→ Chi tiết: [`web/CLAUDE.md`](../../web/CLAUDE.md)

---

### 3.2 `web-gateway/` — Backend For Frontend

| Thuộc tính    | Giá trị                                                                                  |
|---------------|------------------------------------------------------------------------------------------|
| Stack         | Java + Spring Cloud Gateway (WebFlux)                                                    |
| Vai trò       | OAuth2 Client duy nhất, proxy request từ web đến các services                            |
| Session store | Redis (`spring-session-data-redis`) — token lưu server-side, web chỉ giữ httpOnly cookie |

→ Chi tiết: [`services/web-gateway/CLAUDE.md`](../../services/web-gateway/CLAUDE.md)

---

### 3.3 `services/oauth2/` — Authentication Service

| Thuộc tính      | Giá trị                                                       |
|-----------------|---------------------------------------------------------------|
| Stack           | Java + Spring Authorization Server                            |
| Vai trò         | Xác thực người dùng, cấp token OAuth2/OIDC                    |
| Database        | MySQL — schema riêng                                          |
| Core domains    | authentication, social-login, device, session, login-activity |
| Planned domains | mfa, login-attempt-lock                                       |

→ Chi tiết: [`services/oauth2/CLAUDE.md`](../../services/oauth2/CLAUDE.md)

---

### 3.4 `services/admin/` — Administration Service

| Thuộc tính      | Giá trị                                                              |
|-----------------|----------------------------------------------------------------------|
| Stack           | Java 21, Spring Boot 4.x, MySQL                                      |
| Vai trò         | Quản trị user, role; ABAC Policy Console                             |
| Database        | MySQL — schema riêng                                                 |
| Core domains    | user-management, role-management, abac (resource/policy/ui-element) |
| Planned domains | mfa-config, login-attempt-config                                     |

→ Chi tiết: [`services/admin/CLAUDE.md`](../../services/admin/CLAUDE.md)

---

### 3.5 `libs/` — Shared Libraries

| Lib           | Nội dung                                                                          |
|---------------|-----------------------------------------------------------------------------------|
| `libs/common` | EventDispatcher, EventHandler, DomainException, ErrorCode, shared utilities       |
| `libs/shared` | [PLANNED] Shared business concepts (shared kernel) — chưa có nội dung             |
| `libs/abac`   | ABAC engine — PdpEngine, PepEngine, Subject, AuthzRequest, CombineAlgorithm       |

**Quy tắc**: Services không được import code trực tiếp của nhau. Mọi sharing phải đi qua `libs/`. `libs/shared` chỉ dành cho business shared concept — utility và infrastructure đã có `libs/common`.

→ Chi tiết: [`libs/common/CLAUDE.md`](../../libs/common/CLAUDE.md)
→ Chi tiết: [`libs/shared/CLAUDE.md`](../../libs/shared/CLAUDE.md)

---

### 3.6 `[PLANNED]` Services sẽ bổ sung sau

| Service      | Mô tả sơ bộ                                    |
|--------------|------------------------------------------------|
| `party-mgmt` | Quản lý organization, individual, relationship |
| *(TBD)*      | Các service nghiệp vụ khác                     |

---

## 4. Luồng xác thực (tóm tắt)

Web không tham gia vào OAuth2 flow — toàn bộ do Web gateway xử lý.

```
web ──── session cookie ────▶ Web gateway ──── Bearer token ────▶ services
                                   │
                        Authorization Code Flow (PKCE)
                                   │
                             oauth2 service
```

- Web gateway là OAuth2 Client duy nhất, giữ toàn bộ token
- Web chỉ biết đến session cookie (httpOnly, Secure)
- Web gateway tự động refresh token, web không biết việc này xảy ra
- Login flow: Angular nhận `401` → redirect `/oauth2/authorization/web-gateway` → Spring tự xử lý

→ Chi tiết flow: [`services/web-gateway/CLAUDE.md`](../../services/web-gateway/CLAUDE.md)
→ Chi tiết cơ chế: [`docs/decisions/001-bff-login-flow.md`](../decisions/001-bff-login-flow.md)
→ Chi tiết oauth2: [`services/oauth2/docs/use-cases/authentication.md`](../../services/oauth2/docs/use-cases/authentication.md)

---

## 5. Database & Infrastructure

| Component                 | Technology | Ghi chú               |
|---------------------------|------------|-----------------------|
| oauth2 service DB         | MySQL      | Schema riêng          |
| admin service DB          | MySQL      | Schema riêng          |
| Web gateway session store | Redis      | Token lưu server-side |

**Quy tắc cứng**: Service không bao giờ đọc/ghi thẳng vào DB của service khác. Nếu cần data từ service khác → gọi qua API.

---

## 6. Giao tiếp giữa services

| Từ          | Đến         | Pattern         | Mục đích                                               |
|-------------|-------------|-----------------|--------------------------------------------------------|
| Web gateway | oauth2      | REST            | Authorization Code Flow, token refresh, revoke         |
| Web gateway | admin       | REST            | Proxy request từ web                                   |
| oauth2      | admin       | REST            | Lấy user info, roles khi issue token                   |
| oauth2      | web-gateway | REST (internal) | Notify revoke session khi user đăng xuất thiết bị khác |

> Chi tiết từng luồng (khi nào gọi, data gì, error handling) mô tả trong use-case doc của từng service.
> Async event (Kafka) chưa xác định — cập nhật khi có quyết định.

**JWT Claims đặc biệt:**
- oauth2 service nhúng `sid` (`OAuth2Authorization.id`) vào JWT access token
- Web gateway parse `sid` sau login → lưu mapping `webgw:oauth:{sid} → session_id` vào Redis
- Khi oauth2 notify revoke → web-gateway dùng `sid` để tìm và invalidate đúng session

**Internal API Auth `[PLANNED]`:**
- Hiện tại: internal endpoints không yêu cầu auth (`permitAll`)
- Sau này: dùng `client_credentials` grant — service lấy token từ oauth2 rồi gắn `Authorization: Bearer` khi gọi internal endpoint, web-gateway verify bằng public key

---

## 7. Những điều agent cần biết khi thay đổi code

1. **Tính năng `[PLANNED]`** → Không tự sinh code, hỏi lại trước
2. **Web không lưu token** → Mọi auth logic phải nằm ở Web gateway
3. **Thêm field vào DB** → Phải có migration file, không sửa schema trực tiếp
4. **Không truy cập DB chéo** giữa các services
5. **ABAC engine** (`libs/abac`) → Services import để enforce policy, không implement engine trong từng service
6. **Internal endpoints** → Hiện tại `permitAll`, không tự thêm auth