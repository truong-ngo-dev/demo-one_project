Prompt: Phase 2 Batch 2 (FE) — Simulator Navigation Mode + Impact Analysis Dialog

Vai trò: Bạn là Senior Frontend Engineer chuyên về Angular và kiến trúc Dashboard Admin.
Phase 2 Batch 1 FE (Visual Builder) và cả 2 BE batches đã xong.
Batch 2 FE implement 2 tính năng cuối của Phase 2:
  1. Simulator — Navigation Mode (kết quả theo từng action, dạng bảng)
  2. Impact Analysis Dialog — hiện trước khi admin lưu rule

Tài liệu căn cứ:
  1. Quy ước: @web/CLAUDE.md
  2. Design: @docs/business_analysis/abac_admin_console_design.md (Section 5.1, Section 7)
  3. Simulator hiện tại: @web/src/app/dashboard/abac/simulator/simulator.ts (.html)
  4. PolicyDetail (nơi save rule): @web/src/app/dashboard/abac/policies/policy-detail/policy-detail.ts
  5. SimulateService: @web/src/app/core/services/simulate.service.ts
  6. ResourceService: @web/src/app/core/services/resource.service.ts

Dữ liệu từ Backend (Bàn giao từ Phase 2 BE):

  Navigation Simulate Endpoint:
    POST /api/admin/v1/abac/simulate/navigation
    Request:
      {
        subject: { userId?: string | null; roles: string[]; attributes?: Record<string, unknown> },
        resourceName: string,
        policySetId?: number | null
      }
    Response: ApiResponse<{
      resourceName: string;
      policySetId: number | null;
      policySetName: string | null;
      decisions: { action: string; decision: 'PERMIT' | 'DENY' }[]
    }>

  Impact Preview Endpoint:
    POST /api/admin/v1/abac/rules/impact-preview
    Request: { targetExpression?: string; conditionExpression?: string }
    Response: ApiResponse<{
      requiredRoles: string[];
      requiredAttributes: string[];
      specificActions: string[];
      navigableWithoutData: boolean;
      hasInstanceCondition: boolean;
      parseWarning: string | null;
    }>

Nhiệm vụ cụ thể:

  1. Cập nhật SimulateService — `web/src/app/core/services/simulate.service.ts`
      - Thêm interfaces:
        ```typescript
        export interface NavigationSimulateRequest {
          subject: SimulateSubjectRequest;
          resourceName: string;
          policySetId?: number | null;
        }
        export interface NavigationActionDecision {
          action: string;
          decision: 'PERMIT' | 'DENY';
        }
        export interface NavigationSimulateResult {
          resourceName: string;
          policySetId: number | null;
          policySetName: string | null;
          decisions: NavigationActionDecision[];
        }
        export interface ImpactPreviewRequest {
          targetExpression?: string;
          conditionExpression?: string;
        }
        export interface ImpactPreviewResult {
          requiredRoles: string[];
          requiredAttributes: string[];
          specificActions: string[];
          navigableWithoutData: boolean;
          hasInstanceCondition: boolean;
          parseWarning: string | null;
        }
        ```
      - Thêm methods:
        - `simulateNavigation(request: NavigationSimulateRequest): Observable<NavigationSimulateResult>`
        - `getImpactPreview(request: ImpactPreviewRequest): Observable<ImpactPreviewResult>`
        - URL: `/api/admin/v1/abac/simulate/navigation` và `/api/admin/v1/abac/rules/impact-preview`
        - Pattern: `.pipe(map(res => res.data))`

  2. Cập nhật SimulatorComponent — `web/src/app/dashboard/abac/simulator/simulator.ts (.html)`
      - Thêm mode toggle: signal `mode = signal<'navigation' | 'instance'>('navigation')`
      - Navigation Mode (mặc định):
        - Form: Subject section (giữ nguyên chip input roles + JSON attributes textarea từ Phase 1)
          + Resource select (load từ ResourceService, giống Phase 1 Instance mode)
        - Nút "Run Simulation" → gọi `simulateNavigation()`
        - Results: bảng mat-table với columns `action`, `decision` (badge PERMIT/DENY)
          + Thêm row cho metadata: policySetName nếu có
          + Empty state nếu decisions rỗng
      - Instance Mode (giữ nguyên Phase 1 — chỉ wrap trong tab/mode switch):
        - Toàn bộ logic/form/result hiện tại giữ nguyên, chỉ hidden khi mode='navigation'
      - Mode toggle: 2 mat-flat-button hoặc mat-button-toggle-group style minimal

      HTML layout gợi ý:
      ```
      <!-- Mode Toggle -->
      <div class="mode-toggle">
        <button (click)="mode.set('navigation')" [class.active]="mode()==='navigation'">Navigation</button>
        <button (click)="mode.set('instance')" [class.active]="mode()==='instance'">Instance</button>
      </div>

      <!-- Navigation Form (visible khi mode='navigation') -->
      @if (mode() === 'navigation') { ... }

      <!-- Instance Form (visible khi mode='instance') -->
      @if (mode() === 'instance') { ... }
      ```

  3. RuleImpactDialogComponent — tạo `web/src/app/dashboard/abac/policies/policy-detail/rule-impact-dialog/`
      - `rule-impact-dialog.ts`, `rule-impact-dialog.html`, `rule-impact-dialog.css`
      - Inject `MAT_DIALOG_DATA: { impact: ImpactPreviewResult }`
      - Display:
        - parseWarning: warning banner màu amber nếu != null ("Expression too complex — limited analysis")
        - requiredRoles: chip list (hoặc "Any role" nếu rỗng)
        - requiredAttributes: chip list (hoặc "-" nếu rỗng)
        - specificActions: chip list (hoặc "All actions" nếu rỗng)
        - navigableWithoutData: icon + text "Accessible without instance data" / "Instance data required"
        - hasInstanceCondition: boolean badge
      - Actions: [Cancel] [Confirm & Save] — `dialogRef.close(confirmed: boolean)`
      - Không có logic fetch ở đây — chỉ display + confirm

  4. Cập nhật PolicyDetailComponent — `web/src/app/dashboard/abac/policies/policy-detail/policy-detail.ts`
      - Import: `SimulateService`, `RuleImpactDialogComponent`, `MatDialog`
      - Sửa save flow cho cả create và edit rule:

      Khi user click Save trong CreateRuleDialog hoặc EditRuleDialog:
        a. Dialog close trả về `{ saved: true, targetExpression, conditionExpression }` thay vì chỉ `true`
        b. PolicyDetailComponent nhận result, gọi:
           ```typescript
           this.simulateService.getImpactPreview({
             targetExpression: result.targetExpression,
             conditionExpression: result.conditionExpression,
           }).subscribe({
             next: impact => {
               this.dialog.open(RuleImpactDialogComponent, {
                 width: '480px',
                 data: { impact },
               }).afterClosed().subscribe(confirmed => {
                 if (confirmed) {
                   // gọi createRule / updateRule API
                 }
               });
             },
             error: () => {
               // Impact preview failed → skip dialog, proceed directly with save
               // gọi createRule / updateRule API trực tiếp
             }
           });
           ```
        c. Nếu impact preview API fail → fallback: proceed với save không có confirmation dialog
           (tránh block user vì tính năng phụ)

      Chú ý: Việc thay đổi return type của dialog (từ `true` sang object) sẽ cần sửa CreateRuleDialog
      và EditRuleDialog để trả đúng shape.

Yêu cầu kỹ thuật:
  - Standalone Components, Signals, Angular Material
  - decision badge: PERMIT = xanh, DENY = đỏ (consistent với Phase 1 simulator)
  - mode toggle: minimal styling, active state rõ ràng
  - No Styling — CSS để trống

Output: Liệt kê các file đã tạo/sửa và mã nguồn.
