# Task: Admin IAM Overview Dashboard (UC-011)

## Trạng thái
- [ ] Task mới — chưa implement

## Definition of Done
- [ ] Dashboard hiển thị đúng 4 thẻ số liệu KPI (Users, Devices, Sessions, Failed Logins Today).
- [ ] Dữ liệu được tổng hợp chính xác từ 3 domain (User, Device, Session/Activity).
- [ ] Truy cập được bảo vệ bởi quyền `ROLE_ADMIN`.

---

## Task 1 — oauth2-service: Dashboard Backend

### Đọc trước
- `services/oauth2/CLAUDE.md`
- `services/oauth2/SERVICE_MAP.md`
- `services/oauth2/docs/use-cases/UC-011_iam_overview.md`

### Implement logic
- **Package**: `application/iam_dashboard/overview/`
- **Query**: `IamOverviewQuery` (không input).
- **Handler**: `IamOverviewHandler`.
- **Logic**: Sử dụng native SQL count trên các bảng:
  · Total Users (từ identity projection).
  · Total Devices (bảng `devices`).
  · Active Sessions (bảng `oauth_sessions` WHERE status='ACTIVE').
  · Failed Logins (bảng `login_activities` WHERE result!='SUCCESS' AND createdAt >= TODAY).
- **Controller**: `IamDashboardController` expose `GET /api/v1/admin/iam/overview`.

---

## Task 2 — Generate API Contract

```
Cập nhật docs/api/oauth2-sessions-history.yaml:
- Thêm path: GET /api/v1/admin/iam/overview
- Định nghĩa Schema: IamOverviewResponse { totalUsers, totalDevices, activeSessions, failedLoginsToday }
```

---

## Task 3 — web (Angular): Dashboard Widgets

### Implement Logic
- **Admin IAM Service**: Thêm `getOverview(): Observable<IamOverviewData>`.
- **Dashboard Component**: Gọi API và gán dữ liệu vào các signals.

### Implement UI
- Tạo 4 thẻ KPI cards ở trên cùng trang chủ Admin.
- Mỗi card gồm: Icon, Label, Value.

---

## Task 4 — web (Angular) — Styling (Gemini)
Yêu cầu Gemini tạo giao diện Dashboard hiện đại, sử dụng Angular Material Cards, thiết kế responsive và có màu sắc nhấn mạnh các con số an ninh.
