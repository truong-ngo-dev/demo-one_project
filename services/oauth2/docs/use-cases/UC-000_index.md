# Use Case Index — oauth2-service

---

## Authentication

*User-triggered — khởi đầu trực tiếp từ hành động của user hoặc Web Gateway.*

| ID     | Tên           | Mô tả ngắn                                                               | Trạng thái  | File                                               |
|--------|---------------|--------------------------------------------------------------------------|-------------|----------------------------------------------------|
| UC-001 | Login         | Đăng nhập bằng username/password hoặc Google qua Authorization Code Flow | Implemented | [UC-001_login.md](UC-001_login.md)                 |
| UC-005 | Logout        | Đăng xuất, revoke Authorization Record                                   | Implemented | [UC-005_logout.md](UC-005_logout.md)               |
| UC-006 | Token Refresh | Web Gateway tái cấp Access Token bằng Refresh Token khi hết hạn          | Implemented | [UC-006_token_refresh.md](UC-006_token_refresh.md) |

---

## Internal Flows

*System-triggered — được gọi bởi Spring Security handlers trong quá trình login/logout, không phải trực tiếp từ user.*

| ID     | Tên                       | Trigger                                   | Trạng thái  | File                                                         |
|--------|---------------------------|-------------------------------------------|-------------|--------------------------------------------------------------|
| UC-002 | Register or Update Device | Phase 1 — Authentication Success Handler  | Implemented | [UC-002_save_update_device.md](UC-002_save_update_device.md) |
| UC-003 | Record Login Activity     | Phase 1 (thất bại) / Phase 2 (thành công) | Implemented | [UC-003_log_activity.md](UC-003_log_activity.md)             |
| UC-004 | Create OAuth Session      | Phase 2 — Token Issued Handler            | Implemented | [UC-004_create_session.md](UC-004_create_session.md)         |

---

## Device & Session Management

*User xem và quản lý thiết bị / session của chính mình.*

| ID     | Tên                        | Mô tả ngắn                                                  | Trạng thái  | File                                               |
|--------|----------------------------|-------------------------------------------------------------|-------------|----------------------------------------------------|
| UC-007 | List My Devices & Sessions | Xem danh sách thiết bị và trạng thái session của chính mình | Implemented | [UC-007_list_devices.md](UC-007_list_devices.md)   |
| UC-008 | Remote Logout              | Đăng xuất một thiết bị cụ thể từ xa                         | Implemented | [UC-008_remote_logout.md](UC-008_remote_logout.md) |

> **UC-008 Partial**: Application logic (`RevokeSession` handler) đã implement. Endpoint `DELETE /api/v1/sessions/me/{sessionId}` chưa expose.

---

## Activity

| ID     | Tên              | Mô tả ngắn                          | Trạng thái  | File                                                     |
|--------|------------------|-------------------------------------|-------------|----------------------------------------------------------|
| UC-009 | My Login History | Xem lịch sử đăng nhập có phân trang | Implemented | [UC-009_my_login_history.md](UC-009_my_login_history.md) |

---

## Admin Management

*Quản trị viên (ROLE_ADMIN) giám sát và quản lý an ninh hệ thống.*

| ID     | Tên                                | Mô tả ngắn                                                              | Trạng thái | File                                                                               |
|--------|------------------------------------|-------------------------------------------------------------------------|------------|------------------------------------------------------------------------------------|
| UC-011 | IAM Overview Dashboard             | Xem các chỉ số KPI an ninh và vận hành (Users, Devices, Sessions, v.v.) | Planned    | [UC-011_iam_overview.md](UC-011_iam_overview.md)                                   |
| UC-012 | Global Login Activity Log          | Tra cứu lịch sử đăng nhập toàn hệ thống có phân trang và filter         | Planned    | [UC-012_global_login_history.md](UC-012_global_login_history.md)                   |
| UC-013 | Global Active Sessions Management  | Quản lý và ngắt kết nối các phiên đang online toàn hệ thống             | Planned    | [UC-013_global_active_sessions.md](UC-013_global_active_sessions.md)               |
| UC-014 | Admin List User Devices & Sessions | Xem danh sách thiết bị gộp với trạng thái session của một user cụ thể   | Planned    | [UC-014_admin_list_user_sessions.md](UC-014_admin_list_user_sessions.md)           |
| UC-015 | Admin List User Login History      | Xem lịch sử đăng nhập chi tiết của một user cụ thể                      | Planned    | [UC-015_admin_list_user_login_history.md](UC-015_admin_list_user_login_history.md) |

---

## [PLANNED]

| ID     | Tên | Mô tả ngắn               |
|--------|-----|--------------------------|
| UC-010 | MFA | Xác thực đa yếu tố       |
