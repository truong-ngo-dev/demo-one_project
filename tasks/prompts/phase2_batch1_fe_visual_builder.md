Prompt: Phase 2 Batch 1 (FE) — Visual Policy Builder

Vai trò: Bạn là Senior Frontend Engineer chuyên về Angular và kiến trúc Dashboard Admin.
Phase 1 FE đã hoàn thành (Resource, PolicySet, Policy, Rule, UIElement, Simulator raw mode).
Phase 2 Batch 1 FE implement Visual Policy Builder — thay thế raw SpEL textarea trong rule editor
bằng form builder có cấu trúc, tự động sinh SpEL.

Tài liệu căn cứ:
  1. Quy ước: @web/CLAUDE.md
  2. Design: @docs/business_analysis/abac_admin_console_design.md (Section 3.3 — Rule Editor Visual Builder)
  3. Rule editor hiện tại (cần sửa):
     - @web/src/app/dashboard/abac/policies/policy-detail/create-rule-dialog/create-rule-dialog.ts
     - @web/src/app/dashboard/abac/policies/policy-detail/edit-rule-dialog/edit-rule-dialog.ts
  4. Pattern tham chiếu: @web/src/app/dashboard/abac/policies/policy-detail/policy-detail.ts
  5. ResourceService: @web/src/app/core/services/resource.service.ts (có getResourceById — trả actions[])

Không có endpoint mới từ BE — Visual Builder là FE-only: form → SpEL string → gọi API hiện có.

SpEL Generation Rules (bắt buộc đọc kỹ trước khi implement):

  Target Expression (WHO làm WHAT):
  ┌────────────────────────────────────┬────────────────────────────────────────────────────────┐
  │ Visual Form                        │ SpEL sinh ra                                           │
  ├────────────────────────────────────┼────────────────────────────────────────────────────────┤
  │ Subject: has role [X]              │ subject.roles.contains('X')                            │
  │ Subject: attribute [A] contains [V]│ subject.getAttribute('A').contains('V')                │
  │ Subject: attribute [A] equals [V]  │ subject.getAttribute('A') == 'V'                       │
  │ Action: is [X]                     │ action.getAttribute('name') == 'X'                     │
  │ Action: is one of [X, Y, Z]        │ #{'X','Y','Z'}.contains(action.getAttribute('name'))   │
  │ Multiple conditions (AND)          │ cond1 && cond2                                         │
  │ Multiple conditions (OR)           │ cond1 || cond2                                         │
  └────────────────────────────────────┴────────────────────────────────────────────────────────┘

  Condition Expression (BỔ SUNG khi PERMIT/DENY):
  ┌──────────────────────────────────────────┬────────────────────────────────────────────────────┐
  │ Visual Form                              │ SpEL sinh ra                                       │
  ├──────────────────────────────────────────┼────────────────────────────────────────────────────┤
  │ Navigation (không có instance data)      │ object.data == null                                │
  │ Navigation OR instance condition         │ object.data == null || [cond]                      │
  │ Instance field [fieldName] == [value]    │ object.data.fieldName == 'value'                   │
  │ Instance field [fieldName] contains [V]  │ object.data.fieldName.contains('value')            │
  │ Subject attr [A] contains field [F]      │ subject.getAttribute('A').contains(object.data.F)  │
  │ Subject attr [A] == field [F]            │ subject.getAttribute('A') == object.data.F         │
  └──────────────────────────────────────────┴────────────────────────────────────────────────────┘

  Khi form có nhiều conditions → join bằng operator đã chọn (AND = ` && `, OR = ` || `).
  Empty form → null (không có expression).

Nhiệm vụ cụ thể:

  1. RuleExpressionBuilderComponent — tạo web/src/app/dashboard/abac/policies/policy-detail/rule-expression-builder/rule-expression-builder.ts (.html, .css)
      - Input: `@Input() mode: 'target' | 'condition'` — quyết định loại conditions hiển thị
      - Input: `@Input() resourceActions: { id: number; name: string }[]` — actions của resource (cho Action dropdown)
      - Output: `@Output() expressionChange = new EventEmitter<string | null>()`
      - Internal state:
        - `conditions = signal<ConditionRow[]>([])` — danh sách condition rows
        - `logicOp = signal<'AND' | 'OR'>('AND')` — operator giữa các conditions
        - `advancedMode = signal(false)` — raw SpEL mode
        - `rawSpel = signal('')` — dùng khi advancedMode = true
      - ConditionRow model:
        ```typescript
        interface ConditionRow {
          id: string;        // uuid hoặc index để track
          type: ConditionType;
          params: Record<string, string>;
        }

        // target conditions
        type ConditionType =
          | 'subject_has_role'
          | 'subject_attr_contains'
          | 'subject_attr_equals'
          | 'action_is'
          | 'action_is_one_of'
          // condition-only (chỉ hiển thị khi mode='condition')
          | 'navigation_only'
          | 'navigation_or_instance'
          | 'instance_field_equals'
          | 'instance_field_contains'
          | 'subject_attr_contains_field'
          | 'subject_attr_equals_field';
        ```
      - Khi conditions thay đổi → tự động sinh SpEL → emit `expressionChange`
      - Nút "+ Add Condition" → thêm row mới với type = 'subject_has_role' mặc định
      - Mỗi row: select type, inputs theo type, nút remove
      - Logic/OR toggle: chỉ hiện khi có >= 2 conditions
      - Advanced Mode toggle button: "Advanced (SpEL)" ↔ "Visual Builder"
        - Khi switch sang Advanced: pre-fill textarea với SpEL đang sinh ra
        - Khi switch về Visual: warning "Visual form will be reset" nếu raw SpEL có nội dung
        - Trong Advanced mode: emit raw textarea value trực tiếp
      - Preview SpEL: show generated SpEL (read-only, monospace) khi mode = Visual

  2. Sửa CreateRuleDialogComponent
      - File: `web/src/app/dashboard/abac/policies/policy-detail/create-rule-dialog/create-rule-dialog.ts`
      - Thay 2 raw SpEL textareas (targetExpression, conditionExpression) bằng 2 instance
        của `RuleExpressionBuilderComponent` (mode='target' và mode='condition')
      - Load `resourceActions` từ `ResourceService.getResourceById(policyDetail.resource_id)` khi dialog mở
        — nếu policy không có resource liên kết, truyền `[]` (builder vẫn hoạt động nhưng action dropdown rỗng)
      - Lấy `policyDetail` từ `MAT_DIALOG_DATA.policyId` → gọi `PolicyService.getPolicyById(policyId)`
        để biết resource context (nếu có)
      - Vẫn giữ Advanced Mode toggle trong builder → admin vẫn có thể nhập raw SpEL

  3. Sửa EditRuleDialogComponent
      - File: `web/src/app/dashboard/abac/policies/policy-detail/edit-rule-dialog/edit-rule-dialog.ts`
      - Tương tự CreateRuleDialog — thay textareas bằng RuleExpressionBuilderComponent
      - Khi dialog mở với rule đang có expression:
        - Pre-fill builder ở Advanced Mode với expression hiện tại
        - (Visual parsing từ SpEL về form là Phase 3 — Phase 2 chỉ cần Advanced Mode pre-fill)

  4. Import RuleExpressionBuilderComponent vào cả 2 dialogs và app.routes.ts (không cần route riêng — component được dùng nội bộ).

Yêu cầu kỹ thuật:
  - Standalone Component, Signals
  - Angular Material: mat-select, mat-input, mat-button, mat-icon, mat-chip (action_is_one_of)
  - SpEL preview: `<pre class="spel-preview">` với monospace font
  - No Styling — CSS để trống
  - Không cần unit test; focus vào SpEL generation logic chính xác

Output: Liệt kê các file đã tạo/sửa và mã nguồn.
