# SERVICE_MAP — admin-service

> **First entry point cho AI agents.** Đọc file này trước khi dùng bất kỳ công cụ tìm kiếm nào.
> Chi tiết convention xem tại [`docs/conventions/ddd-structure.md`](../../docs/conventions/ddd-structure.md).

---

## 📂 1. Domain Layer (`domain/`)

Chi tiết từng domain xem tại docs riêng:

| Domain                                                    | File                                         |
|-----------------------------------------------------------|----------------------------------------------|
| **user**                                                  | [docs/domains/user.md](docs/domains/user.md) |
| **role**                                                  | [docs/domains/role.md](docs/domains/role.md) |
| **abac** (resource, policy_set, policy, uielement, audit) | [docs/domains/abac.md](docs/domains/abac.md) |
| **reference** (building)                                  | (External reference data from property-service) |

---

## 🚀 2. Application Layer (`application/`)

### user — Commands

- `register/RegisterUser`: UC-002 — User tự đăng ký bằng email + password. Validate email/username unique, hash password, gán default role nếu có. **Command**
- `admin_create/AdminCreateUser`: UC-001 — Admin tạo user, chỉ định roles ngay lúc tạo. **Command**
- `social_register/SocialRegisterUser`: UC-003 — Tạo hoặc tìm user từ social provider (find-or-create). Trả `requiresProfileCompletion=true` nếu user mới. **Command**
- `lock/LockUser`: UC-007a — Admin lock user. Guard: không lock user đã LOCKED. **Command**
- `unlock/UnlockUser`: UC-007b — Admin unlock user. Guard: không unlock user đang ACTIVE. **Command**
- `assign_roles/AssignRoles`: UC-013 — Gán thêm roles cho user (additive, không replace). **Command**
- `remove_role/RemoveRole`: UC-014 — Gỡ một role khỏi user. **Command**
- `update_profile/UpdateProfile`: UC-016 — User cập nhật username (1 lần), fullName, phoneNumber. **Command**
- `change_password/ChangePassword`: UC-017 — User đổi password, verify password hiện tại trước. **Command**

### user — Queries

- `find_by_id/FindUserById`: UC-004 — Tìm user theo ID, trả detail bao gồm roles. **Query**
- `search/SearchUsers`: UC-006 — Tìm kiếm có phân trang với filter keyword/status/roleId. Đi qua `UserRepository.findAll()` với JPQL dynamic query. **Query**
- `get_my_profile/GetMyProfile`: — Lấy profile của user hiện tại theo userId từ JWT. **Query**

### user — Internal (service-to-service)

- `find_by_identity/`: UC-U05 — Tìm user theo email hoặc username (auto-detect), trả `UserView` kèm hashed password + roles. Phục vụ oauth2 service cho form login.
    - `GetUserByIdentityHandler`, `GetUserByIdentityQuery`, `UserView`, `UserViewMapper`
    - *Lưu ý*: slice này dùng standalone classes thay vì outer-class pattern — exception vì là internal handler, không phải use case nghiệp vụ.

### role — Commands

- `create/CreateRole`: UC-008 — Tạo role mới. Validate tên unique. **Command**
- `update/UpdateRole`: UC-011 — Cập nhật description của role. **Command**
- `delete/DeleteRole`: UC-012 — Xóa role. Guard: không xóa nếu có user đang dùng role này. **Command**

### role — Queries

- `find_by_id/FindRoleById`: UC-009 — Tìm role theo ID. **Query**
- `find_all/FindAllRoles`: UC-010 — Danh sách roles có phân trang + filter keyword. **Query**

### reference — Queries

- `get_all_buildings/GetAllBuildings`: Lấy danh sách tất cả các tòa nhà từ `building_reference`. Phục vụ dropdown gán Operator và quản lý tòa nhà. **Query**

### abac/resource — Commands *(UC-019)*

- `create_resource/CreateResourceDefinition`: Tạo resource mới, validate name unique. **Command**
- `update_resource/UpdateResourceDefinition`: Cập nhật description + serviceName. **Command**
- `delete_resource/DeleteResourceDefinition`: Xóa resource — guard: không xóa nếu có UIElement ref. **Command**
- `add_action/AddActionToResource`: Thêm action vào resource, validate name unique trong resource. **Command**
- `update_action/UpdateActionDefinition`: Cập nhật description của action. **Command**
- `remove_action/RemoveActionFromResource`: Xóa action — guard: không xóa nếu có UIElement ref. **Command**

### abac/resource — Queries *(UC-019)*

- `get_resource/GetResourceDefinition`: Lấy resource theo ID kèm danh sách actions. **Query**
- `list_resources/ListResourceDefinitions`: Tìm kiếm có phân trang + filter keyword. **Query**

### abac/policy_set — Commands *(UC-020)*

- `create_policy_set/CreatePolicySet`: Tạo PolicySet mới, validate name unique. Dispatch `AbacAuditLogEvent(POLICY_SET/CREATED)`. **Command**
- `update_policy_set/UpdatePolicySet`: Cập nhật scope, combineAlgorithm, isRoot. Dispatch `AbacAuditLogEvent(POLICY_SET/UPDATED)`. **Command**
- `delete_policy_set/DeletePolicySet`: Load entity trước khi xóa. Dispatch `AbacAuditLogEvent(POLICY_SET/DELETED)`. **Command**

### abac/policy_set — Queries *(UC-020)*

- `get_policy_set/GetPolicySet`: Lấy PolicySet theo ID kèm danh sách policy summary. **Query**
- `list_policy_sets/ListPolicySets`: Phân trang + filter keyword. **Query**
- `delete_preview/GetPolicySetDeletePreview`: Trả số lượng policy + rule sẽ bị xóa theo cascade. **Query**

### abac/policy — Commands *(UC-021)*

- `create_policy/CreatePolicy`: Tạo Policy trong PolicySet, validate target SpEL. Dispatch `AbacAuditLogEvent(POLICY/CREATED)`. **Command**
- `update_policy/UpdatePolicy`: Cập nhật targetExpression + combineAlgorithm. Dispatch `AbacAuditLogEvent(POLICY/UPDATED)`. **Command**
- `delete_policy/DeletePolicy`: Load entity trước khi xóa. Dispatch `AbacAuditLogEvent(POLICY/DELETED)`. **Command**

### abac/policy — Queries *(UC-021)*

- `get_policy/GetPolicy`: Lấy Policy theo ID kèm danh sách RuleView. **Query**
- `list_policies/ListPolicies`: Danh sách policy theo policySetId. **Query**
- `delete_preview/GetPolicyDeletePreview`: Trả số lượng rule sẽ bị xóa theo cascade. **Query**

### abac/rule — Commands *(UC-022)*

- `create_rule/CreateRule`: Thêm Rule vào Policy, validate SpEL expressions. Dispatch `AbacAuditLogEvent(RULE/CREATED)`. **Command**
- `update_rule/UpdateRule`: Cập nhật name, description, expressions, effect. Dispatch `AbacAuditLogEvent(RULE/UPDATED)`. **Command**
- `delete_rule/DeleteRule`: Load rule trước khi xóa (để lấy name). Dispatch `AbacAuditLogEvent(RULE/DELETED)`. **Command**
- `reorder_rules/ReorderRules`: Đổi thứ tự rules theo ordered list of IDs. **Command**

### abac/rule — Queries *(UC-022, UC-032)*

- `get_rule/GetRule`: Lấy Rule theo ID, trả `RuleView`. **Query**
- `list_rules/ListRules`: Danh sách rules theo policyId. **Query**
- `impact_preview/GetRuleImpactPreview`: UC-032 — Phân tích SpEL target + condition expression qua `SpelExpressionAnalyzer`. Trả `requiredRoles`, `requiredAttributes`, `specificActions`, `navigableWithoutData`, `hasInstanceCondition`. **Query**

### abac/rule — Shared Application Services

- `service/SpelExpressionAnalyzer`: Static helper — walk SpEL AST để phân tích expressions. Dùng bởi: impact preview, reverse lookup, UIElement coverage check. Trả `AnalysisResult(requiredRoles, requiredAttributes, specificActions, navigableWithoutData, hasInstanceCondition, parseWarning)`.

### abac/ui_element — Commands *(UC-023)*

- `create_ui_element/CreateUIElement`: Tạo UIElement, validate elementId unique + actionId thuộc resourceId. Dispatch `AbacAuditLogEvent(UI_ELEMENT/CREATED)`. **Command**
- `update_ui_element/UpdateUIElement`: Cập nhật label, type, group, orderIndex, resourceId, actionId. elementId immutable. Dispatch `AbacAuditLogEvent(UI_ELEMENT/UPDATED)`. **Command**
- `delete_ui_element/DeleteUIElement`: Load element trước khi xóa. Dispatch `AbacAuditLogEvent(UI_ELEMENT/DELETED)`. **Command**

### abac/ui_element — Queries *(UC-023, UC-036)*

- `get_ui_element/GetUIElement`: Lấy UIElement theo ID kèm resourceName + actionName + `hasPolicyCoverage`. **Query**
- `list_ui_elements/ListUIElements`: Phân trang + filter resourceId/group; cache resource lookups tránh N+1. Thêm `hasPolicyCoverage` — tính `CoverageIndex` 1 lần/request. **Query**
- `list_uncovered_ui_elements/ListUncoveredUIElements`: UC-036 — Trả danh sách UIElement không được cover bởi bất kỳ PERMIT rule nào. **Query**
- `evaluate/EvaluateUIElements`: Batch evaluate — load elements theo elementIds → build Subject từ JWT → gọi PdpEngine 1 lần per element (policy load 1 lần) → trả `Map<elementId, "PERMIT"|"DENY">`. **Query**

### simulate *(UC-024, UC-031, UC-033, UC-034)*

- `simulate_policy/SimulatePolicy`: UC-024/UC-033 — Load PolicySet → build virtual Subject → `PdpEngine.authorizeWithTrace()` → trả `SimulateResult` kèm `List<RuleTraceEntry>` (per-rule trace). **Command**
- `simulate_navigation/SimulateNavigation`: UC-031/UC-034 — Evaluate tất cả actions của resource cho virtual Subject. Mỗi `ActionDecision` kèm `matchedRuleName` (lấy từ trace entry `wasDeciding=true`). **Query**
- `reverse_lookup/GetReverseLookup`: UC-034 — Tìm tất cả PERMIT/DENY rules cover resource+action. Dùng `SpelExpressionAnalyzer` + `UserRepository.countByRoleName()`. **Query**

### audit *(UC-035)*

- `list_audit_log/ListAuditLog`: Trả `Page<AuditLogEntry>` filter by entityType/entityId/performedBy. **Query**

### tenant — Commands *(UC-037, UC-038)*

- `assign_sub_role/AssignSubRole`: UC-037 — Gán TenantSubRole cho user trong org. B7: assignedBy phải là TENANT_ADMIN của org. B8: userId phải có TENANT context tại org. **Command**
- `revoke_sub_role/RevokeSubRole`: UC-038 — Gỡ TenantSubRole của user trong org. **Command**

### tenant — Queries *(UC-039)*

- `find_sub_roles_by_org/FindSubRolesByOrg`: UC-039 — Danh sách TenantSubRoleAssignment theo orgId. **Query**

### auth — Queries *(UC-040)*

- `get_contexts/GetUserContexts`: UC-040 — Load User → filter RoleContext ACTIVE → resolve displayName từ BuildingReference (OPERATOR/RESIDENT) hoặc OrgReference (TENANT) → trả `List<ContextView>`. **Query**

### operator — Commands *(UC-041, UC-042, UC-043, UC-045)*

- `link_party_id/LinkPartyId`: UC-041 — Gắn partyId vào User (một lần, không overwrite). **Command**
- `assign_operator_context/AssignOperatorContext`: UC-042 — Gán OPERATOR context cho user tại building. B2: partyId required. B3: building phải có trong building_reference. B6: role scope phải là OPERATOR. **Command**
- `revoke_operator_context/RevokeOperatorContext`: UC-043 — Thu hồi OPERATOR context. **Command**
- `assign_roles_to_operator/AssignRolesToOperatorContext`: UC-045 — Replace toàn bộ roleIds trong OPERATOR context. B6: scope check. **Command**

### operator — Queries *(UC-044)*

- `find_operators_by_building/FindOperatorsByBuilding`: UC-044 — Danh sách user có OPERATOR context ACTIVE tại building. Trả `List<OperatorView>`. **Query**

### reference — Queries *(Phase 3.5)*

- `get_all_buildings/GetAllBuildings`: Trả `List<BuildingView>` từ `building_reference` cache. **Query**

### event — Consumers *(Phase 4)*

- `event/BuildingCreatedEventHandler`: Consume `BuildingCreatedEvent` → upsert `BuildingReference`.
- `event/OrganizationCreatedEventHandler`: Consume `OrganizationCreatedEvent` → upsert `OrgReference` nếu `orgType == BQL`.
- `event/OccupancyAgreementActivatedEventHandler`: Consume `OccupancyAgreementActivatedEvent` → tạo RoleContext (RESIDENT/TENANT) theo agreementType + partyType.
- `event/OccupancyAgreementTerminatedEventHandler`: Consume `OccupancyAgreementTerminatedEvent` → revoke RoleContext RESIDENT/TENANT; xóa TenantSubRoleAssignment nếu LEASE+ORGANIZATION.
- `event/EmploymentTerminatedEventHandler`: Consume `EmploymentTerminatedEvent` → revoke OPERATOR RoleContext liên quan.

---

## 🛠️ 3. Infrastructure Layer (`infrastructure/`)

### 📦 Persistence (`persistence/`)

- **Entity mapping**:
    - `User` ↔ `UserJpaEntity` → bảng `users`
        - `UserPassword` → embedded cột `hashed_password`
    - `SocialConnection` ↔ `SocialConnectionJpaEntity` → bảng `social_connections` (FK: `user_id`)
    - `RoleContext` ↔ `UserRoleContextJpaEntity` → bảng `user_role_context` (FK: `user_id`); roles qua join table `user_role_context_roles`
    - `Role` ↔ `RoleJpaEntity` → bảng `roles`
    - `ResourceDefinition` + `ActionDefinition` ↔ `ResourceDefinitionJpaEntity` + `ActionDefinitionJpaEntity` → bảng `resource_definition`, `action_definition`
    - `PolicySetDefinition` ↔ `PolicySetJpaEntity` → bảng `policy_set`
    - `PolicyDefinition` ↔ `PolicyJpaEntity` → bảng `policy`
    - `RuleDefinition` ↔ `RuleJpaEntity` → bảng `rule`
    - `ExpressionVO` ↔ `AbacExpressionJpaEntity` → bảng `abac_expression` (LITERAL only Phase 1)
    - `UIElement` ↔ `UIElementJpaEntity` → bảng `ui_element`
    - `BuildingReference` ↔ `BuildingReferenceJpaEntity` → bảng `building_reference`

- **`persistence/user/`**: `UserJpaEntity`, `UserJpaRepository`, `UserMapper`, `SocialConnectionJpaEntity`
- **`persistence/role/`**: `RoleJpaEntity`, `RoleJpaRepository`, `RoleMapper`
- **`persistence/reference/`**: `BuildingReferenceJpaEntity`, `BuildingReferenceJpaRepository`, `BuildingReferenceMapper`
- **`persistence/abac/resource/`**: `ResourceDefinitionJpaEntity`, `ActionDefinitionJpaEntity`, `ResourceDefinitionJpaRepository`, `ResourceDefinitionMapper`
- **`persistence/abac/expression/`**: `AbacExpressionJpaEntity`, `AbacExpressionJpaRepository`
- **`persistence/abac/policy_set/`**: `PolicySetJpaEntity`, `PolicySetJpaRepository`, `PolicySetMapper`
- **`persistence/abac/policy/`**: `PolicyJpaEntity`, `PolicyJpaRepository`, `PolicyMapper`
- **`persistence/abac/rule/`**: `RuleJpaEntity`, `RuleJpaRepository`
- **`persistence/abac/uielement/`**: `UIElementJpaEntity`, `UIElementJpaRepository`, `UIElementMapper`
- **`persistence/abac/audit/`**: `AbacAuditLogJpaEntity`, `AbacAuditLogJpaRepository` (JPA entity tách khỏi domain; domain `AbacAuditLog` là plain POJO)
- **`persistence/tenant/`**: `TenantSubRoleAssignmentJpaEntity`, `TenantSubRoleAssignmentJpaRepository`, `TenantSubRoleAssignmentMapper`
- **`persistence/reference/`**: `BuildingReferenceJpaEntity`, `BuildingReferenceJpaRepository`, `BuildingReferenceMapper`, `OrgReferenceJpaEntity`, `OrgReferenceJpaRepository`, `OrgReferenceMapper`

### 🔌 Adapters (`adapter/`)

- **`adapter/repository/user/UserPersistenceAdapter`**: implement `UserRepository` — CRUD + findBySocialConnection + existsBy* + searchUsers (delegate tới JPA `@Query`)
- **`adapter/repository/role/RolePersistenceAdapter`**: implement `RoleRepository` — CRUD + findAllByIds + existsByName
- **`adapter/repository/reference/BuildingReferencePersistenceAdapter`**: implement `BuildingReferenceRepository` — upsert + existsById + findById + findAll
- **`adapter/repository/abac/ResourceDefinitionPersistenceAdapter`**: implement `ResourceDefinitionRepository` — CRUD + search + existsByName + existsByIdWith*Ref
- **`adapter/repository/abac/PolicySetPersistenceAdapter`**: implement `PolicySetRepository` — CRUD + search + findAllRoot
- **`adapter/repository/abac/PolicyPersistenceAdapter`**: implement `PolicyRepository` — CRUD + findByPolicySetId; handles expression upsert (abac_expression) + rule upsert
- **`adapter/repository/abac/UIElementPersistenceAdapter`**: implement `UIElementRepository` — CRUD + batch findByElementIds + existsBy*
- **`adapter/repository/abac/AbacAuditLogPersistenceAdapter`**: implement `AbacAuditLogRepository` domain interface — maps between `AbacAuditLog` (domain POJO) và `AbacAuditLogJpaEntity` (infrastructure)
- **`adapter/repository/tenant/TenantSubRoleAssignmentPersistenceAdapter`**: implement `TenantSubRoleAssignmentRepository` — CRUD + findByOrgId + existsByUserIdAndOrgIdAndSubRole + deleteAllByOrgId
- **`adapter/repository/reference/BuildingReferencePersistenceAdapter`**: implement `BuildingReferenceRepository` — upsert + existsById + findById + findAll
- **`adapter/repository/reference/OrgReferencePersistenceAdapter`**: implement `OrgReferenceRepository` — upsert + existsById + findById
- **`adapter/abac/AdminPolicyProvider`**: implement `PolicyProvider` (libs/abac) — tải root PolicySet từ DB → map sang libs/abac domain để PdpEngine evaluate. Đọc header `X-Portal-Scope` (`ADMIN` | `OPERATOR` | ...) từ request để resolve đúng policy set. Header này bắt buộc có mặt trên mọi request qua `@PreEnforce`.
- **`adapter/abac/AdminSubjectProvider`**: implement `SubjectProvider` (libs/abac) — build `Subject` từ `Principal.getName()` (userId) → load user + role names từ DB

### 🔐 Security (`security/`)

- `SecurityConfiguration`: Stateless JWT resource server. Permit: `/api/v1/internal/**` (service-to-service), `POST /api/v1/users/register` (self-registration). Require auth: mọi endpoint còn lại. `PasswordEncoder` bean (BCrypt strength=10).

### ⚙️ Cross-cutting (`cross-cutting/`)

- `config/EventDispatcherConfig`: Wire `EventDispatcher` bean với tất cả `EventHandler` — không chứa logic.
- `config/DataInitializer`: Seed `ADMIN` role và user `admin@example.com` khi khởi động lần đầu (idempotent).
- `config/AbacConfig`: Wire `PdpEngine` bean với `DecisionStrategy.DEFAULT_DENY`.
- `audit/AuditLogEventHandler`: `EventHandler<AbacAuditLogEvent>` — lắng nghe event, persist `AbacAuditLog`. Auto-registered bởi `EventDispatcherConfig`.
- `audit/AuditHelper`: Static helper — `currentPerformedBy()` lấy username từ `SecurityContextHolder`.

---

## 🖥️ 4. Presentation Layer (`presentation/`)

### `user/UserController` — `/api/v1/users`

| Method   | Path                   | Use Case               | Auth   |
|----------|------------------------|------------------------|--------|
| `POST`   | `/register`            | UC-002 RegisterUser    | Public |
| `POST`   | `/`                    | UC-001 AdminCreateUser | JWT    |
| `GET`    | `/{id}`                | UC-004 FindUserById    | JWT    |
| `GET`    | `/`                    | UC-006 SearchUsers     | JWT    |
| `POST`   | `/{id}/lock`           | UC-007a LockUser       | JWT    |
| `POST`   | `/{id}/unlock`         | UC-007b UnlockUser     | JWT    |
| `POST`   | `/{id}/roles`          | UC-013 AssignRoles     | JWT    |
| `DELETE` | `/{id}/roles/{roleId}` | UC-014 RemoveRole      | JWT    |
| `GET`    | `/me`                  | GetMyProfile           | JWT    |
| `PATCH`  | `/me`                  | UC-016 UpdateProfile   | JWT    |
| `POST`   | `/me/password`         | UC-017 ChangePassword  | JWT    |

DTOs: `user/model/` — `CreateUserRequest`, `RegisterUserRequest`, `AssignRolesRequest`, `UpdateProfileRequest`, `ChangePasswordRequest`

### `role/RoleController` — `/api/v1/roles`

| Method   | Path    | Use Case            | Auth |
|----------|---------|---------------------|------|
| `POST`   | `/`     | UC-008 CreateRole   | JWT  |
| `GET`    | `/{id}` | UC-009 FindRoleById | JWT  |
| `GET`    | `/`     | UC-010 FindAllRoles | JWT  |
| `PATCH`  | `/{id}` | UC-011 UpdateRole   | JWT  |
| `DELETE` | `/{id}` | UC-012 DeleteRole   | JWT  |

DTOs: `role/model/` — `CreateRoleRequest`, `UpdateRoleRequest`

### `reference/BuildingReferenceController` — `/api/v1/buildings`

| Method | Path | Use Case               | Auth |
|--------|------|------------------------|------|
| `GET`  | `/`  | GetAllBuildings        | JWT  |

### `internal/InternalUserController` — `/api/v1/internal/users`

| Method | Path               | Use Case                  | Auth                   |
|--------|--------------------|---------------------------|------------------------|
| `GET`  | `/identity?value=` | UC-U05 GetUserByIdentity  | Permit (network-level) |
| `POST` | `/social`          | UC-003 SocialRegisterUser | Permit (network-level) |

DTOs: `internal/model/` — `SocialRegisterRequest`

### `abac/ResourceDefinitionController` — `/api/v1/abac/resources` *(UC-019)*

| Method   | Path                               | Use Case                  | Auth |
|----------|------------------------------------|---------------------------|------|
| `POST`   | `/`                                | CreateResourceDefinition  | JWT  |
| `GET`    | `/{id}`                            | GetResourceDefinition     | JWT  |
| `GET`    | `/`                                | ListResourceDefinitions   | JWT  |
| `PUT`    | `/{id}`                            | UpdateResourceDefinition  | JWT  |
| `DELETE` | `/{id}`                            | DeleteResourceDefinition  | JWT  |
| `POST`   | `/{resourceId}/actions`            | AddActionToResource       | JWT  |
| `PATCH`  | `/{resourceId}/actions/{actionId}` | UpdateActionDefinition    | JWT  |
| `DELETE` | `/{resourceId}/actions/{actionId}` | RemoveActionFromResource  | JWT  |

### `abac/PolicySetController` — `/api/v1/abac/policy-sets` *(UC-020)*

| Method   | Path    | Use Case          | Auth |
|----------|---------|-------------------|------|
| `GET`    | `/`     | ListPolicySets    | JWT  |
| `GET`    | `/{id}` | GetPolicySet      | JWT  |
| `POST`   | `/`     | CreatePolicySet   | JWT  |
| `PUT`    | `/{id}` | UpdatePolicySet   | JWT  |
| `DELETE` | `/{id}` | DeletePolicySet   | JWT  |

### `abac/PolicyController` — `/api/v1/abac/policies` *(UC-021)*

| Method   | Path    | Use Case      | Auth |
|----------|---------|---------------|------|
| `GET`    | `/`     | ListPolicies  | JWT  |
| `GET`    | `/{id}` | GetPolicy     | JWT  |
| `POST`   | `/`     | CreatePolicy  | JWT  |
| `PUT`    | `/{id}` | UpdatePolicy  | JWT  |
| `DELETE` | `/{id}` | DeletePolicy  | JWT  |

### `abac/RuleController` — `/api/v1/abac/policies/{policyId}/rules` *(UC-022)*

| Method   | Path               | Use Case      | Auth |
|----------|--------------------|---------------|------|
| `POST`   | `/`                | CreateRule    | JWT  |
| `PUT`    | `/{ruleId}`        | UpdateRule    | JWT  |
| `DELETE` | `/{ruleId}`        | DeleteRule    | JWT  |
| `PUT`    | `/reorder`         | ReorderRules  | JWT  |

### `abac/UIElementController` — `/api/v1/abac/ui-elements` *(UC-023, UC-036)*

| Method   | Path          | Use Case                  | Auth |
|----------|---------------|---------------------------|------|
| `POST`   | `/`           | CreateUIElement           | JWT  |
| `GET`    | `/{id}`       | GetUIElement              | JWT  |
| `GET`    | `/`           | ListUIElements            | JWT  |
| `GET`    | `/uncovered`  | ListUncoveredUIElements   | JWT  |
| `PUT`    | `/{id}`       | UpdateUIElement           | JWT  |
| `DELETE` | `/{id}`       | DeleteUIElement           | JWT  |
| `POST`   | `/evaluate`   | EvaluateUIElements        | JWT  |

Params list: `resourceId`, `type`, `group`, `page`, `size`

### `abac/AbacSimulateController` — `/api/v1/abac/simulate` *(UC-024, UC-031, UC-034)*

| Method | Path          | Use Case           | Auth |
|--------|---------------|--------------------|------|
| `POST` | `/`           | SimulatePolicy     | JWT  |
| `POST` | `/navigation` | SimulateNavigation | JWT  |
| `GET`  | `/reverse`    | GetReverseLookup   | JWT  |

### `abac/AuditLogController` — `/api/v1/abac/audit-log` *(UC-035)*

| Method | Path | Use Case      | Auth |
|--------|------|---------------|------|
| `GET`  | `/`  | ListAuditLog  | JWT  |

Params: `entityType`, `entityId`, `performedBy`, `page`, `size`

### `tenant/TenantSubRoleController` — `/api/v1/tenants/{orgId}/sub-roles`

| Method   | Path                    | Use Case        | Auth |
|----------|-------------------------|-----------------|------|
| `POST`   | `/`                     | AssignSubRole   | JWT  |
| `DELETE` | `/{userId}/{subRole}`   | RevokeSubRole   | JWT  |
| `GET`    | `/`                     | FindSubRolesByOrg | JWT |

### `auth/AuthContextController` — `/api/v1/auth`

| Method | Path          | Use Case          | Auth |
|--------|---------------|-------------------|------|
| `GET`  | `/contexts`   | GetUserContexts   | JWT  |

Params: `userId`

### `operator/OperatorContextController` — `/api/v1/operators`

| Method   | Path                            | Use Case                     | Auth |
|----------|---------------------------------|------------------------------|------|
| `POST`   | `/link-party`                   | LinkPartyId                  | JWT  |
| `POST`   | `/{buildingId}/assign`          | AssignOperatorContext         | JWT  |
| `DELETE` | `/{buildingId}/revoke/{userId}` | RevokeOperatorContext         | JWT  |
| `GET`    | `/{buildingId}`                 | FindOperatorsByBuilding       | JWT  |
| `PUT`    | `/{buildingId}/roles/{userId}`  | AssignRolesToOperatorContext  | JWT  |

### `reference/BuildingReferenceController` — `/api/v1/buildings`

| Method | Path | Use Case          | Auth |
|--------|------|-------------------|------|
| `GET`  | `/`  | GetAllBuildings   | JWT  |

### `base/`

- `ApiResponse<T>`, `PagedApiResponse<T>` — wrapper response chuẩn
- `ErrorResponse` — format lỗi
- `GlobalExceptionHandler` — map `DomainException` → HTTP error

---

## 📄 5. Resources & Configs

- `application.properties` / `application-dev.properties`: Server port, DB datasource, OAuth2 resource server JWT issuer URI.
- `db/migration/`:
    - `V1__create_roles_table.sql` — bảng `roles`
    - `V2__create_users_tables.sql` — bảng `users`, `social_connections`
    - `V3__abac_schema.sql` — bảng ABAC: `resource_definition`, `action_definition`, `abac_expression`, `policy_set`, `policy`, `rule`, `ui_element`
    - `V4__create_abac_audit_log.sql` — bảng `abac_audit_log` + indexes
    - `V5__abac_scope_expansion.sql` — mở rộng scope enum
    - `V6__user_role_context.sql` — bảng `user_role_context`, `user_role_context_roles`
    - `V7__migrate_user_roles_to_context.sql` — migrate flat `user_roles` → context model, drop `user_roles`
    - `V8__named_expression_and_tree_ref.sql` — named expression, tree reference
    - `V9__iam_extension.sql` — thêm `scope` vào `roles`; thêm `status`, `org_type` vào `user_role_context`; thêm `party_id` vào `users`
    - `V10__tenant_sub_role.sql` — bảng `tenant_sub_role_assignment`
    - `V11__reference_cache.sql` — bảng `building_reference`, `org_reference`
- `db/seed/operator_portal_seed.sql` — seed resource/action/UIElement cho Operator Portal và Building Management (chạy thủ công)
