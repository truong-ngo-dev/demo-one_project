Prompt: Style Giao diện — UIElement Registry + Simulator (Gemini UI/UX)

Vai trò: Bạn là Senior UI/UX-focused Frontend Engineer. Style các trang UIElement và Simulator nhất quán
với phần Resources và Policy đã có.

Tài liệu căn cứ:
  1. Quy ước thiết kế: @web/docs/layout/dashboard.md
  2. CSS tham chiếu list page: @web/src/app/dashboard/abac/resources/resources.css
  3. CSS tham chiếu detail page: @web/src/app/dashboard/abac/resources/resource-detail/resource-detail.css

FILES TO RESTYLE (Mã nguồn từ Batch 3 FE):

Files created:

File: web/src/app/core/services/ui-element.service.ts                                                   Description: Service + all interfaces (UIElementView, UIElementSummary, UIElementPage, Create/Update
request, EvaluateRequest/Response)                                                                    ────────────────────────────────────────                                                                File: web/src/app/core/services/simulate.service.ts                                                     Description: Service + SimulateRequest, SimulateResponse interfaces                                   
────────────────────────────────────────
File: web/src/app/dashboard/abac/ui-elements/ui-elements.ts/.html/.css
Description: List page with resource + type filters, table, create dialog
────────────────────────────────────────
File:
web/src/app/dashboard/abac/ui-elements/create-ui-element-dialog/create-ui-element-dialog.ts/.html/.css
Description: Create dialog — resourceId select triggers action reload, error code 30013
────────────────────────────────────────
File: web/src/app/dashboard/abac/ui-elements/ui-element-detail/ui-element-detail.ts/.html/.css        
Description: Detail page — elementId read-only, editable label/type/group/orderIndex, "Linked to      
Policy" section
────────────────────────────────────────
File: web/src/app/dashboard/abac/simulator/simulator.ts/.html/.css
Description: Simulator — chip input for roles, JSON textareas, resource+action selects, PERMIT/DENY   
result badge

Files updated:

┌──────────────────────────────────────┬───────────────────────────────────────────────────────────┐  
│                 File                 │                          Change                           │  
├──────────────────────────────────────┼───────────────────────────────────────────────────────────┤  
│ web/src/app/app.routes.ts            │ Added 3 routes: abac/ui-elements, abac/ui-elements/:id,   │  
│                                      │ abac/simulator                                            │  
├──────────────────────────────────────┼───────────────────────────────────────────────────────────┤  
│ web/src/app/dashboard/dashboard.html │ Added "UI Elements" (widgets icon) and "Simulator"        │  
│                                      │ (science icon) to ACCESS CONTROL section                  │  
└──────────────────────────────────────┴───────────────────────────────────────────────────────────┘

Yêu cầu thiết kế chi tiết:

  1. UIElements List Page:
      - Follow pattern resources.css
      - `.element-id-badge`: monospace badge, background #f1f5f9, color #334155
      - `.type-badge`: BUTTON → background #eff6ff, color #1d4ed8
                       TAB    → background #f0fdf4, color #15803d
                       MENU_ITEM → background #faf5ff, color #6b21a8
      - Filter row: 2 selects inline (Resource + Type) — flex row, gap 16px, margin-bottom 24px
      - Table: element-id (monospace), label, type chip, resource, action, actions buttons

  2. UIElement Detail Page:
      - Section cards giống resource-detail.css
      - elementId: immutable-chip style (monospace, border)
      - "Linked to Policy" section: 2 read-only fields (resource + action) side by side

  3. Simulator Page (design đặc biệt — khác các trang còn lại):
      - Layout: 2 columns (form bên trái, results bên phải) trên desktop, stack trên mobile
        Left: max-width 480px / Right: flex 1
      - Form card: section-card với border, padding 24px
      - Subject section: tiêu đề "Subject" uppercase secondary, roles chip list màu primary-green
      - Resource + Action section: 2 form fields side by side
      - JSON textarea (instance data): dark background #0f172a, color #e2e8f0, monospace,
        border-radius 8px, padding 12px, min-height 120px
      - Nút "Run Simulation": full width, --primary-green, height 48px
      - Results card (bên phải):
        + Decision banner lớn: PERMIT → background #f0fdf4, color #15803d, border #bbf7d0
          DENY → background #fef2f2, color #dc2626, border #fecaca
          font-size 1.5rem, font-weight 700, border-radius 12px, padding 24px, text-center
        + Timestamp: font-size 0.75rem, color secondary, text-center
        + Details section: code block với background #0f172a, color #e2e8f0, monospace,
          font-size 0.8125rem, border-radius 8px, padding 16px, overflow auto, max-height 400px

  4. Create UIElement Dialog:
      - Follow create-resource-dialog pattern
      - Action select: disabled cho đến khi resource được chọn (greyed out)

Ràng buộc kỹ thuật:
  - KHÔNG sửa file .ts
  - @import dashboard.css với đúng relative path cho mỗi file
  - CSS variables chuẩn từ dashboard.css
  - Simulator layout: dùng CSS Grid 2 columns trên desktop

OUTPUT: Nội dung hoàn chỉnh cho từng cặp .html + .css.
