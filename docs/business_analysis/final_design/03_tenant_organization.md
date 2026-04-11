# Tenant Organization — Design Specification

*Tài liệu thiết kế cuối cùng. Tham khảo phân tích tại: `../ACTOR_FUNCTION_ANALYSIS.md`, `../abac_and_ui_element_on_authz.md`*

---

## 1. Định nghĩa Tenant doanh nghiệp

Tenant doanh nghiệp là một **tổ chức** (công ty, shop owner, v.v.) thuê không gian trong tòa nhà.
Đây là khách hàng B2B gián tiếp của Apartcom — họ dùng hệ thống thông qua quyền được BQL cấp.

```
Platform Apartcom (bán phần mềm)
  └── Building X (mua phần mềm để quản lý tòa nhà)
        └── Company Z (thuê văn phòng trong Building X)
              └── Nhân viên Company Z (end-user của TENANT portal)
```

Apartcom không cung cấp business tools cho Company Z — chỉ cung cấp **building-context services**.

---

## 2. Scope hỗ trợ — 2 Tier

### 2.1 Tier 1 — Building Access Services (bắt buộc)

Là lý do tenant có tài khoản trong hệ thống:

| Feature | Mô tả | Ai thực hiện |
|---|---|---|
| Access card | Request cấp thẻ vào tòa nhà cho nhân viên | TENANT_ADMIN request, BQL approve |
| Facility booking | Đặt meeting room, tiện ích chung | TENANT_ADMIN + TENANT_EMPLOYEE |
| Billing | Xem hóa đơn phí dịch vụ, thanh toán | TENANT_ADMIN |
| Visitor registration | Đăng ký khách thăm | TENANT_ADMIN + TENANT_EMPLOYEE |
| Parking | Đăng ký xe | TENANT_ADMIN + TENANT_EMPLOYEE |
| Announcements | Nhận thông báo từ BQL | TENANT_ADMIN + TENANT_EMPLOYEE |

### 2.2 Tier 2 — Tenant Internal Operations (value-added)

Quản lý nội bộ của tenant trong building context:

| Feature | Mô tả | Ai thực hiện |
|---|---|---|
| Employee directory | Danh sách nhân viên đang làm việc tại tòa nhà | TENANT_ADMIN |
| Access card management | Quản lý ai có thẻ, ai cần thêm/thu hồi | TENANT_ADMIN |
| Attendance / presence | Xem lịch sử check-in/out nhân viên tại tòa nhà | TENANT_ADMIN |
| Internal announcement | Gửi thông báo nội bộ cho nhân viên cùng org | TENANT_ADMIN |

**Ranh giới quan trọng**: Tất cả features Tier 1+2 đều là **building-context** — không bao gồm
quy trình nội bộ của doanh nghiệp (lương, KPI, workflow nghiệp vụ, CRM khách hàng).

---

## 3. Role Model

### 3.1 Chỉ cần 2 roles cho Tier 1+2

Tier 1+2 đều là building-context features → nhu cầu phân quyền **uniform** giữa các loại hình
doanh nghiệp. Không cần sub-role phức tạp.

```
TENANT_ADMIN
  Capabilities:
  • Full CRUD: employee directory, access card requests, booking management
  • Gửi internal announcement
  • Xem attendance của toàn bộ nhân viên trong org
  • Assign/revoke TENANT_EMPLOYEE cho user trong cùng orgId
  
  Constraints:
  • Chỉ assign TENANT_* roles
  • Chỉ trong orgId của mình
  • Không edit PolicySet
  • Không thấy data của tenant khác

TENANT_EMPLOYEE
  Capabilities:
  • Book facility (meeting room, gym, v.v.)
  • Register visitor
  • Xem own attendance / access history
  • Nhận announcement (từ BQL + từ công ty)
  • Đăng ký parking
```

Lý do 2 roles là đủ: Sự khác biệt giữa các doanh nghiệp thể hiện ở *ai họ chỉ định làm
TENANT_ADMIN*, không phải ở số lượng loại admin. Công ty có office manager, HR, hay CEO lo
building affairs — đều có cùng capabilities trong hệ thống.

### 3.2 Lifecycle của RoleContext

```
Onboarding:
  Platform admin tạo RoleContext { scope=TENANT, orgId=company-z, roles=[TENANT_ADMIN] }
  cho người đầu tiên của tenant

TENANT_ADMIN tự quản:
  Assign TENANT_EMPLOYEE cho nhân viên mới
  Revoke khi nhân viên nghỉ việc

Offboarding tenant:
  BQL/Platform admin deactivate toàn bộ RoleContext của orgId=company-z
```

---

## 4. Cross-boundary — BQL đọc dữ liệu Tenant

### 4.1 Ranh giới rõ ràng

BQL cần một số dữ liệu của tenant để vận hành tòa nhà:

```
BQL được phép đọc:                    BQL KHÔNG được phép đọc:
──────────────────────────────────    ──────────────────────────────
• Danh sách nhân viên (tên, phòng)    • Mức lương
• Số lượng nhân viên active           • Hồ sơ nhân sự chi tiết
• Access card active                  • Nội dung email/tài liệu nội bộ
• Phương tiện đăng ký                 • Hiệu suất công việc
• Hóa đơn phí dịch vụ của tenant     • Dữ liệu kinh doanh nội bộ
```

### 4.2 Enforce 2 tầng

Ranh giới này phải được enforce ở **cả 2 tầng**, không chỉ ABAC:

```
Tầng 1 — Schema/Data level:
  Dữ liệu TENANT_PRIVATE (lương, hồ sơ nhân sự chi tiết) tách biệt về mặt storage
  hoặc không tồn tại trong BMS database (vì Apartcom không quản lý dữ liệu này)

Tầng 2 — ABAC explicit DENY:
  PolicySet OPERATOR phải có Rule DENY tường minh cho resource TENANT_PRIVATE
  Không được implicit deny — phải explicit để audit được
```

### 4.3 ABAC rule cho cross-boundary read

```
// BQL_MANAGER đọc employee summary của tenant thuộc building mình quản lý:
subject.roles.contains('BQL_MANAGER')
  AND subject.getAttribute('scope') == 'OPERATOR'
  AND subject.getAttribute('orgId') == object.getAttribute('buildingId')
  AND action.name == 'employee:read:summary'
→ PERMIT

// Tất cả access khác của OPERATOR vào TENANT_PRIVATE resource:
→ DENY (explicit)
```

---

## 5. Authorization management — Ai quản lý gì

```
Tầng                  │ Ai quản lý                    │ Đối tượng quản lý
──────────────────────┼───────────────────────────────┼────────────────────────────────
Platform define       │ Platform admin / Engineering  │ PolicySet TENANT template
                      │                               │ Capabilities của từng role
                      │                               │ UIElement scope=TENANT
──────────────────────┼───────────────────────────────┼────────────────────────────────
TENANT self-manage    │ TENANT_ADMIN                  │ Ai có role TENANT_EMPLOYEE
                      │                               │ Ai được booking, visitor, v.v.
──────────────────────┼───────────────────────────────┼────────────────────────────────
BQL oversee           │ BQL_MANAGER                   │ Onboard/offboard TENANT_ADMIN
                      │                               │ Approve access card requests
                      │                               │ Read employee summary khi cần
```

TENANT_ADMIN **không** tự edit PolicySet. Nếu cần custom policy phức tạp hơn template —
đây là scope riêng (Tenant Self-Authorization), không build trong giai đoạn này.

---

## 6. CRM Integration — Module bán riêng

### 6.1 Định vị

CRM là **add-on module** được bán thêm cho tenant doanh nghiệp có nhu cầu. Không phải feature
của BMS core. Scope: quản lý business process nội bộ của tenant (leads, contacts, deals, v.v.).

Apartcom BMS không build CRM — BMS là **Identity Provider và Data Provider** cho CRM module.

### 6.2 Kiến trúc tích hợp

```
BMS OAuth2 Server (Identity Provider)
       ↑ authenticate (SSO)
CRM Application (service riêng, domain riêng)
       ↓ API call với scoped token
BMS Resource APIs (attendance, employee summary, access logs)
```

CRM là một **OAuth2 client** đăng ký với BMS OAuth2 server. Không phải module nội tuyến.

### 6.3 SSO Flow

```
[1] User đang dùng TENANT portal (BMS)
    Token: { sub, activeScope: "TENANT", activeOrgId: "company-z" }

[2] User click "Mở CRM" → redirect đến CRM app
    CRM → redirect BMS OAuth2 /authorize
    params: client_id=crm-app, scope="bms:attendance:read bms:employee:summary"

[3] BMS OAuth2 check:
    • User đã authenticated? (có session → skip login form)
    • Company Z có CRM license không? (feature flag check)
    • Nếu OK → issue CRM-scoped token

[4] CRM-scoped token:
    {
      "sub":         "user-001",
      "aud":         "crm-app",
      "activeScope": "TENANT",
      "activeOrgId": "company-z",
      "crm_roles":   ["CRM_ADMIN"],
      "scope":       "bms:attendance:read bms:employee:summary"
    }

[5] User trải nghiệm: single sign-on, không cần login lại
```

### 6.4 BMS Data APIs cho CRM

BMS expose read-only endpoints, CRM consume với scoped token:

```
bms:employee:summary
  GET /tenant/{orgId}/employees
  Response: tên, phòng ban, trạng thái active
  KHÔNG trả: lương, hồ sơ nhân sự chi tiết

bms:attendance:read
  GET /tenant/{orgId}/attendance?from=&to=
  Response: check-in/out times, presence duration
  Scope theo orgId trong token → company-z chỉ thấy data của mình

bms:access_log:read (optional)
  GET /tenant/{orgId}/access-logs
  Response: log ra vào tòa nhà
```

BMS kiểm soát hoàn toàn data gì được expose. CRM không có direct DB access vào BMS.

### 6.5 Authorization trong CRM

CRM có RBAC riêng, **hoàn toàn độc lập** với BMS ABAC:

```
CRM tự define roles (ví dụ):
  CRM_ADMIN        → full access, config pipeline
  CRM_SALESPERSON  → manage own leads/contacts
  CRM_VIEWER       → read-only

CRM_ADMIN của một tenant = TENANT_ADMIN được promote, hoặc do TENANT_ADMIN designate.
CRM tự quản lý assignment này — BMS không cần biết.
```

Lý do tách: CRM authorization là business-specific, khác nhau giữa các loại hình doanh nghiệp.
Đưa vào BMS ABAC sẽ làm phức tạp toàn bộ authorization core.

### 6.6 Feature flag / Licensing

```
Subscription table (trong BMS admin service):
  orgId=company-z, feature=CRM, status=ACTIVE, validUntil=2027-01-01

OAuth2 flow:
  → BMS check license khi CRM redirect đến /authorize
  → Không có license → error "CRM module not available for this organization"
  → Có license → issue token với CRM scopes
```

### 6.7 Thay đổi BMS cần thiết để support CRM integration

| Component | Thay đổi | Complexity |
|---|---|---|
| OAuth2 service | Register CRM là OAuth2 client | Low |
| OAuth2 service | Support custom scope `bms:*` | Low |
| OAuth2 service | Feature flag check trong authorization flow | Low |
| BMS APIs | 2-3 read-only endpoints với scope validation | Low-Medium |
| ABAC core | Không thay đổi | Zero |
| UIElement / nav | Không thay đổi | Zero |

**Điểm quan trọng**: Kiến trúc này không tạo thêm complexity cho BMS core. CRM là consumer
của BMS APIs, không phải một phần của BMS.

---

## 7. Những gì nằm ngoài scope của BMS

```
✗ Business process / workflow nội bộ của tenant
✗ CRM (quản lý khách hàng của tenant) — bán riêng nếu có nhu cầu
✗ Payroll / lương
✗ Internal document management
✗ Tenant federation (tenant dùng IdP riêng như Azure AD, Okta)
✗ Parent company hierarchy (công ty mẹ xem báo cáo subsidiary)
✗ Tenant self-service ABAC console — defer, build riêng nếu cần
```

---

*Tài liệu thiết kế — 2026-04-11*
