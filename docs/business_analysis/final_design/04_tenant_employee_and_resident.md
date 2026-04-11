# Tenant Employee & Resident — Design Specification

*Tài liệu thiết kế cuối cùng. Tham khảo phân tích tại: `../ACTOR_FUNCTION_ANALYSIS.md`*

---

## 1. So sánh hai actor

### 1.1 Vị trí trong hệ thống

```
TENANT_EMPLOYEE:
  Person → Employment → Organization (TENANT)
  Nhân viên của công ty đang thuê văn phòng trong tòa nhà
  Dùng tòa nhà như môi trường làm việc hàng ngày

RESIDENT:
  Person → PartyRole (OWNER hoặc LESSEE) → Unit
  Cư dân đang sống tại căn hộ trong tòa nhà
  Dùng tòa nhà như nơi sinh sống
```

### 1.2 Feature overlap — Rất lớn

```
Feature                  │ TENANT_EMPLOYEE        │ RESIDENT
─────────────────────────┼────────────────────────┼──────────────────────
Access card              │ ✓ vào văn phòng        │ ✓ vào chung cư
Facility booking         │ ✓ meeting room, gym    │ ✓ gym, BBQ, pool
Visitor registration     │ ✓                      │ ✓
Parking                  │ ✓                      │ ✓
Attendance / presence    │ ✓ check-in tòa nhà     │ ~ (ít quan tâm)
Announcement từ BQL      │ ✓                      │ ✓
Announcement từ org      │ ✓ (từ công ty mình)    │ ✗
Billing / hóa đơn        │ ✗ (công ty trả)        │ ✓ (tự trả phí DV)
Community board          │ ✗                      │ ✓
Package / delivery       │ ✗                      │ ✓
```

**Nhận xét**: Hai actor dùng phần lớn cùng building services. Sự khác biệt chính:
- TENANT_EMPLOYEE không có billing (công ty trả)
- TENANT_EMPLOYEE có announcement channel từ công ty mình
- RESIDENT có billing, community features, package management

### 1.3 Điểm đặc thù — Announcement có 2 nguồn với TENANT_EMPLOYEE

```
Nguồn 1: BQL tòa nhà (OPERATOR scope)
  → "Thang máy bảo trì ngày 15/4"
  → Broadcast theo tòa nhà hoặc theo tầng
  → Cả TENANT_EMPLOYEE và RESIDENT đều nhận

Nguồn 2: Công ty của họ (TENANT scope, orgId=company-z)
  → "Họp toàn công ty thứ Sáu, 9h"
  → Chỉ nhân viên cùng orgId thấy
  → RESIDENT không có nguồn này
```

UIElement phân biệt: merged feed với label nguồn, hoặc 2 section riêng trong màn hình
announcement. Cần quyết định UX khi implement.

---

## 2. Portal và navigation

### 2.1 Chung một SPA, khác context

Theo thiết kế multi-portal Phase 1, TENANT_EMPLOYEE và RESIDENT dùng chung 1 Angular SPA.
UIElement.scope filter điều khiển navigation mỗi người thấy gì.

```
TENANT_EMPLOYEE login → activeScope=TENANT, activeOrgId=company-z
→ navigate về /tenant/**
→ GET /abac/navigate?scope=TENANT&orgId=company-z
→ visibilityMap chỉ chứa UIElement scope=TENANT

RESIDENT login → activeScope=RESIDENT, activeOrgId=unit-10b
→ navigate về /resident/**
→ GET /abac/navigate?scope=RESIDENT&orgId=unit-10b
→ visibilityMap chỉ chứa UIElement scope=RESIDENT
```

### 2.2 Navigation map — TENANT_EMPLOYEE

```
/tenant/dashboard          → Overview (bookings sắp tới, announcements mới)
/tenant/access-card        → My access card (xem trạng thái)
/tenant/booking            → Book facility
/tenant/booking/my         → My bookings
/tenant/visitor            → Register visitor / My visitor registrations
/tenant/parking            → My parking registration
/tenant/attendance         → My attendance history
/tenant/announcements      → Announcements (BQL + Company)
```

### 2.3 Navigation map — RESIDENT

```
/resident/dashboard        → Overview (hóa đơn cần thanh toán, announcements)
/resident/access-card      → My access card
/resident/booking          → Book facility (gym, BBQ, pool, v.v.)
/resident/booking/my       → My bookings
/resident/visitor          → Register visitor
/resident/parking          → My parking
/resident/billing          → Bills & payments
/resident/announcements    → Announcements từ BQL
/resident/community        → Community board
/resident/package          → Package & delivery
```

---

## 3. Một người có thể có cả 2 context

Trường hợp thực tế: người X là nhân viên của Công ty Z (văn phòng tầng 5) VÀ là cư dân
căn hộ tầng 10 trong cùng tòa nhà.

```
RoleContext của X:
  scope=TENANT,   orgId=company-z,  roles=[TENANT_EMPLOYEE]
  scope=RESIDENT, orgId=unit-10b,   roles=[RESIDENT]
```

Hai-phase auth xử lý tự nhiên:

```
Login → Bare Token
GET /auth/contexts → [
  { scope: "TENANT",   orgId: "company-z", orgName: "Công ty Z",    roles: ["TENANT_EMPLOYEE"] },
  { scope: "RESIDENT", orgId: "unit-10b",  orgName: "Căn hộ 10B",   roles: ["RESIDENT"] }
]
→ FE hiển thị context selector: "Bạn muốn vào với tư cách nào?"
```

Switch context trong session: POST /auth/context/activate với context mới → token mới →
FE reload navigation. Người dùng thấy đúng menu của context đang active.

---

## 4. Dùng chung app hay tách app?

### Quyết định: Chung app, khác context (Phase 1)

Lý do:
- Overlap features lớn → tránh duplicate code
- UIElement.scope + context switching đã handle tự nhiên
- UX nhất quán (cùng design system, cùng navigation pattern)
- Multi-portal Phase 1 đã thiết kế cho đúng use case này

### Khi nào nên tách (Phase 2+)?

```
Tách TENANT portal thành app riêng khi:
  • UX cần khác biệt lớn (mobile-first cho nhân viên vs web cho cư dân)
  • Branding per tenant (Company Z muốn custom logo, màu sắc)
  • Scale yêu cầu deployment riêng

Tách RESIDENT portal thành app riêng khi:
  • Cần mobile app native (iOS/Android) riêng cho cư dân
  • Features resident quá phong phú để share với tenant
```

Thiết kế Phase 1 (path-based, UIElement scope) không cản trở việc tách sau này —
đây là điểm đã được đảm bảo trong multi-portal architecture.

---

## 5. Data isolation

### 5.1 TENANT_EMPLOYEE

- Chỉ thấy data có `tenantId = activeOrgId` của mình
- Không thấy data của tenant khác trong cùng tòa nhà
- Attendance: chỉ xem của bản thân (TENANT_EMPLOYEE), TENANT_ADMIN xem toàn org

### 5.2 RESIDENT

- Chỉ thấy data có `unitId = activeOrgId` của mình
- Billing: chỉ xem hóa đơn của unit mình
- Community board: thấy tất cả post trong tòa nhà (public), không phải per-unit

### 5.3 ABAC rules

```
// TENANT_EMPLOYEE chỉ xem own data:
subject.roles.contains('TENANT_EMPLOYEE')
  AND object.getAttribute('userId') == subject.userId
→ PERMIT: attendance:read:own, booking:read:own, visitor:read:own

// RESIDENT chỉ xem billing của unit mình:
subject.roles.contains('RESIDENT')
  AND object.getAttribute('unitId') == subject.getAttribute('orgId')
→ PERMIT: billing:read

// Community board — public trong tòa nhà:
subject.roles.contains('RESIDENT')
  AND object.getAttribute('buildingId') == targetBuilding
→ PERMIT: community:read
```

---

## 6. UIElement mapping (ví dụ)

```
elementId                        │ scope    │ actionId
─────────────────────────────────┼──────────┼────────────────────────────
nav_tenant_booking               │ TENANT   │ facility:booking:navigate
nav_tenant_visitor               │ TENANT   │ visitor:navigate
nav_tenant_announcements         │ TENANT   │ announcement:navigate
nav_tenant_attendance            │ TENANT   │ attendance:navigate
btn_tenant_book_facility         │ TENANT   │ facility:booking:create
btn_tenant_register_visitor      │ TENANT   │ visitor:create
nav_resident_booking             │ RESIDENT │ facility:booking:navigate
nav_resident_billing             │ RESIDENT │ billing:navigate
nav_resident_community           │ RESIDENT │ community:navigate
btn_resident_pay_bill            │ RESIDENT │ billing:pay
btn_resident_post_community      │ RESIDENT │ community:post:create
```

Cùng `actionId: facility:booking:navigate` nhưng khác `elementId` và khác `scope` —
đây là 2 UI artifact cho cùng 1 capability ở 2 portal khác nhau. Không phải duplicate.

---

*Tài liệu thiết kế — 2026-04-11*
