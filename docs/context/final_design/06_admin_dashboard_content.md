# Admin Dashboard — Content Specification

*Dashboard home page tại `/admin/dashboard`. Audience: IT Admin / System Admin.*

---

## Mục đích

Dashboard trả lời 3 câu hỏi thực tế của admin:

```
[1] Hệ thống đang hoạt động bình thường không?
[2] Có vấn đề bảo mật nào cần chú ý không?
[3] Platform đang có gì, ai đang dùng?
```

---

## Section 1 — System Health (real-time metrics)

> **Trạng thái**: Khả thi ngay — API đã có.

| Metric               | API                                                           | Ghi chú                   |
|----------------------|---------------------------------------------------------------|---------------------------|
| Active Sessions      | `GET /api/oauth2/v1/admin/iam/overview` → `activeSessions`    |                           |
| Failed Logins Today  | `GET /api/oauth2/v1/admin/iam/overview` → `failedLoginsToday` | Highlight nếu > threshold |
| Locked Accounts      | `GET /api/admin/v1/users?status=LOCKED` → total count         | Highlight nếu > 0         |
| Uncovered UIElements | `GET /api/admin/v1/abac/ui-elements/uncovered` → count        | Cảnh báo policy gap       |

---

## Section 2 — Platform Overview

### 2a. Users (khả thi ngay)

> **Trạng thái**: Khả thi ngay — API đã có.

| Metric       | API                                                    |
|--------------|--------------------------------------------------------|
| Total users  | `GET /api/admin/v1/users` → `meta.total`               |
| Active users | `GET /api/admin/v1/users?status=ACTIVE` → `meta.total` |
| Locked users | `GET /api/admin/v1/users?status=LOCKED` → `meta.total` |

### 2b. Organizations (placeholder)

> **Trạng thái**: ⚠️ PLACEHOLDER — Phụ thuộc Party Model (Organization aggregate chưa implement).
> Hiển thị card với số liệu `—` hoặc `N/A`. **Không có action, không navigate.**

| Metric             | Ghi chú                               |
|--------------------|---------------------------------------|
| Buildings active   | Chờ Organization management (Phase A) |
| Tenant orgs active | Chờ Organization management (Phase A) |

### 2c. Role Contexts (placeholder)

> **Trạng thái**: ⚠️ PLACEHOLDER — Phụ thuộc RoleContext model (chưa implement).
> Hiển thị card với số liệu `—` hoặc `N/A`. **Không có action, không navigate.**

| Metric                   | Ghi chú                         |
|--------------------------|---------------------------------|
| Total active memberships | Chờ RoleContext model (Phase A) |

---

## Section 3 — ABAC Health

> **Trạng thái**: Khả thi ngay — API đã có.

| Metric                     | API                                            | Hành động                                                   |
|----------------------------|------------------------------------------------|-------------------------------------------------------------|
| Uncovered UIElements count | `GET /api/admin/v1/abac/ui-elements/uncovered` | Click → navigate `/admin/abac/ui-elements?filter=uncovered` |
| Recent policy changes      | `GET /api/admin/v1/abac/audit-log?size=5`      | Click → navigate `/admin/abac/audit-log`                    |

---

## Section 4 — Recent Activity feed

> **Trạng thái**: Khả thi ngay — API đã có.

| Feed                             | API                                                              | Số entries hiển thị |
|----------------------------------|------------------------------------------------------------------|---------------------|
| Latest login activities          | `GET /api/oauth2/v1/login-activities/me` *(admin view endpoint)* | 5–10                |
| Latest admin actions (audit log) | `GET /api/admin/v1/abac/audit-log?size=5`                        | 5                   |

---

## Layout

```
┌─────────────┬─────────────┬─────────────┬─────────────┐
│ Active      │ Failed      │ Locked      │ Uncovered   │
│ Sessions    │ Logins/day  │ Accounts    │ UIElements  │
│   [số]      │   [số]      │  [số] ⚠️   │  [số] ⚠️  │
└─────────────┴─────────────┴─────────────┴─────────────┘

┌──────────────────────────┬──────────────────────────────┐
│ Platform Overview        │ ABAC Health                  │
│  Users: [số]             │  Uncovered: [số]             │
│  ├ Active: [số]          │  Recent changes:             │
│  └ Locked: [số]          │    • [audit entry]           │
│                          │    • [audit entry]           │
│  Buildings: — (*)        │                              │
│  Tenants: — (*)          │                              │
│  Memberships: — (*)      │                              │
└──────────────────────────┴──────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│ Recent Activity                                         │
│  • [timestamp] Login failed: unknown@...                │
│  • [timestamp] Policy updated: OPERATOR PolicySet       │
│  • [timestamp] User locked: user-005                    │
└─────────────────────────────────────────────────────────┘

(*) Placeholder — hiển thị "—", không có action, không navigate.
    Sẽ có data khi Party Model / Organization management implement xong.
```

---

## Phân loại theo trạng thái implement

| Section                            | Trạng thái            | Dependency                           |
|------------------------------------|-----------------------|--------------------------------------|
| System Health (4 metrics)          | ✅ Implement được ngay | API có sẵn                           |
| Users overview                     | ✅ Implement được ngay | API có sẵn                           |
| ABAC Health                        | ✅ Implement được ngay | API có sẵn                           |
| Recent Activity feed               | ✅ Implement được ngay | API có sẵn                           |
| Organizations (Buildings, Tenants) | ⚠️ Placeholder        | Party Model — Organization aggregate |
| Role Contexts / Memberships        | ⚠️ Placeholder        | RoleContext model (Phase A)          |

---

## Ghi chú placeholder

Các card placeholder phải:
- Hiển thị label đúng (không bỏ trống)
- Hiển thị giá trị `—` thay vì số
- **Không có button, link, hoặc click handler**
- Có thể thêm tooltip nhỏ: *"Tính năng này sẽ khả dụng sau khi hoàn thiện Organization Management"*

---

*Tài liệu thiết kế — 2026-04-12*
