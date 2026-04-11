# UC-028: Frontend ABAC Route Guard

## Tóm tắt
Angular route guard kiểm tra evaluate result trước khi activate route — ngăn URL-typing bypass. Guard load visibility map sau login và cache trong session. Nếu route tương ứng với một UIElement bị DENY → redirect 403 page hoặc `/admin/dashboard`.

## Actor
Angular Router (transparent với user — xảy ra tự động khi navigate)

## Trạng thái
Planned

---

## Bối cảnh

Phase 1 gap: Angular chỉ có `authGuard` (có JWT?) + `adminGuard` (có role ADMIN?). Người dùng biết URL `/admin/abac/policy-sets` có thể gõ thẳng và truy cập dù policy DENY.

Phase 2 thêm một lớp guard: kiểm tra evaluate result từ backend.

---

## UC-028-1: AbacService — load + cache visibility map

```typescript
// web/src/app/core/services/abac.service.ts
@Injectable({ providedIn: 'root' })
export class AbacService {
  private visibilityMap = signal<Record<string, 'PERMIT' | 'DENY'> | null>(null);

  loadVisibility(elementIds: string[]): Observable<void> {
    return this.uiElementService.evaluateUIElements({ elementIds }).pipe(
      tap(res => this.visibilityMap.set(res.results)),
      map(() => void 0)
    );
  }

  isPermitted(elementId: string): boolean {
    const map = this.visibilityMap();
    if (map === null) return false;  // chưa load → deny
    return map[elementId] === 'PERMIT';
  }

  clearVisibility(): void {
    this.visibilityMap.set(null);
  }
}
```

**Load timing**: sau khi login thành công → `loadVisibility(ALL_ELEMENT_IDS)` một lần.

---

## UC-028-2: AbacGuard

```typescript
// web/src/app/core/guards/abac.guard.ts
export const abacGuard = (elementId: string) => (): boolean | UrlTree => {
  const abacService = inject(AbacService);
  const router = inject(Router);

  if (abacService.isPermitted(elementId)) return true;
  return router.createUrlTree(['/admin/dashboard']);
};
```

**Sử dụng trong routes**:

```typescript
{
  path: 'abac/policy-sets',
  loadComponent: () => import('./dashboard/abac/policy-sets/policy-sets')
    .then(m => m.PolicySetsComponent),
  canActivate: [authGuard, adminGuard, abacGuard('route:abac:policy-sets')],
},
```

---

## UC-028-3: Route UIElements

Cần thêm vào UIElement Registry các entry cho routes (type: MENU_ITEM):

| elementId | label | type | resource | action |
|---|---|---|---|---|
| `route:abac:policy-sets` | Policy Sets Page | MENU_ITEM | `abac_policy_set` | `VIEW` |
| `route:abac:ui-elements` | UI Elements Page | MENU_ITEM | `abac_ui_element` | `VIEW` |
| `route:abac:simulator` | Simulator Page | MENU_ITEM | `abac_simulate` | `EXECUTE` |
| `route:users` | Users Page | MENU_ITEM | `user` | `LIST` |

---

## UC-028-4: Sidebar visibility binding

```html
@if (abacService.isPermitted('route:abac:policy-sets')) {
  <mat-list-item routerLink="abac/policy-sets">
    <mat-icon matListItemIcon>account_tree</mat-icon>
    <span matListItemTitle>Policy Sets</span>
  </mat-list-item>
}
```

Kết hợp guard + sidebar binding → consistent visibility giữa menu và URL access.

---

## UC-028-5: Refresh visibility sau policy change

Khi admin thay đổi policy → visibility map có thể lỗi thời. Cần refresh:

- **Trigger thủ công**: nút "Refresh permissions" trong header.
- **Auto-refresh**: mỗi X phút (Phase 3).

```typescript
// Sau khi save policy → emit event → AbacService.loadVisibility()
```

---

## Ghi chú thiết kế

- Guard chỉ là **defense-in-depth** cho frontend — không thay thế backend enforcement (UC-025).
- `visibilityMap = null` trước khi load → mọi guard đều trả `false` (fail-closed).
- Không block non-route UIElements trong guard — chỉ block routes. Button visibility vẫn dùng `*ngIf`.
- `ALL_ELEMENT_IDS` = danh sách tất cả elementId đã đăng ký trong hệ thống — load từ `/api/v1/abac/ui-elements?size=1000` hoặc hard-code constants.
