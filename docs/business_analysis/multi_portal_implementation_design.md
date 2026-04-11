# Multi-Portal Implementation Design

## 1. Bối cảnh & Quyết định kiến trúc

### Hiện trạng

```
1 Angular SPA (web)
1 Web Gateway (BFF / OAuth2 Client)
1 OAuth2 Service (Authorization Server)
1 Admin Service (User, Role, ABAC)
```

Hệ thống hiện tại chưa có khái niệm "portal" ở infrastructure level — chỉ có 1 client, 1 frontend app.

### Vấn đề cần giải quyết

Một người thực có thể đồng thời là:
- Super Admin của platform Apartcom
- BQL Manager của Tòa nhà X
- Cư dân căn hộ 10B tại Tòa nhà Y
- Nhân viên Công ty Z (tenant tại Tòa nhà X)

Hệ thống phải nhìn nhận đây là **một danh tính duy nhất**, không phải 4 tài khoản riêng biệt. Phân quyền và navigation phải phụ thuộc vào context người đó đang hoạt động.

### Quyết định

Áp dụng **Solution 2 + 4 hybrid**:
- **Solution 2** (Single Account, Context Switching): một tài khoản, nhiều role contexts, user chọn context khi đăng nhập.
- **Solution 4** (Workspace Model): mỗi context là một "membership" — user được assign vào org với role cụ thể.

Bản chất: Solution 4 = Solution 2 + cơ chế quản lý membership rõ ràng hơn. Core mechanics giống nhau.

**Ràng buộc triển khai**: Không tách service/client ngay. Toàn bộ implement trong infrastructure hiện tại, dùng **interface abstraction** để migration sau không tốn effort.

---

## 2. Portal Taxonomy

```
┌──────────────────┬─────────────────────────────┬────────────────────────┬────────────────────┐
│     Portal       │          Scope               │     System Roles       │   OrgId Dimension  │
├──────────────────┼─────────────────────────────┼────────────────────────┼────────────────────┤
│ ADMIN            │ Platform-wide                │ SUPER_ADMIN            │ null               │
│                  │                              │ PLATFORM_STAFF         │                    │
├──────────────────┼─────────────────────────────┼────────────────────────┼────────────────────┤
│ OPERATOR         │ Per building                 │ BQL_MANAGER            │ Building ID        │
│ (BQL)            │                              │ BQL_STAFF              │                    │
│                  │                              │ BQL_FINANCE            │                    │
│                  │                              │ BQL_TECHNICAL          │                    │
├──────────────────┼─────────────────────────────┼────────────────────────┼────────────────────┤
│ RESIDENT         │ Per unit                     │ RESIDENT               │ Unit ID            │
├──────────────────┼─────────────────────────────┼────────────────────────┼────────────────────┤
│ TENANT           │ Per company                  │ TENANT_ADMIN           │ Company ID         │
│ (Doanh nghiệp)   │                              │ TENANT_EMPLOYEE        │                    │
└──────────────────┴─────────────────────────────┴────────────────────────┴────────────────────┘
```

**Ghi chú**:
- `ADMIN` portal: không có OrgId — quyền platform-wide.
- `OPERATOR` portal: OrgId là Building. Một user có thể là BQL_MANAGER ở nhiều building.
- `RESIDENT` portal: OrgId là Unit (căn hộ cụ thể). Một user có thể là RESIDENT ở nhiều unit (ví dụ: chủ nhiều căn).
- `TENANT` portal: OrgId là Company. TENANT_ADMIN tự quản membership nội bộ công ty.

---

## 3. Identity & Membership Model

### User Aggregate — Extension

Hiện tại `User` giữ `List<RoleId>` flat, không có org context. Cần mở rộng thành `List<RoleContext>`:

```
User (Aggregate Root)
├── UserId
├── Username / Credentials
├── ...
└── List<RoleContext>                     ← thay thế List<RoleId>
    ├── scope    (ADMIN | OPERATOR | RESIDENT | TENANT)
    ├── orgId    (String — nullable, opaque reference đến entity ở service khác)
    └── roleIds  (List<RoleId>)
```

**Sample data**:

```
user_id  │ scope     │ org_id      │ role_ids
─────────┼───────────┼─────────────┼──────────────────────
user-001 │ ADMIN     │ null        │ [SUPER_ADMIN]
user-001 │ OPERATOR  │ building-x  │ [BQL_MANAGER]
user-001 │ OPERATOR  │ building-y  │ [BQL_MANAGER]
user-001 │ RESIDENT  │ unit-10b    │ [RESIDENT]
user-001 │ TENANT    │ company-z   │ [TENANT_ADMIN]
```

**Invariants**:
- Unique constraint trên `(userId, scope, orgId)`.
- `orgId` là opaque string — admin service không validate entity tồn tại hay không (validation thuộc caller).
- Lifecycle gắn với User: khi User bị xóa/deactivate, toàn bộ RoleContext bị cascade.

### Ai tạo/quản lý RoleContext

```
Portal     │ Ai tạo RoleContext                          │ Constraint
───────────┼─────────────────────────────────────────────┼──────────────────────────────
ADMIN      │ SUPER_ADMIN                                 │ Không giới hạn
OPERATOR   │ Platform admin                              │ Building phải tồn tại
           │ + BQL_MANAGER (assign BQL_STAFF nội bộ)    │ Chỉ trong building của mình
RESIDENT   │ BQL_MANAGER của building đó                 │ Unit phải thuộc building của BQL
TENANT     │ Platform admin (tạo TENANT_ADMIN)           │ Company phải tồn tại
           │ + TENANT_ADMIN (assign nhân viên nội bộ)    │ Chỉ trong company của mình
```

---

## 4. Two-Phase Authentication Flow

Tách authentication (ai bạn là) và context activation (bạn đang hoạt động với tư cách gì).

### Phase 1 — Login → Bare Token

```
POST /oauth2/token  (standard Authorization Code flow)

Response — Bare Token:
{
  "sub": "user-001",
  "email": "a@apartcom.vn",
  "token_type": "bearer"
  // Không có activeScope, không có roles
  // Chỉ được gọi: GET /auth/contexts + POST /auth/context/activate
}
```

Bare token **bị giới hạn** — chỉ có thể gọi context-selection endpoints, không gọi được business API.

### Phase 2a — Lấy danh sách contexts

```
GET /auth/contexts
Authorization: Bearer <bare_token>

Response:
[
  {
    "scope":   "ADMIN",
    "orgId":   null,
    "orgName": null,
    "roles":   ["SUPER_ADMIN"]
  },
  {
    "scope":   "OPERATOR",
    "orgId":   "building-x",
    "orgName": "Tòa nhà X",
    "roles":   ["BQL_MANAGER"]
  },
  {
    "scope":   "OPERATOR",
    "orgId":   "building-y",
    "orgName": "Tòa nhà Y",
    "roles":   ["BQL_MANAGER"]
  },
  {
    "scope":   "RESIDENT",
    "orgId":   "unit-10b",
    "orgName": "Căn hộ 10B",
    "roles":   ["RESIDENT"]
  }
]
```

FE hiển thị context selector dựa trên danh sách này. Nếu chỉ có 1 context → auto-activate, bỏ qua selector.

### Phase 2b — Activate context → Scoped Token

```
POST /auth/context/activate
Authorization: Bearer <bare_token>
{
  "scope": "OPERATOR",
  "orgId": "building-x"
}

Response — Scoped Token:
{
  "sub":         "user-001",
  "activeScope": "OPERATOR",
  "activeOrgId": "building-x",
  "roles":       ["BQL_MANAGER"]
}
```

Backend validate: `(userId, scope, orgId)` phải tồn tại trong `user_role_context`. Nếu không → 403.

### Context Switch trong session

User đang ở OPERATOR, muốn chuyển sang RESIDENT:

```
POST /auth/context/activate
Authorization: Bearer <scoped_token>   ← dùng scoped token hiện tại
{
  "scope": "RESIDENT",
  "orgId": "unit-10b"
}
→ Scoped Token mới với activeScope=RESIDENT
```

FE huỷ scoped token cũ, lưu token mới, reload navigation.

---

## 5. Token Structure

### Bare Token Claims

```json
{
  "sub":        "user-001",
  "email":      "a@apartcom.vn",
  "iss":        "https://auth.apartcom.vn",
  "iat":        1712800000,
  "exp":        1712803600,
  "token_use":  "identity"
}
```

### Scoped Token Claims

```json
{
  "sub":          "user-001",
  "email":        "a@apartcom.vn",
  "iss":          "https://auth.apartcom.vn",
  "iat":          1712800000,
  "exp":          1712814400,
  "token_use":    "access",
  "activeScope":  "OPERATOR",
  "activeOrgId":  "building-x",
  "roles":        ["BQL_MANAGER"]
}
```

**TTL theo scope** (risk-appropriate expiry):

```
ADMIN    → 1 giờ    (high privilege)
OPERATOR → 4 giờ
TENANT   → 4 giờ
RESIDENT → 8 giờ   (low risk)
```

---

## 6. ABAC — Thay đổi cần thiết

### 6.1 PolicySetDefinition — Scope per root

`isRoot: boolean` hiện tại ngầm định "1 root duy nhất toàn hệ thống". Cần sửa thành root per scope:

```
PolicySet A: scope=ADMIN,     isRoot=true
PolicySet B: scope=OPERATOR,  isRoot=true
PolicySet C: scope=RESIDENT,  isRoot=true
PolicySet D: scope=TENANT,    isRoot=false, tenantId=null    (template)
PolicySet E: scope=TENANT,    isRoot=false, tenantId="co-z"  (Company Z)
```

Thêm enum value `ADMIN`, `RESIDENT` vào `PolicySetDefinition.scope`.

### 6.2 PolicyProvider — Context-aware

```java
// Hiện tại (libs/abac interface)
AbstractPolicy getPolicy(String serviceName);

// Cần thêm overload
AbstractPolicy getPolicy(String serviceName, String activeScope, String orgId);
```

`AdminPolicyProvider` implement method mới:
1. Load PolicySetDefinition where `scope = activeScope AND isRoot = true`
2. Nếu scope = TENANT: ưu tiên PolicySet theo `tenantId = orgId`, fallback về template

### 6.3 AdminSubjectProvider — Inject context attributes

```java
// Hiện tại
Subject { userId, roles: ["BQL_MANAGER"], attributes: {} }

// Cần thêm
Subject {
  userId: "user-001",
  roles:  ["BQL_MANAGER"],
  attributes: {
    "scope": "OPERATOR",
    "orgId": "building-x"
  }
}
```

Extract `activeScope` và `activeOrgId` từ JWT claims khi build Subject.

### 6.4 UIElement — Thêm scope field

```
UIElement
├── elementId     (String — unique, immutable)
├── scope         (Enum: ADMIN | OPERATOR | RESIDENT | TENANT)   ← field mới
├── label
├── type
├── resourceId
└── actionId
```

`SimulateNavigation` filter UIElements theo `scope = activeScope` trước khi evaluate.

---

## 7. Frontend Authorization — 3-Layer Model

### Layer 1 & 2 — Navigation + Page Actions (static per context)

Evaluate một lần khi activate context. Cache visibility map cho toàn session.

```
Trigger: POST /auth/context/activate thành công
→ GET /abac/navigate?scope=OPERATOR&orgId=building-x
→ visibilityMap = { "nav_user_management": true, "btn_create_user": true, ... }
→ FE cache map, dùng cho toàn session
→ Invalidate khi switch context
```

### Layer 3 — Instance Actions (dynamic per record)

Evaluate sau khi fetch resource thành công. Backend đã enforce 403 nếu user không có quyền xem.

```
Step 1: GET /api/users/user-001
  → 403 nếu không có quyền xem → stop
  → 200 + UserDetailDto { id, orgId, status, ... }

Step 2: POST /abac/evaluate-elements
  {
    "elementIds": ["btn_edit_user", "btn_deactivate_user"],
    "objectData": { "orgId": "building-x", "status": "ACTIVE" }
  }
  → { "btn_edit_user": true, "btn_deactivate_user": false }

Step 3: FE render buttons theo visibility map
```

`objectData` do FE forward từ response Step 1. Nếu FE tamper data → worst case là hiển thị button không đúng, nhưng backend `@PreAuthorize` vẫn enforce ở Step thực thi — không phải security hole.

---

## 8. Interface Abstractions — Migration Path

Toàn bộ logic hiện tại implement trong admin service. Interfaces được đặt đúng chỗ để khi tách service chỉ cần thêm implementation mới.

```
libs/abac
└── PolicyProvider (interface)         ← context-aware signature
└── SubjectProvider (interface)

services/admin
└── AdminPolicyProvider                implements PolicyProvider
└── AdminSubjectProvider               implements SubjectProvider
└── AdminUserContextService            implements UserContextService (mới)
    ↳ query user_role_context từ User aggregate

services/oauth2
└── ContextActivationService           orchestrate Phase 2 flow
    ↳ gọi AdminUserContextService để validate + get roles
```

### Khi tách portal service (tương lai)

```
Thêm:   OperatorPolicyProvider   implements PolicyProvider
        → load PolicySet của riêng Operator service

Giữ:    AdminUserContextService  vẫn là source of truth cho user-role-context
        (identity concern không migrate)

Không cần sửa:
        OAuth2 flow, token structure, FE context selector
```

---

## 9. Ghi chú Open Items

| Item | Quyết định | Defer đến khi |
|------|------------|---------------|
| PolicySet fallback khi TENANT chưa có policy | Deny-by-default | Tenant portal implementation |
| TENANT self-service ABAC console | TENANT_ADMIN được edit PolicySet của mình | Tenant portal session riêng |
| Unit → Building validation khi assign RESIDENT | Trust caller (BQL portal validate trước) | Property service có API |
| Invitation flow (email invite, accept, expiry) | Không build giai đoạn này — admin assign trực tiếp | Tenant portal session riêng |
| Multiple OAuth2 clients per portal | 1 client hiện tại, tách khi portal service ra riêng | Portal service extraction |
