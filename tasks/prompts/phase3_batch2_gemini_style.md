Prompt: Style Giao diện — Audit Log + UIElement Coverage Warning (Gemini UI/UX)

Vai trò: Bạn là Senior UI/UX-focused Frontend Engineer. Style Phase 3 Batch 2 FE
nhất quán với design system hiện tại.

Tài liệu căn cứ:
  1. Quy ước thiết kế: @web/docs/layout/dashboard.md
  2. CSS tham chiếu page layout: @web/src/app/dashboard/abac/policy-sets/policy-sets.css
  3. CSS tham chiếu table + badges: @web/src/app/dashboard/abac/resources/resources.css
  4. CSS tham chiếu dialog: @web/src/app/dashboard/abac/policies/policy-detail/rule-impact-dialog/rule-impact-dialog.css
  5. UIElement CSS hiện tại: @web/src/app/dashboard/abac/ui-elements/ui-elements.css

FILES TO RESTYLE:

  File: audit-log/audit-log.html + audit-log.css (tạo mới)
  File: audit-log/snapshot-dialog/snapshot-dialog.html + snapshot-dialog.css (tạo mới)
  File: ui-elements/ui-elements.html (sửa thêm coverage warning) + ui-elements.css (append)

Fe logic implement output
Files created:

- web/src/app/core/services/audit-log.service.ts — AuditLogService with AuditLogEntry,
  AuditLogParams, AuditLogPage, getAuditLog()
- web/src/app/dashboard/abac/audit-log/audit-log.ts — AuditLogComponent với signals, filter by       
  entityType/performedBy, pagination
- web/src/app/dashboard/abac/audit-log/audit-log.html — bảng log với badges cho entityType +
  actionType, snapshot icon-button, paginator
- web/src/app/dashboard/abac/audit-log/audit-log.css — (empty, style pass riêng)
- web/src/app/dashboard/abac/audit-log/snapshot-dialog/snapshot-dialog.ts — dialog hiển thị formatted
  JSON
- web/src/app/dashboard/abac/audit-log/snapshot-dialog/snapshot-dialog.html
- web/src/app/dashboard/abac/audit-log/snapshot-dialog/snapshot-dialog.css

Files modified:

- web/src/app/core/services/ui-element.service.ts — thêm hasPolicyCoverage vào UIElementView, thêm   
  UncoveredUIElement interface, thêm getUncoveredUIElements()
- web/src/app/dashboard/abac/ui-elements/ui-elements.ts — thêm uncoveredCount, showUncoveredOnly     
  signals, loadUncoveredCount(), filterUncovered(), cột coverage vào displayedColumns
- web/src/app/dashboard/abac/ui-elements/ui-elements.html — warning banner + cột coverage
  (check_circle / warning icon)
- web/src/app/app.routes.ts — thêm route /admin/abac/audit-log
- web/src/app/dashboard/dashboard.html — thêm "Audit Log" vào sidebar nav


---

Yêu cầu thiết kế chi tiết:

  1. AuditLogComponent:

     a. Page layout: chuẩn dashboard page
        - `.page-container`, `.page-header` (title "Audit Log", subtitle)
        - `.filter-row`: display flex, gap 12px, align-items center, margin-bottom 20px
        - Filter select: mat-select appearance outline, width 200px
        - Total count badge: background #f1f5f9, border-radius 9999px, padding 2px 10px,
          font-size 0.8125rem, color var(--text-secondary)

     b. Table:
        - `.audit-table`: width 100%, mat-table minimal style
        - Columns widths: time (160px), entityType (130px), entityName (flex 1), action (110px),
          performedBy (flex 1), snapshot (80px)
        - Row hover: background #f8fafc
        - Header: font-weight 600, font-size 0.75rem, text-transform uppercase,
          letter-spacing 0.04em, color var(--text-secondary)

     c. EntityType badge (`.entity-badge`):
        Base: display inline-flex, align-items center, border-radius 4px, padding 2px 10px,
        font-size 0.75rem, font-weight 600, font-family monospace
        - POLICY_SET: background #eff6ff, color #1d4ed8
        - POLICY:     background #f0fdf4, color #15803d
        - RULE:       background #faf5ff, color #6b21a8
        - UI_ELEMENT: background #fff7ed, color #c2410c

     d. ActionType badge (`.action-badge`):
        Base: giống entity-badge nhưng không monospace
        - CREATED: background #f0fdf4, color #15803d
        - UPDATED: background #fef3c7, color #92400e
        - DELETED: background #fef2f2, color #dc2626

     e. Snapshot button: mat-icon-button, icon `visibility`, size small
        `.snapshot-btn`: opacity 0.6, hover opacity 1, transition 0.15s

     f. Time cell: font-size 0.8125rem, color var(--text-secondary), white-space nowrap

     g. Entity name cell: font-weight 500, color var(--text-primary), font-family monospace,
        font-size 0.875rem

     h. Performer cell: font-size 0.8125rem, color var(--text-secondary),
        overflow hidden, text-overflow ellipsis, max-width 160px

     i. Paginator: margin-top 8px, border-top 1px solid var(--border-color)

     j. Empty state: padding 48px, text-align center, color var(--text-secondary),
        icon history font-size 40px

  2. SnapshotDialogComponent:
     - Width: 600px, max-height 70vh
     - Header: "Change Snapshot", subtitle thể hiện entityName + actionType
     - Content: `<pre>` với code style:
       background #1e1e1e, color #d4d4d4 (VS Code dark theme),
       border-radius 8px, padding 16px, overflow auto,
       font-family ui-monospace/monospace, font-size 0.8125rem, line-height 1.6,
       max-height 400px
     - Actions: [Close] button

  3. UIElement Coverage Warning Banner (`.coverage-warning-banner`):
     - display flex, align-items center, gap 12px
     - background #fef3c7, border 1px solid #fde68a, border-radius 8px
     - padding 14px 16px, margin-bottom 20px
     - Icon `warning`: color #92400e, font-size 22px, flex-shrink 0
     - Text: font-size 0.875rem, color #92400e, flex 1
     - "Show only uncovered" button: mat-button, color #92400e, font-weight 600,
       text-decoration underline, background none

  4. UIElement Table — `hasPolicyCoverage` column:
     - Header: "COVERAGE"
     - Cell: icon badge
       ✓ (hasPolicyCoverage=true): icon `verified`, color #15803d, font-size 18px
       ⚠ (hasPolicyCoverage=false): icon `warning`, color #f59e0b, font-size 18px
     - Tooltip: "Policy coverage active" / "No policy — element hidden from all users"
     - Column width: 100px, text-align center

Ràng buộc:
  - audit-log.css: `@import '../../dashboard.css'` ở đầu file
  - snapshot-dialog.css: `@import '../../../dashboard.css'` ở đầu file
  - ui-elements.css: chỉ APPEND — không viết lại
  - Không sửa file .ts

OUTPUT: Nội dung hoàn chỉnh cho từng file CSS + HTML nếu có thay đổi template.
Với ui-elements.css: chỉ trả phần CSS mới cần append.
