# CLAUDE.md — Web (Angular)

## 1. Project Overview

- **Role**: Admin dashboard + user-facing pages (Angular SPA).
- **Stack**: Angular 21, Node 22, Angular Material, Tailwind CSS.
- **Base URL**: Mọi API call đều đi qua Web Gateway — không call trực tiếp backend service.
- **Auth**: Dùng `SESSION` cookie — không lưu token ở bất kỳ đâu trên client.

---

## 2. Stack Conventions

### Component
- **Standalone components** — không dùng `NgModule`.
- Mỗi component tự declare dependencies trong `imports[]`.

### State Management
- **Signals** làm mặc định — dùng `signal()`, `computed()`, `effect()`.
- RxJS chỉ dùng khi cần (HTTP, event streams) — không dùng cho local state.
- Không dùng NgRx trừ khi được yêu cầu rõ ràng.

### Routing
- Lazy loading dùng `loadComponent()` — không dùng `loadChildren()`.
- Route guard dùng functional guard (`CanActivateFn`).

### Styling
- **Angular Material** cho UI components (form, table, dialog, v.v.).
- **Tailwind** cho layout và spacing — không override Angular Material styles bằng Tailwind.
- **Theme**: Light, minimal.
- **Primary color**: Slate `#475569` — dùng làm primary palette cho Angular Material.
- Không dùng inline style trừ trường hợp dynamic value.

---

## 3. Auth Rules — Không được vi phạm

1. **Không lưu token** — access token, refresh token, id token không được lưu ở localStorage, sessionStorage, cookie, hay memory.
2. **SESSION cookie** — do Web Gateway set, Angular không tự tạo hay đọc.
3. **Mọi API call qua Web Gateway** — không call trực tiếp oauth2 service hay admin service.
4. **Logout**: Gọi `POST /webgw/auth/logout` → nhận `202 + Location` → `window.location.href = location`.
5. **401 response**: Redirect về login page — không tự retry hay refresh token (Web Gateway lo việc đó).

---

## 4. Application Structure

### Routing
```
/               → Landing page (public)
/login          → Login page (public)
/register       → Register page (public) — [PLANNED]
/admin/**       → Admin dashboard (role = ADMIN only)
/app/**         → Resident portal (authenticated, non-admin)
```

- Landing page: giới thiệu hệ thống + 2 button Đăng nhập / Đăng ký.
- Sau login: auth guard đọc role → redirect đúng section.
  - role `ADMIN` → `/admin/dashboard`
  - role khác hoặc không có role → `/app/dashboard`
- `/admin` và `/app` có layout riêng biệt — sidebar, header, navigation khác nhau.
- Share: services, models, interceptors, auth logic.
- Không share: layout components, route modules.

### Admin Dashboard (`/admin`)
| Feature          | Route                  |
|------------------|------------------------|
| User management  | `/admin/users`         |
| Role management  | `/admin/roles`         |

### Resident Portal (`/app`) — [PLANNED]
| Feature          | Route                  |
|------------------|------------------------|
| Profile          | `/app/profile`         |
| Submit request   | `/app/requests`        |
| Parking          | `/app/parking`         |

---

## 5. [PLANNED] — Resident Portal (`/app`)

| Feature         | Mô tả                                                          | API                              |
|-----------------|----------------------------------------------------------------|----------------------------------|
| Profile page    | Xem và cập nhật thông tin cá nhân                              | `PATCH /api/v1/users/me`         |
| Change password | Đổi password — hỗ trợ cả social-only user set password lần đầu | `POST /api/v1/users/me/password` |
| Submit request  | Gửi yêu cầu phục vụ                                            | TBD                              |
| Parking         | Xem trạng thái parking                                         | TBD                              |

> Chi tiết profile/password xem tại [UC-016](../services/admin/docs/use-cases/UC-016_update_profile.md) và [UC-017](../services/admin/docs/use-cases/UC-017_change_password.md).

---

## 6. Agent Guardrails

1. **Không lưu token** — bất kỳ hình thức nào.
2. **Standalone components** — không tạo NgModule mới.
3. **Signals** — ưu tiên signals cho state, không tự thêm NgRx.
4. **Angular Material** — dùng component có sẵn trước khi tự build.
5. **[PLANNED]** — không tự implement user pages trừ khi được yêu cầu.
