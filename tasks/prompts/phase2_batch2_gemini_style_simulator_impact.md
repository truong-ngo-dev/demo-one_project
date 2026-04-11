Prompt: Style Giao diện — Simulator Navigation Mode + Impact Analysis Dialog (Gemini UI/UX)

Vai trò: Bạn là Senior UI/UX-focused Frontend Engineer. Style các thay đổi của Phase 2 Batch 2 FE
nhất quán với design system hiện tại.

Tài liệu căn cứ:
  1. Quy ước thiết kế: @web/docs/layout/dashboard.md
  2. CSS tham chiếu simulator hiện tại: @web/src/app/dashboard/abac/simulator/simulator.css
  3. CSS tham chiếu dialog: @web/src/app/dashboard/abac/resources/create-resource-dialog/create-resource-dialog.css
  4. CSS tham chiếu table + badges: @web/src/app/dashboard/abac/policy-sets/policy-sets.css

FILES TO RESTYLE (Mã nguồn từ Phase 2 Batch 2 FE):

Files created/updated:

File: core/services/simulate.service.ts                                                              
Change: + 5 interfaces + simulateNavigation() + getImpactPreview()
────────────────────────────────────────
File: abac/simulator/simulator.ts
Change: Redesigned: mode signal, navForm, subjectForm, nav simulation logic
────────────────────────────────────────
File: abac/simulator/simulator.html
Change: Mode toggle + navigation mode section (resource select + decision table) + instance mode     
preserved
────────────────────────────────────────
File: policy-detail/rule-impact-dialog/rule-impact-dialog.ts
Change: New dialog — display only, confirm/cancel
────────────────────────────────────────
File: policy-detail/rule-impact-dialog/rule-impact-dialog.html
Change: Roles/attributes/actions chips, navigation flag, instance condition flag, parseWarning banner
────────────────────────────────────────
File: policy-detail/rule-impact-dialog/rule-impact-dialog.css
Change: Empty (as required)
────────────────────────────────────────
File: policy-detail/create-rule-dialog/create-rule-dialog.ts
Change: Removed API call; exports CreateRuleFormData; closes with form data
────────────────────────────────────────
File: policy-detail/create-rule-dialog/create-rule-dialog.html
Change: Removed error banner + spinner; button label "Continue"
────────────────────────────────────────
File: policy-detail/edit-rule-dialog/edit-rule-dialog.ts
Change: Same refactor; exports EditRuleFormData
────────────────────────────────────────
File: policy-detail/edit-rule-dialog/edit-rule-dialog.html
Change: Same simplification
────────────────────────────────────────
File: policy-detail/policy-detail.ts
Change: Injected SimulateService; openAddRuleDialog + openEditRuleDialog → previewAndSave() →        
RuleImpactDialog → doSave() with API call + SpEL error handling in snackbar

Yêu cầu thiết kế chi tiết:

  1. SimulatorComponent — Mode Toggle + Navigation Results:

     a. Mode Toggle:
        - `.mode-toggle`: display flex, gap 8px, margin-bottom 24px
        - Mỗi mode button: `mat-flat-button` hoặc `mat-stroked-button`, height 40px, min-width 140px
        - Active (navigation): background var(--primary-green), color white
          → label "Navigation", mat-icon "explore" bên trái
        - Active (instance): background var(--primary-green), color white
          → label "Instance", mat-icon "data_object" bên trái
        - Inactive: background white, color var(--text-secondary), border 1px solid var(--border-color)
        - Hover inactive: border-color var(--primary-green), color var(--primary-green)

     b. Navigation Form (mode='navigation'):
        - Layout giống Instance mode: 2 columns trên desktop (form trái, results phải)
        - Form card: `.section-card` — giống style hiện tại của simulator
        - Subject section: giống Instance mode (roles chip + attributes textarea)
        - Resource select: full width mat-select
        - `.nav-form-hint`: font-size 0.75rem, color var(--text-secondary), margin-top 4px
          text: "Evaluates all actions for this resource without instance data (navigation level)"

     c. Navigation Results Table:
        - Results card (bên phải, giống Instance results card):
          → Header: flex row với policySetName badge ở góc phải nếu có
          → `.policy-set-badge`: background #f1f5f9, color #334155, border-radius 9999px,
            padding 2px 10px, font-size 0.75rem, font-family monospace
        - mat-table với 2 columns: ACTION và DECISION
        - `.action-name-cell`: font-family monospace, font-size 0.875rem, color var(--text-primary)
        - Decision badge (dùng lại từ simulator cũ):
          `.decision-permit`: background #f0fdf4, color #15803d, border-radius 9999px,
            padding 3px 12px, font-size 0.75rem, font-weight 600
          `.decision-deny`: background #fef2f2, color #dc2626, border-radius 9999px,
            padding 3px 12px, font-size 0.75rem, font-weight 600
        - Row hover: background #f8fafc
        - Empty state (no decisions): text "Run simulation to see results", icon "play_circle_outline"
        - Loading state: mat-spinner diameter 32, centered

     d. Không sửa Instance mode style — giữ nguyên simulator.css hiện tại,
        chỉ append CSS mới cho navigation-specific classes.

  2. RuleImpactDialogComponent (.html / .css):

     a. Dialog layout:
        - Width: 480px
        - Header: tiêu đề "Confirm Rule Change", subtitle "Review the impact before saving"
        - mat-dialog-content: padding 0 24px
        - mat-dialog-actions: padding 16px 24px, justify-content flex-end, gap 8px

     b. Parse Warning Banner (hiện khi parseWarning != null):
        - `.parse-warning`: display flex, gap 10px, align-items flex-start,
          background #fef3c7, border 1px solid #fde68a, border-radius 8px,
          padding 12px 14px, margin-bottom 16px
        - Icon: mat-icon "warning_amber", color #92400e, font-size 20px, flex-shrink 0
        - Text: font-size 0.8125rem, color #92400e, line-height 1.5

     c. Impact Sections:
        - `.impact-section`: margin-bottom 16px
        - `.impact-label`: font-size 0.75rem, font-weight 600, color var(--text-secondary),
          text-transform uppercase, letter-spacing 0.05em, margin-bottom 8px
        - Chip list (roles, attributes, actions):
          `.impact-chip`: display inline-flex, align-items center, background #f1f5f9,
          border 1px solid var(--border-color), border-radius 9999px, padding 3px 12px,
          font-size 0.8125rem, font-family monospace, color var(--text-primary), margin 2px
          → roles chips: background #eff6ff, color #1d4ed8, border-color #bfdbfe
          → attribute chips: background #faf5ff, color #6b21a8, border-color #e9d5ff
          → action chips: background #f0fdf4, color #15803d, border-color #bbf7d0
        - Empty text ("Any" / "All actions" / "-"):
          font-size 0.875rem, color var(--text-secondary), font-style italic

     d. Boolean Indicators:
        - `.indicator-row`: display flex, align-items center, gap 8px,
          padding 8px 0, border-top 1px solid #f1f5f9
        - Icon: mat-icon font-size 18px
          → true/positive: color #15803d (check_circle)
          → false/negative: color #94a3b8 (radio_button_unchecked)
        - Text: font-size 0.875rem, color var(--text-primary)
        - Sub-text (hint): font-size 0.75rem, color var(--text-secondary)

        Visual indicators cụ thể:
        - `navigableWithoutData = true`:
          icon check_circle màu xanh + "Accessible without instance data"
          hint: "Rule applies at navigation level (menu/button visibility)"
        - `navigableWithoutData = false`:
          icon info màu secondary + "Requires instance data"
          hint: "Rule only evaluates correctly when object data is provided"
        - `hasInstanceCondition = true`:
          icon data_object + "Has instance-level conditions"
          hint: "Impact on specific records depends on their field values"

     e. Confirm button style:
        - [Cancel]: mat-button, color secondary
        - [Confirm & Save]: mat-flat-button, background var(--primary-green), color white

Ràng buộc kỹ thuật:
  - KHÔNG sửa file .ts
  - @import path:
    + simulator.css: `@import '../../dashboard.css'` (giữ nguyên đầu file, append CSS mới vào cuối)
    + rule-impact-dialog.css: `@import '../../../../../dashboard.css'`
  - CSS variables từ dashboard.css: --text-primary, --text-secondary, --border-color,
    --card-bg, --primary-green, --primary-green-hover, --status-error
  - Simulator: chỉ APPEND vào simulator.css — không viết lại file đang có

OUTPUT: Nội dung hoàn chỉnh cho từng cặp .html + .css (4 files).
Với simulator.css: trả về phần CSS mới cần append (không trả toàn bộ file).
