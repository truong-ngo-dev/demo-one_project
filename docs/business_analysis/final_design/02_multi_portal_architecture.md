# Multi-Portal Architecture — Design Specification

*Tài liệu thiết kế cuối cùng. Tham khảo phân tích tại: `../multi_portal_implementation_design.md`, `../multi_role_and_cross_portal_identity.md`*

---

## 1. Quyết định kiến trúc

### 1.1 Mô hình identity

Áp dụng **Solution 2 + 4 hybrid**:
- **Single Account**: một người = một tài khoản, không tạo account riêng per portal
- **Context Switching**: user chọn context (scope + org) khi đăng nhập hoặc switch trong session
- **Workspace/Membership model**: mỗi context là một RoleContext — user được assign vào org với role cụ thể

### 1.2 Phase 1 — Single SPA, path-based portal routing

**Ràng buộc**: Không tách service/client ngay. Toàn bộ implement trong 1 Angular SPA hiện tại.

```
1 Angular SPA (web)
1 Web Gateway (BFF / OAuth2 Client)
1 OAuth2 Service (Authorization Server)
1 Admin Service (User, Role, ABAC)
```

**Portal được phân biệt bằng URL path**, không phải domain riêng:

```
/admin/**      → ADMIN portal     (SUPER_ADMIN, PLATFORM_STAFF)
/operator/**   → OPERATOR portal  (BQL_MANAGER, BQL_STAFF, BQL_FINANCE, ...)
/tenant/**     → TENANT portal    (TENANT_ADMIN, TENANT_EMPLOYEE)
/resident/**   → RESIDENT portal  (RESIDENT)
```

FE dùng `activeScope` từ scoped token để:
1. Redirect user về đúng base path sau khi activate context
2. Load UIElements cho đúng scope
3. Guard routes (OPERATOR user không thể navigate vào `/admin/**`)

### 1.3 Migration path lên Phase 2

Thiết kế Phase 1 đảm bảo **migration không cần refactor lớn**:

```
Phase 1 (hiện tại):         Phase 2 (tương lai, khi cần):
─────────────────────────   ────────────────────────────────────
1 SPA, path-based           Tách thành app riêng per portal
  /admin/**                   admin.apartcom.vn
  /operator/**                operator.apartcom.vn
  /tenant/**                  tenant.apartcom.vn
  /resident/**                resident.apartcom.vn

1 OAuth2 client             Multiple OAuth2 clients, 1 per portal

AdminPolicyProvider         OperatorPolicyProvider (implements PolicyProvider)
implements PolicyProvider   → load PolicySet riêng của Operator service

Token structure:            Không thay đổi
Two-phase auth flow:        Không thay đổi
UIElement scope:            Không thay đổi
```

Interface abstraction tại `libs/abac` (PolicyProvider, SubjectProvider) là migration seam — khi tách
portal service chỉ cần thêm implementation mới, không sửa contract.

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
│                  │                              │ BQL_SECURITY           │                    │
├──────────────────┼─────────────────────────────┼────────────────────────┼────────────────────┤
│ TENANT           │ Per company                  │ TENANT_ADMIN           │ Company ID         │
│ (Doanh nghiệp)   │                              │ TENANT_EMPLOYEE        │                    │
├──────────────────┼─────────────────────────────┼────────────────────────┼────────────────────┤
│ RESIDENT         │ Per unit                     │ RESIDENT               │ Unit ID            │
└──────────────────┴─────────────────────────────┴────────────────────────┴────────────────────┘
```

**Ghi chú**:
- `ADMIN`: không có OrgId — quyền platform-wide
- `OPERATOR`: OrgId = Building. Một user có thể là BQL_MANAGER ở nhiều building (nhiều RoleContext)
- `TENANT`: OrgId = Company. TENANT_ADMIN tự quản membership nội bộ
- `RESIDENT`: OrgId = Unit. Một user có thể là RESIDENT ở nhiều unit (chủ nhiều căn)

---

## 3. Identity & RoleContext Model

### 3.1 User Aggregate — Extension

```
User (Aggregate Root)
├── UserId
├── Username / Credentials
└── List<RoleContext>                     ← thay thế List<RoleId>
    ├── scope    : Enum(ADMIN | OPERATOR | TENANT | RESIDENT)
    ├── orgId    : String — nullable, opaque reference
    └── roleIds  : List<RoleId>
```

**Sample data**:

```
userId   │ scope     │ orgId       │ roleIds
─────────┼───────────┼─────────────┼──────────────────────
user-001 │ ADMIN     │ null        │ [SUPER_ADMIN]
user-001 │ OPERATOR  │ building-x  │ [BQL_MANAGER]
user-001 │ OPERATOR  │ building-y  │ [BQL_MANAGER]
user-001 │ RESIDENT  │ unit-10b    │ [RESIDENT]
user-001 │ TENANT    │ company-z   │ [TENANT_ADMIN]
```

**Invariants**:
- Unique constraint trên `(userId, scope, orgId)`
- `orgId` là opaque string — admin service không validate entity tồn tại (validation thuộc caller)
- Khi User deactivate → toàn bộ RoleContext cascade deactivate

### 3.2 Ai tạo/quản lý RoleContext

```
Portal     │ Ai tạo RoleContext                           │ Constraint
───────────┼──────────────────────────────────────────────┼───────────────────────────────
ADMIN      │ SUPER_ADMIN                                  │ Không giới hạn
OPERATOR   │ Platform admin (tạo BQL_MANAGER đầu tiên)   │ Building phải tồn tại
           │ BQL_MANAGER (assign BQL_STAFF nội bộ)       │ Chỉ trong building của mình
TENANT     │ Platform admin (tạo TENANT_ADMIN đầu tiên)  │ Company phải tồn tại
           │ TENANT_ADMIN (assign TENANT_EMPLOYEE)        │ Chỉ trong company của mình
RESIDENT   │ BQL_MANAGER của building đó                  │ Unit phải thuộc building của BQL
```

---

## 4. Two-Phase Authentication Flow

### 4.1 Tổng quan

Tách authentication (ai bạn là) và context activation (bạn đang hoạt động với tư cách gì) thành 2 bước.

```
Phase 1: Login → Bare Token
  POST /oauth2/token (Authorization Code flow)
  → Token chỉ chứa identity, không có scope/role
  → Chỉ được gọi: GET /auth/contexts + POST /auth/context/activate

Phase 2a: Lấy danh sách contexts
  GET /auth/contexts
  → Danh sách tất cả RoleContext của user
  → FE hiển thị context selector

Phase 2b: Activate context → Scoped Token
  POST /auth/context/activate { scope, orgId }
  → Scoped Token với activeScope + activeOrgId + roles
  → Dùng cho tất cả business API calls
```

### 4.2 Bare Token

```json
{
  "sub":       "user-001",
  "email":     "a@apartcom.vn",
  "iss":       "https://auth.apartcom.vn",
  "iat":       1712800000,
  "exp":       1712803600,
  "token_use": "identity"
}
```

Bare token bị giới hạn — **không** được gọi business API, chỉ được gọi `/auth/contexts` và
`/auth/context/activate`.

### 4.3 Scoped Token

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

### 4.4 Context Switch trong session

```
User đang ở OPERATOR, switch sang RESIDENT:

POST /auth/context/activate
Authorization: Bearer <scoped_token hiện tại>
{ "scope": "RESIDENT", "orgId": "unit-10b" }

→ Scoped Token mới với activeScope=RESIDENT
→ FE: huỷ token cũ, lưu token mới, reload navigation
```

### 4.5 TTL theo scope (risk-appropriate)

```
ADMIN    → 1 giờ    (high privilege)
OPERATOR → 4 giờ
TENANT   → 4 giờ
RESIDENT → 8 giờ   (low risk)
```

---

## 5. Frontend — Context Activation Flow

### 5.1 Sau khi activate context

```
Trigger: POST /auth/context/activate thành công
→ Redirect về base path của scope (vd: /operator)
→ GET /abac/navigate?scope=OPERATOR&orgId=building-x
→ visibilityMap = { "nav_user_management": true, ... }
→ FE cache visibilityMap cho toàn session
→ Invalidate cache khi switch context
```

### 5.2 Route guard

```
/admin/**    → guard: activeScope == 'ADMIN'
/operator/** → guard: activeScope == 'OPERATOR'
/tenant/**   → guard: activeScope == 'TENANT'
/resident/** → guard: activeScope == 'RESIDENT'
```

Nếu user navigate trực tiếp vào path sai scope → redirect về context selector.

### 5.3 Auto-activate khi chỉ có 1 context

```
GET /auth/contexts → [ { scope: "RESIDENT", orgId: "unit-10b", roles: ["RESIDENT"] } ]
→ Chỉ có 1 context → auto POST /auth/context/activate, bỏ qua selector
```

---

## 6. Interface Abstractions (Migration Seams)

```
libs/abac
├── PolicyProvider (interface)    ← context-aware signature (xem 01_abac_and_ui_element.md)
└── SubjectProvider (interface)

services/admin
├── AdminPolicyProvider           implements PolicyProvider
├── AdminSubjectProvider          implements SubjectProvider
└── UserContextService            (mới) — query RoleContext từ User aggregate

services/oauth2
└── ContextActivationService      — orchestrate Phase 2 flow
    → gọi UserContextService để validate context + get roles
```

---

*Tài liệu thiết kế — 2026-04-11*
