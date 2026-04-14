# ABAC & UIElement — Design Specification

*Tài liệu thiết kế cuối cùng. Tham khảo phân tích tại: `../abac_and_ui_element_on_authz.md`, `../ACTOR_FUNCTION_ANALYSIS.md`*

---

## 1. Tổng quan

ABAC (Attribute-Based Access Control) là cơ chế phân quyền trung tâm của hệ thống. UIElement Registry
là cơ chế kiểm soát visibility của UI component dựa trên ABAC evaluation.

Hai thành phần này phải hoạt động cùng nhau và phải **context-aware** — tức là biết user đang hoạt động
với tư cách nào (scope nào, orgId nào) trước khi evaluate.

---

## 2. PolicySetDefinition — Thay đổi bắt buộc

### 2.1 Vấn đề hiện tại

`isRoot: boolean` hiện tại ngầm định "1 root duy nhất cho toàn hệ thống". Với multi-scope, mỗi scope
cần root PolicySet riêng.

### 2.2 Thiết kế mới

```
PolicySet A: scope=ADMIN,     isRoot=true
PolicySet B: scope=OPERATOR,  isRoot=true
PolicySet C: scope=RESIDENT,  isRoot=true
PolicySet D: scope=TENANT,    isRoot=true,  tenantId=null      ← template mặc định
PolicySet E: scope=TENANT,    isRoot=true,  tenantId="co-z"    ← Company Z (nếu có custom)
```

**Invariant mới**: `isRoot=true` là unique per `(scope, tenantId)`, không unique toàn hệ thống.

**Thay đổi enum scope**: Thêm `ADMIN` và `RESIDENT` vào `PolicySetDefinition.scope`.
`tenantId` được dùng để phân biệt TENANT PolicySet theo từng công ty khi cần custom.

**Fallback cho TENANT scope**:
- Tìm PolicySet `scope=TENANT, tenantId=company-z` → nếu có, dùng
- Nếu không có → fallback về `scope=TENANT, tenantId=null` (template)
- Nếu template cũng không có → deny-by-default

---

## 3. PolicyProvider — Context-aware

### 3.1 Signature thay đổi (breaking change ở libs/abac)

```java
// Hiện tại — không có context
AbstractPolicy getPolicy(String serviceName);

// Cần thêm overload
AbstractPolicy getPolicy(String serviceName, String activeScope, String orgId);
```

### 3.2 AdminPolicyProvider implement

```java
// Logic của AdminPolicyProvider:
// 1. Load PolicySetDefinition where scope = activeScope AND isRoot = true
// 2. Nếu scope = TENANT:
//    a. Tìm PolicySet với tenantId = orgId
//    b. Nếu không có → fallback về tenantId = null (template)
// 3. Build policy tree từ PolicySet tìm được
```

**PepEngine** (caller) phải truyền `activeScope` và `orgId` từ JWT claims vào `getPolicy()`.

---

## 4. SubjectProvider — Inject context attributes

### 4.1 Subject hiện tại (thiếu context)

```java
Subject { userId, roles: ["BQL_MANAGER"], attributes: {} }
```

SpEL expression không thể check orgId → toàn bộ phân quyền theo tổ chức sẽ sai.

### 4.2 Subject mới

```java
Subject {
  userId: "user-001",
  roles:  ["BQL_MANAGER"],         // chỉ roles của active context
  attributes: {
    "scope": "OPERATOR",
    "orgId": "building-x"
  }
}
```

`AdminSubjectProvider` extract `activeScope` và `activeOrgId` từ JWT claims khi build Subject.

### 4.3 Ví dụ SpEL expression với context attributes

```
// BQL_MANAGER chỉ xem user trong building của mình:
subject.roles.contains('BQL_MANAGER')
  AND subject.getAttribute('orgId') == object.getAttribute('orgId')

// TENANT_ADMIN chỉ manage employee trong company của mình:
subject.roles.contains('TENANT_ADMIN')
  AND subject.getAttribute('scope') == 'TENANT'
  AND subject.getAttribute('orgId') == object.getAttribute('tenantId')

// Cross-boundary: BQL đọc employee summary của tenant thuộc building của mình:
subject.roles.contains('BQL_MANAGER')
  AND subject.getAttribute('orgId') == object.getAttribute('buildingId')
  AND action.name == 'employee:read:summary'
```

---

## 5. UIElement — Thêm scope field

### 5.1 Schema mới

```
UIElement {
  elementId    : String   — unique globally, immutable
  scope        : Enum(ADMIN | OPERATOR | TENANT | RESIDENT)   ← field mới, bắt buộc
  label        : String
  type         : Enum(BUTTON | MENU_ITEM | PAGE | ...)
  elementGroup : String
  resourceId   : → ResourceDefinition
  actionId     : → ActionDefinition
}
```

**Migration**: Tất cả UIElement hiện có phải được gán scope. Không có UIElement nào scope=null sau migration.

### 5.2 Nguyên tắc scope của UIElement

- `elementId` là duy nhất — không duplicate giữa các scope
- Cùng một `actionId` có thể có nhiều UIElement ở các scope khác nhau (đây là đúng về mặt design)
- Ví dụ: `btn_admin_create_user` (scope=ADMIN) và `btn_operator_invite_resident` (scope=OPERATOR)
  cùng trỏ về `action: create_user` — đây là 2 UI artifact khác nhau cho cùng 1 capability

### 5.3 Resource/Action — Không duplicate

Resource và Action là shared, không scope-specific:

```
resource: user-management   (serviceName: admin)
  action: create_user
  action: list_users
  action: deactivate_user
```

Policy/Rule quyết định ai được làm gì — không phải Resource/Action.

---

## 6. SimulateNavigation — Context-aware

### 6.1 Luồng xử lý mới

```
// Hiện tại: load tất cả UIElements → evaluate tất cả
// Vấn đề: không filter theo portal → trả về menu của portal khác

// Mới:
Step 1: Filter UIElements by scope = activeScope (+ orgId nếu scope=TENANT)
Step 2: Load PolicySet matching activeScope (+ tenantId nếu scope=TENANT)
Step 3: Build Subject với attributes {scope, orgId} từ token
Step 4: Evaluate từng UIElement với Subject
Step 5: Return visibility map { elementId: boolean }
```

### 6.2 API

```
GET /abac/navigate?scope=OPERATOR&orgId=building-x
Authorization: Bearer <scoped_token>

Response:
{
  "nav_user_management":   true,
  "nav_facility_management": true,
  "btn_create_user":       true,
  "btn_deactivate_user":   false,
  ...
}
```

### 6.3 Instance-level evaluation (cho action buttons trên record)

Navigation simulation hoạt động ở type-level — đủ cho ẩn/hiện menu.
Với action buttons trên record cụ thể, cần evaluate với object context:

```
POST /abac/evaluate-elements
{
  "elementIds": ["btn_edit_user", "btn_deactivate_user"],
  "objectData": { "orgId": "building-x", "status": "ACTIVE" }
}
→ { "btn_edit_user": true, "btn_deactivate_user": false }
```

`objectData` do FE forward từ response của GET resource. Nếu FE tamper → worst case hiển thị
button sai, nhưng `@PreAuthorize` ở backend vẫn enforce — không phải security hole.

---

## 7. Phân quyền cho Tenant Tier 2

### 7.1 Role model

Tier 2 features đều là building-context → nhu cầu phân quyền uniform giữa các loại doanh nghiệp.
Không cần sub-role phức tạp.

```
TENANT_ADMIN
  • Full CRUD: employee directory, access card requests, facility booking mgmt, visitor mgmt
  • Assign/revoke TENANT_EMPLOYEE role cho user trong cùng orgId
  • Constraint: chỉ assign TENANT_* roles, chỉ trong orgId của mình

TENANT_EMPLOYEE
  • Self-service: book facility, register visitor, view own attendance/access
  • Không quản lý được người khác
```

### 7.2 PolicySet TENANT template

```
Policy: "TENANT_ADMIN capabilities"
  Rule: subject.roles.contains('TENANT_ADMIN')
        AND subject.getAttribute('orgId') == object.getAttribute('tenantId')
  → PERMIT: employee:*, access_card:request, booking:*, visitor:*, announcement:read

Policy: "TENANT_EMPLOYEE capabilities"
  Rule: subject.roles.contains('TENANT_EMPLOYEE')
  → PERMIT: booking:write:own, visitor:write:own, attendance:read:own, announcement:read

Policy: "BQL cross-read tenant summary"
  Rule: subject.roles.contains('BQL_MANAGER')
        AND subject.getAttribute('orgId') == object.getAttribute('buildingId')
        AND action.name matches 'employee:read:summary|access_card:read'
  → PERMIT
```

### 7.3 Ai quản lý PolicySet TENANT

PolicySet TENANT do **platform define và maintain**. TENANT_ADMIN không tự edit policy.
TENANT_ADMIN chỉ quản lý **assignment** (ai được role gì trong org của mình).

---

## 8. Ma trận thay đổi cần implement

| Component | Thay đổi | Mức độ |
|---|---|---|
| `PolicySetDefinition.scope` | Thêm ADMIN, RESIDENT | Migration |
| `PolicySetDefinition.isRoot` | Unique per (scope, tenantId) | Breaking invariant |
| `libs/abac PolicyProvider` | Thêm overload với scope+orgId | Breaking (libs) |
| `AdminPolicyProvider` | Implement context-aware getPolicy | Localized |
| `AdminSubjectProvider` | Inject scope+orgId vào attributes | Localized |
| `UIElement.scope` | Field mới bắt buộc | Migration + data |
| `SimulateNavigation` | Filter by scope, load scoped PolicySet | Significant refactor |
| `/abac/evaluate-elements` | Endpoint mới cho instance-level | New |

---

## 9. Thứ tự implement

```
[1] Extend PolicySetDefinition.scope enum + fix isRoot invariant
[2] Update AdminSubjectProvider — inject scope + orgId vào Subject.attributes
[3] Update AdminPolicyProvider.getPolicy() — context-aware (coordinate với libs/abac)
[4] Add scope field vào UIElement + migration data
[5] Update SimulateNavigation — filter by scope
[6] Thêm TENANT PolicySet template data
[7] /abac/evaluate-elements endpoint
```

---

*Tài liệu thiết kế — 2026-04-11*
