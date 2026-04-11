Prompt: Style Giao diện — PolicySet / Policy / Rule Management (Gemini UI/UX)

Vai trò: Bạn là Senior UI/UX-focused Frontend Engineer. Nhiệm vụ của bạn là style các trang quản lý
Policy nhất quán với phần Resources đã có sẵn.

Tài liệu căn cứ:
  1. Quy ước thiết kế: @web/docs/layout/dashboard.md
  2. CSS tham chiếu (follow chính xác): @web/src/app/dashboard/abac/resources/resources.css
  3. CSS tham chiếu detail page: @web/src/app/dashboard/abac/resources/resource-detail/resource-detail.css

FILES TO RESTYLE (Mã nguồn từ Batch 2 FE):

Angular (web)                                                                                                                                                                                                 
┌──────────────────────────────────────────────────────┬───────────────────────────────────────────┐    
│                          File                        │                  Action                   │  
├──────────────────────────────────────────────────────┼───────────────────────────────────────────┤
│ core/services/policy.service.ts                      │ Created — interfaces + service cho        │  
│                                                      │ PolicySet/Policy/Rule                     │  
├──────────────────────────────────────────────────────┼───────────────────────────────────────────┤  
│ dashboard/abac/policy-sets/policy-sets.ts/.html/.css │ Created — list page                       │  
├──────────────────────────────────────────────────────┼───────────────────────────────────────────┤  
│ policy-sets/create-policy-set-dialog/ (3 files)      │ Created — dialog tạo mới                  │  
├──────────────────────────────────────────────────────┼───────────────────────────────────────────┤  
│ policy-sets/policy-set-detail/ (3 files)             │ Created — detail + settings + nested      │  
│                                                      │ policies table                            │  
├──────────────────────────────────────────────────────┼───────────────────────────────────────────┤  
│ policy-set-detail/create-policy-dialog/ (3 files)    │ Created — dialog thêm policy              │  
├──────────────────────────────────────────────────────┼───────────────────────────────────────────┤  
│ dashboard/abac/policies/policy-detail/ (3 files)     │ Created — breadcrumb, SpEL textarea,      │  
│                                                      │ rules table với ↑↓ reorder                │  
├──────────────────────────────────────────────────────┼───────────────────────────────────────────┤  
│ policy-detail/create-rule-dialog/ (3 files)          │ Created — dialog tạo rule                 │  
├──────────────────────────────────────────────────────┼───────────────────────────────────────────┤  
│ policy-detail/edit-rule-dialog/ (3 files)            │ Created — dialog sửa rule                 │  
├──────────────────────────────────────────────────────┼───────────────────────────────────────────┤  
│ app.routes.ts                                        │ Updated — 3 routes mới                    │  
├──────────────────────────────────────────────────────┼───────────────────────────────────────────┤  
│ dashboard.html                                       │ Updated — sidebar item "Policy Sets"      │  
├──────────────────────────────────────────────────────┼───────────────────────────────────────────┤  
│ create-resource-dialog.css                           │ Fixed — sửa đường dẫn @import sai         │  
│                                                      │ (pre-existing bug)                        │  
└──────────────────────────────────────────────────────┴───────────────────────────────────────────┘

Yêu cầu thiết kế chi tiết:

  1. PolicySets List Page:
      - Follow chính xác pattern resources.css — table, search, header, empty state
      - `.policy-set-name`: monospace badge, background #f1f5f9 (giống resource-name-badge)
      - `.is-root-badge`: pill nhỏ kế bên name — background #fefce8, color #854d0e, border 1px solid #fef08a
        Text: "ROOT"
      - `.scope-badge`: OPERATOR → background #eff6ff, color #1d4ed8 / TENANT → background #faf5ff, color #6b21a8
      - `.algorithm-badge`: monospace, background #f8fafc, color var(--text-secondary), font-size 0.75rem

  2. PolicySet Detail Page:
      - Section cards giống resource-detail.css (section-card, section-header, section-title)
      - Policy table trong section card: name, combineAlgorithm badge, clickable row

  3. Policy Detail Page:
      - Breadcrumb: flex row, separator "/", font-size nhỏ, color secondary — clickable links
      - SpEL textarea: font-family monospace, background #0f172a (dark code editor feel),
        color #e2e8f0, border-radius 8px, padding 16px, font-size 0.875rem
        Label "Target Expression" / "Condition Expression" màu secondary phía trên
      - `.spel-hint`: font-size 0.75rem, color var(--text-secondary), margin-top 4px
      - Rules table: order handle (⋮⋮), name, effect badge, up/down buttons, edit/delete
      - `.effect-permit`: background #f0fdf4, color #15803d, border-radius 9999px, padding 2px 10px, font-size 0.75rem
      - `.effect-deny`: background #fef2f2, color #dc2626, border-radius 9999px, padding 2px 10px, font-size 0.75rem
      - `.order-handle`: color var(--text-secondary), cursor grab, font-size 20px

  4. Dialogs (Create PolicySet, Create Policy, Create/Edit Rule):
      - Follow pattern create-resource-dialog.css
      - Rule dialog: 2 SpEL textareas — dùng dark monospace style như detail page
      - Select fields (scope, combineAlgorithm, effect): mat-select, appearance outline

Ràng buộc kỹ thuật:
  - KHÔNG sửa file .ts
  - @import path: `../../dashboard.css` cho policy-sets page, `../../../dashboard.css` cho sub-pages
  - CSS variables: --text-primary, --text-secondary, --border-color, --card-bg, --primary-green,
    --primary-green-hover, --status-error
  - MDC overrides cho buttons (--mdc-filled-button-*)

OUTPUT: Nội dung hoàn chỉnh cho từng cặp .html + .css.
