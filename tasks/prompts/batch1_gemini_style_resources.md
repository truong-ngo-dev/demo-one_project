Prompt: Style Giao diện — Resource & Action Catalogue (Gemini UI/UX)

Vai trò: Bạn là Senior UI/UX-focused Frontend Engineer. Nhiệm vụ của bạn là biến cấu trúc HTML thô
thành giao diện quản trị hiện đại, nhất quán với phần còn lại của dashboard.

Tài liệu căn cứ:
  1. Quy ước thiết kế (bắt buộc): @web/docs/layout/dashboard.md
  2. CSS tham chiếu (follow chính xác pattern này): @web/src/app/dashboard/roles/roles.css
  3. Công nghệ: Angular Material (MDC) + Tailwind CSS

FILES TO RESTYLE (Mã nguồn từ Batch 1 FE):

  ┌──────────────────────────────────────────────────────────────────────┬──────────────────────────────────────────┐
  │ File                                                                 │ Thay đổi                                 │
  ├──────────────────────────────────────────────────────────────────────┼──────────────────────────────────────────┤
  │ dashboard/abac/resources/resources.html                              │ List page — table, search, header        │
  │ dashboard/abac/resources/resources.css                               │ Style list page                          │
  ├──────────────────────────────────────────────────────────────────────┼──────────────────────────────────────────┤
  │ dashboard/abac/resources/resource-detail/resource-detail.html        │ Detail page — form + actions table       │
  │ dashboard/abac/resources/resource-detail/resource-detail.css         │ Style detail page                        │
  ├──────────────────────────────────────────────────────────────────────┼──────────────────────────────────────────┤
  │ dashboard/abac/resources/create-resource-dialog/create-resource-dialog.html │ Create dialog               │
  │ dashboard/abac/resources/create-resource-dialog/create-resource-dialog.css  │ Style create dialog         │
  ├──────────────────────────────────────────────────────────────────────┼──────────────────────────────────────────┤
  │ dashboard/abac/resources/resource-detail/add-action-dialog/add-action-dialog.html │ Add action dialog  │
  │ dashboard/abac/resources/resource-detail/add-action-dialog/add-action-dialog.css  │ Style add dialog   │
  └──────────────────────────────────────────────────────────────────────┴──────────────────────────────────────────┘

Yêu cầu thiết kế chi tiết:

  1. List Page (resources.html / .css):
      - Follow CHÍNH XÁC pattern của roles.css — dùng cùng CSS variables, class names, table styles
      - `.resource-name-badge`: monospace, slate background (khác với `.role-name-badge` màu green)
        → dùng: background #f1f5f9, color #334155, font-family monospace
      - `.action-count-badge`: pill badge nhỏ hiển thị số lượng action
        → dùng: background #f0fdf4, color #15803d, border-radius 9999px, padding 2px 10px
      - Row có thể click (cursor pointer) → hover highlight nhẹ như roles table
      - Empty state: icon="policy" — style giống empty state của roles

  2. Detail Page (resource-detail.html / .css):
      - Layout: 2 section cards xếp dọc với gap 24px
      - `.section-card`: border 1px solid var(--border-color), border-radius 12px, padding 24px,
        background var(--card-bg), box-shadow nhẹ — giống `.table-container` của roles
      - `.section-header`: flex, justify-content space-between, align-items center, margin-bottom 16px
      - `.section-title`: font-size 1rem, font-weight 600, color var(--text-primary)
      - `.field-readonly`: layout flex column, gap 4px, margin-bottom 16px
      - `.field-label`: font-size 0.75rem, font-weight 600, color var(--text-secondary), text-transform uppercase
      - `.immutable-chip`: inline-block, background #f8fafc, border 1px solid var(--border-color),
        border-radius 6px, padding 4px 12px, font-family monospace, font-size 0.875rem, color var(--text-primary)
      - `.field-hint`: font-size 0.75rem, color var(--text-secondary), font-style italic
      - Actions table trong section card: dùng cùng table style như roles (header uppercase, row hover)
      - `.actions-table`: width 100%
      - isStandard chip: mat-chip — standard → màu xanh (--primary-green), custom → màu mặc định (secondary)
      - Back button + title: flex row, align-items center, gap 8px — button mat-icon-button, title h1

  3. Create Resource Dialog (create-resource-dialog.html / .css):
      - Follow CHÍNH XÁC pattern của create-role-dialog.css
      - `.dialog-header`: padding-bottom 0
      - `.dialog-subtitle`: font-size 0.875rem, color var(--text-secondary), margin-top 4px
      - Form fields: `w-full` spacing, gap giữa các field là 16px
      - `.error-banner`: flex, gap 8px, background #fef2f2, border 1px solid #fecaca,
        border-radius 8px, padding 12px, color #dc2626
      - Submit button: dùng MDC variables cho màu xanh (--primary-green) như roles

  4. Add Action Dialog (add-action-dialog.html / .css):
      - Tương tự create-resource-dialog nhưng có thêm `mat-checkbox`
      - Checkbox: margin-top 8px, màu checkbox override sang --primary-green
      - Hint text dưới name field: "Action name cannot be changed after creation."

Ràng buộc kỹ thuật:
  - KHÔNG sửa logic trong file .ts
  - CSS: dùng @import '../../../dashboard.css' ở đầu CSS của list page
        dùng @import '../../../../dashboard.css' cho resource-detail.css
        dùng @import '../../../../dashboard.css' cho dialog CSS files
  - CSS variables từ dashboard.css: --text-primary, --text-secondary, --border-color,
    --card-bg, --primary-green, --primary-green-hover, --status-error
  - Tailwind: chỉ dùng cho layout utilities (w-full, flex, gap-*)
  - Angular Material: dùng MDC Custom Properties (--mdc-filled-button-*) để override màu button

OUTPUT: Trả về nội dung hoàn chỉnh cho từng cặp .html + .css (8 files).
