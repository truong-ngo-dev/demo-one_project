Phân tích: Tenant Doanh nghiệp — Scope, Authorization, Dynamic CRM

  ---
1. Framing — Tenant doanh nghiệp là ai trong hệ thống này?

Trước khi trả lời ba câu hỏi, cần xác định rõ Tenant doanh nghiệp là một khách hàng B2B của      
Apartcom, không phải end-user cá nhân. Điều này tạo ra một asymmetry quan trọng:

Platform Apartcom (bán phần mềm)
└── Building X (mua phần mềm để quản lý tòa nhà)
└── Company Z (thuê văn phòng trong Building X)
└── Nhân viên của Company Z (end-user của tenant portal)

Company Z không mua phần mềm từ Apartcom. Họ được BQL cấp quyền truy cập vào một subset của hệ   
thống. Đây là ranh giới quan trọng để calibrate mức độ support.

  ---
2. Câu hỏi 1 — Support tenant đến mức nào?

2.1 Ba tầng support

Tầng 1 — Building-Scoped Access (Minimal, phải có)
─────────────────────────────────────────────────
Tenant dùng hệ thống của BQL để:
• Cấp/quản lý access card cho nhân viên
• Đặt meeting room, tiện ích chung
• Nhận hóa đơn phí dịch vụ, thanh toán
• Đăng ký khách thăm

    → Đây là lý do tenant có tài khoản. Không có tầng này = product không bán được.

Tầng 2 — Tenant Internal Operations (Value-added, nên có)
──────────────────────────────────────────────────────────
Tenant dùng hệ thống để quản lý nội bộ của họ:
• Quản lý nhân viên nội bộ (ai đang làm việc ở văn phòng)
• Chấm công / timesheet tích hợp với access control
• Phân quyền nhân viên nội bộ (ai được đặt phòng họp, ai được mang khách vào)

    → Value-added service. Thêm stickiness cho product.

Tầng 3 — Dynamic CRM / Custom Business Logic (Advanced, cần quyết định)
────────────────────────────────────────────────────────────────────────
Tenant dùng hệ thống như một mini-ERP:
• CRM quản lý khách hàng của chính họ
• Custom workflow riêng
• Tự định nghĩa UI và authorization

    → Đây là câu hỏi lớn — và câu trả lời không phải là yes/no đơn giản.

2.2 Benchmark với các hệ thống tương tự

┌───────────────────────┬─────────────────────────┬─────────────────────────────────────────┐    
│       Hệ thống        │      Model tenant       │               Mức support               │    
├───────────────────────┼─────────────────────────┼─────────────────────────────────────────┤    
│ WeWork / IWG          │ Tenant = member,        │ Chỉ tầng 1 — booking, access, billing   │    
│                       │ building app            │                                         │    
├───────────────────────┼─────────────────────────┼─────────────────────────────────────────┤    
│ Yardi Voyager         │ Property management     │ Tầng 1+2, tenant portal read-only       │    
│                       │ focus                   │ mostly                                  │    
├───────────────────────┼─────────────────────────┼─────────────────────────────────────────┤    
│ Salesforce (khi tích  │ Tenant tự mang CRM      │ Apartcom integrate, không tự build CRM  │    
│ hợp)                  │                         │                                         │    
├───────────────────────┼─────────────────────────┼─────────────────────────────────────────┤    
│ Slack/Notion          │ Workspace per org       │ Tầng 2+3 — workspace model đầy đủ       │    
├───────────────────────┼─────────────────────────┼─────────────────────────────────────────┤    
│ Microsoft Entra       │ Identity per org        │ Delegation model — tenant admin quản lý │    
│                       │                         │  user của họ                            │    
└───────────────────────┴─────────────────────────┴─────────────────────────────────────────┘

Nhận xét: Không có hệ thống BMS (Building Management System) nào build full CRM nội bộ cho       
tenant. Các hệ thống làm được tầng 3 đều là general-purpose platform (Salesforce, Notion) — không
phải BMS. Đây là ranh giới quan trọng.

Recommendation: Tầng 1 là bắt buộc. Tầng 2 là mục tiêu hợp lý. Tầng 3 cần phân tích riêng (xem
mục 4).

  ---
3. Câu hỏi 2 — Có nên cấp phân quyền riêng cho tenant không?

3.1 Phân biệt "phân quyền của tenant" — 3 nghĩa khác nhau

Nghĩa A: Tenant có PolicySet riêng (đã được plan trong multi_portal_implementation_design.md)    
PolicySet E: scope=TENANT, tenantId="company-z"
→ TENANT_ADMIN của Company Z KHÔNG được edit PolicySet này
→ Platform admin define, tenant hưởng lợi
→ Đã trong roadmap, không phải câu hỏi mới

Nghĩa B: Tenant có thể tự edit PolicySet của mình (Tenant Self-Authorization)
→ TENANT_ADMIN vào ABAC console, tự viết Rule
→ Ví dụ: "Chỉ Manager của team A được approve timesheet"
→ Đây là feature lớn, abac_and_ui_element_on_authz.md đã classify là "Complex, deferrable"

Nghĩa C: Tenant có authorization engine riêng, tách khỏi ABAC core
→ Company Z tích hợp Keycloak hoặc Auth0 của họ
→ Federation model
→ Ngoài scope của product hiện tại

3.2 Phân tích Nghĩa B — Tenant Self-Authorization

Đây là câu hỏi thực sự. Có nên cho TENANT_ADMIN tự viết policy không?

Trường hợp nên làm:
- Tenant lớn (công ty 200+ nhân viên) có quy trình phân quyền phức tạp riêng
- Apartcom muốn upsell "Enterprise tier" với ABAC self-service
- Tenant muốn integrate với HR system của họ

Trường hợp KHÔNG nên làm ngay:

Complexity chain khi enable Tenant Self-Authorization:

[1] TENANT_ADMIN cần UI để edit Rule/Policy
→ Phải expose ABAC console với scoped view (chỉ thấy PolicySet của mình)
→ UIElement của ABAC console cần scope=TENANT filter

[2] ABAC console phải bảo vệ chính nó bằng ABAC
→ Rule: subject.roles.contains('TENANT_ADMIN')
AND subject.getAttribute('orgId') == targetPolicySet.tenantId
→ Nếu sai → TENANT_ADMIN của Company Z edit được policy của Company W (security breach)

[3] SpEL expression do tenant viết phải được sandbox
→ Tenant có thể viết expression độc hại (infinite loop, file access)
→ Cần expression validator, timeout, whitelist functions
→ Không phải feature nhỏ

[4] Nếu tenant viết Rule sai → user của họ bị lock out
→ Cần rollback mechanism, audit log per-tenant, "break glass" procedure

[5] Test surface explodes: mỗi tenant có expression riêng → không thể test toàn diện

Verdict: Implement Approach A trước (platform define UIElements + Policy template, tenant KHÔNG  
tự viết Rule). Approach B (tenant self-service ABAC) là feature riêng, cần thiết kế độc lập,     
không bundle vào multi-portal migration.

  ---
4. Câu hỏi 3 — Dynamic CRM — Có nên build không?

4.1 "Dynamic CRM" nghĩa là gì trong context này?

Cần clarify trước khi phân tích. Có 3 interpretation:

Interpretation A — CRM cho BQL quản lý quan hệ với Tenant
→ BQL track: hợp đồng, liên lạc, complaint history, upsell opportunity
→ User: BQL_MANAGER, BQL_STAFF
→ KHÔNG phải "tenant module" — đây là BGL internal tool

Interpretation B — Module quản lý nhân viên nội bộ của Tenant
→ Company Z dùng để quản lý nhân viên của mình trong tòa nhà
→ User: TENANT_ADMIN, TENANT_EMPLOYEE
→ Đây là Tầng 2 trong mục 2.1 — hợp lý để build

Interpretation C — Full CRM để Tenant quản lý khách hàng của họ
→ Company Z dùng Apartcom để quản lý clients của Company Z
→ Apartcom trở thành mini-Salesforce cho tenant
→ Đây là out-of-scope với BMS product

4.2 Phân tích Interpretation B — Tenant Internal Management Module

Giả sử "Dynamic CRM" = module quản lý nội bộ tenant (nhân viên, phòng ban, quy trình nội bộ). Câu
hỏi "có nên tích hợp phân quyền riêng không" được phân tích như sau:

Ma trận tích hợp ABAC:

Option 1: Flat RBAC cho Tenant module
──────────────────────────────────────
TENANT_ADMIN: full access trong company
TENANT_EMPLOYEE: read-only, self-service only

    Implement: hardcode 2 roles vào PolicySet template
    Complexity: Thấp
    Limitation: Không flexible — Company Z với 3 phòng ban có hierarchy khác
                Company W với flat structure

Option 2: Tenant có thể define sub-roles (not ABAC, just RBAC extension)
──────────────────────────────────────────────────────────────────────────
TENANT_ADMIN tạo: TENANT_DEPT_HEAD, TENANT_HR, TENANT_FINANCE
Platform define capability của mỗi sub-role (fixed)
Tenant chỉ assign sub-role cho người

    Implement: RoleContext mở rộng — thay vì enum roles, tenant có role registry
    Complexity: Trung bình
    Limitation: Sub-role capability vẫn do platform define — tenant không tự thêm capability       

Option 3: Tenant ABAC self-service (Interpretation B của câu hỏi 2)
─────────────────────────────────────────────────────────────────────
Đã phân tích ở trên → defer

Recommendation: Option 2 là sweet spot. Cho phép TENANT_ADMIN tổ chức hierarchy nội bộ mà không  
cần expose ABAC console.

4.3 "Dynamic" trong CRM — Có thực sự cần không?

"Dynamic" thường có nghĩa là: entity structure, fields, forms có thể customize per-tenant.

Nếu "Dynamic" = custom fields:
Company Z muốn thêm field "Employee Badge Number" riêng
Company W muốn thêm field "Department Code" riêng

    → Cần EAV (Entity-Attribute-Value) model hoặc JSONB column
    → Phức tạp về index, query, validation
    → Phức tạp hơn về ABAC: policy viết trên custom field như thế nào?

    SpEL: object.getAttribute('badge_number') != null
    → attribute name là string → typo không bị catch tại compile time
    → Rủi ro cao nếu tenant tự viết policy trên custom fields

Nếu "Dynamic" = custom workflows/approval chains:
→ Đây là BPM (Business Process Management) territory
→ Camunda, Activiti — dedicated systems
→ Không nên build custom

Nếu "Dynamic" = configurable UI (bật/tắt modules):
→ Đây là Feature flags per tenant
→ Tích hợp được với UIElement.scope + orgId
→ Khả thi, mức độ vừa phải

Verdict quan trọng: "Dynamic CRM" nếu hiểu theo nghĩa full là anti-pattern cho một BMS product.  
Apartcom nên định vị rõ: "Chúng tôi cung cấp building-context tools cho tenant, không phải       
general-purpose CRM." Nếu tenant cần full CRM, họ dùng Salesforce và integrate qua API.

  ---
5. Câu hỏi 4 — Những role nào khác có thể access Tenant module?

5.1 Mapping đầy đủ

Actor                  │ Access type        │ Lý do hợp lệ
───────────────────────┼────────────────────┼──────────────────────────────────────
TENANT_ADMIN           │ Full CRUD          │ Owner của module
TENANT_EMPLOYEE        │ Read own data      │ Xem timesheet, booking của mình
TENANT_DEPT_HEAD       │ Read team data     │ Quản lý phòng ban (nếu có sub-role)
BQL_MANAGER            │ Read limited data  │ Cần biết số nhân viên để cấp access card
BQL_SECURITY           │ Read limited data  │ Verify identity khi cấp thẻ từ
BQL_FINANCE            │ Read limited data  │ Billing — số nhân viên → phí parking/service       
PLATFORM_STAFF         │ Support access     │ Customer support, troubleshooting
SUPER_ADMIN            │ Full (god mode)    │ Platform admin
CONTRACTOR             │ None / Exceptional │ Chỉ khi được invite explicit
VISITOR                │ None               │ Self-service kiosk only
PROPERTY_OWNER         │ Aggregate read     │ Occupancy report, không phải individual data

5.2 Cross-boundary access — Vấn đề quan trọng nhất

BQL_MANAGER cần đọc dữ liệu tenant để vận hành. Nhưng ranh giới nào?

BQL được phép đọc:                    BQL KHÔNG được phép đọc:
──────────────────────────────────    ──────────────────────────────
• Danh sách nhân viên (tên, phòng)    • Mức lương
• Số lượng nhân viên đang có mặt      • Hồ sơ nhân sự chi tiết
• Access card active                  • Nội dung email/chat nội bộ
• Phương tiện đăng ký                 • Hiệu suất công việc
• Hóa đơn phí dịch vụ                • Tài liệu nội bộ công ty

Vấn đề 2 trong ACTOR_FUNCTION_ANALYSIS.md đã chỉ ra: ranh giới này phải được enforce ở 2 tầng    
(DB-level isolation + ABAC explicit DENY), không chỉ ABAC. Đây là kiến trúc decision cần resolve
trước khi build tenant module.

5.3 External entities có thể access tenant module

Case 1 — Auditor / Inspector
Company Z có thể muốn cho external auditor xem timesheet/attendance
→ Cần "Guest access" với time-limited token
→ Tương tự Contractor model (time-bound + work-scoped)
→ Phụ thuộc vào giải quyết Vấn đề 5 (Contractor time-bound) trong ACTOR_FUNCTION_ANALYSIS

Case 2 — Parent company của tenant
Công ty mẹ muốn xem báo cáo của subsidiary đang thuê văn phòng
→ Đây là cross-organization hierarchy — Party Model cần handle
→ Phức tạp, defer

Case 3 — External HR system integration (via API)
Tenant dùng BambooHR/SAP, muốn sync nhân viên sang Apartcom
→ Service account, không phải human actor
→ Cần API key management riêng cho tenant
→ Tách khỏi ABAC human-centric model

  ---
6. Tổng hợp — Ma trận độ phức tạp tích hợp

6.1 Tích hợp với ABAC hiện tại

Feature                          │ Dependency chain                        │ Complexity
─────────────────────────────────┼─────────────────────────────────────────┼────────────
Tenant PolicySet template        │ PolicySet.scope += TENANT (đã plan)     │ Low
TENANT sub-roles                 │ RoleContext mở rộng + Role registry      │ Medium
BQL cross-read limited data      │ Explicit DENY rule + DB isolation       │ Medium-High
Tenant self-service ABAC         │ ABAC console scoped + SpEL sandbox      │ Very High
Dynamic custom fields in ABAC    │ Object attributes cần support EAV       │ Very High

6.2 Tích hợp với UIElement

Feature                          │ Dependency                              │ Impact
─────────────────────────────────┼─────────────────────────────────────────┼────────────
Tenant module nav (Tầng 1+2)     │ UIElement.scope=TENANT (đã plan)        │ Low — in roadmap    
TENANT_ADMIN thấy khác EMPLOYEE  │ UIElement.targetScope (Vấn đề 4)        │ Medium
BQL thấy subset tenant data      │ UIElement cross-scope với filter        │ High
Tenant custom UI elements        │ UIElement.orgId + dynamic fetch         │ Very High (Approach
B)

6.3 Tích hợp với multi-portal

Feature                          │ Token impact                            │ FE impact
─────────────────────────────────┼─────────────────────────────────────────┼────────────
Basic TENANT portal              │ activeScope=TENANT đã thiết kế          │ Low
Sub-role hierarchy               │ roles[] thêm sub-role values            │ Medium
BQL accessing tenant data        │ Cross-scope request — cần special token │ High
Tenant admin invites external    │ Short-lived guest token                 │ Medium

  ---
7. Recommendation — Thứ tự triển khai

Phase 0 (Prerequisite — phải có trước):
[a] Giải quyết Vấn đề 2 (Cross-Tenant Boundary): quyết định DB isolation strategy
[b] Giải quyết Vấn đề 4 (UIElement targetScope): quyết định pre-filter vs tách action

Phase 1 (Tầng 1 — Core):
1. Tenant PolicySet template (scope=TENANT, isRoot per scope)
2. UIElement scope=TENANT cho: access card management, facility booking, billing view
3. BQL read-only view với explicit DENY rules cho TENANT_PRIVATE data
4. AdminSubjectProvider inject orgId=companyId vào Subject khi scope=TENANT

Phase 2 (Tầng 2 — Internal Operations):
5. Tenant employee directory (read-only, limited fields visible to BQL)
6. TENANT sub-roles (Option 2 — platform-defined capabilities, tenant assigns)
7. Attendance/timesheet module với TENANT scope
8. Guest access (time-limited, work-scoped) — sau khi Contractor model xong

Phase 3 (Advanced — sau khi Phase 1+2 stable):
9. Tenant self-service policy (Approach A: platform define UIElements, tenant define who sees)
10. Dynamic CRM nếu có business case rõ ràng (MVP: configurable modules per tenant)
11. Tenant self-service ABAC console (Approach B) — chỉ khi có enterprise customer yêu cầu

Không build (out of scope for BMS):
✗ Full CRM với custom entity/workflow
✗ Tenant federation (external IdP)
✗ Parent company hierarchy

  ---
8. Câu hỏi mở cần quyết định trước khi code

[Q1] DB isolation cho TENANT_PRIVATE data:
→ Separate schema hay encrypted columns hay chỉ ABAC?
→ Ảnh hưởng: toàn bộ cross-tenant query design

[Q2] TENANT sub-role: platform define hay tenant define?
→ Nếu platform define: liệt kê đủ sub-roles trước khi code (TENANT_HR, TENANT_FINANCE, ...)
→ Nếu tenant define: cần Role Registry table per-tenant

[Q3] BQL cross-read ranh giới cụ thể:
→ Danh sách tường minh field nào của tenant BQL được thấy
→ Không quyết định được → ABAC rule không viết được

[Q4] "Dynamic CRM" — clarify với stakeholder:
→ Configurable modules (feature flags per tenant)? → Khả thi trong Phase 2
→ Custom fields? → Cần EAV, phức tạp cao
→ Custom workflows? → Cần BPM engine, out of scope

  ---
Tóm lại: Tenant doanh nghiệp nên được support ở Tầng 1+2 với authorization theo template model   
(platform define, tenant không tự viết rule). Dynamic CRM ở mức "configurable modules" là khả thi
nhưng phải sau khi multi-portal core stable. Tenant self-service ABAC là feature độc lập, phức  
tạp cao, không nên bundle vào roadmap hiện tại. Câu hỏi cốt lõi cần resolve ngay là DB isolation
boundary — mọi thứ khác phụ thuộc vào quyết định này.
