Prompt: Phase 3 Batch 1 (BE) — Evaluation Trace + Reverse Lookup (UC-033, UC-034)

Vai trò: Bạn là Senior Backend Engineer thực hiện Phase 3 Batch 1 của ABAC Admin Console.
Phase 2 đã hoàn thành (Visual Builder, Navigation Simulator, Impact Analysis).
Phase 3 tập trung vào Observability: admin thấy được TẠI SAO một decision được đưa ra.

Tài liệu căn cứ:
  1. Design: @docs/business_analysis/abac_admin_console_design.md (Section 5.2, 5.3)
  2. PdpEngine: @libs/abac/src/main/java/vn/truongngo/apartcom/one/lib/abac/pdp/PdpEngine.java
  3. PolicyEvaluators: @libs/abac/src/main/java/vn/truongngo/apartcom/one/lib/abac/evaluation/PolicyEvaluators.java
  4. EvaluationContext: @libs/abac/src/main/java/vn/truongngo/apartcom/one/lib/abac/context/EvaluationContext.java
  5. SimulatePolicy: @services/admin/src/main/java/vn/truongngo/apartcom/one/service/admin/application/simulate/simulate_policy/SimulatePolicy.java
  6. SimulateNavigation: @services/admin/src/main/java/vn/truongngo/apartcom/one/service/admin/application/simulate/simulate_navigation/SimulateNavigation.java
  7. AdminPolicyProvider: @services/admin/src/main/java/vn/truongngo/apartcom/one/service/admin/infrastructure/adapter/abac/AdminPolicyProvider.java
  8. Rule domain: @libs/abac/src/main/java/vn/truongngo/apartcom/one/lib/abac/domain/Rule.java
  9. Convention: @docs/conventions/ddd-structure.md

Context hiện tại (đọc kỹ trước khi bắt đầu):
  - `PdpEngine.authorize()` trả về `AuthzDecision { decision, timestamp, details }`.
    `details` hiện tại = `IndeterminateCause` nếu indeterminate, `"No policy applicable"` nếu N/A,
    `null` nếu normal.
  - `PolicyEvaluators.ruleEvaluator` evaluate từng `Rule` — có target + condition eval logic đầy đủ,
    nhưng không thu thập trace.
  - `Rule` (libs/abac domain) có field `id` (String) — AdminPolicyProvider map từ
    `RuleDefinition.getId().getValue().toString()`.
  - `EvaluationContext` chứa subject, resource, action, environment — chưa có trace collector.

---

## UC-033 — Evaluation Trace

### Mục đích
Admin chạy Instance Simulation và thấy được: rule nào được evaluate, target/condition có match hay không,
rule nào quyết định final decision.

### Nhiệm vụ

  1. Thêm trace support vào `libs/abac`

     a. Tạo `libs/abac/.../evaluation/RuleTraceEntry.java`:
        ```java
        public record RuleTraceEntry(
            String ruleId,           // Rule.id
            String ruleDescription,  // Rule.description
            Rule.Effect effect,
            boolean targetMatched,
            Boolean conditionMatched, // null nếu target không match → condition không evaluate
            boolean wasDeciding       // true nếu rule này quyết định final decision
        ) {}
        ```

     b. Thêm vào `EvaluationContext`:
        ```java
        private final List<RuleTraceEntry> traceEntries = new ArrayList<>();
        private boolean tracingEnabled = false;

        public void enableTracing() { this.tracingEnabled = true; }
        public boolean isTracingEnabled() { return tracingEnabled; }
        public void addTraceEntry(RuleTraceEntry entry) { traceEntries.add(entry); }
        public List<RuleTraceEntry> getTraceEntries() { return Collections.unmodifiableList(traceEntries); }
        ```

     c. Sửa `PolicyEvaluators.ruleEvaluator`: khi `context.isTracingEnabled()`, sau khi evaluate rule,
        append `RuleTraceEntry` vào context:
        - `targetMatched` = `target.isMatch()`
        - `conditionMatched` = null nếu target không match, else `condition.isMatch()`
        - `wasDeciding` = true nếu result là PERMIT hoặc DENY (không phải NOT_APPLICABLE)
        Không thay đổi logic evaluation — chỉ collect side-effect.

     d. Sửa `PdpEngine.authorize()`: nếu `tracingEnabled`, include trace trong details:
        ```java
        // wrap details: nếu có trace, bọc vào object chứa cả trace + original details
        // dùng Map<String, Object> hoặc tạo record EvaluationDetails { Object cause; List<RuleTraceEntry> trace }
        ```

  2. Sửa `SimulatePolicy.Handler` trong `services/admin`:
     - Thêm optional param `traceEnabled = true` (mặc định bật cho admin console)
     - Build `AuthzRequest` với `context.enableTracing()`
     - Thêm `List<RuleTraceEntry>` vào `SimulateResult`:
       ```java
       public record SimulateResult(
           String decision,
           Long timestamp,
           Long policySetId,
           String policySetName,
           Object details,          // giữ nguyên
           List<RuleTraceEntry> trace // mới
       ) {}
       ```
     - Map từ `AuthzDecision.details` (EvaluationDetails) → extract trace list

  3. Sửa `SimulateNavigation.ActionDecision` để thêm `matchedRuleName`:
     ```java
     public record ActionDecision(
         String action,
         String decision,
         String matchedRuleName   // description của rule quyết định, null nếu không có
     ) {}
     ```
     Extract từ trace: lấy `RuleTraceEntry.ruleDescription` của entry đầu tiên có `wasDeciding = true`.

  4. UC Doc:
     - Tạo `UC-033_evaluation_trace.md`
     - Cập nhật `UC-000_index.md`

  5. Không cần endpoint riêng — trace trả về inline trong `/api/v1/abac/simulate` response.

---

## UC-034 — Reverse Lookup

### Mục đích
Admin hỏi: "Ai có quyền làm gì với resource X, action Y?"
System trả về: danh sách PERMIT rules match, subject conditions trích được, số users ước tính.

### Nhiệm vụ

  1. Application Layer — `application/simulate/reverse_lookup/GetReverseLookup.java`
     - Query:
       ```java
       public record Query(String resourceName, String actionName, Long policySetId) {}
       ```
     - Result:
       ```java
       public record RuleCoverage(
           Long ruleId,
           String ruleName,
           String policyName,
           String effect,              // "PERMIT" | "DENY"
           List<String> requiredRoles,
           List<String> requiredAttributes,
           boolean hasInstanceCondition,
           Integer userCountByRole,    // null nếu không tính được
           String userCountNote        // "Exact count unavailable — instance conditions apply" nếu hasInstanceCondition
       ) {}
       public record Result(
           String resourceName,
           String actionName,
           List<RuleCoverage> permitRules,
           List<RuleCoverage> denyRules
       ) {}
       ```
     - Handler logic:
       1. Load tất cả `PolicyDefinition` từ `PolicyRepository.findAll()` (hoặc filter by policySetId nếu set)
       2. Với mỗi `RuleDefinition` trong mỗi policy:
          a. Parse `targetExpression` bằng `GetRuleImpactPreview.Handler` logic (tái dùng hoặc extract SpEL AST walker)
          b. Check nếu `specificActions.contains(actionName)` — nếu không có specificActions (rule áp dụng tất cả actions) → vẫn include
          c. Extract `requiredRoles`, `requiredAttributes`, `hasInstanceCondition`
       3. Với mỗi role trong `requiredRoles`: query `UserRepository.countByRole(roleName)` → `userCountByRole`
          - Nếu `hasInstanceCondition = true` → thêm note
          - Nếu `requiredRoles` rỗng (không giới hạn role) → `userCountByRole = null`, note = "All users"
       4. Phân nhóm thành `permitRules` và `denyRules`
     - Inject: `PolicyRepository`, `UserRepository`, (SpEL AST walker — tái dùng logic từ GetRuleImpactPreview)

     Lưu ý tái dùng SpEL AST walker: extract walker logic ra private static helper hoặc package-private
     utility để GetRuleImpactPreview và GetReverseLookup đều dùng được.
     KHÔNG tạo Spring bean riêng cho walker — dùng static helper.

  2. Presentation Layer — thêm vào `AbacSimulateController.java`:
     ```
     GET /api/v1/abac/simulate/reverse?resourceName=employee&actionName=READ&policySetId=1
     ```
     - Response: `ApiResponse<GetReverseLookup.Result>`
     - `policySetId` optional

  3. UC Doc:
     - Tạo `UC-034_reverse_lookup.md`
     - Cập nhật `UC-000_index.md`

---

## Yêu cầu kỹ thuật chung
  - `libs/abac` thay đổi → phải chạy `mvn clean compile -DskipTests` cho cả libs/abac VÀ services/admin
  - Không thay đổi behavior của `PolicyEvaluators` — trace chỉ là side-effect, không ảnh hưởng result
  - `UserRepository` đã có `findByRole` hoặc query — kiểm tra trước khi implement; nếu chưa có, thêm method

---

## Yêu cầu Handoff (Bắt buộc)

Sau khi `mvn clean compile -DskipTests` thành công cho cả admin và libs/abac:

  PHASE 3 BATCH 1 BE CONTEXT BLOCK
    - Shape thực tế của `SimulateResult` sau khi thêm trace
    - Shape thực tế của `ActionDecision` sau khi thêm matchedRuleName
    - UC-034 endpoint: GET /api/v1/abac/simulate/reverse?...

  FRONTEND CONTEXT BLOCK — Phase 3 Batch 1
    TypeScript interfaces:
      RuleTraceEntry {
        ruleId: string;
        ruleDescription: string | null;
        effect: 'PERMIT' | 'DENY';
        targetMatched: boolean;
        conditionMatched: boolean | null;
        wasDeciding: boolean;
      }
      SimulateResult {
        decision: 'PERMIT' | 'DENY';
        timestamp: number;
        policySetId: number | null;
        policySetName: string | null;
        details: unknown;
        trace: RuleTraceEntry[];
      }
      NavigationActionDecision {
        action: string;
        decision: 'PERMIT' | 'DENY';
        matchedRuleName: string | null;  // mới
      }
      RuleCoverage {
        ruleId: number;
        ruleName: string;
        policyName: string;
        effect: 'PERMIT' | 'DENY';
        requiredRoles: string[];
        requiredAttributes: string[];
        hasInstanceCondition: boolean;
        userCountByRole: number | null;
        userCountNote: string | null;
      }
      ReverseLookupResult {
        resourceName: string;
        actionName: string;
        permitRules: RuleCoverage[];
        denyRules: RuleCoverage[];
      }

    Endpoints:
      POST /api/admin/v1/abac/simulate → SimulateResult (trace array added)
      GET  /api/admin/v1/abac/simulate/reverse?resourceName=X&actionName=Y → ReverseLookupResult
