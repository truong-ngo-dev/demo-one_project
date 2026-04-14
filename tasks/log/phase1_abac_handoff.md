# Phase 1 ABAC Enforcement — Handoff Context

*Completed: 2026-04-13*

---

## Files tạo mới

| File                                                        | Layer          | Nội dung                                                  |
|-------------------------------------------------------------|----------------|-----------------------------------------------------------|
| `infrastructure/adapter/abac/AdminEnvironmentProvider.java` | Infrastructure | Returns empty Environment (global + service HashMap rỗng) |

---

## Files sửa đổi

### libs/abac (breaking changes)

| File                                         | Thay đổi                                                                                                       |
|----------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| `libs/abac/.../pip/PipEngine.java`           | Bỏ `ResourceAccessConfig` khỏi record — còn 3 params: `(PolicyProvider, EnvironmentProvider, SubjectProvider)` |
| `libs/abac/.../pep/AuthorizationAspect.java` | Bỏ fallback URL extraction — thiếu `@ResourceMapping` → throw `AuthorizationException` ngay (fail fast)        |

### Infrastructure

| File                                                       | Thay đổi                                                                        |
|------------------------------------------------------------|---------------------------------------------------------------------------------|
| `infrastructure/cross_cutting/config/AbacConfig.java`      | + PipEngine bean (3 params), PepEngine bean. Không có ResourceAccessConfig bean |
| `infrastructure/cross_cutting/config/DataInitializer.java` | + seedAbacPolicy(), seedResources()                                             |

### Presentation

| File                                                  | Thay đổi                                                                                                     |
|-------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| `presentation/base/GlobalExceptionHandler.java`       | + AuthorizationException → 403 handler                                                                       |
| `presentation/user/UserController.java`               | @PreEnforce + @ResourceMapping trên 7 endpoints (CREATE, READ, LIST, LOCK, UNLOCK, ASSIGN_ROLE, REMOVE_ROLE) |
| `presentation/role/RoleController.java`               | @PreEnforce + @ResourceMapping trên 5 endpoints (CREATE, READ, LIST, UPDATE, DELETE)                         |
| `presentation/abac/PolicySetController.java`          | @PreEnforce + @ResourceMapping trên 6 endpoints                                                              |
| `presentation/abac/PolicyController.java`             | @PreEnforce + @ResourceMapping trên 6 endpoints                                                              |
| `presentation/abac/RuleController.java`               | @PreEnforce + @ResourceMapping trên 6 endpoints                                                              |
| `presentation/abac/ResourceDefinitionController.java` | @PreEnforce + @ResourceMapping trên 8 endpoints                                                              |
| `presentation/abac/UIElementController.java`          | @PreEnforce + @ResourceMapping trên 5 endpoints — `/evaluate` không annotate (xem bên dưới)                  |
| `presentation/abac/AbacSimulateController.java`       | @PreEnforce + @ResourceMapping trên 3 endpoints                                                              |
| `presentation/abac/AuditLogController.java`           | @PreEnforce + @ResourceMapping                                                                               |
| `presentation/abac/RuleImpactController.java`         | @PreEnforce + @ResourceMapping                                                                               |

---

## Policy seed (DataInitializer)

| Entity     | Name           | Config                                                        |
|------------|----------------|---------------------------------------------------------------|
| PolicySet  | `admin-root`   | scope=ADMIN, isRoot=true, combineAlgorithm=DENY_UNLESS_PERMIT |
| Policy     | `admin-access` | policySetId → admin-root, combineAlgorithm=PERMIT_OVERRIDES   |
| Expression | —              | LITERAL, `subject.roles.contains('ADMIN')`                    |
| Rule       | `admin-permit` | effect=PERMIT, conditionExpressionId → expression trên        |

---

## Resource/Action seed (DataInitializer)

| Resource name     | Actions                                                    |
|-------------------|------------------------------------------------------------|
| `user`            | LIST, READ, CREATE, LOCK, UNLOCK, ASSIGN_ROLE, REMOVE_ROLE |
| `role`            | LIST, READ, CREATE, UPDATE, DELETE                         |
| `abac_policy_set` | LIST, READ, CREATE, UPDATE, DELETE                         |
| `abac_policy`     | LIST, READ, CREATE, UPDATE, DELETE                         |
| `abac_rule`       | LIST, READ, CREATE, UPDATE, DELETE                         |
| `abac_resource`   | LIST, READ, CREATE, UPDATE, DELETE                         |
| `abac_ui_element` | LIST, READ, CREATE, UPDATE, DELETE                         |
| `abac_simulate`   | EXECUTE                                                    |
| `abac_audit_log`  | LIST                                                       |

---

## Pattern decisions

**ResourceAccessConfig bị loại bỏ hoàn toàn**: URL path không thể resolve action (GET /users → LIST hay READ? không có mapping 1-1). Mọi endpoint enforce phải có `@ResourceMapping` explicit. Thiếu annotation = `AuthorizationException` khi method được gọi.

**ignoredPaths không dùng**: với `@PreEnforce` annotation-driven, endpoint không annotate = không enforce. Không cần bypass config. Public endpoints (`/register`, `/me`, `/me/password`) đơn giản là không có `@PreEnforce`.

**`/evaluate` không annotate**: `POST /abac/ui-elements/evaluate` là endpoint Angular shell dùng để load visibility map khi khởi động. Nếu annotate sẽ tạo chicken-and-egg: cần PERMIT để gọi evaluate, nhưng evaluate là thứ xác định UI nào được phép.

**Resource DB entries ≠ enforcement dependency**: PolicySet/Rule evaluate bằng SpEL string match. DB resource entries chỉ phục vụ UIElement binding trong ABAC console.

---

## Build status

```
libs/abac: mvn install -DskipTests → BUILD SUCCESS
services/admin: mvn compile -DskipTests → BUILD SUCCESS (182 source files)
```

---

## TODO còn lại

- [ ] Smoke test thực tế: admin JWT → 200, no JWT → 401, non-ADMIN JWT → 403
- [ ] Phase 2: Angular Shell (xem `docs/context/final_design/07_admin_portal_implementation_plan.md`)
