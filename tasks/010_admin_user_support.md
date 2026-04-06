# Task: Admin User Security Support (UC-014, UC-015)

## Trạng thái
- [ ] Task mới — chưa implement

## Definition of Done
- [ ] Admin xem được danh sách gộp Thiết bị & Phiên của một User cụ thể.
- [ ] Admin có thể thực hiện Revoke từ trang Profile Admin của User đó.
- [ ] Admin xem được lịch sử đăng nhập chi tiết của riêng User đó.

---

## Task 1 — oauth2-service: User Support Backend

### 1.1 Implement UC-014 (Merged View)
- Package: `application/device/admin_query/`
- Logic: Left Join `devices` với `oauth_sessions` (lọc theo `targetUserId` và ACTIVE status).

### 1.2 Implement UC-015 (Individual History)
- Package: `application/login_activity/admin_query/`
- Logic: Query `login_activities` lọc theo `targetUserId`.

---

## Task 2 — Generate API Contract

```
Cập nhật docs/api/oauth2-sessions-history.yaml:
- GET /api/v1/admin/users/{userId}/sessions
- GET /api/v1/admin/users/{userId}/login-activities
```

---

## Task 3 — web (Angular): Support UI

### 3.1 Update User Profile Admin
- Thêm Tab: "Security & Devices".
- Table 1: Devices & Sessions (Merged View) + Nút Revoke.
- Table 2: User Login History.

---

## Task 4 — web (Angular) — Styling (Gemini)
Yêu cầu Gemini thiết kế Tab Security tích hợp vào giao diện Profile hiện tại, sử dụng MatTabs và danh sách dạng List Cards hoặc Table gọn gàng.
