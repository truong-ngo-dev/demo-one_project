# Actor & Function Analysis — Properties & Building Management

## 1. Phạm vi phân tích

Phân tích này xác định toàn bộ **Actor** (người dùng/chủ thể) và **Chức năng** (function domains)
trong hệ thống quản lý bất động sản và tòa nhà (Properties & Building Management).

### Loại hình BĐS ảnh hưởng đến Actor Set

| Loại hình                | Đặc điểm                                            |
|--------------------------|-----------------------------------------------------|
| Chung cư (Residential)   | Cư dân ở lâu dài, có HOA/BQL, unit owner ≠ resident |
| Văn phòng (Commercial) x | Công ty thuê cả tầng/tòa, nhân viên đông            |
| Mixed-use                | Kết hợp cả hai — phức tạp nhất                      |
| TTTM (Retail)            | Shop owner thuê mặt bằng, lưu lượng khách cao       |

---

## 2. Actor Map — Phân theo tầng

```
┌─────────────────────────────────────────────────┐
│  PLATFORM OPERATOR (Apartcom)                   │  ← Super Admin, System Config
├─────────────────────────────────────────────────┤
│  PROPERTY OWNER / INVESTOR                      │  ← Chủ đầu tư, báo cáo tài chính
├─────────────────────────────────────────────────┤
│  BUILDING MANAGEMENT (BQL / PMC)                │
│  ├── BQL Manager / Director                     │
│  ├── Finance / Accounting                       │
│  ├── Technical / Engineering                    │
│  ├── Security                                   │
│  ├── Reception / Concierge                      │
│  └── Cleaning / Facility                        │
├─────────────────────────────────────────────────┤
│  TENANT (Tổ chức thuê)                          │
│  ├── [Commercial] Corporate Tenant Admin        │
│  ├── [Commercial] Tenant Employee               │
│  ├── [Residential] Unit Owner (cho thuê lại)    │
│  └── [Retail] Shop Owner                        │
├─────────────────────────────────────────────────┤
│  RESIDENT / END USER                            │
│  ├── [Residential] Cư dân (chủ / người thuê)    │
│  └── [Commercial] Nhân viên công ty thuê        │
├─────────────────────────────────────────────────┤
│  EXTERNAL                                       │
│  ├── Contractor / Vendor (nhà thầu)             │
│  ├── Visitor (khách thăm)                       │
│  └── Inspector / Auditor                        │
└─────────────────────────────────────────────────┘
```

---

## 3. Function Domains

### Domain 1: Asset & Lease Management
*Quản lý tài sản và hợp đồng thuê*

| Chức năng                                          | BQL Manager | Finance | Property Owner |
|----------------------------------------------------|-------------|---------|----------------|
| Quản lý danh mục tòa nhà / tầng / unit             | ✓           | —       | View           |
| Quản lý hợp đồng thuê (ký, gia hạn, chấm dứt)      | ✓           | ✓       | View           |
| Quản lý tiền cọc                                   | ✓           | ✓       | —              |
| Theo dõi tình trạng unit (trống/đang thuê/bảo trì) | ✓           | —       | View           |
| Định giá / pricing                                 | ✓           | —       | ✓              |

---

### Domain 2: Finance & Billing
*Tài chính và hóa đơn*

| Chức năng                       | BQL Finance | Tenant Admin    | Resident  |
|---------------------------------|-------------|-----------------|-----------|
| Phát hành hóa đơn phí dịch vụ   | ✓           | —               | —         |
| Ghi nhận thanh toán             | ✓           | —               | —         |
| Xem hóa đơn của mình            | —           | ✓               | ✓         |
| Thanh toán online               | —           | ✓               | ✓         |
| Hóa đơn điện nước (sub-meter)   | ✓ phát      | —               | ✓ xem/trả |
| Phí gửi xe                      | ✓           | —               | ✓ xem/trả |
| Báo cáo tài chính tòa nhà       | ✓           | —               | —         |
| Xem công nợ của tenant/resident | ✓           | Tenant của mình | —         |

---

### Domain 3: Facility & Maintenance
*Cơ sở vật chất và bảo trì — domain có nhiều actor chồng lấp nhất*

| Chức năng                                     | BQL Technical | Tenant Admin   | Resident | Contractor |
|-----------------------------------------------|---------------|----------------|----------|------------|
| Tạo work order sửa chữa                       | ✓             | Khu vực của họ | Của họ   | —          |
| Assign công việc cho kỹ thuật viên            | ✓             | —              | —        | —          |
| Assign cho nhà thầu                           | ✓             | —              | —        | —          |
| Cập nhật tiến độ                              | ✓             | —              | —        | ✓          |
| Xem lịch sử bảo trì                           | ✓             | Khu vực của họ | Của họ   | —          |
| Bảo trì định kỳ (PPM)                         | ✓             | —              | —        | —          |
| Quản lý vật tư / tồn kho                      | ✓             | —              | —        | —          |
| Quản lý asset tòa nhà (thang máy, PCCC, điện) | ✓             | —              | —        | —          |

---

### Domain 4: Access & Security
*Kiểm soát ra vào*

| Chức năng                 | BQL Security | Reception | Tenant Admin     | Resident  |
|---------------------------|--------------|-----------|------------------|-----------|
| Cấp / thu hồi access card | ✓            | ✓         | Nhân viên của họ | —         |
| Đăng ký khách thăm        | —            | ✓         | ✓                | ✓         |
| Blacklist visitor         | ✓            | —         | —                | —         |
| Xem log ra vào            | ✓            | —         | —                | Của mình  |
| Quản lý xe / parking      | ✓            | ✓         | —                | ✓ đăng ký |
| Giám sát CCTV             | ✓            | —         | —                | —         |

---

### Domain 5: HR & Staff (BQL nội bộ)
*Nhân sự nội bộ ban quản lý*

| Chức năng                     | BQL Director | BQL Manager | Staff         |
|-------------------------------|--------------|-------------|---------------|
| Quản lý hồ sơ nhân sự         | ✓            | ✓           | Của mình      |
| Chấm công / timesheet         | —            | ✓           | ✓ self-report |
| Lịch trực / shift scheduling  | —            | ✓           | View          |
| Phân công công việc hàng ngày | —            | ✓           | View          |

---

### Domain 6: Tenant HR *(Value-added — optional)*
*BQL cung cấp thêm module nhân sự cho tenant — dùng chung hạ tầng*

| Chức năng                     | Tenant Admin | Tenant HR | Tenant Employee |
|-------------------------------|--------------|-----------|-----------------|
| Quản lý nhân viên nội bộ      | ✓            | ✓         | —               |
| Chấm công nhân viên           | —            | ✓         | ✓ check-in      |
| Quản lý access card nhân viên | ✓            | ✓         | —               |

---

### Domain 7: Amenities & Community
*Tiện ích và cộng đồng*

| Chức năng                                           | BQL        | Tenant Admin | Resident |
|-----------------------------------------------------|------------|--------------|----------|
| Quản lý danh mục tiện ích (gym, pool, meeting room) | ✓          | —            | —        |
| Đặt chỗ tiện ích                                    | —          | ✓            | ✓        |
| Gửi thông báo / announcement                        | ✓          | Tenant space | —        |
| Khiếu nại / feedback                                | —          | ✓            | ✓        |
| Community board                                     | ✓ moderate | —            | ✓ post   |
| Package / delivery management                       | Reception  | —            | ✓ nhận   |

---

### Domain 8: Reporting & Analytics
*Báo cáo và phân tích*

| Chức năng               | Super Admin | Property Owner | BQL Director | BQL Finance |
|-------------------------|-------------|----------------|--------------|-------------|
| Occupancy rate          | —           | ✓              | ✓            | —           |
| Revenue report          | —           | ✓              | ✓            | ✓           |
| Maintenance cost report | —           | View           | ✓            | ✓           |
| Tenant satisfaction     | —           | View           | ✓            | —           |
| Platform-wide analytics | ✓           | —              | —            | —           |

---

## 4. Điểm phức tạp — Cần lưu ý thiết kế

### 4.1 Unit Owner ≠ Resident (chung cư)

Chủ căn hộ có thể cho thuê lại. Khi đó hai actor tồn tại độc lập trên cùng một unit:

- **Unit Owner**: xem hợp đồng, nhận báo cáo thu nhập, không cần vào tòa nhà hàng ngày.
- **Resident** (người thuê): dùng tiện ích, trả phí dịch vụ hàng tháng, có access card.

→ Party Model handle qua Relationship:
```
Person (Owner) → OWNS       → Unit
Person (Resident) → LEASES  → Unit
```

### 4.2 Tenant Employee ≠ Tenant Admin (văn phòng)

Nhân viên công ty thuê văn phòng: chỉ cần access card, đặt meeting room, xem thông báo.
Không có quyền admin gì. Đây là actor đông nhất nhưng ít quyền nhất.

### 4.3 Contractor lifecycle đặc thù

Nhà thầu không phải user thường trực:
- Được mời vào theo từng work order cụ thể
- Access tạm thời, có ngày hết hạn
- Cần audit trail riêng

→ Không nên model như user thông thường, không có Employment với BQL.

### 4.4 Property Owner vs BQL Director

- Tòa nhà nhỏ: thường là cùng một người.
- PMC lớn: hoàn toàn tách biệt — Owner chỉ xem báo cáo, không vận hành hệ thống hàng ngày.

---

## 5. Tổng hợp — Actor → Scope Mapping

| Actor                              | Party Model                          | ABAC Scope | App                |
|------------------------------------|--------------------------------------|------------|--------------------|
| Platform Super Admin               | —                                    | SYSTEM     | Admin portal       |
| Property Owner                     | Organization (INVESTOR)              | OWNER      | Portal (read-only) |
| BQL Director / Manager             | Organization (OPERATOR) + Employment | OPERATOR   | Admin portal       |
| BQL Finance / Technical / Security | Organization (OPERATOR) + Employment | OPERATOR   | Admin portal       |
| Corporate Tenant Admin / HR        | Organization (TENANT) + Employment   | TENANT     | Tenant portal      |
| Corporate Tenant Employee          | Person + Employment → TENANT org     | TENANT     | Tenant app (light) |
| Unit Owner (tự ở)                  | Person + PartyRole = OWNER           | OWNER      | Resident app       |
| Unit Owner (cho thuê)              | Person + PartyRole = OWNER           | OWNER      | Resident app       |
| Resident (người thuê căn hộ)       | Person + PartyRole = RESIDENT        | RESIDENT   | Resident app       |
| Shop Owner (retail tenant)         | Organization (TENANT) + Employment   | TENANT     | Tenant portal      |
| Contractor / Vendor                | Organization (VENDOR) + Relationship | EXTERNAL   | Limited portal     |
| Visitor                            | —                                    | —          | Self-service kiosk |

---

## 6. Ghi chú kiến trúc

- **Backend & Database**: Core logic (Party, HR, Lease, Maintenance) là duy nhất và dùng chung.
  Mọi query đều bắt buộc phải kèm `context_org_id` hoặc `tenant_id`.
- **Authorization (ABAC)**: Đóng vai trò "người gác cổng đa nhân cách" — đọc Subject context
  để áp dụng đúng PolicySet (OPERATOR / TENANT / RESIDENT).
- **Frontend**: Mỗi actor group có thể cần app riêng (Admin portal, Tenant portal, Resident mobile app)
  do UX khác biệt lớn. UIElement Registry + Dynamic Navigation xử lý visibility trong cùng một app;
  nếu UX quá khác nhau thì tách app là quyết định đúng hơn.

---

## 7. Vấn đề mở — Cần giải quyết trước khi ra bản phân tích cuối cùng

Các vấn đề dưới đây chưa có quyết định thiết kế. Mỗi vấn đề ảnh hưởng trực tiếp đến cách
ABAC và UIElement Registry được thiết kế — giải quyết sai hoặc bỏ qua sẽ dẫn đến phải refactor
toàn bộ authorization layer sau này.

---

### Vấn đề 1: Multi-hat Identity — Một người nhiều vai

**Mô tả:**
Một Person có thể đồng thời là:
- Resident tại căn hộ tầng 10
- Tenant Employee của Công ty X tại tầng 5
- Unit Owner của một căn hộ khác đang cho thuê

Party Model xử lý được phần data (nhiều Relationship trên cùng một Person). Phần chưa được
giải quyết là **session và token phải phản ánh active context nào** khi người này đăng nhập.

**Câu hỏi cần quyết định:**
- Token phát ra mang context nào khi một người có nhiều vai?
- Context switching có re-issue token không, hay chỉ là state trên frontend?
- Nếu re-issue token: oauth2 service cần biết danh sách Relationship của Person để cho phép
  chọn context — ai cung cấp dữ liệu đó?

**Ảnh hưởng đến ABAC:**

`AdminSubjectProvider` hiện build Subject từ `userId + roles[]`. Với multi-hat identity, Subject
phải phản ánh đúng active context:

```
# Sai — load tất cả roles của person bất kể context
Subject { userId, roles: ["RESIDENT", "TENANT_EMPLOYEE", "UNIT_OWNER"] }

# Đúng — chỉ load roles thuộc active context
Subject { userId, roles: ["TENANT_EMPLOYEE"], attributes: { tenantId: "X", orgId: "..." } }
```

Nếu Subject chứa tất cả roles cùng lúc, SpEL expression không thể phân biệt người dùng đang
hành động với tư cách nào — PERMIT/DENY sẽ sai.

**Ảnh hưởng đến UIElement:**

Navigation simulation nhận Subject với active context → chỉ evaluate UIElement thuộc context
đó. Nếu Subject mang tất cả roles, toàn bộ menu của cả 3 vai sẽ VISIBLE cùng lúc.

Cần quyết định: `simulate-navigation` nhận `contextId` như một parameter tường minh, hay tự
suy ra từ Subject?

---

### Vấn đề 2: Cross-Tenant Access Boundary — BQL thấy được bao nhiêu dữ liệu Tenant?

**Mô tả:**
BQL (OPERATOR scope) cần một số dữ liệu của Tenant để vận hành tòa nhà (danh sách nhân viên
để cấp access card, hóa đơn để thu phí). Nhưng không được phép xem dữ liệu nhạy cảm nội bộ
của Tenant (lương, hồ sơ nhân sự chi tiết, nội dung email nội bộ).

Ranh giới này chưa được định nghĩa tường minh.

**Câu hỏi cần quyết định:**
- Những resource nào của Tenant được phép BQL đọc? Danh sách tường minh.
- Cơ chế nào enforce ranh giới đó: ABAC policy, DB-level isolation, hay cả hai?

**Ảnh hưởng đến ABAC:**

ABAC policy **không thể là cơ chế isolation duy nhất**. Policy chặn ở application layer —
DB admin, compromised service account, hoặc bug trong SubjectProvider đều bypass được.

Ranh giới cần được quyết định ở hai tầng:

```
Tầng 1 — DB/Schema level (quyết định kiến trúc):
  Dữ liệu TENANT_PRIVATE (lương, hồ sơ nhân sự) → separate schema hoặc encrypted column

Tầng 2 — ABAC policy (bổ sung, không thay thế tầng 1):
  PolicySet OPERATOR phải có Rule DENY tường minh cho resource TENANT_PRIVATE
  Không được implicit deny — phải explicit để audit được
```

Nếu chưa quyết định tầng 1, mọi thiết kế ABAC cho cross-tenant access là không đáng tin cậy.

**Ảnh hưởng đến UIElement:**

UIElement của các chức năng "nhạy cảm" (xem lương Tenant, hồ sơ chi tiết nhân viên Tenant)
không được xuất hiện trong navigation simulation của OPERATOR user — ngay cả khi ABAC trả DENY,
menu item vẫn không nên hiển thị.

Cần thêm metadata trên UIElement để đánh dấu `sensitivityLevel` hoặc `ownerScope`, dùng để
pre-filter trước khi đưa vào PDP.

---

### Vấn đề 3: Instance-level Authorization — Data scope vs Feature scope

**Mô tả:**
Hiện tại ABAC evaluate ở resource-type level:

```
"User này có được phép thực hiện finance:invoice:view không?" → PERMIT/DENY
```

Nhưng nhiều chức năng cần instance-level decision:

```
"User này có được phép xem hóa đơn #4521 (thuộc Tenant B) không?" → khác nhau tùy người hỏi
```

Trong `AuthzRequest` hiện tại, không có object attributes — `resource` chỉ là abstract resource
name (`finance:invoice`), không phải instance với `tenantId`, `orgId`, hay `ownerId`.

**Câu hỏi cần quyết định:**
- `AuthzRequest` có được mở rộng để nhận object attributes không?
- Ai inject các attribute đó (caller, PEP, hay middleware)?
- SpEL expression viết ở đâu để reference `object.tenantId`?

**Ảnh hưởng đến ABAC:**

Nếu không có object attributes, mọi SpEL expression chỉ có thể làm type-level check:

```text
// Có thể viết — type-level
subject.roles.contains('FINANCE') && action.name == 'finance:invoice:view'

// Không thể viết — instance-level (thiếu object context)
subject.getAttribute('tenantId') == object.tenantId
```

Hầu hết các data isolation requirement trong hệ thống multi-tenant đều cần instance-level.
Thiếu cơ chế này, ABAC chỉ giải quyết được feature visibility, không giải quyết được data
boundary.

**Ảnh hưởng đến UIElement:**

Navigation simulation (`simulate-navigation`) hoạt động ở type-level — đúng cho mục đích
ẩn/hiện menu. Nhưng không thể thay thế instance-level check tại thời điểm thực thi request.

Cần làm rõ: UIElement + navigation simulation chỉ là **UX layer** (ẩn những gì user không
có quyền thấy), còn PEP enforcement tại API boundary mới là **security layer** thực sự. Hai
lớp này phải cùng tồn tại, không thể dùng một trong hai để thay thế cái còn lại.

---

### Vấn đề 4: UIElement Action Overlap — Nhiều persona chia sẻ cùng Action

**Mô tả:**
Khi nhiều persona khác nhau có quyền thực hiện cùng một action nhưng cần hiển thị menu khác
nhau, `simulate-navigation` sẽ trả VISIBLE cho tất cả UIElement trỏ về action đó:

```
menu_bql_staff    → hr:employee:view  ─┐
menu_tenant_staff → hr:employee:view  ─┤→ Cả 3 VISIBLE nếu user có quyền hr:employee:view
menu_resident_?   → hr:employee:view  ─┘
```

**Câu hỏi cần quyết định:**
- UIElement có cần thêm trường `targetScope` (OPERATOR / TENANT / RESIDENT / ALL) không?
- `simulate-navigation` có pre-filter UIElement theo Subject scope trước khi đưa vào PDP không?
- Hay tách thành các action riêng biệt per-persona (làm phình resource catalogue)?

**Ảnh hưởng đến ABAC:**

Nếu tách action per-persona (`hr:employee:view:operator` vs `hr:employee:view:tenant`):
- Resource catalogue tăng gấp N lần theo số persona
- Policy cũng tăng theo — maintainability giảm
- Backend API phải nhận action name khác nhau tùy caller

Nếu thêm `targetScope` vào UIElement và pre-filter:
- Resource catalogue gọn hơn
- Navigation simulation cần thêm bước pre-filter trước PDP
- Subject phải carry active scope để pre-filter hoạt động → phụ thuộc vào Vấn đề 1

**Ảnh hưởng đến UIElement:**

`UIElement` hiện có: `elementId`, `label`, `type`, `elementGroup`, `resourceId`, `actionId`.

Cần bổ sung: `targetScope` hoặc cơ chế tương đương để navigation simulation biết UIElement
này dành cho persona nào — thiếu trường này thì không thể giải quyết overlap mà không tách action.

---

### Vấn đề 5: Contractor Access — Time-bound và Work-order-scoped

**Mô tả:**
Contractor là Organization (VENDOR) với nhân viên là Person có Employment. Party Model xử lý
được phần data. Phần chưa được thiết kế là access của contractor bị giới hạn bởi:
- Work order cụ thể được assign
- Thời gian hiệu lực của work order
- Chỉ được thấy thông tin liên quan đến công việc của họ

Đây là instance-level + time-bound policy — phức tạp hơn org-scoped policy thông thường.

**Câu hỏi cần quyết định:**
- `AuthzRequest` khi contractor thực hiện action có carry `workOrderId` không?
- Token của contractor có encode `workOrderId` và `expiresAt` không?
- Ai revoke access khi work order đóng?

**Ảnh hưởng đến ABAC:**

SpEL expression cho contractor cần:

```
subject.getAttribute('activeWorkOrderId') != null
  && object.workOrderId == subject.getAttribute('activeWorkOrderId')
  && subject.getAttribute('accessExpiresAt') > #now
```

Điều này đòi hỏi:
1. Object attributes trong `AuthzRequest` (liên quan Vấn đề 3)
2. Time-aware expression evaluation trong PdpEngine
3. Token hoặc Subject phải carry `activeWorkOrderId` và `accessExpiresAt`

**Ảnh hưởng đến UIElement:**

Navigation simulation cho contractor phải dynamic theo work order đang active, không phải
theo org scope cố định. Menu của contractor thay đổi theo từng work order — đây là use case
khác hoàn toàn so với các persona khác.

---

### Vấn đề 6: PolicyProvider Selection — Chọn PolicySet nào khi evaluate?

**Mô tả:**
Hiện tại `AdminPolicyProvider` luôn load PolicySet `isRoot=true` duy nhất, bỏ qua mọi context.
Với multi-tenant và multi-persona, cần cơ chế chọn đúng PolicySet:

```
OPERATOR user  → load OPERATOR root PolicySet
TENANT user    → load TENANT PolicySet của tenant đó + OPERATOR root? (combine?)
RESIDENT user  → load RESIDENT PolicySet + ?
Contractor     → load EXTERNAL PolicySet scoped theo work order
```

**Câu hỏi cần quyết định:**
- PolicySet OPERATOR và TENANT có được combine không? Combine algorithm nào?
- Khi OPERATOR admin xem dữ liệu TENANT, PolicySet nào được evaluate?
- `tenantId` trong `PolicySetDefinition` nên là `Long` reference đến `Organization.id` hay
  vẫn là String?

**Ảnh hưởng đến ABAC:**

`AdminPolicyProvider.getPolicy()` cần signature mới:

```java
// Hiện tại — không có context
AbstractPolicy getPolicy(String serviceName);

// Cần — có subject context
AbstractPolicy getPolicy(String serviceName, PolicyEvaluationContext context);
  // context: { subjectScope, tenantId, ... }
```

Khi chưa quyết định, toàn bộ TENANT-scoped và RESIDENT-scoped PolicySet đang tồn tại trong DB
nhưng không được load khi evaluate — dead config.

**Ảnh hưởng đến UIElement:**

`simulate-navigation` gọi `AdminPolicyProvider` để lấy policy trước khi evaluate. Nếu
PolicyProvider không tenant-aware, navigation simulation của TENANT user sẽ evaluate bằng
OPERATOR policy — kết quả visibility sai hoàn toàn.

---

### Tóm tắt — Ma trận ảnh hưởng

| Vấn đề                      | Phải quyết định trước               | Ảnh hưởng ABAC                        | Ảnh hưởng UIElement                                 |
|-----------------------------|-------------------------------------|---------------------------------------|-----------------------------------------------------|
| 1. Multi-hat Identity       | Session/token architecture          | Subject chỉ carry active context      | Navigation simulation cần active context param      |
| 2. Cross-Tenant Boundary    | DB isolation strategy               | Explicit DENY rule cho TENANT_PRIVATE | Pre-filter UIElement theo `sensitivityLevel`        |
| 3. Instance-level AuthZ     | `AuthzRequest` object attributes    | SpEL cần `object.*` attributes        | Navigation ≠ enforcement — phải có cả hai           |
| 4. UIElement Action Overlap | Thêm `targetScope` hay tách action? | Resource catalogue scope              | `UIElement.targetScope` + pre-filter trong simulate |
| 5. Contractor Time-bound    | Token encode workOrderId?           | Time-aware SpEL + object scope        | Navigation dynamic theo work order                  |
| 6. PolicyProvider Selection | PolicySet combine strategy          | `getPolicy()` cần context             | Simulate dùng sai policy nếu chưa fix               |

**Thứ tự ưu tiên giải quyết:**

```
[1] Session/Token architecture (Multi-hat)      — ảnh hưởng tất cả các vấn đề còn lại
[2] PolicyProvider selection mechanism          — ABAC evaluate sai nếu chưa fix
[3] AuthzRequest object attributes              — instance-level policy không khả thi nếu thiếu
[4] UIElement targetScope                       — navigation overlap không giải quyết được
[5] Cross-Tenant DB isolation strategy          — quyết định kiến trúc, không phải ABAC
[6] Contractor time-bound access design         — có thể sau khi [1][2][3] xong
```

---

*Tài liệu phân tích — 2026-04-08*
