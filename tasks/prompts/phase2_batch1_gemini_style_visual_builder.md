@Prompt: Style Giao diện — Visual Policy Builder (Gemini UI/UX)

Vai trò: Bạn là Senior UI/UX-focused Frontend Engineer. Style component RuleExpressionBuilder
nhất quán với design system hiện tại của dashboard.

Tài liệu căn cứ:
  1. Quy ước thiết kế: @web/docs/layout/dashboard.md
  2. CSS tham chiếu dialog: @web/src/app/dashboard/abac/resources/resource-detail/resource-detail.css
  3. CSS tham chiếu dark textarea (SpEL): @web/src/app/dashboard/abac/policies/policy-detail/policy-detail.css
  4. CSS tham chiếu dialog form: @web/src/app/dashboard/abac/resources/create-resource-dialog/create-resource-dialog.css

FILES TO RESTYLE (Mã nguồn từ Phase 2 Batch 1 FE):

  ┌─────────────────────────────────────────────────────────────────────────────┬─────────────────────────────────────────────────────────────┐
  │ File                                                                        │ Mô tả                                                       │
  ├─────────────────────────────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────┤
  │ dashboard/abac/policies/policy-detail/rule-expression-builder/              │ Component builder — condition rows,                         │
  │   rule-expression-builder.html / .css                                       │ AND/OR toggle, SpEL preview, advanced mode                  │
  ├─────────────────────────────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────┤
  │ dashboard/abac/policies/policy-detail/create-rule-dialog/                   │ Cập nhật dialog tạo rule — giờ nhúng                        │
  │   create-rule-dialog.html / .css                                            │ 2 RuleExpressionBuilder thay cho textarea                   │
  ├─────────────────────────────────────────────────────────────────────────────┼─────────────────────────────────────────────────────────────┤
  │ dashboard/abac/policies/policy-detail/edit-rule-dialog/                     │ Tương tự create dialog                                      │
  │   edit-rule-dialog.html / .css                                              │                                                             │
  └─────────────────────────────────────────────────────────────────────────────┴─────────────────────────────────────────────────────────────┘

Yêu cầu thiết kế chi tiết:

  1. RuleExpressionBuilderComponent (.html / .css):

     a. Builder Header:
        - Tiêu đề section nhỏ (do parent dialog truyền, builder không tự render)
        - Mode toggle button ở góc phải: "Visual Builder" / "Advanced (SpEL)"
          → `mat-stroked-button`, font-size 0.75rem, height 28px, padding 0 12px
          → Active state: background var(--primary-green), color white, border-color var(--primary-green)
          → Inactive: color var(--text-secondary), border-color var(--border-color)

     b. Condition Rows (Visual Mode):
        - `.condition-list`: display flex, flex-direction column, gap 8px
        - `.condition-row`: display flex, align-items flex-start, gap 8px, padding 10px 12px,
          background #f8fafc, border 1px solid var(--border-color), border-radius 8px
        - Type select: min-width 180px, mat-select appearance outline, font-size 0.875rem
        - Param inputs: flex 1, mat-input appearance outline, font-size 0.875rem
        - Remove button: mat-icon-button, color var(--text-secondary), hover color #ef4444,
          align-self center, width 32px, height 32px
        - `.condition-row:hover`: border-color #cbd5e1

     c. Logic Operator Toggle (hiện khi có ≥ 2 conditions):
        - `.logic-toggle`: display flex, align-items center, gap 8px, margin 4px 0,
          font-size 0.75rem, color var(--text-secondary)
        - Buttons AND / OR: `mat-button` nhỏ, font-size 0.75rem, min-width 48px, height 28px
          → Active: background #dbeafe, color #1d4ed8, font-weight 600
          → Inactive: background transparent, color var(--text-secondary)

     d. "+ Add Condition" Button:
        - `mat-stroked-button`, width 100%, height 36px, margin-top 8px
        - border: 1px dashed var(--border-color), border-radius 8px
        - color: var(--text-secondary), font-size 0.875rem
        - hover: border-color var(--primary-green), color var(--primary-green), background #f0fdf4

     e. SpEL Preview (Visual Mode — hiện ở cuối builder):
        - `.spel-preview-section`: margin-top 12px, border-top 1px solid var(--border-color), padding-top 12px
        - `.spel-preview-label`: font-size 0.7rem, font-weight 600, color var(--text-secondary),
          text-transform uppercase, letter-spacing 0.05em, margin-bottom 6px
        - `.spel-preview-code`: font-family 'Courier New', monospace, font-size 0.8125rem,
          background #0f172a, color #86efac, border-radius 6px, padding 10px 14px,
          min-height 40px, word-break break-all, white-space pre-wrap
          → Khi empty (no conditions): color #475569, font-style italic, content "(no expression)"

     f. Advanced Mode (raw SpEL textarea):
        - `.advanced-mode-container`: hiện thay cho condition list + SpEL preview
        - `.advanced-warning`: font-size 0.75rem, color #92400e, background #fef3c7,
          border 1px solid #fde68a, border-radius 6px, padding 8px 12px, margin-bottom 8px
          Icon cảnh báo + text "Advanced mode — expression will not be validated visually"
        - Textarea: font-family monospace, background #0f172a, color #e2e8f0,
          border-radius 8px, padding 14px, font-size 0.875rem, min-height 100px,
          border 1px solid #334155, resize vertical, width 100%
          focus: border-color #22c55e, outline none

  2. Create/Edit Rule Dialog (.html / .css):

     - Dialog width: 640px (rộng hơn dialog thông thường vì có builder)
     - 2 builder sections xếp dọc, mỗi section có label và builder component:
       + `.builder-section`: margin-bottom 20px
       + `.builder-section-label`: font-size 0.8125rem, font-weight 600, color var(--text-secondary),
         text-transform uppercase, letter-spacing 0.05em, margin-bottom 8px
         → "TARGET EXPRESSION — WHO + WHAT" và "CONDITION EXPRESSION — CONSTRAINTS"
       + `.builder-hint`: font-size 0.75rem, color var(--text-secondary), margin-top 4px
         → Target hint: "Leave empty to match all subjects and actions"
         → Condition hint: "Leave empty for unconditional PERMIT/DENY"
     - Section divider: border-top 1px solid var(--border-color), margin 8px 0
     - Dialog max-height: 80vh; mat-dialog-content overflow-y auto

Ràng buộc kỹ thuật:
  - KHÔNG sửa file .ts
  - @import path:
    + rule-expression-builder.css: `@import '../../../../../dashboard.css'`
    + create-rule-dialog.css: `@import '../../../../../dashboard.css'`
    + edit-rule-dialog.css: `@import '../../../../../dashboard.css'`
  - CSS variables từ dashboard.css: --text-primary, --text-secondary, --border-color,
    --card-bg, --primary-green, --primary-green-hover, --status-error

OUTPUT: Nội dung hoàn chỉnh cho từng cặp .html + .css (6 files).
