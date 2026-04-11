Prompt: Batch 3 (FE) — UIElement Registry + Policy Simulator

Vai trò: Bạn là Senior Frontend Engineer chuyên về Angular và kiến trúc Dashboard Admin.

Tài liệu căn cứ:
  1. Quy ước dự án: @web/CLAUDE.md
  2. Cấu trúc routes hiện tại: @web/src/app/app.routes.ts
  3. Pattern tham chiếu service: @web/src/app/core/services/resource.service.ts
  4. Pattern tham chiếu list: @web/src/app/dashboard/abac/resources/resources.ts
  5. Pattern tham chiếu detail: @web/src/app/dashboard/abac/resources/resource-detail/resource-detail.ts
  6. Sidebar hiện tại: @web/src/app/dashboard/dashboard.html

Dữ liệu từ Backend (Bàn giao từ Batch 3):

Endpoints — Batch 3

UIElement — /api/v1/abac/ui-elements

┌────────┬───────────┬───────────────────────────┬─────────────────────────────────────────────────┐  
│ Method │   Path    │           Body            │                   Description                   │  
├────────┼───────────┼───────────────────────────┼─────────────────────────────────────────────────┤  
│ POST   │ /         │ CreateUIElementRequest    │ Create → ApiResponse<{id}> (201)                │  
├────────┼───────────┼───────────────────────────┼─────────────────────────────────────────────────┤  
│ GET    │ /{id}     │ —                         │ Detail → ApiResponse<UIElementView>             │  
├────────┼───────────┼───────────────────────────┼─────────────────────────────────────────────────┤  
│ GET    │ /         │ —                         │ List (params: resourceId, type, group, page,    │  
│        │           │                           │ size) → PagedApiResponse<UIElementSummary>      │  
├────────┼───────────┼───────────────────────────┼─────────────────────────────────────────────────┤  
│ PUT    │ /{id}     │ UpdateUIElementRequest    │ Update → 204                                    │  
├────────┼───────────┼───────────────────────────┼─────────────────────────────────────────────────┤  
│ DELETE │ /{id}     │ —                         │ Delete → 204                                    │  
├────────┼───────────┼───────────────────────────┼─────────────────────────────────────────────────┤  
│ POST   │ /evaluate │ EvaluateUIElementsRequest │ Batch evaluate → ApiResponse<{results:          │  
│        │           │                           │ Map<elementId, "PERMIT"|"DENY">}>               │  
└────────┴───────────┴───────────────────────────┴─────────────────────────────────────────────────┘

Simulate — /api/v1/abac/simulate

┌────────┬──────┬─────────────────┬────────────────────────────────────────┐
│ Method │ Path │      Body       │              Description               │
├────────┼──────┼─────────────────┼────────────────────────────────────────┤
│ POST   │ /    │ SimulateRequest │ Simulate → ApiResponse<SimulateResult> │
└────────┴──────┴─────────────────┴────────────────────────────────────────┘

  ---
AdminPolicyProvider wiring:
- AdminPolicyProvider implements PolicyProvider (libs/abac), annotated @Component
- Loads isRoot=true PolicySet(s) from DB → maps to libs/abac PolicySet → Policy → Rule
- PdpEngine is a Spring @Bean defined in AbacConfig, using DecisionStrategy.DEFAULT_DENY
- AdminSubjectProvider implements SubjectProvider, builds Subject from Principal.getName() (userId    
  string) → loads user + roles from DB

Known limitations:
- Simulate details field is raw (Object) from AuthzDecision.getDetails() — no structured rule-by-rule
  trace (Phase 3)
- existsByIdWithPolicyRef in ResourceDefinitionPersistenceAdapter still returns false (no FK from     
  policy to resource_definition — policy uses SpEL strings, not FK)
- Phase 1 only returns the first isRoot=true PolicySet — multi-root or per-service routing is Phase 2

  ---
FRONTEND CONTEXT BLOCK — Batch 3

TypeScript interfaces:

export interface UIElementView {
id: number;
elementId: string;
label: string;
type: 'BUTTON' | 'TAB' | 'MENU_ITEM';
elementGroup: string | null;
orderIndex: number;
resourceId: number;
resourceName: string;
actionId: number;
actionName: string;
}

export type UIElementSummary = UIElementView; // same shape

export interface EvaluateRequest {
elementIds: string[];
}

export interface EvaluateResponse {
results: Record<string, 'PERMIT' | 'DENY'>;
}

export interface SimulateSubjectRequest {
userId?: string | null;
roles: string[];
attributes?: Record<string, unknown>;
}

export interface SimulateResourceRequest {
name: string;
data?: unknown;
}

export interface SimulateRequest {
subject: SimulateSubjectRequest;
resource: SimulateResourceRequest;
action: string;
policySetId?: number | null;
}

export interface SimulateResult {
decision: 'PERMIT' | 'DENY';
timestamp: number;
policySetId: number | null;
policySetName: string | null;
details: unknown;
}

Error codes:

┌───────┬──────┬─────────────────────────┐
│ Code  │ HTTP │         Meaning         │
├───────┼──────┼─────────────────────────┤
│ 30012 │ 404  │ UI Element not found    │
├───────┼──────┼─────────────────────────┤
│ 30013 │ 409  │ UI Element ID duplicate │
└───────┴──────┴─────────────────────────┘

UI notes:
- POST /evaluate requires JWT — call once after login to get visibility map for all registered        
  elements
- Evaluate response: res.data.results is Record<string, "PERMIT"|"DENY">
- Simulate /evaluate vs /simulate: evaluate is for the logged-in user (JWT-based subject), simulate is
  for a virtual subject (admin debug tool, SUPER_ADMIN only)
- elementId convention: {type}:{resource}:{action-slug}, e.g. btn:employee:update


Nhiệm vụ cụ thể:

  1. Angular Service — tạo web/src/app/core/services/ui-element.service.ts
      - Interfaces: UIElementView, UIElementSummary, UIElementPage,
        CreateUIElementRequest, UpdateUIElementRequest,
        EvaluateRequest { elementIds: string[] },
        EvaluateResponse { results: Record<string, 'PERMIT' | 'DENY'> }
      - Methods CRUD: getUIElements(params), getUIElementById, createUIElement,
        updateUIElement, deleteUIElement
      - Method: evaluateUIElements(request: EvaluateRequest): Observable<EvaluateResponse>
      - Pattern: giống resource.service.ts

  2. Simulate Service — tạo web/src/app/core/services/simulate.service.ts
      - Interfaces: SimulateRequest, SimulateResponse (decision, details, timestamp)
      - Method: simulate(request: SimulateRequest): Observable<SimulateResponse>

  3. UIElementsComponent — tạo web/src/app/dashboard/abac/ui-elements/ui-elements.ts (.html, .css)
      - Filter by resourceId (select dropdown — load từ ResourceService)
      - Filter by type (select: ALL / BUTTON / TAB / MENU_ITEM)
      - Table: elementId, label, type chip, resourceName, actionName, actions (Edit, Delete)
      - Nút "+ New UIElement" mở CreateUIElementDialogComponent
      - Delete: xử lý lỗi UI_ELEMENT_ID_DUPLICATE / generic error

  4. CreateUIElementDialogComponent — tạo trong ui-elements/create-ui-element-dialog/
      - Form: elementId (required), label (required), type (select), elementGroup,
        orderIndex (number), resourceId (select — load resources), actionId (select — load actions của resource đã chọn)
      - Khi thay đổi resourceId → reload actionId options
      - Xử lý lỗi UI_ELEMENT_ID_DUPLICATE → mat-error dưới field elementId

  5. UIElementDetailComponent — tạo web/src/app/dashboard/abac/ui-elements/ui-element-detail/
      - Hiển thị elementId (read-only), label, type, group, orderIndex (editable → PUT)
      - Section "Linked to Policy": hiển thị resourceName + actionName (read-only)

  6. SimulatorComponent — tạo web/src/app/dashboard/abac/simulator/simulator.ts (.html, .css)
      - Form subject: roles (chip input — thêm/xoá role), attributes (key-value list)
      - Form resource: name (select từ resources), data (JSON textarea — optional)
      - Form action: select từ actions của resource đã chọn
      - Nút "Run Simulation" → gọi simulate API
      - Results section: decision badge (PERMIT xanh / DENY đỏ), timestamp, details raw JSON

  7. Routing — cập nhật app.routes.ts
      - /admin/abac/ui-elements           → UIElementsComponent
      - /admin/abac/ui-elements/:id       → UIElementDetailComponent
      - /admin/abac/simulator             → SimulatorComponent

  8. Sidebar — cập nhật dashboard.html
      - Thêm vào section "ABAC":
        item icon="widgets", label="UI Elements", routerLink="abac/ui-elements"
        item icon="science", label="Simulator", routerLink="abac/simulator"

Yêu cầu kỹ thuật:
  - Standalone Components, Signals, Angular Material — pattern giống resources (Batch 1)
  - Chip input cho roles: dùng MatChipsModule + COMMA/ENTER separator
  - JSON textarea: monospace font, min 120px height
  - No Styling — CSS để trống, Gemini style sau

Output: Liệt kê các file đã tạo/sửa và mã nguồn.
