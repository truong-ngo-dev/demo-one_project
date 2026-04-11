Prompt: Phase 2 Batch 1 (BE) — Navigation Simulator (UC-030)

Vai trò: Bạn là Senior Backend Engineer thực hiện Phase 2 Batch 1 của ABAC Admin Console trong `services/admin`.
Phase 1 (UC-019 → UC-024) đã hoàn thành. Phase 2 tập trung vào usability: admin tự quản lý policy
mà không cần developer. Batch 1 implement Navigation Simulate — đánh giá toàn bộ actions của một
resource cho một subject.

Tài liệu căn cứ:
  1. Design: @docs/business_analysis/abac_admin_console_design.md (Section 5.1 — Navigation Simulation)
  2. Phase 1 Simulate pattern: @services/admin/src/main/java/vn/truongngo/apartcom/one/service/admin/application/simulate/simulate_policy/SimulatePolicy.java
  3. AdminPolicyProvider: @services/admin/src/main/java/vn/truongngo/apartcom/one/service/admin/infrastructure/adapter/abac/AdminPolicyProvider.java
  4. Convention: @docs/conventions/ddd-structure.md
  5. Service map: @services/admin/SERVICE_MAP.md

Context từ Phase 1:
  - SimulatePolicy.java: `POST /api/v1/abac/simulate` — evaluate single resource + action
  - PdpEngine: `authorize(AuthzRequest)` → `AuthzDecision { decision, timestamp, details }`
  - AdminPolicyProvider: `getPolicy("admin-service")` → loads root PolicySet từ DB
  - ResourceDefinitionRepository: `findByName(name)` → `Optional<ResourceDefinition>`
    (ResourceDefinition có `List<ActionDefinition> actions` với field `name`)
  - PolicySetRepository: `findById(id)`, `findAllRoot()`

Nhiệm vụ cụ thể:

  1. UC Doc
      - Tạo `services/admin/docs/use-cases/UC-030_navigation_simulate.md`
      - Mô tả: evaluate tất cả actions của một resource cho một virtual subject
      - Input/output contract, error cases

  2. Application Layer — `application/simulate/simulate_navigation/SimulateNavigation.java`
      - Command:
        ```java
        public record Command(
            SimulatePolicy.SimulateSubjectRequest subject,
            String resourceName,
            Long policySetId          // null → dùng root
        ) {}
        ```
      - Result:
        ```java
        public record ActionDecision(String action, String decision) {}
        public record Result(
            String resourceName,
            Long policySetId,
            String policySetName,
            List<ActionDecision> decisions
        ) {}
        ```
      - Handler logic:
        1. Tìm ResourceDefinition theo resourceName → throw AbacException.resourceNotFound() nếu không có
        2. Load PolicySet: nếu policySetId != null → findById, else findAllRoot().get(0)
        3. `AbstractPolicy policy = adminPolicyProvider.getPolicy("admin-service")`
        4. Build Subject từ command.subject (pattern giống SimulatePolicy.Handler)
        5. For each ActionDefinition trong resource:
           - `Action action = Action.semantic(actionDef.getName())`
           - `Resource resourceCtx = new Resource(resourceName, null)` — navigation level: null data
           - `AuthzRequest request = new AuthzRequest(subject, resourceCtx, action, new Environment(), policy)`
           - `AuthzDecision decision = pdpEngine.authorize(request)`
           - Collect `ActionDecision(actionDef.getName(), decision.isPermit() ? "PERMIT" : "DENY")`
        6. Return Result với list đã sort theo action name (alphabetical)
      - Inject: `ResourceDefinitionRepository`, `PolicySetRepository`, `PdpEngine`, `AdminPolicyProvider`

  3. Presentation Layer — thêm vào `AbacSimulateController.java`
      - Thêm endpoint: `POST /api/v1/abac/simulate/navigation`
      - Request body:
        ```java
        record NavigationSimulateRequest(
            SimulatePolicy.SimulateSubjectRequest subject,
            String resourceName,
            Long policySetId           // optional
        ) {}
        ```
      - Response: `ApiResponse<SimulateNavigation.Result>`

  4. UC Index — cập nhật `services/admin/docs/use-cases/UC-000_index.md`
      - Thêm UC-030 vào table ABAC, trạng thái Implemented

Không implement: Instance trace (Phase 3), rule name resolution (Phase 3 — PdpEngine hiện không
trả rule name trong details).

Yêu cầu Handoff (Bắt buộc):
  Sau khi xong và `mvn clean compile -DskipTests` thành công, cung cấp:

  PHASE 2 BATCH 1 CONTEXT BLOCK
    - Package path thực tế của SimulateNavigation.Handler
    - Endpoint shape đầy đủ (method, path, request, response)
    - Known limitations: không có matchedRuleName (Phase 3)

  FRONTEND CONTEXT BLOCK — Phase 2 Batch 1
    TypeScript interfaces:
      NavigationSimulateRequest { subject, resourceName, policySetId? }
      NavigationActionDecision { action: string; decision: 'PERMIT' | 'DENY' }
      NavigationSimulateResult { resourceName, policySetId, policySetName, decisions }

    Endpoint: POST /api/admin/v1/abac/simulate/navigation → ApiResponse<NavigationSimulateResult>

    UI notes:
      - decisions array sorted alphabetically by action name
      - policySetId null trong response → no root policy set configured (DENY tất cả)
      - Dùng cùng subject form với UC-024 Simulator (Virtual User pattern)
