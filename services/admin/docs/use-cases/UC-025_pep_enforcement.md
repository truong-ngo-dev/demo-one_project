# UC-025: PEP — API Enforcement

## Tóm tắt
Wire Policy Enforcement Point (PEP) vào admin service. Mỗi controller method nhạy cảm được đánh dấu `@PreEnforce` + `@ResourceMapping` — mọi request đến đều được PdpEngine evaluate trước khi handler chạy. Nếu DENY → 403 Forbidden.

## Actor
System (transparent với caller — xảy ra tự động trên mọi request có annotation)

## Trạng thái
Implemented (Phase 1 — 2026-04-13)

---

## Bối cảnh kỹ thuật

`libs/abac` đã có sẵn toàn bộ infrastructure:

```
AuthorizationAspect   (@Aspect, @Component) — intercept @PreEnforce / @PostEnforce
  └── PepEngine.enforce(AuthzRequest, ignoredPaths[]) → AuthzDecision
        └── PdpEngine.authorize(AuthzRequest) → AuthzDecision

PipEngine (record)    — trung gian lấy Subject / Policy / Environment
  ├── SubjectProvider   ← AdminSubjectProvider (implemented Phase 1)
  ├── PolicyProvider    ← AdminPolicyProvider  (implemented Phase 1)
  └── EnvironmentProvider ← AdminEnvironmentProvider (cần implement Phase 2)
```

Phase 2 cần:
1. Implement `AdminEnvironmentProvider` (trả `Environment` rỗng hoặc với service metadata).
2. Wire `PipEngine`, `PepEngine`, `AuthorizationAspect` vào Spring context (`AbacConfig`).
3. Thêm `@PreEnforce` + `@ResourceMapping` vào controller methods.

---

## UC-025-1: Wire PEP vào Spring context

**Thay đổi `AbacConfig`**:

```java
@Bean
public PipEngine pipEngine(PolicyProvider policyProvider,
                            EnvironmentProvider environmentProvider,
                            SubjectProvider subjectProvider,
                            ResourceAccessConfig resourceAccessConfig) {
    return new PipEngine(policyProvider, environmentProvider,
                         subjectProvider, resourceAccessConfig);
}

@Bean
public PepEngine pepEngine() {
    return new PepEngine(DecisionStrategy.DEFAULT_DENY);
}

@Bean
public ResourceAccessConfig resourceAccessConfig() {
    return ResourceAccessConfig.builder()
        .resourceNameExtractor("/api/[^/]+/[^/]+/([^/]+)(?:/.*)?")
        // pattern lấy segment sau /v1/abac/ hoặc /v1/
        .build();
}
```

`AuthorizationAspect` tự được Spring component-scan vì đã có `@Component`.

---

## UC-025-2: Gắn @PreEnforce vào controller

Chiến lược áp dụng:

| Controller                     | Endpoint                            | Resource          | Action        |
|--------------------------------|-------------------------------------|-------------------|---------------|
| `UserController`               | `GET /users`                        | `user`            | `LIST`        |
| `UserController`               | `GET /users/{id}`                   | `user`            | `READ`        |
| `UserController`               | `POST /users`                       | `user`            | `CREATE`      |
| `UserController`               | `PUT /users/{id}/roles`             | `user`            | `ASSIGN_ROLE` |
| `UserController`               | `DELETE /users/{id}/roles/{roleId}` | `user`            | `REMOVE_ROLE` |
| `UserController`               | `POST /users/{id}/lock`             | `user`            | `LOCK`        |
| `UserController`               | `POST /users/{id}/unlock`           | `user`            | `UNLOCK`      |
| `RoleController`               | `POST /roles`                       | `role`            | `CREATE`      |
| `RoleController`               | `PUT /roles/{id}`                   | `role`            | `UPDATE`      |
| `RoleController`               | `DELETE /roles/{id}`                | `role`            | `DELETE`      |
| `ResourceDefinitionController` | `POST /abac/resources`              | `abac_resource`   | `CREATE`      |
| `PolicySetController`          | `POST /abac/policy-sets`            | `abac_policy_set` | `CREATE`      |
| ...                            | ...                                 | ...               | ...           |

**Pattern**:
```java
@GetMapping("/{id}")
@ResourceMapping(resource = "user", action = "READ")
@PreEnforce
public ResponseEntity<ApiResponse<UserView>> getUserById(@PathVariable String id) {}
```

---

## UC-025-3: Implement AdminEnvironmentProvider

```java
@Component
public class AdminEnvironmentProvider implements EnvironmentProvider {
    @Override
    public Environment getEnvironment(String serviceName) {
        Environment env = new Environment();
        // Phase 2: có thể thêm service metadata (e.g., env.addServiceEnv("service", serviceName))
        return env;
    }
}
```

---

## UC-025-4: @PostEnforce cho resource-instance authorization

`@PostEnforce` evaluate SAU KHI handler chạy — dùng khi cần kiểm tra instance data (ví dụ: user chỉ được đọc user trong cùng tenant).

```java
@GetMapping("/{id}")
@ResourceMapping(resource = "user", action = "READ")
@PostEnforce
public ResponseEntity<ApiResponse<UserView>> getUserById(@PathVariable String id) {
    // Handler trả object; AuthorizationAspect gắn object vào Resource.data rồi evaluate
}
```

Phase 2 scope: chỉ implement `@PreEnforce`. `@PostEnforce` là Phase 3 (cần ResourceProvider để load instance data).

---

## Luồng enforce

```
Request → AuthorizationAspect.preEnforce()
    │
    ├─ PipEngine.getSubject(request.getUserPrincipal())
    │     └─ AdminSubjectProvider: load userId + roles từ DB
    │
    ├─ PipEngine.getPolicy("admin-service")
    │     └─ AdminPolicyProvider: load root PolicySet từ DB
    │
    ├─ PipEngine.getEnvironment("admin-service")
    │     └─ AdminEnvironmentProvider: return empty Environment
    │
    ├─ ResourceMapping annotation → resource.name + action.name
    │
    ├─ PepEngine.enforce(authzRequest, ignoredPaths[])
    │     └─ PdpEngine.authorize() → PERMIT | DENY
    │
    ├─ PERMIT → handler chạy bình thường
    └─ DENY   → throw AuthorizationException → 403 Forbidden
```

---

## SpEL examples cho enforcement

Sau khi wire xong, policy rules có thể viết:

```
// Chỉ SUPER_ADMIN được tạo user
subject.roles.contains('SUPER_ADMIN')

// ADMIN và SUPER_ADMIN được đọc user list
subject.roles.contains('ADMIN') || subject.roles.contains('SUPER_ADMIN')

// Chỉ SUPER_ADMIN được thao tác ABAC policy
subject.roles.contains('SUPER_ADMIN')
```

---

## Error Response

HTTP 403 khi DENY:

```json
{
  "error": {
    "code": "FORBIDDEN",
    "message": "Access denied",
    "httpStatus": 403
  }
}
```

`GlobalExceptionHandler` cần handle `AuthorizationException` → 403.

---

## Dependencies

- Phase 1 đã có: `AdminPolicyProvider`, `AdminSubjectProvider`, `PdpEngine` bean
- Phase 2 cần thêm: `AdminEnvironmentProvider`, wire `PipEngine` + `PepEngine`, annotate controllers
- `libs/abac` không cần sửa — mọi thứ đã sẵn sàng

---

## Ghi chú thiết kế

- **Internal endpoints** (`/api/internal/...`) nằm trong `ignoredPaths[]` của `@PreEnforce` — không enforce.
- **Phase 2 chỉ enforce admin-service** — web-gateway và oauth2 service là Phase 3+.
- Policy data phải được seed trước khi bật enforce. Nếu không có root PolicySet → `AdminPolicyProvider` trả empty PolicySet → mọi request đều DENY. Cần có migration script với dữ liệu seed policy mặc định.
