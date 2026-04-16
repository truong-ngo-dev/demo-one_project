# SERVICE_MAP — web (Angular SPA)

> **First entry point cho AI agents.** Đọc file này trước khi dùng bất kỳ công cụ tìm kiếm nào.
> Chi tiết convention xem tại [`CLAUDE.md`](CLAUDE.md).

---

## 🗺️ 1. Routing Structure

Toàn bộ routes định nghĩa tại [`src/app/app.routes.ts`](src/app/app.routes.ts).
Tất cả components dùng **lazy loading** qua `loadComponent()`.

```
/                   → LandingComponent          (public)
/register           → RegisterComponent         (public) [PLANNED]
/admin/**           → DashboardComponent        (guard: authGuard + adminGuard)
/app/**             → PortalComponent           (guard: authGuard)
/**                 → redirect về /
```

### Admin routes (`/admin`)

| Path | Component | Mô tả |
|---|---|---|
| `/admin/dashboard` | `HomeComponent` | IAM overview — metrics |
| `/admin/users` | `UsersComponent` | Danh sách users, tìm kiếm, filter |
| `/admin/users/:id` | `UserDetailComponent` | Chi tiết user, sessions, roles |
| `/admin/roles` | `RolesComponent` | Quản lý roles |
| `/admin/active-sessions` | `ActiveSessionsComponent` | Sessions đang active toàn hệ thống |
| `/admin/login-activities` | `LoginActivitiesComponent` | Lịch sử đăng nhập |
| `/admin/abac/resources` | `ResourcesComponent` | Resource & Action catalogue |
| `/admin/abac/resources/:id` | `ResourceDetailComponent` | Chi tiết resource + actions |
| `/admin/abac/policy-sets` | `PolicySetsComponent` | Danh sách PolicySet |
| `/admin/abac/policy-sets/:id` | `PolicySetDetailComponent` | Chi tiết PolicySet + policies |
| `/admin/abac/policies/:id` | `PolicyDetailComponent` | Chi tiết Policy + rules |
| `/admin/abac/ui-elements` | `UIElementsComponent` | UIElement registry |
| `/admin/abac/ui-elements/:id` | `UIElementDetailComponent` | Chi tiết UIElement + coverage |
| `/admin/abac/expressions` | `ExpressionsComponent` | Named expression library (list + delete) |
| `/admin/abac/simulator` | `SimulatorComponent` | Policy simulator |
| `/admin/abac/audit-log` | `AuditLogComponent` | ABAC change audit log |

### Portal routes (`/app`)

| Path | Component | Mô tả |
|---|---|---|
| `/app/dashboard` | `PortalHomeComponent` | User home |
| `/app/profile` | `ProfileComponent` | Xem / cập nhật profile |
| `/app/profile/password` | `ChangePasswordComponent` | Đổi password |
| `/app/devices` | `DevicesComponent` | Devices đã đăng nhập + revoke session |
| `/app/login-history` | `LoginHistoryComponent` | Lịch sử đăng nhập của user |

---

## 🧩 2. Core Layer (`src/app/core/`)

### Guards (`core/guards/`)

- **`auth.guard.ts`** — `authGuard: CanActivateFn`
  Gọi `AuthService.loadCurrentUser()`. Null → redirect `/`. Dùng cho mọi route cần xác thực.

- **`admin.guard.ts`** — `adminGuard: CanActivateFn`
  Check `currentUser.roles` có `ADMIN` không. Không có → redirect `/app/dashboard`.

### Interceptors (`core/interceptors/`)

- **`auth.interceptor.ts`** — `authInterceptor: HttpInterceptorFn`
  Bắt HTTP 401 (trừ `/webgw/auth/session`) → redirect `/`. Web Gateway lo việc attach token.

### Shared Components (`core/components/`)

- **`user-avatar/UserAvatarComponent`** — hiển thị avatar user (initials fallback).

### Services (`core/services/`)

#### Auth & Session

- **`auth.service.ts` — `AuthService`**
  - `currentUser: signal<SessionUser | null | undefined>` — `undefined` = chưa load, `null` = chưa auth
  - `loadCurrentUser()` — GET `/webgw/auth/session` → GET `/api/admin/v1/users/{sub}` → cache vào signal
  - `checkAuth()` — kiểm tra session còn hợp lệ không (boolean)
  - `logout()` — POST `/webgw/auth/logout` → follow `Location` header

- **`session.service.ts` — `SessionService`** *(Portal — user tự quản)*
  - `getMyDevices()` — GET `/api/oauth2/v1/sessions/me`
  - `revokeSession(sessionId)` — DELETE `/api/oauth2/v1/sessions/me/{sessionId}`
  - `getMyLoginActivities(page, size)` — GET `/api/oauth2/v1/login-activities/me`
  - **Models**: `DeviceSession`, `LoginActivityItem`, `LoginActivityPage`

- **`admin-session.service.ts` — `AdminSessionService`** *(Dashboard — admin quản lý)*
  - `getActiveSessions()` — GET `/api/oauth2/v1/sessions/admin/active`
  - `forceTerminate(sessionId)` — DELETE `/api/oauth2/v1/sessions/admin/{sessionId}`
  - `getUserDeviceSessions(userId)` — GET `/api/oauth2/v1/admin/users/{userId}/sessions`
  - **Models**: `ActiveSessionView`, `AdminDeviceSessionView`

- **`admin-activity.service.ts` — `AdminActivityService`**
  - Login activities toàn hệ thống (admin view)

- **`iam-dashboard.service.ts` — `IamDashboardService`**
  - `getOverview()` — GET `/api/oauth2/v1/admin/iam/overview`
  - **Models**: `IamOverviewData` — `totalUsers`, `totalDevices`, `activeSessions`, `failedLoginsToday`

#### User & Role

- **`user.service.ts` — `UserService`** *(Admin — quản lý users)*
  - `getUsers(params)` — GET `/api/admin/v1/users` (paginated + filter)
  - `getUserById(id)` — GET `/api/admin/v1/users/{id}`
  - `createUser(data)` — POST `/api/admin/v1/users`
  - `lockUser(id)` / `unlockUser(id)` — POST `/{id}/lock|unlock`
  - `assignRoles(userId, roleIds)` — POST `/{userId}/roles`
  - `removeRole(userId, roleId)` — DELETE `/{userId}/roles/{roleId}`
  - **Models**: `UserSummary`, `UserDetail`, `RoleRef`, `SocialConnectionRef`, `UserPage`, `GetUsersParams`

- **`user-self.service.ts` — `UserSelfService`** *(Portal — user tự quản)*
  - `getMyProfile()` — GET `/api/admin/v1/users/me`
  - `updateProfile(data)` — PATCH `/api/admin/v1/users/me`
  - `changePassword(data)` — POST `/api/admin/v1/users/me/password`

- **`role.service.ts` — `RoleService`**
  - `getRoles(params)` — GET `/api/admin/v1/roles`
  - `getRoleById(id)` — GET `/api/admin/v1/roles/{id}`
  - `createRole(data)` — POST `/api/admin/v1/roles`
  - `updateRole(id, data)` — PATCH `/api/admin/v1/roles/{id}`
  - `deleteRole(id)` — DELETE `/api/admin/v1/roles/{id}`

#### ABAC

- **`resource.service.ts` — `ResourceService`**
  - CRUD resources + actions — `/api/admin/v1/abac/resources`
  - `addAction(resourceId, data)` / `updateAction(...)` / `removeAction(...)`

- **`policy.service.ts` — `PolicyService`**
  - CRUD PolicySets — `/api/admin/v1/abac/policy-sets`
  - CRUD Policies — `/api/admin/v1/abac/policies`
  - CRUD Rules + reorder — `/api/admin/v1/abac/policies/{id}/rules`
  - **Expression models**: `ExpressionNodeView`, `ExpressionNodeRequest`, `NamedExpressionView`
  - Rule expressions: `targetExpression / conditionExpression: ExpressionNodeView | null` (Phase 4, replaces `string | null`)

- **`ui-element.service.ts` — `UIElementService`**
  - CRUD UIElements — `/api/admin/v1/abac/ui-elements`
  - `evaluate(elementIds, objectData)` — POST `/evaluate`
  - `getUncovered()` — GET `/uncovered`

- **`simulate.service.ts` — `SimulateService`**
  - `simulate(request)` — POST `/api/admin/v1/abac/simulate`
  - `simulateNavigation(request)` — POST `/api/admin/v1/abac/simulate/navigation`
  - `getImpactPreview(request)` — POST `/api/admin/v1/abac/rules/impact-preview`
  - `getReverseLookup(resourceName, actionName, policySetId?)` — GET `/api/admin/v1/abac/simulate/reverse`
  - **Models**: `SimulateRequest/Response`, `RuleTraceEntry`, `NavigationSimulateRequest/Result`, `ImpactPreviewRequest/Result`, `ReverseLookupResult`, `RuleCoverage`

- **`expression.service.ts` — `ExpressionService`**
  - `getNamedExpressions()` — GET `/api/admin/v1/abac/expressions`
  - `deleteNamedExpression(id)` — DELETE `/api/admin/v1/abac/expressions/{id}` (409 if in use)
  - **Models**: `NamedExpressionView` (from `policy.service.ts`)

- **`audit-log.service.ts` — `AuditLogService`**
  - `getAuditLog(params)` — GET `/api/admin/v1/abac/audit-log`

---

## 🖥️ 3. Dashboard (`src/app/dashboard/`) — Admin Portal

Layout: `DashboardComponent` — sidebar + header wrapper cho tất cả `/admin/**` routes.

### `home/HomeComponent`
IAM overview metrics — dùng `IamDashboardService.getOverview()`.

### `users/`
- **`UsersComponent`** — bảng users có paginate, filter keyword/status/role, dialog tạo user mới.
  - Dialogs: `CreateUserDialogComponent`, `LockConfirmDialogComponent`
- **`user-detail/UserDetailComponent`** — tab detail: profile, roles (assign/remove), sessions (force terminate).
  - Dialogs: `AssignRolesDialogComponent`

### `roles/`
- **`RolesComponent`** — danh sách roles, CRUD.
  - Dialogs: `CreateRoleDialogComponent`, `EditRoleDialogComponent`, `ConfirmDeleteDialogComponent`

### `active-sessions/`
- **`ActiveSessionsComponent`** — bảng sessions đang active, force terminate.
  - Dialogs: `ForceTerminateDialogComponent`

### `login-activities/`
- **`LoginActivitiesComponent`** — lịch sử login toàn hệ thống (admin view).

### `abac/resources/`
- **`ResourcesComponent`** — danh sách resources.
  - Dialogs: `CreateResourceDialogComponent`
- **`resource-detail/ResourceDetailComponent`** — actions của resource.
  - Dialogs: `AddActionDialogComponent`

### `abac/policy-sets/`
- **`PolicySetsComponent`** — danh sách PolicySets.
  - Dialogs: `CreatePolicySetDialogComponent`
- **`policy-set-detail/PolicySetDetailComponent`** — policies trong PolicySet.
  - Dialogs: `CreatePolicyDialogComponent`

### `abac/policies/`
- **`policy-detail/PolicyDetailComponent`** — rules trong Policy, rule impact preview, reverse lookup.
  - Dialogs: `CreateRuleDialogComponent`, `EditRuleDialogComponent`, `RuleImpactDialogComponent`
  - Component: `ExpressionNodeEditorComponent` — recursive expression tree editor (wraps `RuleExpressionBuilderComponent`)
  - Component: `RuleExpressionBuilderComponent` — leaf node editor (Builder / Library / Raw tabs)

### `abac/expressions/`
- **`ExpressionsComponent`** — danh sách NamedExpression library, delete (409 guard)

### `abac/ui-elements/`
- **`UIElementsComponent`** — danh sách UIElements + coverage indicator.
  - Dialogs: `CreateUIElementDialogComponent`
- **`ui-element-detail/UIElementDetailComponent`** — detail + reverse lookup coverage.

### `abac/simulator/`
- **`SimulatorComponent`** — policy simulator: nhập subject/resource/action → xem trace.

### `abac/audit-log/`
- **`AuditLogComponent`** — ABAC audit log filter/search.
  - Dialogs: `SnapshotDialogComponent`

---

## 👤 4. Portal (`src/app/portal/`) — User Self-Service

Layout: `PortalComponent` — wrapper cho `/app/**` routes.

### `home/PortalHomeComponent`
Dashboard cá nhân.

### `profile/ProfileComponent`
Xem + cập nhật profile — dùng `UserSelfService`.

### `profile/change-password/ChangePasswordComponent`
Đổi password — hỗ trợ cả user social-only set password lần đầu.

### `devices/DevicesComponent`
Danh sách devices đã đăng nhập, revoke session — dùng `SessionService`.

### `login-history/LoginHistoryComponent`
Lịch sử đăng nhập của chính user — dùng `SessionService`.

---

## 🌐 5. Public Pages

- **`landing/LandingComponent`** — trang giới thiệu, 2 button: Đăng nhập / Đăng ký.
- **`register/RegisterComponent`** — [PLANNED] trang đăng ký.

---

## ⚙️ 6. App Config (`src/app/app.config.ts`)

- `provideRouter(routes)` — cấu hình routing
- `provideHttpClient(withInterceptors([authInterceptor]))` — HTTP client + auth interceptor
- `provideAnimationsAsync()` — Angular Material animations

---

## 📌 Ghi chú quan trọng

| Rule | Chi tiết |
|---|---|
| Không lưu token | localStorage, sessionStorage, cookie, memory — đều không được |
| Auth flow | Session cookie do Web Gateway quản lý — Angular không touch |
| 401 handling | `authInterceptor` bắt 401 → redirect `/` tự động |
| State management | Signals cho local state, RxJS chỉ cho HTTP + event streams |
| Component style | Standalone — không có NgModule |
| Lazy loading | Tất cả routes dùng `loadComponent()` |
