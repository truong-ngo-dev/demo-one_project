# Design Report: ABAC with Dynamic Navigation Based on Authorization

> Tổng hợp các quyết định thiết kế đã được thống nhất.
> Đây là source of truth cho phase implement.

---

## 1. Bài toán

Xây dựng hệ thống phân quyền cho Apartcom theo mô hình:
- **Platform (Apartcom)** cung cấp phần mềm quản lý tòa nhà cho BQL
- **BQL** (Ban Quản Lý) là SaaS tenant — đơn vị trả tiền, quản lý tòa nhà
- **Building tenants** (hộ gia đình, doanh nghiệp thuê mặt bằng) là user groups trong BQL
- UI phải **dynamic theo authorization** — mỗi user thấy một giao diện khác nhau tùy quyền

---

## 2. Authorization Layer

### 2 tầng rõ ràng

```
Layer 1 — BQL level (implement trước)
  Platform/BQL kiểm soát:
    - BQL staff có quyền gì
    - Building tenant được dùng module nào (entitlement)
  Algorithm: DENY_OVERRIDES — BQL deny không thể bị override

Layer 2 — Tenant internal (implement độc lập sau)
  Tenant admin tự quản lý:
    - Members trong org có quyền gì
    - Delegated policy management
  Algorithm: DENY_UNLESS_PERMIT
```

### Multi-tenant model

```
SaaS tenant = BQL Tòa Golden Star    ← đơn vị mua phần mềm
Building tenant = Công ty ABC,       ← người thuê mặt bằng
                  Hộ gia đình 1201      = user groups trong SaaS tenant
```

### Party management

Subject có 2 dạng khi là building tenant:
- `PERSON` → hộ gia đình, cá nhân
- `ORGANIZATION` → doanh nghiệp, team

Subject từ token được enrich thêm:
```
Token claims:   userId, platformRoles
Enrich thêm:    partyType, tenantUnit, tenantParty, tenantRoles, managedDepartments, ...
```

Rule phân nhánh theo partyType khi cần (COMPOSITION OR).

---

## 3. Core Engine

Dùng lại `abac-authorization` library (JitPack):
- `authz-core`: PdpEngine, domain model (PolicySet/Policy/Rule/Expression)
- `authz-client`: PEP (@PreEnforce/@PostEnforce), PIP (providers), RAP

### Domain model

```
PolicySet (isRoot: true)
  combineAlgorithmName: DENY_OVERRIDES | DENY_UNLESS_PERMIT
  └── Policy
        target: Expression (object.name == 'employee')
        combineAlgorithmName: DENY_UNLESS_PERMIT
        └── Rule
              target: Expression   — ai / hành động nào áp dụng
              condition: Expression — điều kiện bổ sung
              effect: PERMIT | DENY
```

Expression có 2 dạng:
- `LITERAL`: SpEL expression đơn — `subject.roles.contains('MANAGER')`
- `COMPOSITION`: kết hợp sub-expressions với AND / OR

SpEL context variables: `subject`, `object`, `action`, `environment`

### Policy scope

```
scope=OPERATOR  → Operator admin (BQL) từng tòa toàn quyền quản lý
                   (staff permissions + building tenant grants)
scope=TENANT    → Tenant admin quản lý (Layer 2, làm sau)
```

> Không có scope PLATFORM: Apartcom chỉ maintain software, không can thiệp policy của từng tòa.
> Feature/module access là licensing concern — xử lý ở infrastructure khi setup instance, không phải ABAC.

---

## 4. RAP Layer — HTTP → Semantic Action

Tránh viết URL trong policy condition. RAP dịch trước khi vào PDP:

```
GET  /api/employees/{id}     → resource="employee", action="READ"
POST /api/employees          → resource="employee", action="CREATE"
PUT  /api/employees/{id}     → resource="employee", action="UPDATE"
DELETE /api/employees/{id}   → resource="employee", action="DELETE"
```

Policy chỉ cần: `action.getAttribute('name') == 'READ'` — không biết URL cụ thể.

Khai báo qua annotation trên controller:
```text
@GetMapping("/employees/{id}")
@ResourceMapping(resource = "employee", action = "READ")
@PreEnforce
public ResponseEntity<EmployeeDetail> getEmployee(...) { ... }
```

---

## 5. API-level Enforcement vs UIElement Visibility

### Hai tầng tách biệt

```
API enforcement (Security — bắt buộc):
  @PreEnforce / @PostEnforce
  BE load data thật từ DB để evaluate
  Không tin bất kỳ input nào từ FE
  Không thể bypass

UIElement visibility (UX — optional):
  AuthorizationContextEngine
  Chỉ ảnh hưởng show/hide button
  FE có thể tamper → chỉ thấy button sai, không làm được gì thêm
  API vẫn chặn ở tầng trên
```

---

## 6. AuthorizationContextEngine

Service mới trong admin service, xử lý batch evaluation cho UIElement:

```java
@Service
public class AuthorizationContextEngine {
    // Providers: Subject, Policy, Environment (dùng lại từ engine)
    // Thêm mới: UIElementRepository, ResourceTypeRegistry

    public AuthzContextResponse resolve(
        Principal principal,
        List<String> elementIds,    // optional — nếu có
        String resource,            // optional — nếu có instance context
        Map<String, Object> data    // optional — FE gửi data đã fetch
    );
}
```

### Synthetic Action (không có HttpServletRequest)

```java
public static Action semantic(String actionName) {
    Action action = new Action();
    action.setAttributes(Map.of("name", actionName));
    return action;
}
```

---

## 7. Authorization Context API

### Endpoint

```
POST /api/authorization-context
```

### Request variants

**Navigation-level** (sau login, screen-level):
```json
{ "scope": "navigation" }
```

**Instance-level** (vào detail page, FE gửi kèm data đã fetch):
```json
{
  "resource": "employee",
  "data": { "id": "123", "department": "engineering", "status": "ACTIVE" }
}
```

### Response

```json
{
  "grants": {
    "employee:READ":   true,
    "employee:UPDATE": true,
    "employee:DELETE": false,
    "employee:LOCK":   false
  }
}
```

Grants map theo pattern `"resource:ACTION"` — không theo elementId.

---

## 8. FE Integration

### GRANTS constants (không hardcode string)

```typescript
export const GRANTS = {
  EMPLOYEE: {
    LIST:   'employee:LIST',
    CREATE: 'employee:CREATE',
    UPDATE: 'employee:UPDATE',
    DELETE: 'employee:DELETE',
  }
} as const;
```

### AuthzContextService

```typescript
@Injectable({ providedIn: 'root' })
export class AuthzContextService {
  private _grants = signal<Record<string, boolean>>({});

  loadNavigation(): void { /* gọi scope=navigation */ }

  loadForInstance(resource: string, data: object): void {
    /* gọi với resource + data, chạy song song với data API call */
  }

  grant(key: string): Signal<boolean> {
    return computed(() => this._grants()[key] ?? false);
  }
}
```

### Route config

```typescript
{
  path: 'employees/:id',
  component: EmployeeDetailComponent,
  data: {
    authzResource: 'employee',
    authzIdParam: 'id'
  }
}
```

### Component pattern

```typescript
export class EmployeeDetailComponent implements OnInit {
  canUpdate = this.authz.grant(GRANTS.EMPLOYEE.UPDATE);
  canDelete = this.authz.grant(GRANTS.EMPLOYEE.DELETE);

  ngOnInit() {
    // Song song: load data + load grants
    forkJoin({
      employee: this.employeeService.getById(this.id),
    }).subscribe(({ employee }) => {
      this.employee.set(employee);
      this.authz.loadForInstance('employee', employee);
    });
  }
}
```

```html
@if (canUpdate()) { <button>Edit</button> }
@if (canDelete()) { <button>Delete</button> }
```

### List page — không dùng AuthzContext cho row-level

List page dùng **backend filter** tại query level — user chỉ nhận data họ được phép xem. Không cần per-row UIElement evaluation.

---

## 9. Architecture Decision: ABAC trong Admin Service

**Chọn**: Embed ABAC capability vào `services/admin` (không tạo service riêng).

**Lý do**:
- Admin đã có user/role data cần cho SubjectProvider
- Ít moving parts, không phụ thuộc service khác
- Có thể extract ra `services/authz` sau khi hệ thống lớn hơn — interface không đổi

**Services khác** (maintenance, parking...) khi cần `@PreEnforce`:
- Load policy từ admin service **lúc startup**
- Cache local — không phụ thuộc admin service ở runtime

---

## 10. Instance-level Data Flow

```
FE vào /employees/123

  t=0:   GET /employees/123 bắt đầu
  t+n:   GET trả về data
  t+n:   POST /authorization-context { resource, data: {...} } bắt đầu
  t+n+m: POST trả về grants
         → render data + buttons cùng lúc, đúng trạng thái ngay từ đầu
```

Sequential để tránh button flicker (hiện rồi ẩn). Delta thời gian không đáng kể vì
authz call không có DB call — nhanh hơn nhiều so với data call.

User click Edit → PUT /employees/123 → @PreEnforce (load data thật từ DB) → PERMIT/DENY

---

## 11. Boundary với Tenant ABAC

```
Apartcom ABAC (report này):
  ✓ Platform-level policies
  ✓ BQL staff permissions
  ✓ Building tenant module grants
  ✓ AuthzContext API
  ✓ UIElement visibility

Tenant ABAC (implement độc lập sau):
  ○ Tenant admin quản lý internal policies
  ○ Delegated policy management (PAP scoping)
  ○ Member-level permissions trong org
  ○ Không ảnh hưởng architecture của phần trên
```
