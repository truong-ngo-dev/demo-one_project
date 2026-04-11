Prompt: Phase 3 Batch 1 (FE) — Evaluation Trace Display + Reverse Lookup UI

Vai trò: Bạn là Senior Frontend Engineer thực hiện Phase 3 Batch 1 FE.
BE batch 1 đã xong: SimulateResult có `trace[]`, ActionDecision có `matchedRuleName`,
endpoint reverse lookup mới.

Tài liệu căn cứ:
  1. Quy ước: @web/CLAUDE.md
  2. Design: @docs/business_analysis/abac_admin_console_design.md (Section 5.2, 5.3)
  3. Simulator hiện tại: @web/src/app/dashboard/abac/simulator/simulator.ts (.html)
  4. SimulateService: @web/src/app/core/services/simulate.service.ts

Dữ liệu từ Backend (Phase 3 Batch 1 BE):

  SimulateResult (updated):
    POST /api/admin/v1/abac/simulate
    Response bổ sung field `trace: RuleTraceEntry[]`:
    ```
    RuleTraceEntry {
      ruleId: string;
      ruleDescription: string | null;
      effect: 'PERMIT' | 'DENY';
      targetMatched: boolean;
      conditionMatched: boolean | null;
      wasDeciding: boolean;
    }
    ```

  NavigationActionDecision (updated):
    decisions array trong NavigationSimulateResult bổ sung:
    `matchedRuleName: string | null`

  Reverse Lookup:
    GET /api/admin/v1/abac/simulate/reverse?resourceName=X&actionName=Y&policySetId=N
    Response:
    ```
    ReverseLookupResult {
      resourceName: string;
      actionName: string;
      permitRules: RuleCoverage[];
      denyRules: RuleCoverage[];
    }
    RuleCoverage {
      ruleId: number;
      ruleName: string;
      policyName: string;
      effect: 'PERMIT' | 'DENY';
      requiredRoles: string[];
      requiredAttributes: string[];
      hasInstanceCondition: boolean;
      userCountByRole: number | null;
      userCountNote: string | null;
    }
    ```

---

Nhiệm vụ cụ thể:

  1. Cập nhật `simulate.service.ts`
     - Thêm interfaces: `RuleTraceEntry`, cập nhật `SimulateResponse` thêm `trace: RuleTraceEntry[]`
     - Cập nhật `NavigationActionDecision` thêm `matchedRuleName: string | null`
     - Thêm interfaces: `RuleCoverage`, `ReverseLookupResult`
     - Thêm method:
       ```typescript
       getReverseLookup(resourceName: string, actionName: string, policySetId?: number): Observable<ReverseLookupResult>
       ```
       URL: `/api/admin/v1/abac/simulate/reverse`

  2. Cập nhật `SimulatorComponent` — thêm mode 'reverse':
     - `mode = signal<'navigation' | 'instance' | 'reverse'>('navigation')`
     - Mode toggle: thêm button thứ 3 "Reverse Lookup" (icon: `manage_search`)

  3. Instance mode — thêm Trace Panel:
     - Sau khi `result()` có giá trị và `result().trace.length > 0`:
       Hiển thị danh sách trace entries bên dưới `Evaluation Details` card
     - Mỗi `RuleTraceEntry` render dạng row:
       ```
       [icon] ruleDescription   effect badge   [target badge] [condition badge]
       ```
       - icon: `check_circle` (wasDeciding=true, PERMIT), `cancel` (wasDeciding=true, DENY),
                `remove_circle_outline` (NOT_APPLICABLE / wasDeciding=false)
       - Target badge: "Target ✓" xanh / "Target ✗" đỏ
       - Condition badge: "Cond ✓" / "Cond ✗" / "—" (null)
       - Row highlight: nếu `wasDeciding=true` → background nhạt

  4. Navigation mode — thêm `matchedRuleName` column:
     - Thêm column `matchedRule` vào `navColumns = ['action', 'decision', 'matchedRule']`
     - Cell: italic, secondary color. "—" nếu null

  5. Reverse Lookup mode — form + results:
     - Form:
       ```
       Resource: [mat-select]   Action: [mat-select, populate từ resource]
       [Run Lookup button]
       ```
       - Load resources từ `ResourceService` (đã có sẵn)
       - Khi chọn resource → load actions (như instance mode `onResourceChange`)
     - State signals: `reverseResult`, `isLookingUp`, `lookupError`
     - Results: 2 sections (PERMIT rules / DENY rules), mỗi section dạng card list:
       ```
       ┌───────────────────────────────────────┐
       │ [rule name]                [policy]   │
       │ Roles: [chip] [chip]                  │
       │ Attributes: [chip] / —                │
       │ ⚠ Instance condition                  │
       │ ~8 users have this role               │
       └───────────────────────────────────────┘
       ```
       - userCountByRole null → hiển thị userCountNote
       - hasInstanceCondition → warning icon + "Instance conditions apply"
       - Empty state nếu permitRules rỗng: "No PERMIT rules found for this resource/action"

Yêu cầu kỹ thuật:
  - Standalone Components, Signals
  - No styling — CSS để trống (style pass riêng)
  - ResourceService đã inject trong SimulatorComponent — tái dùng

Output: Liệt kê files đã tạo/sửa và mã nguồn.
