# Phân tích nghiệp vụ: Admin — Device, Session, Activity

## 1. Bối cảnh hiện tại

Hệ thống đã triển khai các tính năng tự phục vụ (**User Self-service**) cho phép người dùng cuối quản lý an toàn cá nhân của họ:
- **UC-007**: `GET /api/v1/sessions/me` — Xem danh sách devices + sessions của chính mình.
- **UC-008**: `DELETE /api/v1/sessions/me/{sessionId}` — Remote logout thiết bị của chính mình (Partial).
- **UC-009**: `GET /api/v1/login-activities/me` — Xem lịch sử đăng nhập của chính mình.

---

## 2. Nhu cầu nghiệp vụ Admin

### Tại sao Admin cần các tính năng quản trị này?
- **Security Incident Response**: Khi phát hiện tài khoản bị xâm phạm, Admin cần xem ngay tất cả thiết bị, phiên đăng nhập, lịch sử của User đó để cưỡng ép chấm dứt (**Force Terminate**) mọi kết nối.
- **Support**: User phản ánh "có ai đó đang dùng tài khoản tôi" — Admin cần công cụ điều tra mà không cần thông tin đăng nhập của User.
- **Compliance / Audit**: Cần truy vết toàn bộ lịch sử đăng nhập hệ thống để phát hiện các mẫu tấn công diện rộng (Brute-force từ một IP lạ).

---

## 3. Các Use Case cần thiết

### 📊 Tầng Tổng quan (Dashboard Widgets)
**UC-011: Security Overview Dashboard (KPI Cards)**
- **Mục tiêu**: Cung cấp cái nhìn nhanh về trạng thái vận hành.
- **Dữ liệu**: `Total Users`, `Total Devices`, `Online Sessions`, `Failed Logins (Today)`.

### 🌎 Tầng Toàn cục (Global Management)
**UC-012: Global Login Activity Log**
- **Endpoint**: `GET /api/v1/admin/iam/overview`
- **Mô tả**: Danh sách toàn bộ lịch sử SUCCESS/FAILED của tất cả User.
- **Filter**: `ipAddress`, `result` (FAILED/SUCCESS), `username`.

**UC-013: Global Active Sessions Management**
- **Endpoint**: `GET /api/v1/admin/active-sessions`
- **Mô tả**: Danh sách chỉ các phiên đang ở trạng thái `ACTIVE` toàn hệ thống.
- **Action**: Nút **"Force Terminate"** để hủy phiên ngay lập tức.

### 👤 Tầng Chi tiết User (Admin User-centric View)
**UC-014: Admin List User Devices & Sessions (Merged View)**
- **Endpoint**: `GET /api/v1/admin/users/{userId}/sessions`
- **Nghiệp vụ**: Gộp danh sách Thiết bị và Phiên thành một bảng duy nhất tại trang Profile Admin.
- **Mô tả**: Hiển thị **tất cả** thiết bị User từng dùng. Dòng nào đang online thì hiển thị trạng thái `ACTIVE` và nút **"Revoke Session"**.

**UC-015: Admin List User Login History**
- **Endpoint**: `GET /api/admin/v1/users/{userId}/login-activities`
- **Mô tả**: Tra cứu lịch sử đăng nhập riêng biệt cho từng User để giải đáp khiếu nại.

---

## 4. Phân tích điểm khác biệt so với User self-service

| Đặc điểm         | User self-service                      | Admin Management                                   |
|:-----------------|:---------------------------------------|:---------------------------------------------------|
| **Target**       | `sub` từ Access Token                  | `targetUserId` từ path param hoặc query            |
| **Auth check**   | Ownership (`userId == sub`)            | Role check (`ROLE_ADMIN`)                          |
| **isCurrent**    | Có — dùng để chặn tự logout chính mình | Không — Admin không có phiên trên thiết bị User    |
| **Revoke limit** | Không được revoke session hiện tại     | **Được phép** revoke bất kỳ session nào            |
| **Audit trail**  | Không bắt buộc                         | **Bắt buộc** ghi log `adminId` thực hiện hành động |

---

## 5. Vị trí trong kiến trúc & Package Placement

### Service Owner: `oauth2-service`
Vì toàn bộ dữ liệu về Device, Session, Activity đều được quản lý tại đây.

### Authorization Flow:
1. Angular gọi qua Web Gateway (Token Relay).
2. OAuth2 Service kiểm tra Access Token có chứa authority `ROLE_ADMIN`.

### Package Placement (application/):
```text
application/
├── device/
│   └── admin_query/           ← UC-011, UC-014
├── session/
│   ├── admin_query/           ← UC-013
│   └── admin_revoke/          ← Xử lý Revoke chung cho Admin (UC-013, UC-014)
└── activity/
    └── admin_query/           ← UC-012, UC-015
```

---

## 6. Giao diện & Trải nghiệm người dùng (UX)

- **Dashboard Home**: Hiển thị 4 thẻ KPI số liệu (UC-011).
- **Admin Menu**: Thêm 2 mục "Nhật ký đăng nhập" (UC-012) và "Quản lý phiên online" (UC-013).
- **User Detail Page**: Thêm Tab **"Security & Devices"** hiển thị danh sách gộp (UC-014) và lịch sử riêng (UC-015).
