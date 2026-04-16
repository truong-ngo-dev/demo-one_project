Prompt: Style Giao diện — Expression System (Gemini UI/UX)

Vai trò: Bạn là Senior UI/UX-focused Frontend Engineer. Style các component mới của Expression System
nhất quán với design system hiện tại của dashboard.

Tài liệu căn cứ:
  1. Quy ước thiết kế: @web/docs/layout/dashboard.md
  2. CSS tham chiếu list page: @web/src/app/dashboard/abac/policy-sets/policy-sets.css
  3. CSS tham chiếu detail card: @web/src/app/dashboard/abac/resources/resource-detail/resource-detail.css
  4. CSS tham chiếu dialog: @web/src/app/dashboard/abac/resources/create-resource-dialog/create-resource-dialog.css
  5. CSS tham chiếu SpEL dark textarea: @web/src/app/dashboard/abac/policies/policy-detail/policy-detail.css
  6. CSS tham chiếu builder hiện tại: @web/src/app/dashboard/abac/policies/policy-detail/rule-expression-builder/rule-expression-builder.css

FILES TO RESTYLE (Mã nguồn từ Expression System FE):

  ┌───────────────────────────────────────────────────────────────────────────────┬──────────────────────────────────────────────────────────┐
  │ File                                                                          │ Mô tả                                                    │
  ├───────────────────────────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
  │ dashboard/abac/policies/policy-detail/rule-expression-builder/                │ Rewritten — giờ có 3 tabs:                               │
  │   rule-expression-builder.html / .css                                         │ Builder / Library / Raw                                  │
  ├───────────────────────────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
  │ dashboard/abac/policies/policy-detail/expression-node-editor/                 │ Mới — recursive wrapper, hiển thị                        │
  │   expression-node-editor.html / .css                                          │ COMPOSITION block và LEAF node                           │
  ├───────────────────────────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
  │ dashboard/abac/policies/policy-detail/create-rule-dialog/                     │ Updated — embed ExpressionNodeEditor                     │
  │   create-rule-dialog.html / .css                                              │ thay cho builder cũ                                      │
  ├───────────────────────────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
  │ dashboard/abac/policies/policy-detail/edit-rule-dialog/                       │ Tương tự create dialog                                   │
  │   edit-rule-dialog.html / .css                                                │                                                          │
  ├───────────────────────────────────────────────────────────────────────────────┼──────────────────────────────────────────────────────────┤
  │ dashboard/abac/expressions/                                                   │ Mới — Expression Library list page                       │
  │   expressions.html / .css                                                     │                                                          │
  └───────────────────────────────────────────────────────────────────────────────┴──────────────────────────────────────────────────────────┘

---

Yêu cầu thiết kế chi tiết:

  1. RuleExpressionBuilderComponent — 3-tab mode (.html / .css)

     a. Tab switcher (thay cho toggle cũ Visual/Advanced):
        - `.builder-mode-header`: display flex, justify-content flex-end, margin-bottom 12px
        - `.mode-pill-group`: display flex, background #f1f5f9, border-radius 8px, padding 3px, gap 2px
        - `.mode-pill-btn`: font-size 0.75rem, height 28px, padding 0 14px, border-radius 6px,
          border none, background transparent, color var(--text-secondary), cursor pointer,
          font-weight 500, transition all 0.15s
          → Active state: background white, color var(--text-primary), font-weight 600,
            box-shadow 0 1px 3px rgba(0,0,0,0.1)

     b. Library tab — named expression list:
        - `.library-search`: mat-form-field appearance outline, width 100%, margin-bottom 8px
        - `.named-expr-list`: display flex, flex-direction column, gap 4px, max-height 220px,
          overflow-y auto
        - `.named-expr-item`: display flex, align-items center, gap 10px, padding 10px 12px,
          border 1px solid var(--border-color), border-radius 8px, cursor pointer,
          background white, transition all 0.15s
          → Hover: border-color #94a3b8, background #f8fafc
          → Selected: border-color var(--primary-green), background #f0fdf4
        - `.named-expr-name`: font-weight 600, font-size 0.875rem, color var(--text-primary)
        - `.named-expr-spel`: font-family monospace, font-size 0.75rem, color var(--text-secondary),
          white-space nowrap, overflow hidden, text-overflow ellipsis, max-width 280px
        - `.library-badge`: display inline-flex, align-items center, gap 3px, font-size 0.7rem,
          background #eff6ff, color #1d4ed8, border-radius 4px, padding 1px 6px, font-weight 500
          → Text: "LIBRARY", icon: push_pin (12px)

     c. Builder tab: giữ nguyên style hiện tại (condition rows, AND/OR toggle, SpEL preview)

     d. Raw tab: giữ nguyên style advanced mode hiện tại
        Thêm `.raw-name-field`: mat-form-field appearance outline, width 100%, margin-bottom 12px
        Placeholder: "Name (optional) — displayed instead of SpEL in the tree"

  2. ExpressionNodeEditorComponent — recursive tree (.html / .css)

     a. LEAF node (wrapper quanh RuleExpressionBuilderComponent):
        - `.node-container`: border 1px solid var(--border-color), border-radius 8px,
          background white, overflow hidden
        - `.node-header`: display flex, align-items center, gap 8px, padding 8px 12px,
          background #f8fafc, border-bottom 1px solid var(--border-color)
        - `.node-name-label`: font-size 0.875rem, font-weight 600, color var(--text-primary)
          → Hiện khi node có name. Nếu không có name: hiện truncated resolvedSpel (monospace, secondary)
        - `.node-library-badge`: cùng style như `.library-badge` ở trên (LIBRARY_REF)
        - `.node-actions`: margin-left auto, display flex, gap 4px
          → Edit button: mat-icon-button, icon edit, 28px
          → Remove button: mat-icon-button, icon close, 28px, hover color #ef4444

     b. COMPOSITION block:
        - `.composition-block`: position relative, border-left 3px solid #e2e8f0,
          padding-left 12px, margin 4px 0
          → depth=1: border-color #94a3b8
          → depth=2: border-color #475569 (dùng CSS var hoặc :host-context nếu cần)
        - `.composition-header`: display flex, align-items center, gap 8px, margin-bottom 8px
        - `.composition-operator-toggle`: display flex, background #f1f5f9, border-radius 6px,
          padding 2px, gap 2px
          → Button AND/OR: font-size 0.7rem, height 24px, padding 0 10px, border-radius 4px,
            border none, cursor pointer, font-weight 600, transition all 0.15s
            Active AND: background #dbeafe, color #1d4ed8
            Active OR: background #fef3c7, color #92400e
            Inactive: background transparent, color var(--text-secondary)
        - `.composition-label`: font-size 0.7rem, font-weight 600, color var(--text-secondary),
          text-transform uppercase, letter-spacing 0.05em
          → Text: "Block"
        - `.composition-remove-btn`: mat-icon-button, margin-left auto, icon delete_outline,
          28px, hover color #ef4444
        - `.composition-children`: display flex, flex-direction column, gap 8px
        - `.add-child-btn`: `mat-stroked-button`, width 100%, height 32px, margin-top 8px,
          border 1px dashed var(--border-color), border-radius 6px,
          color var(--text-secondary), font-size 0.8rem
          hover: border-color var(--primary-green), color var(--primary-green), background #f0fdf4

     c. Wrap buttons (hiện ở cuối LEAF node, ẩn khi depth >= 3):
        - `.wrap-actions`: display flex, gap 6px, padding 8px 12px, border-top 1px solid var(--border-color),
          background #f8fafc
        - `.wrap-btn`: `mat-stroked-button`, font-size 0.75rem, height 28px, padding 0 10px
          → "Wrap AND": border-color #bfdbfe, color #1d4ed8, background #eff6ff hover
          → "Wrap OR": border-color #fde68a, color #92400e, background #fefce8 hover

  3. Create/Edit Rule Dialog (.html / .css)

     - Dialog width: 680px (rộng hơn để chứa node editor)
     - mat-dialog-content: max-height 72vh, overflow-y auto, padding 0 24px
     - `.expression-section`: margin-bottom 24px
     - `.expression-section-label`: font-size 0.75rem, font-weight 700, color var(--text-secondary),
       text-transform uppercase, letter-spacing 0.06em, margin-bottom 6px, display flex,
       align-items center, gap 6px
       → Icon nhỏ 14px trước label: `filter_alt` (Target) / `rule` (Condition)
     - `.expression-section-hint`: font-size 0.75rem, color var(--text-secondary), margin-bottom 10px,
       font-style italic
       → Target: "Leave empty to match all requests"
       → Condition: "Leave empty for unconditional effect"
     - `.expression-divider`: border-top 1px solid var(--border-color), margin 16px 0

  4. Expression Library page (.html / .css)

     - Follow chính xác pattern policy-sets.css (page-container, page-header, table-container)
     - `.expression-name-cell`: font-weight 600, font-size 0.875rem, color var(--text-primary)
     - `.expression-spel-cell`: font-family 'Courier New', monospace, font-size 0.8rem,
       color var(--text-secondary), max-width 400px, overflow hidden, text-overflow ellipsis,
       white-space nowrap
     - `.spel-expand-btn`: mat-icon-button, icon expand_more / expand_less, 24px
       → Click toggle hiển thị full SpEL trong row (không modal)
     - `.spel-full`: font-family monospace, font-size 0.8rem, background #0f172a, color #86efac,
       border-radius 6px, padding 10px 14px, margin-top 6px, white-space pre-wrap, word-break break-all
     - Empty state: icon `auto_awesome`, title "No named expressions", subtitle "Named expressions
       are created from the rule builder when you save an inline expression with a name."
     - No create button — expressions được tạo từ rule builder

Ràng buộc kỹ thuật:
  - KHÔNG sửa file .ts
  - @import paths:
    + rule-expression-builder.css: `@import '../../../../../dashboard.css'`
    + expression-node-editor.css: `@import '../../../../../dashboard.css'`
    + create-rule-dialog.css: `@import '../../../../../dashboard.css'`
    + edit-rule-dialog.css: `@import '../../../../../dashboard.css'`
    + expressions.css: `@import '../../../dashboard.css'`
  - CSS variables: --text-primary, --text-secondary, --border-color, --card-bg,
    --primary-green, --primary-green-hover, --status-error
  - COMPOSITION depth indicator dùng class `.depth-1`, `.depth-2` trên `.composition-block`
    thay vì CSS nesting — tương thích Angular ViewEncapsulation

OUTPUT: Nội dung hoàn chỉnh cho từng cặp .html + .css (10 files).
