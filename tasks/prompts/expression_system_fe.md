Prompt: Expression System — Frontend Implementation (FE)

Vai trò: Bạn là Senior Frontend Engineer thực hiện Expression System FE trong `web/`.
BE đã xong: NamedExpression API, ExpressionNodeView response, ExpressionNodeRequest payload.
Nhiệm vụ này rewrite expression builder từ flat SpEL textarea sang tree-based node editor.

Tài liệu căn cứ:
  1. Design spec: @docs/context/final_design/08_expression_system_design.md
  2. Quy ước: @web/CLAUDE.md
  3. Builder hiện tại (cần rewrite):
     - @web/src/app/dashboard/abac/policies/policy-detail/rule-expression-builder/rule-expression-builder.ts
     - @web/src/app/dashboard/abac/policies/policy-detail/rule-expression-builder/rule-expression-builder.html
  4. Dialog hiện tại (cần cập nhật):
     - @web/src/app/dashboard/abac/policies/policy-detail/create-rule-dialog/create-rule-dialog.ts
     - @web/src/app/dashboard/abac/policies/policy-detail/edit-rule-dialog/edit-rule-dialog.ts
  5. Policy service hiện tại: @web/src/app/core/services/policy.service.ts
  6. Routes: @web/src/app/app.routes.ts

Context từ Backend (paste FRONTEND CONTEXT BLOCK từ BE handoff vào đây):
  [EXPRESSION SYSTEM BE FRONTEND CONTEXT BLOCK]

---

Nhiệm vụ cụ thể:

  1. Cập nhật `policy.service.ts`

      Thêm types mới, giữ nguyên types cũ (backward compat):

      ```typescript
      export type ExpressionNodeType = 'INLINE' | 'LIBRARY_REF' | 'COMPOSITION';
      export type CompositionOperator = 'AND' | 'OR';

      export interface ExpressionNodeView {
        type: ExpressionNodeType;
        name: string | null;
        resolvedSpel: string | null;
        refId: number | null;          // LIBRARY_REF: id của NamedExpression
        operator: CompositionOperator | null;  // COMPOSITION only
        children: ExpressionNodeView[] | null;
      }

      export interface ExpressionNodeRequest {
        type: ExpressionNodeType;
        name?: string;             // INLINE only
        spel?: string;             // INLINE only
        refId?: number;            // LIBRARY_REF only
        operator?: CompositionOperator;   // COMPOSITION only
        children?: ExpressionNodeRequest[];
      }

      export interface NamedExpressionView {
        id: number;
        name: string;
        spel: string;
      }
      ```

      Cập nhật `RuleView`: `targetExpression` và `conditionExpression` từ `string | null` → `ExpressionNodeView | null`.
      Cập nhật `CreateRuleRequest` và `UpdateRuleRequest`: thay `String` → `ExpressionNodeRequest`.
      Cập nhật `UpdatePolicyRequest`: thêm `targetExpression?: ExpressionNodeRequest`.

  2. Service mới: `expression.service.ts`
      File: `web/src/app/core/services/expression.service.ts`

      ```typescript
      @Injectable({ providedIn: 'root' })
      export class ExpressionService {
        private base = '/api/admin/v1/abac/expressions';

        getNamedExpressions(): Observable<NamedExpressionView[]>
        deleteNamedExpression(id: number): Observable<void>
      }
      ```

  3. SpEL reverse parser: `spelParser.ts`
      File: `web/src/app/dashboard/abac/policies/policy-detail/rule-expression-builder/spel-parser.ts`

      Logic:
      - `parseToConditionRows(spel: string): ConditionRow[] | null`
        - `splitByTopLevelOperator(spel)` — split theo `&&` / `||` bỏ qua operator trong ngoặc
        - Mỗi phần: thử `tryParseCondition(part)` → trả `ConditionRow | null`
        - Nếu tất cả phần đều parse được → trả array `ConditionRow[]`
        - Nếu bất kỳ phần nào null → trả `null` (fallback về raw mode)

      `tryParseCondition(part: string): ConditionRow | null` — invert của `conditionToSpel()`:
      - `subject.roles.contains('X')` → `{ type: 'subject_has_role', params: { role: 'X' } }`
      - `subject.getAttribute('A') == 'V'` → `{ type: 'subject_attr_equals', params: { attribute: 'A', value: 'V' } }`
      - `subject.getAttribute('A').contains('V')` → `{ type: 'subject_attr_contains', ... }`
      - `action.getAttribute('name') == 'X'` → `{ type: 'action_is', params: { action: 'X' } }`
      - `#{'X','Y'}.contains(action.getAttribute('name'))` → `{ type: 'action_is_one_of', params: { actions: 'X,Y' } }`
      - `object.data == null` → `{ type: 'navigation_only', params: {} }`
      - `object.data == null || <cond>` → `{ type: 'navigation_or_instance', params: { instanceCondition: '<cond>' } }`
      - `object.data.F == 'V'` → `{ type: 'instance_field_equals', ... }`
      - `object.data.F.contains('V')` → `{ type: 'instance_field_contains', ... }`
      - `subject.getAttribute('A').contains(object.data.F)` → `{ type: 'subject_attr_contains_field', ... }`
      - `subject.getAttribute('A') == object.data.F` → `{ type: 'subject_attr_equals_field', ... }`
      - Không match pattern nào → return `null`

      Export: `detectBuilderMode(spel: string): 'builder' | 'raw'`
      Export: `parseToConditionRows(spel: string): ConditionRow[] | null`

  4. Rewrite `RuleExpressionBuilderComponent`

      Component này vẫn là component chính, nhưng giờ đại diện cho một node expression.
      Input thay đổi:
      ```typescript
      @Input() mode: 'target' | 'condition' = 'target';
      @Input() resourceActions: { id: number; name: string }[] = [];
      @Input() initialValue: ExpressionNodeView | null = null;  // từ ExpressionNodeView
      @Output() nodeChange = new EventEmitter<ExpressionNodeRequest | null>();
      ```

      3 tabs mode trong UI: `[Builder]  [Library]  [Raw]`

      **Builder tab** (mode = 'builder'):
        - Giữ nguyên ConditionRow[] + logicOp signal (AND/OR) từ implement cũ
        - Khi `initialValue.type == 'INLINE'`: dùng `spelParser.parseToConditionRows(resolvedSpel)`
          - Nếu parse được → hiện builder mode với rows pre-populated, detect `logicOp` từ split operator
          - Nếu không parse được → tự động switch sang Raw tab
        - Thêm 2 button: "Wrap in AND block" / "Wrap in OR block"
          - Khi click: emit `ExpressionNodeRequest { type: 'COMPOSITION', operator: 'AND'|'OR', children: [currentNode] }`
          - Parent component (`ExpressionNodeEditorComponent`) nhận và wrap

      **Library tab** (mode = 'library'):
        - Load `ExpressionService.getNamedExpressions()` lazy khi tab được chọn lần đầu
        - `namedExpressions = signal<NamedExpressionView[]>([])`
        - Search input để filter theo name
        - List: mỗi item hiển thị `name` + truncated `spel`
        - Khi select → emit `{ type: 'LIBRARY_REF', refId: selected.id }`
        - Khi `initialValue.type == 'LIBRARY_REF'` → pre-select item có `id == initialValue.refId`

      **Raw tab** (mode = 'raw'):
        - Name field (`<input>`) — optional
        - Textarea SpEL
        - SpEL preview (readonly monospace)
        - Emit: `{ type: 'INLINE', name: name || undefined, spel: rawSpel }`
        - Khi `initialValue.type == 'INLINE'` và parser trả null → pre-fill textarea với `resolvedSpel`

      Detect initial tab:
      ```typescript
      ngOnChanges(): void {
        if (!initialValue) return;
        if (initialValue.type === 'LIBRARY_REF') { activeTab = 'library'; return; }
        if (initialValue.type === 'COMPOSITION') { /* handled by ExpressionNodeEditorComponent */ return; }
        // INLINE: try parse
        activeTab = detectBuilderMode(initialValue.resolvedSpel) === 'builder' ? 'builder' : 'raw';
      }
      ```

  5. Component mới: `ExpressionNodeEditorComponent` — wrapper recursive

      File: `web/src/app/dashboard/abac/policies/policy-detail/expression-node-editor/expression-node-editor.ts` (.html, .css)

      ```typescript
      @Input() mode: 'target' | 'condition' = 'target';
      @Input() resourceActions: { id: number; name: string }[] = [];
      @Input() initialValue: ExpressionNodeView | null = null;
      @Input() depth: number = 0;          // để enforce max depth
      @Output() nodeChange = new EventEmitter<ExpressionNodeRequest | null>();
      ```

      Hiển thị:
      - Nếu `initialValue.type == 'COMPOSITION'` (hoặc user đang tạo COMPOSITION):
        ```
        ┌─ [AND|OR toggle] Block ──────────────────── [×] ┐
        │  <ExpressionNodeEditorComponent *ngFor="children"> │
        │  [+ Add node]                                   │
        └─────────────────────────────────────────────────┘
        ```
        - Mỗi child là một `ExpressionNodeEditorComponent` recursive
        - [×] button xóa block (emit null)
        - [+ Add node] thêm child `ExpressionNodeRequest { type: 'INLINE', spel: '' }`

      - Nếu không phải COMPOSITION: render `RuleExpressionBuilderComponent`
        - Khi builder emit "Wrap in AND/OR block" → tạo COMPOSITION parent và di chuyển current node vào children[0]

      Giới hạn depth: `depth >= 3` → ẩn "Wrap" buttons, không cho thêm COMPOSITION nữa.

      Emit khi thay đổi: tổng hợp lại `ExpressionNodeRequest` từ toàn bộ cây hiện tại.

  6. Cập nhật `CreateRuleDialogComponent`

      - Thay 2 `RuleExpressionBuilderComponent` cũ bằng 2 `ExpressionNodeEditorComponent`:
        ```html
        <app-expression-node-editor
          [mode]="'target'"
          [resourceActions]="resourceActions()"
          (nodeChange)="targetNode.set($event)">
        </app-expression-node-editor>
        ```
      - `targetNode = signal<ExpressionNodeRequest | null>(null)`
      - `conditionNode = signal<ExpressionNodeRequest | null>(null)`
      - Submit: truyền `targetNode()` và `conditionNode()` thay vì SpEL string

  7. Cập nhật `EditRuleDialogComponent`

      - Nhận `rule: RuleView` từ `MAT_DIALOG_DATA`
      - `[initialValue]="rule.targetExpression"` và `[initialValue]="rule.conditionExpression"`
        (đây là `ExpressionNodeView`, component tự detect mode)
      - Phần còn lại tương tự CreateRuleDialog

  8. Component mới: Expression Library page

      File: `web/src/app/dashboard/abac/expressions/expressions.ts` (.html, .css)

      State:
      - `expressions = signal<NamedExpressionView[]>([])`
      - `isLoading = signal(false)`

      Columns: `name`, `spel` (truncated, expandable), `actions` (delete)

      Delete flow:
      - Gọi `ExpressionService.deleteNamedExpression(id)`
      - Success → reload list
      - Error 409 → snackbar "Expression đang được sử dụng, không thể xóa"

      No create form — expressions được tạo inline từ rule builder.

  9. Thêm route + sidebar

      `app.routes.ts`:
      ```typescript
      {
        path: 'admin/abac/expressions',
        loadComponent: () => import('.../expressions/expressions').then(m => m.ExpressionsComponent)
      }
      ```

      `dashboard.html`: thêm sidebar item "Expressions" dưới ABAC section.

---

Yêu cầu kỹ thuật:
  - Standalone Components, Signals
  - Angular Material: mat-tab-group, mat-button, mat-input, mat-select, mat-table
  - `ExpressionNodeEditorComponent` phải handle `@if (value?.type === 'COMPOSITION')` để render đúng
  - SpEL parser dùng regex thuần — không import thư viện ngoài
  - No Styling — CSS để trống (style pass riêng)
  - Không break existing APIs: `PolicyService` methods giữ nguyên signature trừ types đã update

Output: Liệt kê files đã tạo/sửa và mã nguồn.
