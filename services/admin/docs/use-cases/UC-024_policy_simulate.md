# UC-024: Policy Simulator (Mock Enforce API)

## Tóm tắt
Admin kiểm tra policy enforcement bằng cách gửi virtual subject + resource + action lên server, nhận kết quả PERMIT/DENY + trace mà không cần deployment hay user thực.

## Actor
BQL SUPER_ADMIN

## Trạng thái
Planned

---

## Endpoint

**POST** `/api/v1/abac/simulate`

Auth: JWT required, role SUPER_ADMIN.

---

## Request

```json
{
  "subject": {
    "userId": null,
    "roles": ["MANAGER"],
    "attributes": {
      "managedDepartments": ["engineering", "product"]
    }
  },
  "resource": {
    "name": "employee",
    "data": {
      "department": "engineering",
      "status": "ACTIVE"
    }
  },
  "action": "READ",
  "policySetId": null
}
```

| Field             | Bắt buộc | Mô tả |
|-------------------|----------|-------|
| `subject.roles`   | Có       | Danh sách role của virtual subject |
| `subject.attributes` | Không | Map attribute tự do — truyền vào Subject.attributes |
| `subject.userId`  | Không    | Nếu truyền → load user từ DB và merge với roles/attributes được chỉ định |
| `resource.name`   | Có       | Tên resource (phải tồn tại trong resource_definition) |
| `resource.data`   | Không    | Instance data — null = navigation-level evaluation |
| `action`          | Có       | Tên action (phải tồn tại trong action_definition của resource) |
| `policySetId`     | Không    | Nếu null → dùng root PolicySet (`isRoot = true`) |

---

## Response

### Trường hợp PERMIT
```json
{
  "decision": "PERMIT",
  "timestamp": "2026-04-06T10:30:00Z",
  "policySetId": 1,
  "policySetName": "bql-root",
  "details": [
    {
      "policyId": 1,
      "policyName": "policy:employee",
      "policyDecision": "PERMIT",
      "rules": [
        {
          "ruleId": 3,
          "ruleName": "MANAGER read own dept only",
          "effect": "PERMIT",
          "targetMatched": true,
          "conditionMatched": true,
          "applied": true
        },
        {
          "ruleId": 1,
          "ruleName": "All actions for HR_ADMIN",
          "effect": "PERMIT",
          "targetMatched": false,
          "conditionMatched": null,
          "applied": false
        }
      ]
    }
  ]
}
```

### Trường hợp DENY
```json
{
  "decision": "DENY",
  "timestamp": "2026-04-06T10:31:00Z",
  "policySetId": 1,
  "policySetName": "bql-root",
  "details": [
    {
      "policyId": 1,
      "policyName": "policy:employee",
      "policyDecision": "DENY",
      "rules": [
        {
          "ruleId": 7,
          "ruleName": "Block CREATE/DELETE for non-HR",
          "effect": "DENY",
          "targetMatched": true,
          "conditionMatched": true,
          "applied": true
        }
      ]
    }
  ]
}
```

---

## Flow

```
POST /api/v1/abac/simulate
    │
    ├─ 1. Validate request (resource.name tồn tại, action tồn tại trong resource)
    │
    ├─ 2. Load PolicySet
    │     ├─ policySetId != null → load theo ID
    │     └─ policySetId == null → load root PolicySet (isRoot = true)
    │
    ├─ 3. Build Subject từ request body
    │     ├─ userId != null → merge DB user data với roles/attrs từ request
    │     └─ userId == null → tạo virtual Subject từ roles + attributes
    │
    ├─ 4. Build Action = Action.semantic(request.action)
    │
    ├─ 5. Build Resource
    │     ├─ resource.name từ request
    │     └─ resource.data từ request (có thể null)
    │
    ├─ 6. Build Environment (serviceName = "admin-service")
    │
    ├─ 7. Build AuthzRequest(subject, resource, action, environment, policy)
    │
    ├─ 8. PdpEngine.authorize(authzRequest)
    │
    └─ 9. Map AuthzDecision → SimulateResponse (bao gồm trace details)
```

---

## Implementation Notes

### SimulateUseCase

```java
// application/simulate/simulate_policy/
public class SimulatePolicy {
    public record Command(
        SimulateSubjectRequest subject,
        SimulateResourceRequest resource,
        String action,
        Long policySetId
    ) {}

    public record Handler(
        PolicySetJpaRepository policySetRepo,
        ResourceDefinitionRepository resourceRepo,
        UserRepository userRepository,
        PdpEngine pdpEngine
    ) implements CommandHandler<Command, SimulateResult> { ... }
}
```

### AdminPolicyProvider

`AdminPolicyProvider` cần implement `PolicyProvider` của libs/abac:
```java
@Component
public class AdminPolicyProvider implements PolicyProvider {
    public AbstractPolicy getPolicy(String policySetName) {
        // Load PolicySet → List<Policy> → List<Rule> → Expressions từ DB
        // Build libs/abac domain objects
        // PolicySet(name, combineAlgo, List<Policy>)
        //   Policy(name, targetExpr, combineAlgo, List<Rule>)
        //     Rule(name, targetExpr, conditionExpr, effect)
    }
}
```

Đây là class quan trọng nhất để bridge DB data với PdpEngine. Cần implement trong Task 4 (Infrastructure).

### Trace Details

`AuthzDecision.getDetails()` trong libs/abac hiện trả `Map<String, Object>`. Để trả trace dạng structured (rule-by-rule), cần implement custom `DecisionStrategy` hoặc wrap PdpEngine để collect evaluation trace.

**Phase 1 scope**: Trả `AuthzDecision.decision` + `details` raw từ PdpEngine. Trace chi tiết (rule-by-rule) là Phase 3 feature. Response `details` field có thể là opaque map trong Phase 1.

---

## Ghi chú thiết kế

- Endpoint này **không trigger** `@PreEnforce`/`@PostEnforce` — đây là test tool, không phải enforcement path thực
- Không log vào Audit Log (test traffic, không phải production action)
- Performance: không cache policy khi simulate (luôn load fresh từ DB để test thay đổi vừa save)
- Security: chỉ SUPER_ADMIN. Không expose ra public — đây là admin debug tool
