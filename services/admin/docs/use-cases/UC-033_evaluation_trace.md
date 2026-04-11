# UC-033 — Evaluation Trace

## Mô tả
Khi thực hiện instance-mode simulation (`POST /api/v1/abac/simulate`), kết quả trả về bao gồm
danh sách `trace[]` — một entry per Rule — ghi lại kết quả đánh giá của từng Rule trong quá trình
PDP Engine duyệt chính sách. Giúp admin hiểu tại sao một request được PERMIT hoặc DENY.

## Các thành phần thay đổi

### libs/abac

| File | Thay đổi |
|------|----------|
| `evaluation/RuleTraceEntry.java` | Tạo mới — record: `ruleId, ruleDescription, effect, targetMatched, conditionMatched, wasDeciding` |
| `evaluation/EvaluationDetails.java` | Tạo mới — record: `cause, trace` — chứa cả nguyên nhân (nếu có) và danh sách trace |
| `context/EvaluationContext.java` | Thêm `tracingEnabled`, `traceEntries`; phương thức `enableTracing()`, `addTraceEntry()`, `getTraceEntries()` |
| `evaluation/PolicyEvaluators.java` | `ruleEvaluator` — capture kết quả vào biến, ghi trace entry nếu `isTracingEnabled()` |
| `pdp/PdpEngine.java` | Thêm `authorizeWithTrace()` — bật tracing trên context, trả `AuthzDecision` với `details = EvaluationDetails` |

### services/admin

| File | Thay đổi |
|------|----------|
| `application/simulate/simulate_policy/SimulatePolicy.java` | `SimulateResult` thêm `List<RuleTraceEntry> trace`; handler dùng `authorizeWithTrace()` |

## API

```
POST /api/v1/abac/simulate
Response: SimulateResult {
  decision: "PERMIT" | "DENY",
  timestamp: long,
  policySetId: Long | null,
  policySetName: String | null,
  details: Object | null,
  trace: [
    {
      ruleId: String,
      ruleDescription: String | null,
      effect: "PERMIT" | "DENY",
      targetMatched: boolean,
      conditionMatched: boolean | null,
      wasDeciding: boolean
    }
  ]
}
```

## Ghi chú thiết kế
- `conditionMatched = null` khi target không match (condition không được đánh giá)
- `wasDeciding = true` chỉ khi rule trả về PERMIT hoặc DENY (không phải NOT_APPLICABLE/INDETERMINATE)
- Tracing không ảnh hưởng logic evaluation — chỉ thu thập side-effect
- Navigation mode dùng trace để extract `matchedRuleName` (UC-034 dùng chung)
