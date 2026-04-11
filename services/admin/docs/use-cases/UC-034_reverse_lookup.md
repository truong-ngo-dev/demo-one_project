# UC-034 — Reverse Lookup

## Mô tả
Cho trước một `resourceName` + `actionName`, tìm tất cả Rules trong PolicySet đang active
có khả năng cover (PERMIT hoặc DENY) action đó. Kết quả chia thành `permitRules` và `denyRules`,
mỗi rule kèm theo danh sách roles/attributes cần thiết và ước tính số users có role đó.

Dùng để admin hiểu "Ai hiện tại có thể làm hành động X trên resource Y?"

## Các thành phần thay đổi

### services/admin

| File | Thay đổi |
|------|----------|
| `application/rule/SpelExpressionAnalyzer.java` | Tạo mới — static helper trích xuất từ `GetRuleImpactPreview`, tái dùng cho reverse lookup và UIElement coverage |
| `application/rule/query/impact_preview/GetRuleImpactPreview.java` | Refactor dùng `SpelExpressionAnalyzer` |
| `application/simulate/reverse_lookup/GetReverseLookup.java` | Tạo mới — UC-034 handler |
| `application/simulate/simulate_navigation/SimulateNavigation.java` | `ActionDecision` thêm `matchedRuleName`; handler dùng `authorizeWithTrace()` để extract deciding rule |
| `domain/user/UserRepository.java` | Thêm `countByRoleName(String roleName)` |
| `infrastructure/persistence/user/UserJpaRepository.java` | Thêm JPQL query `countByRoleName` |
| `infrastructure/adapter/repository/user/UserPersistenceAdapter.java` | Implement `countByRoleName` |
| `presentation/abac/AbacSimulateController.java` | Thêm `GET /api/v1/abac/simulate/reverse` |

## API

```
GET /api/v1/abac/simulate/reverse?resourceName=X&actionName=Y[&policySetId=N]
Response: ApiResponse<ReverseLookupResult>

ReverseLookupResult {
  resourceName: String,
  actionName: String,
  permitRules: RuleCoverage[],
  denyRules: RuleCoverage[]
}

RuleCoverage {
  ruleId: Long | null,
  ruleName: String,
  policyName: String,
  effect: "PERMIT" | "DENY",
  requiredRoles: String[],
  requiredAttributes: String[],
  hasInstanceCondition: boolean,
  userCountByRole: Long | null,
  userCountNote: String | null
}
```

Navigation mode response cũng cập nhật:
```
ActionDecision { action, decision, matchedRuleName: String | null }
```

## Ghi chú thiết kế
- Rule coverage xác định bằng SpEL AST analysis (literal-only, không runtime eval)
- Rule với `specificActions` rỗng → áp dụng cho mọi action
- `userCountByRole`:
  - Roles rỗng → `null` + `userCountNote = "Applies to all users"`
  - 1 role → exact count
  - Nhiều roles → tổng (có thể overlap), kèm note
- Khi `policySetId = null` → dùng root policy set đầu tiên
