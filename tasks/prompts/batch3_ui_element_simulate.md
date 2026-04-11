Prompt: Batch 3 — UIElement Registry + Policy Simulator (UC-023, UC-024)

Vai trò: Bạn là Senior Backend Engineer thực hiện Batch 3 — phần cuối của ABAC Phase 1.
Batch 1 (Resource/Action) và Batch 2 (PolicySet/Policy/Rule + AdminPolicyProvider) đã xong.

Tài liệu căn cứ:
  1. Task plan: @tasks/012_admin_phase1_abac_console.md
  2. UC-023: @services/admin/docs/use-cases/UC-023_ui_element_registry.md
  3. UC-024: @services/admin/docs/use-cases/UC-024_policy_simulate.md
  4. Convention: @docs/conventions/ddd-structure.md
  5. libs/abac engine: @libs/abac/src/main/java/vn/truongngo/apartcom/one/lib/abac/pdp/PdpEngine.java
  6. Service map: @services/admin/SERVICE_MAP.md

Context từ Batch 2:
BATCH 2 BACKEND — FRONTEND CONTEXT BLOCK

Endpoints

PolicySet — /api/v1/abac/policy-sets

┌────────┬───────────────────────────────┬──────────────────┬──────────────────────────────────────┐  
│ Method │             Path              │       Body       │             Description              │  
├────────┼───────────────────────────────┼──────────────────┼──────────────────────────────────────┤  
│ GET    │ /api/v1/abac/policy-sets      │ —                │ List (params: keyword, page, size) → │  
│        │                               │                  │  PagedApiResponse<PolicySetSummary>  │  
├────────┼───────────────────────────────┼──────────────────┼──────────────────────────────────────┤  
│ GET    │ /api/v1/abac/policy-sets/{id} │ —                │ Detail + nested policies →           │  
│        │                               │                  │ ApiResponse<PolicySetDetail>         │  
├────────┼───────────────────────────────┼──────────────────┼──────────────────────────────────────┤  
│ POST   │ /api/v1/abac/policy-sets      │ PolicySetRequest │ Create → ApiResponse<{id}>  (201)    │  
├────────┼───────────────────────────────┼──────────────────┼──────────────────────────────────────┤  
│ PUT    │ /api/v1/abac/policy-sets/{id} │ PolicySetRequest │ Update → 204                         │  
├────────┼───────────────────────────────┼──────────────────┼──────────────────────────────────────┤  
│ DELETE │ /api/v1/abac/policy-sets/{id} │ —                │ Delete → 204                         │  
└────────┴───────────────────────────────┴──────────────────┴──────────────────────────────────────┘

Policy — /api/v1/abac/policies

┌────────┬─────────────────────────────────────┬───────────────┬───────────────────────────────────┐  
│ Method │                Path                 │     Body      │            Description            │  
├────────┼─────────────────────────────────────┼───────────────┼───────────────────────────────────┤  
│ GET    │ /api/v1/abac/policies?policySetId=X │ —             │ List by PolicySet →               │  
│        │                                     │               │ ApiResponse<PolicySummary[]>      │  
├────────┼─────────────────────────────────────┼───────────────┼───────────────────────────────────┤  
│ GET    │ /api/v1/abac/policies/{id}          │ —             │ Detail + rules →                  │  
│        │                                     │               │ ApiResponse<PolicyDetail>         │  
├────────┼─────────────────────────────────────┼───────────────┼───────────────────────────────────┤  
│ POST   │ /api/v1/abac/policies               │ PolicyRequest │ Create → ApiResponse<{id}> (201)  │  
├────────┼─────────────────────────────────────┼───────────────┼───────────────────────────────────┤  
│ PUT    │ /api/v1/abac/policies/{id}          │ PolicyRequest │ Update → 204                      │  
├────────┼─────────────────────────────────────┼───────────────┼───────────────────────────────────┤  
│ DELETE │ /api/v1/abac/policies/{id}          │ —             │ Delete (cascades rules) → 204     │  
└────────┴─────────────────────────────────────┴───────────────┴───────────────────────────────────┘

Rule — /api/v1/abac/policies/{policyId}/rules

┌────────┬─────────────────────────────────────────────────┬───────────────┬──────────────────────┐   
│ Method │                      Path                       │     Body      │     Description      │   
├────────┼─────────────────────────────────────────────────┼───────────────┼──────────────────────┤   
│        │                                                 │               │ Create →             │   
│ POST   │ /api/v1/abac/policies/{policyId}/rules          │ RuleRequest   │ ApiResponse<{id}>    │   
│        │                                                 │               │ (201)                │   
├────────┼─────────────────────────────────────────────────┼───────────────┼──────────────────────┤   
│ PUT    │ /api/v1/abac/policies/{policyId}/rules/{ruleId} │ RuleRequest   │ Update → 204         │   
├────────┼─────────────────────────────────────────────────┼───────────────┼──────────────────────┤   
│ DELETE │ /api/v1/abac/policies/{policyId}/rules/{ruleId} │ —             │ Delete → 204         │   
├────────┼─────────────────────────────────────────────────┼───────────────┼──────────────────────┤   
│ PUT    │ /api/v1/abac/policies/{policyId}/rules/reorder  │ {ruleIds:     │ Reorder → 204        │   
│        │                                                 │ number[]}     │                      │   
└────────┴─────────────────────────────────────────────────┴───────────────┴──────────────────────┘

Error Codes (30007–30011)

┌───────┬──────┬───────────────────────────┐
│ Code  │ HTTP │          Meaning          │
├───────┼──────┼───────────────────────────┤
│ 30007 │ 404  │ Policy Set not found      │
├───────┼──────┼───────────────────────────┤
│ 30008 │ 409  │ Policy Set name duplicate │
├───────┼──────┼───────────────────────────┤
│ 30009 │ 404  │ Policy not found          │
├───────┼──────┼───────────────────────────┤
│ 30010 │ 404  │ Rule not found            │
├───────┼──────┼───────────────────────────┤
│ 30011 │ 400  │ Invalid SpEL expression   │
└───────┴──────┴───────────────────────────┘


Nhiệm vụ cụ thể:

  1. Domain Layer — package `domain/abac/uielement/`
      - Enum: `UIElementType` (BUTTON, TAB, MENU_ITEM)
      - Aggregate Root: `UIElement` (id: Long, elementId: String, label, type, elementGroup, orderIndex,
        resourceId: ResourceId, actionId: ActionId)
      - Port: `UIElementRepository` — CRUD + findByResourceId + existsByElementId + existsByActionId
      - Extend `AbacErrorCode` thêm: UI_ELEMENT_NOT_FOUND, UI_ELEMENT_ID_DUPLICATE

  2. Application Layer
      - UIElement slices trong `application/ui_element/`:
        create, update, delete, get, list (by resourceId optional)
      - Batch evaluate slice: `application/ui_element/query/evaluate/EvaluateUIElements`
        Logic: load UIElements theo elementIds → build Subject từ JWT principal →
        với mỗi element: Action.semantic(actionName) + Resource(resourceName, null) →
        PdpEngine.authorize() → return Map<elementId, "PERMIT"/"DENY">
        Inject: `UIElementRepository`, `PdpEngine`, `AdminPolicyProvider`, `SubjectProvider`
      - Simulate slice: `application/simulate/simulate_policy/SimulatePolicy`
        Logic xem UC-024: load PolicySet (theo id hoặc isRoot) → build virtual Subject →
        build AuthzRequest → PdpEngine.authorize() → return SimulateResult

  3. Infrastructure Layer
      - Package `infrastructure/persistence/abac/uielement/`:
        `UIElementJpaEntity`, `UIElementMapper`, `UIElementJpaRepository`, `UIElementPersistenceAdapter`
      - Fix `ResourceDefinitionPersistenceAdapter.existsByIdWithUIElementRef()`:
        inject `UIElementJpaRepository` → `existsByResourceId(resourceId)`
      - Fix `ActionDefinitionPersistenceAdapter` nếu cần:
        check UIElement reference khi xoá action (inject UIElementJpaRepository)
      - SubjectProvider implementation: `infrastructure/adapter/abac/AdminSubjectProvider`
        Builds Subject từ `java.security.Principal` (JWT sub = userId)
        Load user roles từ UserRepository

  4. Presentation Layer
      - `UIElementController` tại `/api/v1/abac/ui-elements` (CRUD + batch evaluate)
        Batch evaluate: `POST /api/v1/abac/ui-elements/evaluate` — auth: JWT required
      - `AbacSimulateController` tại `/api/v1/abac/simulate`
        `POST /api/v1/abac/simulate` — auth: JWT required (SUPER_ADMIN)

Không implement: Visual Builder, Audit Log, Reverse Lookup (Phase 2/3).

Yêu cầu Handoff (Bắt buộc):
  Sau khi xong và `mvn clean compile -DskipTests` thành công:

  PHASE 1 COMPLETE CONTEXT BLOCK
    - Tất cả endpoints đã hoạt động (UC-019 → UC-024)
    - AdminPolicyProvider path + cách wire vào PdpEngine
    - Known limitations (trace chi tiết chưa có, chỉ trả decision raw)

  FRONTEND CONTEXT BLOCK
    - TypeScript interfaces: UIElementView, UIElementSummary, EvaluateRequest/Response, SimulateRequest/Response
    - Endpoints UIElement CRUD + batch evaluate + simulate
    - Error codes
    - UI notes: evaluate response là Map<string, "PERMIT"|"DENY">, simulate trace format
