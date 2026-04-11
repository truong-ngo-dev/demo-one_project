# Task 012: Admin Console — Phase 1 ABAC Policy Management

## Trạng thái
- [ ] Đang thiết kế

## Mục tiêu
Implement Phase 1 của ABAC Admin Console trong `services/admin`:
1. **Resource & Action Catalogue CRUD** — khai báo resource + action definitions
2. **Policy Set / Policy / Rule CRUD** (raw SpEL mode) — quản lý cây policy
3. **UIElement Registry CRUD** — ánh xạ UIElement → resource:action
4. **Simulate API** — endpoint test policy enforcement không cần deploy full stack

## Phạm vi Phase 1
Không bao gồm: Visual Policy Builder (Phase 2), Simulator UI (Phase 2), Impact Analysis (Phase 2), Audit Log (Phase 3).

## Definition of Done
- [ ] Flyway migrations cho 6 bảng: `resource_definition`, `action_definition`, `policy_set`, `policy`, `rule`, `expression`, `ui_element`
- [ ] Domain aggregates compile được
- [ ] CRUD API hoạt động cho Resource, PolicySet, Policy, Rule, UIElement
- [ ] `POST /api/v1/abac/simulate` trả PERMIT/DENY dựa trên PdpEngine với policy load từ DB
- [ ] Use case files đầy đủ: UC-019 → UC-024
- [ ] SERVICE_MAP.md và UC-000_index.md cập nhật

## Tham khảo
- Design: `docs/business_analysis/abac_admin_console_design.md`
- ABAC engine: `libs/abac/`
- Task 011 (libs/abac base): `tasks/011_libs_abac_base.md`

---

## Task 1 — DB Schema (Flyway Migration)

File: `services/admin/src/main/resources/db/migration/V{n}__abac_policy_schema.sql`

```sql
CREATE TABLE resource_definition (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    service_name VARCHAR(100) NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE action_definition (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id     BIGINT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    is_standard     BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_action_resource FOREIGN KEY (resource_id) REFERENCES resource_definition(id) ON DELETE CASCADE,
    CONSTRAINT uq_action_per_resource UNIQUE (resource_id, name)
);

CREATE TABLE expression (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    type                ENUM('LITERAL', 'COMPOSITION') NOT NULL,
    spel_expression     TEXT,
    combination_type    ENUM('AND', 'OR'),
    parent_id           BIGINT,
    CONSTRAINT fk_expression_parent FOREIGN KEY (parent_id) REFERENCES expression(id)
);

CREATE TABLE policy_set (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(200) NOT NULL UNIQUE,
    scope               ENUM('OPERATOR', 'TENANT') NOT NULL DEFAULT 'OPERATOR',
    combine_algorithm   VARCHAR(50) NOT NULL DEFAULT 'DENY_OVERRIDES',
    is_root             BOOLEAN NOT NULL DEFAULT FALSE,
    tenant_id           VARCHAR(100),
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE policy (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_set_id       BIGINT NOT NULL,
    name                VARCHAR(200) NOT NULL,
    target_expression_id BIGINT,
    combine_algorithm   VARCHAR(50) NOT NULL DEFAULT 'DENY_UNLESS_PERMIT',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_policy_policy_set FOREIGN KEY (policy_set_id) REFERENCES policy_set(id) ON DELETE CASCADE,
    CONSTRAINT fk_policy_target_expr FOREIGN KEY (target_expression_id) REFERENCES expression(id)
);

CREATE TABLE rule (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_id               BIGINT NOT NULL,
    name                    VARCHAR(200) NOT NULL,
    description             TEXT,
    target_expression_id    BIGINT,
    condition_expression_id BIGINT,
    effect                  ENUM('PERMIT', 'DENY') NOT NULL,
    order_index             INT NOT NULL DEFAULT 0,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_rule_policy FOREIGN KEY (policy_id) REFERENCES policy(id) ON DELETE CASCADE,
    CONSTRAINT fk_rule_target_expr FOREIGN KEY (target_expression_id) REFERENCES expression(id),
    CONSTRAINT fk_rule_condition_expr FOREIGN KEY (condition_expression_id) REFERENCES expression(id)
);

CREATE TABLE ui_element (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    element_id      VARCHAR(200) NOT NULL UNIQUE,
    label           VARCHAR(200) NOT NULL,
    type            ENUM('BUTTON', 'TAB', 'MENU_ITEM') NOT NULL,
    element_group   VARCHAR(100),
    order_index     INT NOT NULL DEFAULT 0,
    resource_id     BIGINT NOT NULL,
    action_id       BIGINT NOT NULL,
    CONSTRAINT fk_ui_element_resource FOREIGN KEY (resource_id) REFERENCES resource_definition(id),
    CONSTRAINT fk_ui_element_action FOREIGN KEY (action_id) REFERENCES action_definition(id)
);
```

---

## Task 2 — Domain Layer

Package: `vn.truongngo.apartcom.one.service.admin.domain`

### Aggregates / Entities

**`abac.resource.ResourceDefinition`** (Aggregate Root)
- Fields: `ResourceId id`, `String name`, `String description`, `String serviceName`, `List<ActionDefinition> actions`
- Invariant: name unique trong system, không xoá nếu có Policy / UIElement tham chiếu

**`abac.resource.ActionDefinition`** (Entity, thuộc ResourceDefinition)
- Fields: `ActionId id`, `ResourceId resourceId`, `String name`, `String description`, `boolean isStandard`

**`abac.policy.PolicySet`** (Aggregate Root)
- Fields: `PolicySetId id`, `String name`, `Scope scope`, `CombineAlgorithmName combineAlgorithm`, `boolean isRoot`, `String tenantId`

**`abac.policy.Policy`** (Aggregate Root — separate AR, không embed vào PolicySet)
- Fields: `PolicyId id`, `PolicySetId policySetId`, `String name`, `Expression targetExpression`, `CombineAlgorithmName combineAlgorithm`

**`abac.policy.Rule`** (Aggregate Root — separate AR, không embed vào Policy)
- Fields: `RuleId id`, `PolicyId policyId`, `String name`, `String description`, `Expression targetExpression`, `Expression conditionExpression`, `Effect effect`, `int orderIndex`

**`abac.policy.Expression`** (Value Object, embedded)
- Fields: `ExpressionType type` (LITERAL/COMPOSITION), `String spelExpression`, `CombinationType combinationType` (AND/OR), `Long parentId`

**`abac.uielement.UIElement`** (Aggregate Root)
- Fields: `UIElementId id`, `String elementId`, `String label`, `UIElementType type`, `String group`, `int orderIndex`, `ResourceId resourceId`, `ActionId actionId`

### Ports

- `ResourceDefinitionRepository` — CRUD + `findByName`, `existsByName`, `existsByIdWithPolicyRef`, `existsByIdWithUIElementRef`
- `PolicySetRepository` — CRUD + `findByName`, `existsByName`
- `PolicyRepository` — CRUD + `findAllByPolicySetId`, `findByName`
- `RuleRepository` — CRUD + `findAllByPolicyId` (ordered by `orderIndex`)
- `UIElementRepository` — CRUD + `findByResourceId`, `findWithoutPolicyRef`

### ErrorCode

`AbacErrorCode`:
- `RESOURCE_NOT_FOUND`, `RESOURCE_NAME_DUPLICATE`
- `ACTION_NOT_FOUND`, `ACTION_NAME_DUPLICATE`
- `POLICY_SET_NOT_FOUND`, `POLICY_SET_NAME_DUPLICATE`
- `POLICY_NOT_FOUND`
- `RULE_NOT_FOUND`
- `UI_ELEMENT_NOT_FOUND`, `UI_ELEMENT_ID_DUPLICATE`
- `RESOURCE_IN_USE` (khi xoá resource nhưng có policy/ui_element tham chiếu)

---

## Task 3 — Application Layer (Use Cases)

Xem chi tiết từng UC file:
- [UC-019](../services/admin/docs/use-cases/UC-019_resource_action_catalogue.md)
- [UC-020](../services/admin/docs/use-cases/UC-020_policy_set_management.md)
- [UC-021](../services/admin/docs/use-cases/UC-021_policy_management.md)
- [UC-022](../services/admin/docs/use-cases/UC-022_rule_management.md)
- [UC-023](../services/admin/docs/use-cases/UC-023_ui_element_registry.md)
- [UC-024](../services/admin/docs/use-cases/UC-024_policy_simulate.md)

Package structure theo vertical slice:
```
application/
├── resource/
│   ├── command/
│   │   ├── create_resource/   (CreateResourceDefinition)
│   │   ├── update_resource/   (UpdateResourceDefinition)
│   │   ├── delete_resource/   (DeleteResourceDefinition)
│   │   ├── add_action/        (AddActionToResource)
│   │   ├── update_action/     (UpdateActionDefinition)
│   │   └── remove_action/     (RemoveActionFromResource)
│   └── query/
│       ├── get_resource/      (GetResourceDefinition)
│       └── list_resources/    (ListResourceDefinitions)
├── policy_set/
│   ├── command/
│   │   ├── create_policy_set/ (CreatePolicySet)
│   │   ├── update_policy_set/ (UpdatePolicySet)
│   │   └── delete_policy_set/ (DeletePolicySet)
│   └── query/
│       ├── get_policy_set/    (GetPolicySet)
│       └── list_policy_sets/  (ListPolicySets)
├── policy/
│   ├── command/
│   │   ├── create_policy/
│   │   ├── update_policy/
│   │   └── delete_policy/
│   └── query/
│       ├── get_policy/
│       └── list_policies/     (by policySetId)
├── rule/
│   ├── command/
│   │   ├── create_rule/
│   │   ├── update_rule/
│   │   ├── delete_rule/
│   │   └── reorder_rules/     (UpdateRuleOrders)
│   └── query/
│       ├── get_rule/
│       └── list_rules/        (by policyId)
├── ui_element/
│   ├── command/
│   │   ├── create_ui_element/
│   │   ├── update_ui_element/
│   │   └── delete_ui_element/
│   └── query/
│       ├── get_ui_element/
│       └── list_ui_elements/  (by resourceId optional)
└── simulate/
    └── simulate_policy/       (UC-024 — SimulatePolicy)
```

---

## Task 4 — Infrastructure

### Persistence
Package `infrastructure/persistence/abac/`

JPA Entities mapping các bảng đã tạo. Dùng pattern giống `user/` và `role/`.

### AbacPolicyProvider (quan trọng)
Implement `vn.truongngo.apartcom.one.lib.abac.pip.PolicyProvider` trong admin service:

```java
@Component
public class AdminPolicyProvider implements PolicyProvider {
    // Inject RuleJpaRepository, PolicyJpaRepository, PolicySetJpaRepository
    // Load root PolicySet → Policies → Rules → Expressions
    // Build libs/abac domain objects (PolicySet, Policy, Rule, Expression)
    // Cache với Spring Cache nếu cần
    public AbstractPolicy getPolicy(String serviceName) { ... }
}
```

Đây là bridge từ DB → libs/abac engine. Cần ở cả `@PreEnforce` enforcement lẫn Simulate API.

---

## Task 5 — Presentation Layer

### Controllers

- `ResourceDefinitionController` — `/api/v1/abac/resources`
- `PolicySetController` — `/api/v1/abac/policy-sets`
- `PolicyController` — `/api/v1/abac/policies`
- `RuleController` — `/api/v1/abac/rules`
- `UIElementController` — `/api/v1/abac/ui-elements`
- `AbacSimulateController` — `/api/v1/abac/simulate`

Chi tiết endpoint xem UC files.

---

## Task 6 — Simulate API (UC-024)

Xem [UC-024](../services/admin/docs/use-cases/UC-024_policy_simulate.md) để biết contract đầy đủ.

Tóm tắt:
- `POST /api/v1/abac/simulate` — nhận virtual subject + resource + action + optional instance data
- Load policy từ DB qua `AdminPolicyProvider`
- Build `AuthzRequest` từ request body (không dùng JWT subject thực)
- Gọi `PdpEngine.authorize()`
- Trả `{ decision, details, timestamp }`

**Lưu ý**: Đây là test-only endpoint. Trong production deployment, cần bảo vệ bằng role SUPER_ADMIN.

---

## Thứ tự thực hiện đề xuất

```
Task 1 (Schema) → Task 2 (Domain) → Task 3+4 (App + Infra) → Task 5 (API)
                                                              → Task 6 (Simulate)
```
