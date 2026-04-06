# Task: Global Security Monitoring (UC-012, UC-013)

## Trạng thái
- [ ] Task mới — chưa implement

## Definition of Done
- [ ] Admin xem được nhật ký đăng nhập toàn hệ thống (Global Activity Log) với filter/pagination.
- [ ] Admin xem được danh sách phiên đang online toàn hệ thống (Global Active Sessions).
- [ ] Admin có thể thực hiện Force Terminate bất kỳ phiên nào từ danh sách Global.
- [ ] Hành động Force Terminate phải được ghi log Audit kèm `adminId`.

---

## Task 1 — oauth2-service: Monitoring Backend

### Đọc trước
- `services/oauth2/docs/use-cases/UC-012_global_login_history.md`
- `services/oauth2/docs/use-cases/UC-013_global_active_sessions.md`

### 1.1 Implement UC-012 (Global Login Log)
- **Package**: `application/login_activity/admin_query/`
- **Logic**: Join `login_activities` với `devices`, bỏ lọc theo `userId` cá nhân.

### 1.2 Implement UC-013 (Global Sessions & Admin Revoke)
- **Query Package**: `application/session/admin_query/` (Lọc status = ACTIVE).
- **Revoke Package**: `application/session/admin_revoke/`.
- **Logic Revoke**: Gọi `SessionTerminationService` + Ghi Audit log + Phát event notification.

---

## Task 2 — Generate API Contract

```
Cập nhật docs/api/oauth2-sessions-history.yaml:
- GET /api/v1/admin/login-activities
- GET /api/v1/admin/active-sessions
- DELETE /api/v1/admin/sessions/{sessionId}
```

---

## Task 3 — web (Angular): Monitoring UI

### 3.1 Global Login Activity Page
- Service: `AdminActivityService.getGlobalActivities(filters)`.
- Page: Table với search Username/IP và filter Result.

### 3.2 Global Active Sessions Page
- Service: `AdminSessionService.getActiveSessions()`, `forceTerminate(sid)`.
- Page: Table hiển thị người dùng online + Nút action "Force Terminate".

---

## Task 4 — web (Angular) — Styling (Gemini)
Yêu cầu Gemini thiết kế 2 trang danh sách chuyên nghiệp sử dụng MatTable, Paginator và các bảng màu nhấn mạnh trạng thái (Online/Offline, Success/Failed).
