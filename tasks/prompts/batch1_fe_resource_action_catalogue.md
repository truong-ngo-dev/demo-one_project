Prompt: Batch 1 (FE) — Resource & Action Catalogue

Vai trò: Bạn là Senior Frontend Engineer chuyên về Angular và kiến trúc Dashboard Admin.

Tài liệu căn cứ:
  1. Quy ước dự án: @web/CLAUDE.md
  2. Cấu trúc routes hiện tại: @web/src/app/app.routes.ts
  3. Pattern tham chiếu service: @web/src/app/core/services/role.service.ts
  4. Pattern tham chiếu component: @web/src/app/dashboard/roles/roles.ts
  5. Sidebar hiện tại: @web/src/app/dashboard/dashboard.html

Dữ liệu từ Backend (Bàn giao từ Batch 1):

  Base URL (qua Web Gateway): /api/admin/v1/abac/resources

  TypeScript Interfaces:

    export interface ActionView {
      id: number;
      name: string;           // IMMUTABLE — render read-only, không dùng input
      description: string | null;
      isStandard: boolean;
    }

    export interface ResourceView {
      id: number;
      name: string;           // IMMUTABLE — render read-only, không dùng input
      description: string | null;
      serviceName: string;
      actions: ActionView[];
    }

    export interface ResourceSummaryView {
      id: number;
      name: string;
      serviceName: string;
      actionCount: number;
    }

    export interface ResourcePage {
      data: ResourceSummaryView[];
      meta: { page: number; size: number; total: number };
    }

    export interface CreateResourceRequest { name: string; description?: string; serviceName: string; }
    export interface UpdateResourceRequest { description?: string; serviceName: string; }
    export interface AddActionRequest { name: string; description?: string; isStandard: boolean; }
    export interface UpdateActionRequest { description?: string; }

  Endpoints:
    GET    /api/admin/v1/abac/resources?keyword=&page=&size=  → ResourcePage
    POST   /api/admin/v1/abac/resources                       → 201 { data: { id: number } }
    GET    /api/admin/v1/abac/resources/:id                   → { data: ResourceView }
    PUT    /api/admin/v1/abac/resources/:id                   → 200 { data: { id: number } }
    DELETE /api/admin/v1/abac/resources/:id                   → 204
    POST   /api/admin/v1/abac/resources/:resourceId/actions   → 201 { data: { actionId: number } }
    PATCH  /api/admin/v1/abac/resources/:resourceId/actions/:actionId → 200
    DELETE /api/admin/v1/abac/resources/:resourceId/actions/:actionId → 204

  Error codes (error.error.error.code):
    RESOURCE_NAME_DUPLICATE → "Resource name already exists"
    RESOURCE_IN_USE         → "Cannot delete: resource is used by policies or UI elements"
    ACTION_NAME_DUPLICATE   → "Action name already exists in this resource"
    ACTION_IN_USE           → "Cannot delete: action is used by UI elements"

  UI Notes:
    - name (resource & action) là IMMUTABLE sau khi tạo — hiển thị dạng text/chip, không cho edit
    - isStandard = true → chip màu primary; false → chip màu default (custom action)
    - Standard actions: LIST, READ, CREATE, UPDATE, DELETE

Nhiệm vụ cụ thể:

  1. Angular Service — tạo web/src/app/core/services/resource.service.ts
      - Khai báo đầy đủ các interfaces trên trong file service (export để component dùng)
      - Implement các methods: getResources(params), getResourceById(id), createResource(data),
        updateResource(id, data), deleteResource(id), addAction(resourceId, data),
        updateAction(resourceId, actionId, data), removeAction(resourceId, actionId)
      - Pattern: inject HttpClient, dùng pipe(map(res => res.data)) như role.service.ts

  2. ResourcesComponent — tạo web/src/app/dashboard/abac/resources/resources.ts (.html, .css)
      - Table hiển thị danh sách: name, serviceName, actionCount, actions (Edit, Delete)
      - Search bằng keyword với debounce (300ms) — pattern giống roles.ts
      - Nút "+ New Resource" mở CreateResourceDialogComponent
      - Click row hoặc nút Edit → navigate tới /admin/abac/resources/:id
      - Delete: mở confirm dialog, xử lý lỗi RESOURCE_IN_USE bằng snackbar

  3. CreateResourceDialogComponent — tạo trong resources/create-resource-dialog/
      - Form: name (required), description, serviceName (required)
      - Xử lý lỗi RESOURCE_NAME_DUPLICATE → hiện mat-error dưới field name

  4. ResourceDetailComponent — tạo web/src/app/dashboard/abac/resources/resource-detail/
      - Load resource theo :id từ route param
      - Hiển thị name, serviceName (read-only), description (editable → PUT)
      - Section "Actions": table với name, description, isStandard chip, delete button
      - Nút "+ Add Action" mở AddActionDialogComponent
      - Delete action: confirm → xử lý lỗi ACTION_IN_USE bằng snackbar

  5. AddActionDialogComponent — tạo trong resource-detail/add-action-dialog/
      - Form: name (required), description, isStandard (checkbox)
      - Xử lý lỗi ACTION_NAME_DUPLICATE → hiện mat-error dưới field name

  6. Routing — cập nhật web/src/app/app.routes.ts
      - Thêm vào children của /admin:
        { path: 'abac/resources', loadComponent: ... → ResourcesComponent }
        { path: 'abac/resources/:id', loadComponent: ... → ResourceDetailComponent }

  7. Sidebar — cập nhật web/src/app/dashboard/dashboard.html
      - Thêm section title "ABAC" sau section "Management"
      - Item: icon="policy", label="Resources", routerLink="abac/resources"

Yêu cầu kỹ thuật:
  - Standalone Components — không tạo NgModule
  - Signals — dùng signal(), computed() cho state (resources, isLoading, error)
  - Angular Material — mat-table, mat-dialog, mat-snack-bar, mat-chip, mat-form-field
  - No Styling — chỉ class Tailwind cơ bản cho layout. CSS chi tiết để trống
  - Pattern: follow chính xác roles.ts và role.service.ts cho consistency

Output: Liệt kê các file đã tạo/sửa và mã nguồn.
