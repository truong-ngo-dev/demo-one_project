Prompt: Batch 2 — PolicySet / Policy / Rule (UC-020, UC-021, UC-022)

Vai trò: Bạn là Senior Backend Engineer thực hiện Batch 2 của ABAC Policy Management trong `services/admin`.
Batch 1 đã xong (Resource & Action Catalogue). Batch 2 implement cây policy: PolicySet → Policy → Rule.

Tài liệu căn cứ:
  1. Task plan: @tasks/012_admin_phase1_abac_console.md (Task 2 scope policy, Task 3 scope policy, Task 4, Task 5)
  2. UC-020: @services/admin/docs/use-cases/UC-020_policy_set_management.md
  3. UC-021: @services/admin/docs/use-cases/UC-021_policy_management.md
  4. UC-022: @services/admin/docs/use-cases/UC-022_rule_management.md
  5. Convention: @docs/conventions/ddd-structure.md
  6. Service map: @services/admin/SERVICE_MAP.md

Context từ Batch 1:
  [BATCH 2 CONTEXT BLOCK — paste output từ Batch 1 BE vào đây]

Nhiệm vụ cụ thể:

  1. Domain Layer — package `domain/abac/policy/`
      - Value Object: `Expression` (type: LITERAL/COMPOSITION, spelExpression, combinationType: AND/OR)
      - Entity: `Rule` (id: Long, policyId, name, description, targetExpression, conditionExpression, effect: PERMIT/DENY, orderIndex)
      - Aggregate Root: `Policy` (id: Long, policySetId, name, targetExpression, combineAlgorithm)
      - Aggregate Root: `PolicySet` (id: Long, name, scope: OPERATOR/TENANT, combineAlgorithm, isRoot, tenantId)
      - Ports: `PolicySetRepository`, `PolicyRepository`, `RuleRepository`
      - Extend `AbacErrorCode` thêm: POLICY_SET_NOT_FOUND, POLICY_SET_NAME_DUPLICATE, POLICY_NOT_FOUND, RULE_NOT_FOUND
      - Pattern tham chiếu: `domain/abac/resource/ResourceDefinition.java` (Batch 1)
      - SpEL validation helper: parse check bằng `SpelExpressionParser` — dùng ở application layer

  2. Application Layer — slices trong `application/policy_set/`, `application/policy/`, `application/rule/`
      - PolicySet: create, update (scope/combineAlgorithm/isRoot), delete (preview trước khi xoá), get, list
        Guard isRoot: nếu set isRoot=true → unset isRoot của PolicySet hiện tại
      - Policy: create, update (targetExpression/combineAlgorithm), delete (preview), get, list-by-policySetId
        Validate spelExpression khi create/update
      - Rule: create, update, delete, get, list-by-policyId (ordered by orderIndex), reorder (UpdateRuleOrders)
        Validate spelExpression khi create/update
      - Pattern tham chiếu: `application/resource/` (Batch 1)

  3. Infrastructure Layer — package `infrastructure/persistence/abac/policy/`
      - JPA Entities: `PolicySetJpaEntity`, `PolicyJpaEntity`, `RuleJpaEntity`, `AbacExpressionJpaEntity`
      - Mappers + JPA Repos + Persistence Adapters cho PolicySet, Policy, Rule
      - Expression: lưu/load riêng qua `AbacExpressionJpaRepository`, không cascade từ Rule
        (Rule chứa targetExpressionId và conditionExpressionId — FK tới abac_expression)
      - Fix trong `ResourceDefinitionPersistenceAdapter.existsByIdWithPolicyRef()`:
        implement thật bằng query kiểm tra bảng `policy` (chưa có entity trong Batch 1, giờ có rồi)
      - Pattern tham chiếu: `infrastructure/persistence/abac/resource/` (Batch 1)

  4. AdminPolicyProvider — implement `PolicyProvider` từ `libs/abac`
      - Package: `infrastructure/adapter/abac/`
      - Class: `AdminPolicyProvider implements PolicyProvider`
      - Load PolicySet root (isRoot=true) → List<Policy> → List<Rule> → Expressions từ DB
      - Map sang libs/abac domain objects: `PolicySet`, `Policy`, `Rule`, `Expression`
      - Inject: `PolicySetJpaRepository`, `PolicyJpaRepository`, `RuleJpaRepository`, `AbacExpressionJpaRepository`
      - Dùng `CombineAlgorithmName.valueOf(entity.getCombineAlgorithm())` để map enum

  5. Presentation Layer
      - `PolicySetController` tại `/api/v1/abac/policy-sets` (CRUD + DELETE preview endpoint)
      - `PolicyController` tại `/api/v1/abac/policies` (CRUD + list-by-policySetId)
      - `RuleController` tại `/api/v1/abac/rules` (CRUD + list-by-policyId + reorder)
      - Pattern tham chiếu: `presentation/abac/ResourceDefinitionController.java` (Batch 1)

Không implement: UIElement, Simulate (Batch 3).

Yêu cầu Handoff (Bắt buộc):
  Sau khi xong và `mvn clean compile -DskipTests` thành công, cung cấp 2 block:

  BATCH 3 CONTEXT BLOCK
    - Files đã tạo theo layer
    - Package paths thực tế
    - Pattern decisions
    - AdminPolicyProvider class path đầy đủ
    - TODO còn lại (existsByIdWithUIElementRef)

  FRONTEND CONTEXT BLOCK
    - Base URLs cho 3 controllers
    - TypeScript interfaces đầy đủ (PolicySetView, PolicyView, RuleView, ExpressionView, ...)
    - Tất cả endpoints với method, path, request/response shape
    - Error codes + message gợi ý
    - UI notes: tree structure PolicySet→Policy→Rule, raw SpEL editor, reorder endpoint
