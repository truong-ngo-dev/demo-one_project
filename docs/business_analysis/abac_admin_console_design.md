# Admin Console Design: ABAC Policy Management (BQL SUPER_ADMIN)

> Thiết kế giao diện quản trị cho BQL Admin để quản lý toàn bộ hệ thống phân quyền ABAC.
> Tham khảo: Axiomatics PAP, AWS Verified Permissions, Keycloak Authorization, Cerbos Playground.

---

## 1. Tổng quan Navigation

```
┌─────────────────────────────────────────────┐
│  ABAC Management                            │
│  ─────────────────────────────────────────  │
│  ▶ Resources & Actions       (Catalogue)    │
│  ▶ Policy Sets               (PAP)          │
│  ▶ UI Components             (UIElement)    │
│  ▶ Simulator                 (Test/Debug)   │
│  ▶ Audit Log                 (History)      │
└─────────────────────────────────────────────┘
```

Workflow tự nhiên của admin:

```
[1. Khai báo Resource]  →  [2. Viết Policy]  →  [3. Gắn UI Component]  →  [4. Test]
```

Đây là flow tương tự AWS VP (Schema → Policy Store → Policies → Simulator).
Tuy nhiên Resource là data-centric (không phải entity graph như Cedar).

---

## 2. Module 1 — Resources & Actions (Catalogue)

> Tương đương "Resource Server" trong Keycloak và "Schema" trong AWS VP.

### 2.1 Resource List

```
┌──────────────────────────────────────────────────────────┐
│  Resources                                  [+ New]      │
│  ────────────────────────────────────────────────────    │
│  Search: [________________]                              │
│                                                          │
│  NAME          ACTIONS                  UI COMPONENTS    │
│  ──────────    ─────────────────────    ──────────────   │
│  employee      LIST READ CREATE         4 elements       │
│                UPDATE DELETE LOCK       [View]           │
│                                                          │
│  user          LIST READ UPDATE         6 elements       │
│                LOCK ASSIGN_ROLE         [View]           │
│                REVOKE_SESSION                            │
│                                                          │
│  maintenance   LIST READ CREATE         3 elements       │
│                UPDATE CLOSE             [View]           │
└──────────────────────────────────────────────────────────┘
```

### 2.2 Resource Detail / Edit

```
┌──────────────────────────────────────────────────────────┐
│  ← Resources  /  employee                               │
│  ────────────────────────────────────────────────────    │
│                                                          │
│  Name         [employee              ]                   │
│  Description  [Hồ sơ nhân viên       ]                   │
│  Service      [admin-service  ▼]                         │
│                                                          │
│  ── Actions ──────────────────────────────────[+ Add]    │
│                                                          │
│  LIST      Standard CRUD    [✎] [✕]                     │
│  READ      Standard CRUD    [✎] [✕]                     │
│  CREATE    Standard CRUD    [✎] [✕]                     │
│  UPDATE    Standard CRUD    [✎] [✕]                     │
│  DELETE    Standard CRUD    [✎] [✕]                     │
│  LOCK      Custom action    [✎] [✕]                     │
│                                                          │
│  ── Policies using this resource ──────────────────────  │
│  policy:employee    [View Policy]                        │
│                                                          │
│                               [Cancel]  [Save Changes]  │
└──────────────────────────────────────────────────────────┘
```

**Design note**: Actions không phải tự do nhập text — admin chọn từ danh sách standard (LIST/READ/CREATE/UPDATE/DELETE) hoặc khai báo custom action. Điều này tránh typo trong SpEL (`action.getAttribute('name') == 'UPADTE'`).

---

## 3. Module 2 — Policy Sets (PAP)

> Tương đương Axiomatics PAP tree: PolicySet → Policy → Rule.

### 3.1 Policy Set List

```
┌──────────────────────────────────────────────────────────┐
│  Policy Sets                               [+ New]       │
│  ────────────────────────────────────────────────────    │
│                                                          │
│  NAME                  SCOPE   ALGORITHM         STATUS  │
│  ────────────────────  ──────  ───────────────   ──────  │
│  bql-root              OPERATOR  DENY_OVERRIDES    Active  │
│    └─ policy:employee          DENY_UNLESS_PERMIT        │
│    └─ policy:user              DENY_UNLESS_PERMIT        │
│    └─ policy:maintenance       DENY_UNLESS_PERMIT        │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

Cây PolicySet → Policy được render như Axiomatics — expandable tree. Admin thấy toàn bộ cấu trúc ở một chỗ.

### 3.2 Policy Detail (Rules List)

```
┌──────────────────────────────────────────────────────────┐
│  ← Policy Sets / bql-root /  policy:employee            │
│  ────────────────────────────────────────────────────    │
│                                                          │
│  Target Resource: employee                               │
│  Combining Algorithm: [DENY_UNLESS_PERMIT ▼]            │
│                                                          │
│  ── Rules ─────────────────────────────── [+ Add Rule]  │
│                                                          │
│  #  NAME                          EFFECT   [↑][↓] [✎][✕]│
│  1  All actions for HR_ADMIN      PERMIT                 │
│  2  MANAGER can LIST              PERMIT                 │
│  3  MANAGER read own dept only    PERMIT                 │
│  4  MANAGER update own dept only  PERMIT                 │
│  5  EMPLOYEE read self only       PERMIT                 │
│  6  EMPLOYEE update self only     PERMIT                 │
│  7  Block CREATE/DELETE for       DENY                   │
│     non-HR                                              │
│                                                          │
│  ⚠ Rule evaluation order matters for FIRST_APPLICABLE   │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### 3.3 Rule Editor (Visual Policy Builder)

Đây là tính năng cốt lõi. Admin không viết SpEL thủ công — form builder tự sinh expression.

```
┌──────────────────────────────────────────────────────────┐
│  Edit Rule: "MANAGER read own dept only"                 │
│  ────────────────────────────────────────────────────    │
│                                                          │
│  Name  [MANAGER read own dept only           ]          │
│  Effect [PERMIT ▼]                                      │
│                                                          │
│  ── Target (WHO + WHAT — ai làm gì thì rule này áp dụng) │
│                                                          │
│  Subject  [has role ▼]  [MANAGER ▼]                      │
│  AND                                                     │
│  Action   [is ▼]        [READ ▼]                         │
│                                                          │
│  [+ Add condition]                                      │
│                                                          │
│  ── Condition (điều kiện bổ sung để PERMIT/DENY)        │
│                                                          │
│  ○ Navigation (không có dữ liệu instance) → luôn PERMIT  │
│  OR                                                      │
│  Subject [attribute ▼] [managedDepartments ▼]            │
│          [contains ▼]                                    │
│  Object  [field ▼]     [department ▼]                    │
│                                                          │
│  [+ Add condition]                                      │
│                                                          │
│  ── Preview SpEL (read-only, advanced) ──────────────── │
│  Target:                                                 │
│  subject.roles.contains('MANAGER')                      │
│  && action.getAttribute('name') == 'READ'               │
│                                                          │
│  Condition:                                              │
│  object.data == null                                    │
│  || subject.getAttribute('managedDepartments')          │
│     .contains(object.data.department)                   │
│                                                          │
│  [Advanced Mode — edit raw SpEL]                        │
│                                                          │
│                          [Cancel]  [Test Rule]  [Save]  │
└──────────────────────────────────────────────────────────┘
```

**Các building block của Visual Builder:**

| Category    | Operators                                                    |
|-------------|--------------------------------------------------------------|
| Subject     | `has role`, `has attribute`, `attribute equals/contains`     |
| Action      | `is` (từ catalogue), `is one of`                             |
| Object      | `field equals`, `field contains`, `has no data (navigation)` |
| Environment | `current time between`, `service is`                         |
| Logic       | `AND`, `OR`, `NOT`, group with parentheses                   |

**Advanced Mode**: Raw SpEL textarea — chỉ dùng cho system admin kỹ thuật. Builder và Advanced Mode sync 2 chiều khi có thể, hiện warning "Cannot parse back to visual form" khi expression quá phức tạp.

---

## 4. Module 3 — UI Components (UIElement Registry)

> Không có tương đương trực tiếp ở Axiomatics/AWS/Keycloak — đây là layer độc đáo của Apartcom.

Mục đích: Ánh xạ UIElement (button, tab, menu item...) → resource:action. Là cầu nối giữa "FE thấy gì" và "Policy cho phép gì".

### 4.1 UIElement List

```
┌──────────────────────────────────────────────────────────┐
│  UI Components                             [+ New]       │
│  Filter by Resource: [All ▼]  Type: [All ▼]              │
│  ────────────────────────────────────────────────────    │
│                                                          │
│  ID                         TYPE    RESOURCE   ACTION    │
│  ───────────────────────────────────────────────────     │
│  btn:employee:create        Button  employee   CREATE    │
│  btn:employee:update        Button  employee   UPDATE    │
│  btn:employee:delete        Button  employee   DELETE    │
│  btn:employee:lock          Button  employee   LOCK      │
│  tab:employee-detail:info   Tab     employee   READ      │
│                                                          │
│  btn:user:assign-role       Button  user       ASSIGN... │
│  btn:user:lock-toggle       Button  user       LOCK      │
│  tab:user-detail:security   Tab     user       READ      │
│                                                          │
│  ⚠ 2 UIElements chưa có policy:                         │
│    btn:maintenance:close  btn:maintenance:reopen         │
│    [Configure Now]                                       │
└──────────────────────────────────────────────────────────┘
```

Notification "chưa có policy" tương đương "safe default": UIElement mới → DENY cho đến khi admin cấu hình xong. Admin không bao giờ bị surprise bởi button bỗng nhiên hiện ra với mọi user.

### 4.2 UIElement Detail / Edit

```
┌──────────────────────────────────────────────────────────┐
│  ← UI Components  /  btn:employee:update                │
│  ────────────────────────────────────────────────────    │
│                                                          │
│  Element ID   [btn:employee:update          ]            │
│  Label        [Edit Employee               ]            │
│  Type         [Button ▼]                                 │
│  Group        [employee-detail-actions ▼]                │
│  Order        [2    ]                                    │
│                                                          │
│  ── Linked to Policy ──────────────────────────────────  │
│  Resource     [employee ▼]                               │
│  Action       [UPDATE ▼]                                 │
│                                                          │
│  ── Policy coverage ───────────────────────────────────  │
│  policy:employee → rule:employee:update:manager-own-dept │
│    PERMIT: MANAGER (own dept) — instance level only      │
│  policy:employee → rule:employee:all:hr-admin            │
│    PERMIT: HR_ADMIN — all levels                         │
│                                                          │
│  Simulated: [Select user to preview ▼]                   │
│                                                          │
│                               [Cancel]  [Save Changes]  │
└──────────────────────────────────────────────────────────┘
```

### 4.3 UIElement ↔ Policy Link

Không có liên kết trực tiếp UIElement → Policy trong data model. Liên kết gián tiếp qua `resource + action`:

```
UIElement btn:employee:update
    resourceRef = "employee"
    actionRef   = "UPDATE"
        ↓
Policy: policy:employee (target: object.name == 'employee')
    Rules với target chứa action.getAttribute('name') == 'UPDATE'
        → rule:employee:update:manager-own-dept
        → rule:employee:all:hr-admin (HR_ADMIN có mọi action)
```

Admin UI tự động tính "Policy coverage" bằng cách query ngược: tìm tất cả rules trong policies có target match resource + action.

---

## 5. Module 4 — Simulator

> Tương đương Keycloak Evaluate tab + AWS VP Policy Analyzer.

### 5.1 Navigation-level Simulation

```
┌──────────────────────────────────────────────────────────┐
│  Simulator                                               │
│  ────────────────────────────────────────────────────    │
│                                                          │
│  Mode: ● Navigation  ○ Instance                         │
│                                                          │
│  Subject                                                 │
│  ┌──────────────────────────────────────────────────┐   │
│  │  ○ Select user:  [Nguyen Van A ▼]                │   │
│  │  ○ Virtual user: roles [MANAGER ▼][+ Add role]   │   │
│  │                  attrs [managedDepartments ▼]    │   │
│  │                        [engineering, product]    │   │
│  └──────────────────────────────────────────────────┘   │
│                                          [Run Simulation]│
│                                                          │
│  ── Results ──────────────────────────────────────────   │
│                                                          │
│  RESOURCE    ACTION   RESULT  RULE                       │
│  ────────────────────────────────────────────────────    │
│  employee    LIST     PERMIT  rule:employee:list:manager  │
│  employee    READ     PERMIT  rule:employee:read:manager  │
│  employee    UPDATE   DENY    (no matching rule)         │
│  employee    DELETE   DENY    rule:...:forbidden-non-hr  │
│  employee    CREATE   DENY    rule:...:forbidden-non-hr  │
│  employee    LOCK     DENY    (no matching rule)         │
│                                                          │
│  UI Components visible:                                  │
│  ✓ btn:employee:create  → DENY  (hidden)                 │
│  ✓ btn:employee:update  → DENY  (hidden, nav level)      │
│  ✓ tab:employee-detail  → PERMIT (visible)               │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### 5.2 Instance-level Simulation

```
┌──────────────────────────────────────────────────────────┐
│  Simulator                                               │
│  ────────────────────────────────────────────────────    │
│                                                          │
│  Mode: ○ Navigation  ● Instance                         │
│                                                          │
│  Subject: [Nguyen Van A ▼]  (roles: MANAGER,            │
│                               managedDepts: engineering) │
│                                                          │
│  Resource: [employee ▼]                                  │
│  Instance data (JSON):                                   │
│  ┌──────────────────────────────────────────────────┐   │
│  │ {                                                │   │
│  │   "employeeId": "emp-001",                       │   │
│  │   "department": "engineering",                   │   │
│  │   "status": "ACTIVE"                             │   │
│  │ }                                                │   │
│  └──────────────────────────────────────────────────┘   │
│                                          [Run Simulation]│
│                                                          │
│  ── Results ──────────────────────────────────────────   │
│                                                          │
│  ACTION   RESULT  TRACE                                  │
│  ─────────────────────────────────────────────────────   │
│  READ     PERMIT  ▶ rule:employee:read:manager-own-dept  │
│                     Target: ✓ has MANAGER, ✓ action READ │
│                     Condition: object.data != null → ✓   │
│                     managedDepts.contains('engineering') │
│                     → ✓ (dept matches)                   │
│                                                          │
│  UPDATE   PERMIT  ▶ rule:employee:update:manager-own-dept│
│                     Target: ✓  Condition: ✓              │
│                                                          │
│  DELETE   DENY    ▶ No matching PERMIT rule              │
│                   ▶ rule:...:forbidden-non-hr (DENY)     │
│                     Target: ✓ has MANAGER, ✓ action DEL  │
│                     Condition: true → DENY applied       │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

**Trace format** tương tự Axiomatics PAP Decision Point log:
- Rule nào được evaluate
- Target match hay không
- Condition eval như thế nào (từng sub-expression)
- Final decision + combining algorithm nếu nhiều rules match

### 5.3 Reverse Lookup

```
┌──────────────────────────────────────────────────────────┐
│  Simulator  /  Reverse Lookup                            │
│  ────────────────────────────────────────────────────    │
│                                                          │
│  "Ai có quyền làm gì với resource này?"                 │
│                                                          │
│  Resource: [employee ▼]  Action: [UPDATE ▼]              │
│                                          [Run Lookup]    │
│                                                          │
│  ── Rules granting PERMIT ────────────────────────────   │
│                                                          │
│  rule:employee:all:hr-admin                              │
│    → Subject: has role HR_ADMIN                          │
│    → Condition: always (no restriction)                  │
│    → Users with this role: 3 users                       │
│       [View users]                                       │
│                                                          │
│  rule:employee:update:manager-own-dept                   │
│    → Subject: has role MANAGER                           │
│    → Condition: instance level, own dept only            │
│    → Users with this role: 8 users                       │
│       ⚠ Permission depends on instance data             │
│       [View users]                                       │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

---

## 6. Module 5 — Audit Log

```
┌──────────────────────────────────────────────────────────┐
│  Audit Log                                               │
│  Filter: [All actions ▼]  [Date range]  [Admin user ▼]   │
│  ────────────────────────────────────────────────────    │
│                                                          │
│  TIME               USER              ACTION             │
│  ────────────────────────────────────────────────────    │
│  2026-04-05 14:30   admin@bql.com     Modified rule      │
│                     rule:employee:update:manager-own-dept │
│                     [View diff]                          │
│                                                          │
│  2026-04-05 11:00   admin@bql.com     Added UIElement    │
│                     btn:employee:lock                    │
│                                                          │
│  2026-04-04 09:15   admin@bql.com     Created policy     │
│                     policy:maintenance                   │
│                                                          │
│  [Load more...]                                          │
└──────────────────────────────────────────────────────────┘
```

---

## 7. Impact Analysis (confirm before save)

Khi admin save thay đổi rule, system hiển thị trước khi commit:

```
┌──────────────────────────────────────────────────────────┐
│  Confirm Changes                                         │
│  ────────────────────────────────────────────────────    │
│                                                          │
│  Rule: rule:employee:update:manager-own-dept             │
│                                                          │
│  Thay đổi:                                               │
│  - Thêm điều kiện: object.data.status == 'ACTIVE'        │
│    (trước: chỉ check department)                         │
│                                                          │
│  Tác động ước tính:                                      │
│  ✗ MANAGER sẽ KHÔNG thể update employee có status khác   │
│    ACTIVE (RESIGNED, ON_LEAVE...)                        │
│  ? Không thể tính số user ảnh hưởng chính xác vì điều   │
│    kiện phụ thuộc instance data                          │
│                                                          │
│  [Xem rule hiện tại]  [Hủy]  [Xác nhận & Lưu]           │
└──────────────────────────────────────────────────────────┘
```

**Giới hạn của Impact Analysis**: Với instance-level conditions (phụ thuộc `object.data`), không thể tính chính xác số user bị ảnh hưởng. System chỉ phân tích được subject-side conditions (role, attribute tĩnh).

---

## 8. So sánh với các hệ thống tham khảo

| Aspect             | Axiomatics           | AWS VP                | Keycloak                         | Cerbos        | Apartcom ABAC                                       |
|--------------------|----------------------|-----------------------|----------------------------------|---------------|-----------------------------------------------------|
| Policy model       | XACML tree           | Cedar language        | Resource+Scope+Policy+Permission | YAML policies | PolicySet→Policy→Rule (custom SpEL)                 |
| Admin entry point  | PolicySet editor     | Schema (entity types) | Resources                        | Git repo      | Resources & Actions catalogue                       |
| Rule authoring     | Form builder         | Code (Cedar)          | Typed policy forms               | YAML          | Visual Builder → SpEL                               |
| Test/Simulate      | Test console + trace | Policy Analyzer       | Evaluate tab                     | Playground    | Simulator (nav + instance)                          |
| UIElement registry | Không có             | Không có              | Partial (scope = UI action)      | Không có      | **Độc đáo**: explicit UIElement→resource:action map |
| Target audience    | Enterprise XACML     | Developer/DevOps      | Developer                        | Developer     | BQL admin (non-developer)                           |

**Điểm khác biệt của Apartcom:**
1. **UIElement registry explicit** — Keycloak có "scopes" có thể map tương tự nhưng không có UI component type/group/order concept
2. **Navigation vs Instance mode trong Simulator** — không hệ thống nào có concept này vì không có "null object.data" pattern
3. **Visual Builder target là business admin** — Axiomatics có builder nhưng vẫn XACML-centric; Apartcom builder hoàn toàn domain-vocabulary

---

## 9. Data Model (tham khảo cho implementation)

```
resource_definition
  id, name, description, service_name

action_definition
  id, resource_id, name, description, is_standard

policy_set
  id, name, scope (OPERATOR|TENANT), combine_algorithm, is_root, tenant_id

policy
  id, policy_set_id, name, target_expression_id, combine_algorithm

rule
  id, policy_id, name, description, target_expression_id,
  condition_expression_id, effect (PERMIT|DENY), order_index

expression
  id, type (LITERAL|COMPOSITION), spel_expression,
  combination_type (AND|OR), parent_id

ui_element
  id, element_id, label, type (BUTTON|TAB|MENU_ITEM),
  group, order_index, resource_id, action_id
```

---

## 10. Thứ tự xây dựng Admin Console

**Phase 1 — Core (unblock được policy management):**
1. Resource & Action Catalogue CRUD
2. Policy Set / Policy / Rule list + basic editor (raw SpEL mode)
3. UIElement Registry CRUD

**Phase 2 — Usability (admin tự làm được không cần developer):**
4. Visual Policy Builder (form → SpEL generation)
5. Simulator — Navigation mode
6. Impact Analysis (subject-side conditions)

**Phase 3 — Observability:**
7. Simulator — Instance mode + Trace
8. Reverse/Forward Lookup
9. Audit Log
10. UIElement "no policy" notification
