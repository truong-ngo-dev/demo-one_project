# UC-022: Rule Management (Raw SpEL Mode)

## Tóm tắt
Admin quản lý Rules trong một Policy. Mỗi Rule có target expression, condition expression, effect (PERMIT/DENY), và thứ tự đánh giá.

## Actor
BQL SUPER_ADMIN

## Trạng thái
Planned

---

## UC-022-1: Tạo Rule

**POST** `/api/v1/abac/rules`

**Request**:
```json
{
  "policyId": 1,
  "name": "MANAGER read own dept only",
  "description": "MANAGER chỉ được đọc nhân viên trong department mình quản lý",
  "effect": "PERMIT",
  "targetExpression": {
    "type": "LITERAL",
    "spelExpression": "subject.roles.contains('MANAGER') && action.getAttribute('name') == 'READ'"
  },
  "conditionExpression": {
    "type": "LITERAL",
    "spelExpression": "object.data == null || subject.getAttribute('managedDepartments').contains(object.data.department)"
  },
  "orderIndex": 3
}
```

**Flow**:
1. Validate `policyId` tồn tại
2. Parse check cả `targetExpression.spelExpression` và `conditionExpression.spelExpression`
3. Persist Expressions, sau đó persist Rule
4. Nếu không truyền `orderIndex`: append vào cuối (max orderIndex + 1)

**Response**: `201 Created` — `RuleView`

---

## UC-022-2: Lấy Rule theo ID

**GET** `/api/v1/abac/rules/{id}`

Response: `RuleView` với đầy đủ expressions

---

## UC-022-3: Danh sách Rules theo Policy

**GET** `/api/v1/abac/policies/{policyId}/rules`

Response: `List<RuleView>` — ordered by `orderIndex` ASC

---

## UC-022-4: Cập nhật Rule

**PUT** `/api/v1/abac/rules/{id}`

Cho phép cập nhật toàn bộ fields: `name`, `description`, `effect`, `targetExpression`, `conditionExpression`.

**Không** cho phép đổi `policyId` (move rule sang policy khác — out of scope Phase 1).

---

## UC-022-5: Xóa Rule

**DELETE** `/api/v1/abac/rules/{id}`

Sau khi xóa, cascade xóa expressions mồ côi (không còn rule nào tham chiếu).

---

## UC-022-6: Reorder Rules

**PATCH** `/api/v1/abac/policies/{policyId}/rules/order`

**Request**:
```json
{
  "orders": [
    { "ruleId": 3, "orderIndex": 1 },
    { "ruleId": 1, "orderIndex": 2 },
    { "ruleId": 2, "orderIndex": 3 }
  ]
}
```

Dùng khi admin kéo thả thứ tự rules (quan trọng với `FIRST_APPLICABLE` algorithm).

---

## Response Models

### `RuleView`
```json
{
  "id": 3,
  "policyId": 1,
  "name": "MANAGER read own dept only",
  "description": "MANAGER chỉ được đọc nhân viên trong department mình quản lý",
  "effect": "PERMIT",
  "orderIndex": 3,
  "targetExpression": {
    "id": 11,
    "type": "LITERAL",
    "spelExpression": "subject.roles.contains('MANAGER') && action.getAttribute('name') == 'READ'"
  },
  "conditionExpression": {
    "id": 12,
    "type": "LITERAL",
    "spelExpression": "object.data == null || subject.getAttribute('managedDepartments').contains(object.data.department)"
  }
}
```

---

## Ghi chú thiết kế

### SpEL validation
Khi admin nhập raw SpEL, server cần validate bằng Spring Expression Language parser — không phải chỉ check non-empty. Parse error nên trả thông tin dòng/cột lỗi nếu có thể.

```java
ExpressionParser parser = new SpelExpressionParser();
try {
    parser.parseExpression(spelExpression);
} catch (ParseException e) {
    throw new DomainException(AbacErrorCode.INVALID_SPEL_EXPRESSION, e.getMessage());
}
```

### `conditionExpression` optional
Nếu không truyền `conditionExpression`, rule luôn áp dụng khi target match (không có extra condition).

### `targetExpression` optional
Nếu không truyền `targetExpression`, rule match mọi request (wildcard). Dùng cẩn thận với DENY rules.

### SpEL variables available ở runtime
| Variable  | Type          | Description                          |
|-----------|---------------|--------------------------------------|
| `subject` | `Subject`     | `.roles` (List), `.getAttribute(key)` |
| `action`  | `Action`      | `.getAttribute('name')`              |
| `resource`| `Resource`    | `.name`, `.data` (instance object)   |
| `object`  | `Resource`    | Alias cho `resource` (backward compat)|
| `env`     | `Environment` | `.getAttribute(key)`, `.serviceName` |
