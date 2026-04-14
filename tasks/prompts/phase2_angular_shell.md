# Prompt: Phase 2 — Angular Shell ABAC Integration

**Vai trò**: Bạn là Senior Frontend Engineer tích hợp ABAC visibility vào Admin Shell Angular đã có sẵn. Backend đã enforce (Phase 1 done). Phase 2 thêm lớp frontend: load visibility map sau login, guard route bằng UIElement evaluate result, ẩn menu item theo policy.

## Tài liệu căn cứ

1. Handoff Phase 1: @tasks/log/phase1_abac_handoff.md
2. UC-028 Route Guard: @services/admin/docs/use-cases/UC-028_frontend_route_guard.md
3. Implementation plan: @docs/context/final_design/07_admin_portal_implementation_plan.md
4. @web/CLAUDE.md, @web/SERVICE_MAP.md

## Verify trước khi implement

Đọc và kiểm tra 3 file:

1. `web/src/app/core/guards/auth.guard.ts` + `admin.guard.ts` — đang hoạt động đúng chưa
2. `web/src/app/core/services/ui-element.service.ts` — `evaluateUIElements()` đã có chưa
3. `web/src/app/app.routes.ts` — routes admin có `canActivate: [authGuard, adminGuard]` chưa

## Implement theo thứ tự

### 2.1 — AbacService (mới)

Tạo `web/src/app/core/services/abac.service.ts`:
- `visibilityMap = signal<Record<string, 'PERMIT' | 'DENY'> | null>(null)` — `null` = chưa load (fail-closed)
- `loadVisibility(elementIds: string[]): Observable<void>` — gọi `UIElementService.evaluateUIElements()`, set signal
- `isPermitted(elementId: string): boolean` — tra cứu map, null → false
- `clearVisibility(): void` — reset signal về null (gọi khi logout)

### 2.2 — DashboardComponent

Sửa `web/src/app/dashboard/dashboard.ts`:
- Inject `AbacService`
- Trong `ngOnInit`: gọi `abacService.loadVisibility(ADMIN_ELEMENT_IDS)` — danh sách hard-code các elementId route (xem bảng seed bên dưới)
- Export `abacService` ra template để dùng trong `@if`
- Gọi `abacService.clearVisibility()` trong `logout()`

### 2.3 — Sidebar visibility

Sửa `web/src/app/dashboard/dashboard.html`:  
Wrap từng `mat-list-item` menu item bằng `@if (abacService.isPermitted('...'))` theo bảng UIElement bên dưới.
- `dashboard` và các placeholder (Organizations) **không wrap** — luôn hiển thị.

### 2.4 — abacGuard (mới)

Tạo `web/src/app/core/guards/abac.guard.ts`:

```typescript
export const abacGuard = (elementId: string): CanActivateFn => () => {
  const abacService = inject(AbacService);
  const router = inject(Router);
  return abacService.isPermitted(elementId)
    ? true
    : router.createUrlTree(['/admin/dashboard']);
};
```

### 2.5 — Wire guard vào routes

Sửa `web/src/app/app.routes.ts` — thêm `abacGuard('route:...')` vào `canActivate` của từng route admin (trừ `dashboard`):

```text
{
  path: 'users',
  canActivate: [authGuard, adminGuard, abacGuard('route:users')],
  loadComponent: ...
}
```

### 2.6 — Backend: seed route UIElements

Sửa `services/admin/src/main/.../config/DataInitializer.java` — thêm `seedRouteUIElements()` gọi sau `seedResources()`.

Seed bảng UIElement sau (type=MENU_ITEM, scope=ADMIN):

| elementId                | label            | resource          | action    |
|--------------------------|------------------|-------------------|-----------|
| `route:users`            | Users            | `user`            | `LIST`    |
| `route:roles`            | Roles            | `role`            | `LIST`    |
| `route:abac:resources`   | Resources        | `abac_resource`   | `LIST`    |
| `route:abac:policy-sets` | Policy Sets      | `abac_policy_set` | `LIST`    |
| `route:abac:ui-elements` | UI Elements      | `abac_ui_element` | `LIST`    |
| `route:abac:simulator`   | Simulator        | `abac_simulate`   | `EXECUTE` |
| `route:abac:audit-log`   | Audit Log        | `abac_audit_log`  | `LIST`    |
| `route:active-sessions`  | Sessions         | `session`         | `LIST`    |
| `route:login-activities` | Login Activities | `session`         | `LIST`    |

Lưu ý: `session` resource chưa có — cần seed thêm vào `seedResources()` với action `LIST`.

Seed dùng `UIElementJpaEntity` — xem mapping tại `infrastructure/persistence/abac/uielement/UIElementJpaEntity.java`. Check `existsByElementId` trước khi insert.

### 2.7 — mvn compile + ng build verify

Sau khi xong:
1. `cd services/admin && mvn compile -DskipTests` — không có error
2. `cd web && ng build --configuration development` — không có error

## Quy tắc

- Dùng **Signals** cho visibility state — không dùng BehaviorSubject
- Guard là **defense-in-depth** — không thay thế backend enforcement
- `visibilityMap = null` trước khi load → tất cả `isPermitted()` trả `false` (fail-closed)
- `evaluateUIElements` gọi **một lần** khi DashboardComponent khởi động — không gọi lại theo route

**Không implement**: Phase 3 (User/Role UI), auto-refresh visibility, 403 page riêng.

## Yêu cầu Handoff (bắt buộc)

### PHASE 3 CONTEXT BLOCK
- Files đã tạo/sửa
- elementIds đã seed + mapping resource/action
- Pattern decisions nếu khác UC-028
- TODO còn lại
