# Frontend Plan — Operator Portal

## Phạm vi

Implement frontend cho Operator Portal — giao diện cho người dùng có scope `OPERATOR` quản lý tòa nhà được giao. Đồng thời bổ sung Admin panel để SUPER_ADMIN/BQL_MANAGER quản lý operators.

Ảnh hưởng: `web/` (Angular) + `services/web-gateway/` (proxy config).

---

## Quyết định thiết kế (đã chốt)

### Q1 — Context Switching UI
**Chọn: Toolbar Switcher (Option B)**

- Auto-redirect sau login theo scope chính (ADMIN → `/admin`, OPERATOR → `/operator`).
- Nếu user có cả ADMIN + OPERATOR: hiển thị dropdown switcher trên toolbar.
- Không dùng `/select-context` page riêng — UX thêm friction không cần thiết.

### Q2 — Property-service Proxy
**Chọn: Thêm proxy vào web-gateway (Option A)**

- Property-service cần thiết cho Operator Portal (assets, agreements).
- Không dùng direct call hay BFF riêng — giữ nhất quán với kiến trúc hiện tại.
- Thêm route `/api/property/v1/**` → `property-service` với `tokenRelay()`.

### Q3 — Create Agreement (Operator tạo hợp đồng)
**Chọn: Party ID là text field, defer autocomplete (Option C)**

- Operator nhập partyId thủ công — đủ dùng cho MVP.
- Không cần gọi party-service hay autocomplete trong phase này.
- Party lookup có thể thêm sau khi party-service sẵn sàng.

---

## Trạng thái hiện tại (baseline)

| Thành phần                           | Trạng thái                                                              |
|--------------------------------------|-------------------------------------------------------------------------|
| `AuthService.SessionUser`            | `{id, roles[], requiresProfileCompletion}` — chưa có `contexts`         |
| `adminGuard`                         | `user.roles.some(r => r.name === 'ADMIN')` — brittle, gắn với role name |
| `operatorGuard`                      | Chưa có                                                                 |
| `ContextService`                     | Chưa có                                                                 |
| `ActiveBuildingService`              | Chưa có                                                                 |
| Web-gateway property-service proxy   | Chưa có                                                                 |
| Operator portal shell `/operator/**` | Chưa có                                                                 |

---

## Phase 1 — Core Infrastructure

> Không render UI mới. Chỉ đặt nền cho các phase sau.

### 1.1 `ContextService`

File: `web/src/app/core/services/context.service.ts`

```typescript
export interface ContextView {
  contextId: string;
  scope: 'ADMIN' | 'OPERATOR' | 'TENANT' | 'RESIDENT';
  orgId: string;
  orgType: string;
  displayName: string;
  roles: string[];
}
```

- Gọi `GET /api/admin/v1/auth/contexts?userId={id}` sau khi user load xong.
- Lưu kết quả vào `contexts = signal<ContextView[]>([])`.
- Expose `contextsForScope(scope)` helper.

### 1.2 `AuthService` refactor

File: `web/src/app/core/services/auth.service.ts`

- Thêm `contexts: ContextView[]` vào `SessionUser`.
- Sau khi load user profile thành công → gọi `ContextService.loadContexts(userId)` → merge vào `SessionUser`.
- Không breaking existing callers — `roles[]` vẫn giữ nguyên.

### 1.3 `adminGuard` refactor

File: `web/src/app/core/guards/admin.guard.ts`

```typescript
// Trước:
user.roles.some(r => r.name === 'ADMIN')
// Sau:
user.contexts.some(c => c.scope === 'ADMIN')
```

### 1.4 `operatorGuard` (mới)

File: `web/src/app/core/guards/operator.guard.ts`

```typescript
export const operatorGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const user = auth.currentUser();
  if (!user) return router.parseUrl('/login');
  if (user.contexts.some(c => c.scope === 'OPERATOR')) return true;
  return router.parseUrl('/app/dashboard');
};
```

### 1.5 `ActiveBuildingService` (mới)

File: `web/src/app/core/services/active-building.service.ts`

- `activeContext = signal<ContextView | null>(null)`.
- `init(contexts: ContextView[])`: nếu chỉ có 1 OPERATOR context → auto-select; nếu nhiều → để null (user tự chọn trên toolbar).
- `select(context: ContextView)`: set signal.

### 1.6 Post-login routing

File: `web/src/app/portal/portal.ts` (component hiện có)

- Sau khi contexts load: nếu có ADMIN context → navigate `/admin`; else nếu có OPERATOR context → navigate `/operator`; else giữ `/app/dashboard`.
- Toolbar `DashboardComponent`: nếu user có cả ADMIN + OPERATOR context → hiển thị `<mat-select>` để switch giữa `/admin` và `/operator`.

---

## Phase 2 — Web Gateway: Property-service + Party-service Proxy

File: `services/web-gateway/src/main/resources/application.properties`

Thêm:
```properties
webgateway.routes.property-service.uri=http://localhost:8083
webgateway.routes.party-service.uri=http://localhost:8084
```

File: `services/web-gateway/src/main/java/.../infrastructure/configuration/RouteConfiguration.java`

Thêm 2 routes:
```java
// /api/property/** → property-service /api/**
.route("property-service", rs -> rs
        .path("/api/property/**")
        .filters(f -> f.tokenRelay().saveSession()
                .rewritePath("/api/property/(?<segment>.*)", "/api/${segment}"))
        .uri(propertyServiceUri))
// /api/party/** → party-service /api/**
.route("party-service", rs -> rs
        .path("/api/party/**")
        .filters(f -> f.tokenRelay().saveSession()
                .rewritePath("/api/party/(?<segment>.*)", "/api/${segment}"))
        .uri(partyServiceUri))
```

---

## Phase 3 — Admin Panel Extensions

> Thêm section "Quản lý Operator" vào admin panel hiện có.

### 3.1 Route

File: `web/src/app/app.routes.ts` — thêm vào children của `/admin`:

```typescript
{ path: 'operators', loadComponent: () => import('./admin/operators/operator-list.component') },
{ path: 'operators/link-party', loadComponent: () => import('./admin/operators/link-party.component') },
{ path: 'operators/:buildingId', loadComponent: () => import('./admin/operators/operator-detail.component') },
```

### 3.2 `OperatorManagementService`

File: `web/src/app/admin/operators/operator-management.service.ts`

| Method                                               | Endpoint                                                      |
|------------------------------------------------------|---------------------------------------------------------------|
| `linkPartyId(userId, partyId)`                       | `POST /api/admin/v1/operators/link-party`                     |
| `assignOperatorContext(buildingId, req)`             | `POST /api/admin/v1/operators/{buildingId}/assign`            |
| `revokeOperatorContext(buildingId, userId)`          | `DELETE /api/admin/v1/operators/{buildingId}/revoke/{userId}` |
| `findOperatorsByBuilding(buildingId)`                | `GET /api/admin/v1/operators/{buildingId}`                    |
| `assignRolesToOperator(buildingId, userId, roleIds)` | `PUT /api/admin/v1/operators/{buildingId}/roles/{userId}`     |

### 3.3 Components

| Component                   | Mô tả                                                                   |
|-----------------------------|-------------------------------------------------------------------------|
| `operator-list.component`   | Chọn building → hiển thị danh sách operators (gọi UC-044)               |
| `operator-detail.component` | Chi tiết operator tại building — gán/gỡ roles (UC-045), revoke (UC-043) |
| `link-party.component`      | Form để admin gắn partyId cho user (UC-041)                             |

### 3.4 Sidebar

File: `web/src/app/admin/dashboard/` — thêm nav item "Quản lý Operator" → `/admin/operators`.

---

## Phase 4 — Operator Portal Shell + Features

### 4.1 Shell

Tạo thư mục `web/src/app/operator/`

File: `web/src/app/operator/operator-shell.component.ts`

- Layout: sidebar trái (nav links) + header với building selector.
- Building selector: `<mat-select>` bind vào `ActiveBuildingService.activeContext`.
- Sidebar nav: Dashboard, Tòa nhà & Căn hộ, Hợp đồng, Operators.

Route trong `app.routes.ts`:
```typescript
{
  path: 'operator',
  canActivate: [authGuard, operatorGuard],
  loadComponent: () => import('./operator/operator-shell.component'),
  children: [
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    { path: 'dashboard', loadComponent: () => import('./operator/dashboard/operator-dashboard.component') },
    { path: 'assets', loadComponent: () => import('./operator/assets/asset-list.component') },
    { path: 'agreements', loadComponent: () => import('./operator/agreements/agreement-list.component') },
    { path: 'agreements/create', loadComponent: () => import('./operator/agreements/create-agreement.component') },
    { path: 'operators', loadComponent: () => import('./operator/operators/operators.component') },
  ]
}
```

### 4.2 Services mới

| Service                     | File                                       | Gọi                                                                               |
|-----------------------------|--------------------------------------------|-----------------------------------------------------------------------------------|
| `FixedAssetService`         | `operator/services/fixed-asset.service.ts` | `GET /api/property/v1/assets/{id}/children`                                       |
| `AgreementService` (extend) | `operator/services/agreement.service.ts`   | `GET /api/property/v1/agreements?buildingId=`, `POST /api/property/v1/agreements` |

### 4.3 Features

**Dashboard** — thống kê nhanh: số căn hộ, số hợp đồng active, số operators.

**Tòa nhà & Căn hộ** (`/operator/assets`)
- Hiển thị cây asset: building → floors → units.
- Gọi `GET /api/property/v1/assets/{buildingId}/children` đệ quy.
- Nguồn `buildingId`: từ `ActiveBuildingService.activeContext().orgId`.

**Hợp đồng** (`/operator/agreements`)
- List: gọi `GET /api/property/v1/agreements?buildingId={orgId}` (UC-013).
- Filter theo trạng thái.
- Nút "Tạo hợp đồng" → form tại `/operator/agreements/create`.

**Tạo hợp đồng** (`/operator/agreements/create`)
- Fields: assetId (select từ asset tree), partyId (text, nhập tay — Option C), agreementType, startDate, endDate.
- Gọi `POST /api/property/v1/agreements`.

**Operators** (`/operator/operators`)
- Danh sách operators tại building hiện tại.
- Gọi `GET /api/admin/v1/operators/{buildingId}` (UC-044).
- Read-only — chỉnh sửa phải qua Admin panel.

---

## Thứ tự implement

```
Phase 1 (core infra) → Phase 2 (gateway) → Phase 3 (admin ext) → Phase 4 (operator shell)
```

Phase 1 và 2 có thể song song. Phase 3 và 4 phụ thuộc Phase 1+2.

---

## Status

| Phase                                                   | Status          |
|---------------------------------------------------------|-----------------|
| Phase 1 — Core Infrastructure                           | `[x] Completed` |
| Phase 2 — Web Gateway Proxy                             | `[x] Completed` |
| Phase 3 — Admin Panel Extensions                        | `[x] Completed` |
| Phase 3.5 — Building Management (admin + property CRUD) | `[x] Completed` |
| Phase 4 — Operator Portal Shell + Features              | `[x] Completed` |
