Prompt: Phase 3 Batch 2 (FE) — Audit Log Page + UIElement Coverage Warning

Vai trò: Bạn là Senior Frontend Engineer thực hiện Phase 3 Batch 2 FE.
BE batch 2 đã xong: Audit Log endpoint, UIElement `/uncovered`, `hasPolicyCoverage` trên UIElementView.

Tài liệu căn cứ:
  1. Quy ước: @web/CLAUDE.md
  2. Design: @docs/business_analysis/abac_admin_console_design.md (Section 6)
  3. UIElement list hiện tại: @web/src/app/dashboard/abac/ui-elements/ui-elements.ts (.html)
  4. Policy Sets (tham khảo table pattern): @web/src/app/dashboard/abac/policy-sets/policy-sets.ts
  5. App routes: @web/src/app/app.routes.ts

Dữ liệu từ Backend (Phase 3 Batch 2 BE):

  Audit Log:
    GET /api/admin/v1/abac/audit-log?entityType=...&entityId=...&performedBy=...&page=0&size=20
    Response: { data: AuditLogEntry[], meta: { page, size, total } }
    AuditLogEntry {
      id: number;
      entityType: 'POLICY_SET' | 'POLICY' | 'RULE' | 'UI_ELEMENT';
      entityId: number;
      entityName: string | null;
      actionType: 'CREATED' | 'UPDATED' | 'DELETED';
      performedBy: string | null;
      changedAt: number;          // epoch millis
      snapshotJson: string | null;
    }

  UIElement (updated):
    UIElementView: thêm `hasPolicyCoverage: boolean`
    GET /api/admin/v1/abac/ui-elements/uncovered → ApiResponse<UIElementView[]>

---

Nhiệm vụ cụ thể:

  1. AuditLogService — `web/src/app/core/services/audit-log.service.ts` (tạo mới):
     ```typescript
     export interface AuditLogEntry { ... }  // per CONTEXT BLOCK trên
     export interface AuditLogParams {
       entityType?: string;
       entityId?: number;
       performedBy?: string;
       page?: number;
       size?: number;
     }
     @Injectable({ providedIn: 'root' })
     export class AuditLogService {
       getAuditLog(params: AuditLogParams): Observable<{ data: AuditLogEntry[]; meta: {...} }>
     }
     ```
     URL: `/api/admin/v1/abac/audit-log`

  2. AuditLogComponent — tạo `web/src/app/dashboard/abac/audit-log/`:
     - Files: `audit-log.ts`, `audit-log.html`, `audit-log.css`
     - Standalone component, `OnInit`
     - State signals: `entries`, `isLoading`, `meta`, `filter`
     - Filter form: entityType select (All / POLICY_SET / POLICY / RULE / UI_ELEMENT)
     - Table columns: `time`, `entityType`, `entityName`, `action`, `performedBy`, `snapshot`
     - Pagination: `MatPaginatorModule` — page 0, size 20
     - `snapshot` column: mat-icon-button [icon: `visibility`] →
       `MatDialog.open(SnapshotDialogComponent, { data: entry.snapshotJson })`
     - `changedAt` column: dùng `DatePipe` format `'medium'`
     - entityType badge: màu theo loại
     - actionType badge: CREATED=xanh, UPDATED=amber, DELETED=đỏ

  3. SnapshotDialogComponent — tạo `web/src/app/dashboard/abac/audit-log/snapshot-dialog/`:
     - Files: `snapshot-dialog.ts`, `snapshot-dialog.html`, `snapshot-dialog.css`
     - Inject `MAT_DIALOG_DATA: { snapshotJson: string | null }`
     - Display: `<pre>` formatted JSON (`JSON.stringify(JSON.parse(data), null, 2)`)
     - Actions: [Close]

  4. Cập nhật UIElement list component:
     a. Thêm `hasPolicyCoverage` column vào table (nếu chưa có): icon badge ✓ / ⚠
     b. Thêm warning banner đầu trang nếu có uncovered elements:
        - Gọi `GET /uncovered` khi component init
        - Nếu `uncoveredCount > 0`:
          ```html
          <div class="coverage-warning-banner">
            <mat-icon>warning</mat-icon>
            <span>{{ uncoveredCount }} UI element(s) have no policy coverage — 
                  they will be hidden from all users.</span>
            <button mat-button (click)="filterUncovered()">Show only uncovered</button>
          </div>
          ```
        - `filterUncovered()`: set filter để chỉ hiện elements có `hasPolicyCoverage = false`

  5. Thêm route Audit Log vào `app.routes.ts`:
     - Path: `/admin/abac/audit-log`
     - `loadComponent` → `AuditLogComponent`
     - Thêm link vào ABAC navigation sidebar (nếu có component quản lý nav)

Yêu cầu kỹ thuật:
  - Standalone Components, Signals, Angular Material
  - No styling — CSS để trống (style pass riêng)
  - DatePipe import trong standalone component
  - MatPaginator: dùng `(page)` event → reload với page/size mới

Output: Liệt kê files đã tạo/sửa và mã nguồn.
