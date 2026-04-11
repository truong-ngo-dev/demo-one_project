Prompt: Style Giao diện — Trace Display + Reverse Lookup (Gemini UI/UX)

Vai trò: Bạn là Senior UI/UX-focused Frontend Engineer. Style Phase 3 Batch 1 FE
nhất quán với design system hiện tại.

Tài liệu căn cứ:
  1. Quy ước thiết kế: @web/docs/layout/dashboard.md
  2. CSS hiện tại simulator: @web/src/app/dashboard/abac/simulator/simulator.css
  3. CSS badge tham chiếu: @web/src/app/dashboard/abac/policy-sets/policy-sets.css
  4. CSS dialog tham chiếu: @web/src/app/dashboard/abac/policies/policy-detail/rule-impact-dialog/rule-impact-dialog.css

FILES TO RESTYLE (kết quả từ Phase 3 Batch 1 FE):

  File: simulator.ts / simulator.html
  Changes:
    - mode signal mở rộng: 'navigation' | 'instance' | 'reverse'
    - Instance mode: thêm Trace Panel phía dưới results
    - Navigation mode: thêm matchedRuleName column
    - Reverse Lookup mode: form (resource + action select) + results cards

Output from logic implement
Phase 3 Batch 1 FE — DONE

simulate.service.ts (updated)

- Added RuleTraceEntry interface (ruleId, ruleDescription, effect, targetMatched, conditionMatched,  
  wasDeciding)
- SimulateResponse — added trace: RuleTraceEntry[]
- NavigationActionDecision — added matchedRuleName: string | null
- Added RuleCoverage + ReverseLookupResult interfaces
- Added getReverseLookup() method → GET /api/admin/v1/abac/simulate/reverse

simulator.ts (updated)

- mode extended to 'navigation' | 'instance' | 'reverse'
- navColumns → ['action', 'decision', 'matchedRule']
- Added reverseForm, reverseActions, isLookingUp, reverseResult, lookupError signals
- Added onReverseResourceChange(), runReverseLookup(), getReverseActionName(),
  getReverseResourceName() methods

simulator.html (updated)

- Mode toggle — 3rd button "Reverse Lookup" with manage_search icon
- Navigation table — matchedRule column: italic text, "—" when null
- Instance results — Trace Panel below Evaluation Details: per-rule rows with icon, rule name,       
  Target/Cond badges, effect chip; wasDeciding highlights row
- New Reverse Lookup section: resource + action selects, permit/deny rule cards with roles,
  attributes, instance warning, user count

rule-impact-dialog.css (fixed)

- Corrected broken @import path from 5 levels up to 4 levels (../../../../dashboard.css)


---

Yêu cầu thiết kế chi tiết:

  1. Mode Toggle — thêm button "Reverse Lookup":
     - 3 buttons, layout giống Phase 2 mode toggle
     - Icon: `manage_search`, label: "Reverse Lookup"
     - Active/inactive style: nhất quán với 2 buttons hiện tại
     - `.mode-toggle`: flex wrap nếu viewport nhỏ

  2. Navigation Decision Table — matchedRule column:
     - Header: "MATCHED RULE"
     - Cell: font-style italic, color var(--text-secondary), font-size 0.8125rem
     - "—" (no match): color var(--text-secondary), opacity 0.5
     - Column width: flex 1 (để 2 cột kia không bị ảnh hưởng)

  3. Instance Trace Panel:
     a. Container `.trace-panel`:
        - margin-top 24px
        - border-top: 2px solid var(--border-color)
        - padding-top 20px

     b. Header `.trace-panel-header`:
        - font-size 0.875rem, font-weight 600, color var(--text-secondary)
        - text-transform uppercase, letter-spacing 0.05em
        - margin-bottom 12px
        - label: "Evaluation Trace"

     c. Mỗi `.trace-row`:
        - display flex, align-items center, gap 12px
        - padding 10px 12px, border-radius 6px
        - margin-bottom 6px
        - background: white, border: 1px solid var(--border-color)
        - Nếu `wasDeciding = true`: background #f0fdf4 (PERMIT) hoặc #fef2f2 (DENY),
          border-color tương ứng (#bbf7d0 / #fecaca)

     d. Trace row icon (`.trace-icon`):
        - font-size 20px, width 20px, flex-shrink 0
        - `wasDeciding + PERMIT`: color #15803d, icon check_circle
        - `wasDeciding + DENY`: color #dc2626, icon cancel
        - `!wasDeciding` (NOT_APPLICABLE): color #94a3b8, icon remove_circle_outline

     e. `.trace-rule-name`:
        - flex 1, font-size 0.875rem, color var(--text-primary)
        - overflow hidden, text-overflow ellipsis, white-space nowrap

     f. Badges nhỏ:
        `.trace-badge` base: display inline-flex, align-items center, border-radius 4px,
        padding 2px 8px, font-size 0.75rem, font-weight 600, margin-left 6px

        `.trace-badge-match`:   background #f0fdf4, color #15803d
        `.trace-badge-nomatch`: background #fef2f2, color #dc2626
        `.trace-badge-null`:    background #f1f5f9, color #94a3b8

        4 badges cần show: "Target ✓/✗", "Cond ✓/✗/—"

     g. `.trace-effect-chip`: bên phải row
        - PERMIT: background #f0fdf4, color #15803d, border 1px solid #bbf7d0
        - DENY:   background #fef2f2, color #dc2626, border 1px solid #fecaca
        - Giống decision-badge nhưng nhỏ hơn (font-size 0.75rem, padding 2px 10px)

  4. Reverse Lookup UI:

     a. `.reverse-lookup-container`: max-width 640px, margin 0 auto

     b. Form card (`.section-card`):
        - Dùng lại `.section-card` style của simulator
        - 2 mat-select trên cùng 1 row (`.form-row`)
        - Button "Run Lookup" — mat-flat-button, giống "Run Simulation"

     c. Results area: 2 sections (PERMIT / DENY)
        `.lookup-section-header`:
        - font-size 0.875rem, font-weight 700, text-transform uppercase, letter-spacing 0.05em
        - padding 8px 0, margin-bottom 12px
        - PERMIT: color #15803d
        - DENY: color #dc2626

     d. Rule coverage card (`.rule-coverage-card`):
        - background white, border 1px solid var(--border-color), border-radius 8px
        - padding 16px, margin-bottom 10px
        - Left border accent: 3px solid #15803d (PERMIT) / 3px solid #dc2626 (DENY)

     e. Card header:
        - display flex, justify-content space-between, align-items flex-start, margin-bottom 8px
        - Rule name: font-size 0.9375rem, font-weight 600, color var(--text-primary)
        - Policy name badge: background #f1f5f9, color #475569, border-radius 4px,
          padding 2px 8px, font-size 0.75rem, font-family monospace

     f. Card body:
        - `.coverage-row`: display flex, gap 8px, align-items center, margin-top 6px, flex-wrap wrap
        - Label: font-size 0.75rem, color var(--text-secondary), min-width 80px
        - Role chips: `.role-chip` (blue, như rule-impact-dialog)
        - Attribute chips: `.attribute-chip` (purple)
        - "—" nếu rỗng: font-style italic, color var(--text-secondary)

     g. User count row:
        - icon group, font-size 0.875rem, color var(--text-secondary)
        - "~N users have this role": dùng icon `group`, gap 4px
        - Note (instance/all users): font-style italic, font-size 0.8125rem

     h. Instance condition warning:
        - `.instance-warning`: display inline-flex, align-items center, gap 4px,
          background #fef3c7, color #92400e, border-radius 4px, padding 2px 8px,
          font-size 0.75rem, margin-top 4px
        - Icon: warning_amber, font-size 14px

     i. Empty state (không có rule):
        - `.lookup-empty`: text-align center, padding 32px, color var(--text-secondary)
        - icon: policy, font-size 36px, display block
        - text: "No rules found for [action] on [resource]"

Ràng buộc:
  - Chỉ APPEND vào `simulator.css` — KHÔNG viết lại toàn bộ file
  - Không sửa file .ts
  - CSS variables: dùng từ dashboard.css (--text-primary, --text-secondary, --border-color,
    --card-bg, --primary-green, --primary-green-hover)

OUTPUT: Phần CSS mới cần append vào simulator.css.
Không trả lại phần CSS đã có, chỉ trả phần mới.
