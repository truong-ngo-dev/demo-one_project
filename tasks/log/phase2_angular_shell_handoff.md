# Phase 2 Angular Shell — Handoff Context

*Completed: 2026-04-13*

---

## Files tạo mới

| File | Nội dung |
|------|----------|
| `web/src/app/core/services/abac.service.ts` | AbacService — visibility map signal, loadVisibility(), isPermitted(), clearVisibility(). Export `ADMIN_ROUTE_ELEMENT_IDS` constant |
| `web/src/app/core/guards/abac.guard.ts` | `abacGuard(elementId)` — factory CanActivateFn, fail-closed |

## Files sửa đổi

### Frontend

| File | Thay đổi |
|------|----------|
| `web/src/app/dashboard/dashboard.ts` | Inject AbacService (public), gọi `loadVisibility(ADMIN_ROUTE_ELEMENT_IDS)` trong ngOnInit, `clearVisibility()` trong logout |
| `web/src/app/dashboard/dashboard.html` | Wrap 9 sidebar menu items với `@if (abacService.isPermitted('...'))`. Dashboard + Organizations placeholder không wrap |
| `web/src/app/app.routes.ts` | Thêm `abacGuard('route:...')` vào canActivate của tất cả admin child routes trừ `dashboard` |

### Backend

| File | Thay đổi |
|------|----------|
| `infrastructure/cross_cutting/config/DataInitializer.java` | + `UIElementJpaRepository`, + `seedRouteUIElements()`, + `session` resource vào `seedResources()` |

---

## Route UIElements seeded

| elementId | label | resource | action | order |
|---|---|---|---|---|
| `route:users` | Users | `user` | LIST | 1 |
| `route:roles` | Roles | `role` | LIST | 2 |
| `route:abac:resources` | Resources | `abac_resource` | LIST | 3 |
| `route:abac:policy-sets` | Policy Sets | `abac_policy_set` | LIST | 4 |
| `route:abac:ui-elements` | UI Elements | `abac_ui_element` | LIST | 5 |
| `route:abac:simulator` | Simulator | `abac_simulate` | EXECUTE | 6 |
| `route:abac:audit-log` | Audit Log | `abac_audit_log` | LIST | 7 |
| `route:active-sessions` | Sessions | `session` | LIST | 8 |
| `route:login-activities` | Login Activities | `session` | LIST | 9 |

Resource mới seeded: `session` (actions: LIST)

---

## Pattern decisions

**fail-closed**: `visibilityMap = null` trước khi `loadVisibility()` hoàn thành → `isPermitted()` = false → guard block. Deep-link lần đầu sẽ redirect về `/admin/dashboard` → visibility load → user navigate thủ công. Chấp nhận được cho Phase 2.

**loadVisibility một lần**: gọi trong `DashboardComponent.ngOnInit` — không gọi lại theo từng route navigate. Refresh thủ công là Phase 3.

**detail routes dùng chung guard với list route**: `users/:id` dùng `abacGuard('route:users')`, `abac/policy-sets/:id` dùng `abacGuard('route:abac:policy-sets')`. Không tạo elementId riêng cho detail page — cùng permission với list.

**`abac/policies/:id` guard**: dùng `route:abac:policy-sets` (policy detail là sub-page của policy-set detail).

---

## Build status

```
services/admin: mvn compile -DskipTests → BUILD SUCCESS
web: ng build --configuration development → BUILD SUCCESS (Application bundle generation complete)
```

---

## TODO còn lại

- [ ] Smoke test: login admin → sidebar hiển thị đúng, logout → clearVisibility
- [ ] Smoke test: non-ADMIN role → redirect về dashboard khi navigate đến guarded route
- [ ] Phase 3: User & Role Management UI (xem implementation plan)
