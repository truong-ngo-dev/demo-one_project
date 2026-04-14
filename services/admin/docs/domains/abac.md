# Domain: ABAC (Attribute-Based Access Control)

## Mô tả
ABAC cung cấp toàn bộ hạ tầng quản trị chính sách phân quyền dựa trên thuộc tính.
Bao gồm: Resource/Action Catalogue, PolicySet/Policy/Rule hierarchy, UIElement Registry, Policy Simulator (navigation + instance + reverse lookup), và Admin Audit Log.

Tích hợp với `libs/abac` — thư viện PDP engine nội bộ thực hiện đánh giá SpEL expression.

**Phases đã implement:**
- **Phase 1 (Core)**: Resource/Action, PolicySet/Policy/Rule CRUD, UIElement Registry, Basic Simulator
- **Phase 2 (Usability)**: Navigation Simulate, Rule Impact Preview (SpEL AST analysis)
- **Phase 3 (Observability)**: Instance Trace, Reverse Lookup, Admin Change Audit Log, UIElement Coverage

---

## Trách nhiệm
- Quản lý vòng đời Resource + Action (namespace định danh cho policy).
- Quản lý PolicySet → Policy → Rule hierarchy (định nghĩa access control logic).
- Quản lý UIElement Registry — map từng phần tử UI đến một Action cụ thể + coverage check.
- Cung cấp `AdminPolicyProvider` bridge: tải policy từ DB → libs/abac PdpEngine để evaluate.
- Cung cấp simulate API cho admin test chính sách (navigation, instance+trace, reverse lookup).
- Ghi và query Admin Change Audit Log — mọi thay đổi policy/rule/UIElement.

## Không thuộc trách nhiệm
- Không enforce policy tại request boundary — đó là trách nhiệm của Resource Server (PepEngine).
- Không quản lý user identity hay roles — dùng thông tin từ User/Role domain.

---

## Cấu trúc Domain

### Package: `domain/abac/resource/`

```
ResourceDefinition (Aggregate Root)
├── ResourceId          (Value Object — Long auto-increment)
├── name                (String — immutable sau khi tạo)
├── description         (String — optional)
├── serviceName         (String — service owner)
└── actions             (List<ActionDefinition> — owned collection)

ActionDefinition (Entity — owned by ResourceDefinition)
├── ActionId            (Value Object — Long)
├── resourceId          (ResourceId — parent)
├── name                (String — unique per resource, immutable)
├── description         (String)
└── isStandard          (boolean)

ResourceErrorCode   (per-aggregate error codes: 30001–30006)
ResourceException   (DomainException factory: resourceNotFound, resourceNameDuplicate, resourceInUse, actionNotFound, actionNameDuplicate, actionInUse)
```

**Invariants**:
- `ResourceDefinition.name` immutable — SpEL expressions reference resource.name by value.
- `ActionDefinition.name` immutable — UIElement references action by name.
- Không xóa Resource nếu có UIElement reference.
- Không xóa Action nếu có UIElement reference.

---

### Package: `domain/abac/policy_set/`

```
PolicySetDefinition (Aggregate Root)
├── PolicySetId         (Value Object — Long)
├── name                (String — unique, immutable)
├── scope               (Enum: ADMIN | OPERATOR | TENANT | RESIDENT)
├── combineAlgorithm    (CombineAlgorithmName — từ libs/abac)
├── isRoot              (boolean — system-wide active policy set)
└── tenantId            (String — nullable, chỉ dùng khi scope=TENANT)

Scope               ADMIN | OPERATOR | TENANT | RESIDENT
PolicySetId         (Value Object — Long)
PolicySetRepository (domain interface)
PolicySetErrorCode  (per-aggregate error codes: 30007–30008)
PolicySetException  (DomainException factory: policySetNotFound, policySetNameDuplicate)
```

---

### Package: `domain/abac/policy/`

```
PolicyDefinition (Aggregate Root — owns RuleDefinition)
├── PolicyId            (Value Object — Long)
├── policySetId         (PolicySetId — parent reference, from policy_set/)
├── name                (String — immutable)
├── targetExpression    (ExpressionVO — nullable, SpEL filter)
├── combineAlgorithm    (CombineAlgorithmName)
└── rules               (List<RuleDefinition> — owned, ordered)

RuleDefinition (Entity — owned by PolicyDefinition)
├── RuleId              (Value Object — Long)
├── policyId            (PolicyId — parent)
├── name                (String)
├── description         (String — optional)
├── targetExpression    (ExpressionVO — nullable)
├── conditionExpression (ExpressionVO — nullable, main auth logic)
├── effect              (Enum: PERMIT | DENY)
└── orderIndex          (int — for rule evaluation order)

ExpressionVO (Value Object — Phase 1: LITERAL only)
├── id                  (Long — DB FK to abac_expression)
└── spelExpression      (String — raw SpEL string)

PolicyErrorCode     (per-aggregate error codes: 30009–30011)
PolicyException     (DomainException factory: policyNotFound, ruleNotFound, invalidSpelExpression)
```

**Invariants**:
- `PolicyDefinition` là aggregate root của `RuleDefinition` — rule chỉ được thêm/sửa/xóa qua Policy.
- `reorderRules()` — tất cả ruleId phải thuộc về policy này.
- Phase 1: chỉ hỗ trợ LITERAL expression (SpEL thuần). COMPOSITION reserved cho Phase 2.

---

### Package: `domain/abac/uielement/`

```
UIElement (Aggregate Root)
├── id              (UIElementId — typed Value Object wrapping Long)
├── elementId       (String — unique, immutable — frontend hardcodes this)
├── label           (String — display label)
├── type            (Enum: BUTTON | TAB | MENU_ITEM)
├── scope           (Enum: ADMIN | OPERATOR | TENANT | RESIDENT — portal owner)
├── elementGroup    (String — optional, tên nhóm/màn hình chứa element)
├── orderIndex      (int)
├── resourceId      (ResourceId — FK to resource_definition)
└── actionId        (ActionId — FK to action_definition)

UIElementId         (Value Object — Long)
UIElementErrorCode  (per-aggregate error codes: 30012–30013)
UIElementException  (DomainException factory: uiElementNotFound, uiElementIdDuplicate)
```

**`elementGroup`**: Metadata phân nhóm, không ảnh hưởng đến ABAC evaluation. Dùng để admin filter/quản lý element theo màn hình (ví dụ: `user-detail-actions`, `admin-nav`). Frontend evaluate theo `elementId`, không theo group.

**Invariants**:
- `elementId` immutable sau khi tạo — frontend dùng để lookup visibility.
- `scope` xác định portal nào sở hữu element — mặc định `ADMIN` nếu không chỉ định khi tạo.
- `actionId` phải thuộc về `resourceId` — cross-validated khi create/update.
- Không xóa Resource/Action nếu có UIElement reference.

---

### Package: `domain/abac/audit/`

```
AbacAuditLog (plain POJO — không có JPA annotation, thuần domain object)
├── id              (Long — auto-increment)
├── entityType      (AuditEntityType — POLICY_SET/POLICY/RULE/UI_ELEMENT)
├── entityId        (Long)
├── entityName      (String — nullable, tên lúc thực hiện hành động)
├── actionType      (AuditActionType — CREATED/UPDATED/DELETED)
├── performedBy     (String — username từ SecurityContext)
├── changedAt       (long — epoch millis)
└── snapshotJson    (String — nullable, JSON snapshot trạng thái sau thay đổi)

AbacAuditLogRepository  (domain interface — không extends JpaRepository)
  JPA entity: infrastructure/persistence/abac/audit/AbacAuditLogJpaEntity
  Spring Data repo: infrastructure/persistence/abac/audit/AbacAuditLogJpaRepository
  Adapter: infrastructure/adapter/repository/abac/AbacAuditLogPersistenceAdapter

AbacAuditLogEvent (extends AbstractDomainEvent — NOT record)
  Dispatched bởi tất cả command handlers; handled bởi AuditLogEventHandler

AuditActionType   CREATED | UPDATED | DELETED
AuditEntityType   POLICY_SET | POLICY | RULE | UI_ELEMENT
```

**Snapshot patterns** (per entity type):
- `POLICY_SET`: `{name, combineAlgorithm, isRoot}`
- `POLICY`: `{name, combineAlgorithm}`
- `RULE`: `{name, effect, targetExpression, conditionExpression}`
- `UI_ELEMENT`: `{elementId, label, type, resourceId, actionId}`
- `DELETED` actions: `snapshotJson = null`

---

## AdminPolicyProvider

Bridge từ DB domain → libs/abac PdpEngine:

```
AdminPolicyProvider implements PolicyProvider
  getPolicy(serviceName: String) → AbstractPolicy
    1. Load root PolicySetDefinition (isRoot=true)
    2. Load PolicyDefinitions of root PolicySet
    3. Load RuleDefinitions of each Policy (embedded)
    4. Map: PolicySetDefinition → libs/abac.PolicySet
           PolicyDefinition → libs/abac.Policy
           RuleDefinition → libs/abac.Rule
           ExpressionVO → libs/abac.Expression (type=LITERAL)
    5. Return mapped PolicySet
```

Phase 1: Chỉ có 1 root PolicySet. `serviceName` bị bỏ qua — luôn trả first root.

---

## SpelExpressionAnalyzer

Static helper tại `application/rule/service/SpelExpressionAnalyzer.java`. Walk SpEL AST để phân tích nội dung expression.

```
SpelExpressionAnalyzer.analyze(String... expressions) → AnalysisResult
  requiredRoles         List<String>   — roles được reference (subject.roles.contains(...))
  requiredAttributes    List<String>   — attributes được reference (subject.getAttribute(...))
  specificActions       List<String>   — actions được check (action.getAttribute('name') == ...)
  navigableWithoutData  boolean        — có pattern object.data == null
  hasInstanceCondition  boolean        — có dùng object.data.*
  parseWarning          String         — lỗi parse nếu expression không hợp lệ
```

Dùng bởi:
- `GetRuleImpactPreview` — phân tích impact trước khi save rule
- `GetReverseLookup` — tìm rules cover resource+action
- `ListUIElements`, `GetUIElement`, `ListUncoveredUIElements` — coverage check

**Lưu ý AST**:
- `CompoundExpression[a, b, method()]` → `a.b.method()` — MethodReference children = ARGUMENTS, không phải receiver
- SpEL parser: `SpelExpressionParser` → `SpelExpression.getAST()`

---

## PdpEngine — Trace Mode

`libs/abac.PdpEngine` có method `authorizeWithTrace()` thêm vào Phase 3:

```
authorizeWithTrace(AuthzRequest) → AuthzDecision
  AuthzDecision.details → EvaluationDetails
    cause        Object           — nguyên nhân nếu INDETERMINATE/NOT_APPLICABLE
    trace        List<RuleTraceEntry>
      ruleId           String
      ruleDescription  String
      effect           Rule.Effect
      targetMatched    boolean
      conditionMatched Boolean    — null nếu target không match
      wasDeciding      boolean    — true nếu rule này quyết định kết quả
```

`SimulatePolicy` dùng `authorizeWithTrace()`, trả `trace` về FE.
`SimulateNavigation` dùng trace để lấy `matchedRuleName` cho mỗi action.

---

## AdminSubjectProvider

Bridge từ JWT Principal → libs/abac Subject:

```
AdminSubjectProvider implements SubjectProvider
  getSubject(Principal principal) → Subject
    1. principal.getName() → userId (String)
    2. Load User từ UserRepository
    3. Load Role names từ RoleRepository (findAllByIds)
    4. Build Subject { userId, roles: [roleName], attributes: {} }
```

---

## Error Codes

| Code  | HTTP | Enum                       | Mô tả                                   |
|-------|------|----------------------------|-----------------------------------------|
| 30001 | 404  | RESOURCE_NOT_FOUND         |                                         |
| 30002 | 409  | RESOURCE_NAME_DUPLICATE    | Resource name đã tồn tại                |
| 30003 | 409  | RESOURCE_IN_USE            | Resource đang được UIElement tham chiếu |
| 30004 | 404  | ACTION_NOT_FOUND           |                                         |
| 30005 | 409  | ACTION_NAME_DUPLICATE      | Action name đã tồn tại trong resource   |
| 30006 | 409  | ACTION_IN_USE              | Action đang được UIElement tham chiếu   |
| 30007 | 404  | POLICY_SET_NOT_FOUND       |                                         |
| 30008 | 409  | POLICY_SET_NAME_DUPLICATE  | Policy set name đã tồn tại              |
| 30009 | 404  | POLICY_NOT_FOUND           |                                         |
| 30010 | 404  | RULE_NOT_FOUND             |                                         |
| 30011 | 400  | INVALID_SPEL_EXPRESSION    | SpEL expression sai cú pháp             |
| 30012 | 404  | UI_ELEMENT_NOT_FOUND       |                                         |
| 30013 | 409  | UI_ELEMENT_ID_DUPLICATE    | elementId đã tồn tại                    |

---

## Quan hệ

| Domain  | Quan hệ                                                                     |
|---------|-----------------------------------------------------------------------------|
| User    | Subject.userId + Subject.roles từ User/Role domain khi evaluate policy      |
| libs/abac | PdpEngine evaluate AuthzRequest; AdminPolicyProvider cung cấp AbstractPolicy |

---

## Tham khảo
- [UC-019 Resource/Action](../use-cases/UC-019_resource_action_catalogue.md)
- [UC-020 PolicySet](../use-cases/UC-020_policy_set_management.md)
- [UC-021 Policy](../use-cases/UC-021_policy_management.md)
- [UC-022 Rule](../use-cases/UC-022_rule_management.md)
- [UC-023 UIElement](../use-cases/UC-023_ui_element_registry.md)
- [UC-024 Simulate](../use-cases/UC-024_policy_simulate.md)
- [UC-031 Navigation Simulate](../use-cases/UC-031_navigation_simulate.md)
- [UC-032 Rule Impact Preview](../use-cases/UC-032_rule_impact_preview.md)
- [UC-033 Evaluation Trace](../use-cases/UC-033_evaluation_trace.md)
- [UC-034 Reverse Lookup](../use-cases/UC-034_reverse_lookup.md)
- [UC-035 Admin Change Audit Log](../use-cases/UC-035_admin_change_audit_log.md)
- [UC-036 UIElement Policy Coverage](../use-cases/UC-036_ui_element_policy_coverage.md)
