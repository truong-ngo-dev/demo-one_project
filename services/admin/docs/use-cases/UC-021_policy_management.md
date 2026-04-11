# UC-021: Policy Management

## Tóm tắt
Admin quản lý Policy trong một Policy Set. Policy định nghĩa target (resource nào áp dụng) và combining algorithm cho các Rules.

## Actor
BQL SUPER_ADMIN

## Trạng thái
Planned

---

## UC-021-1: Tạo Policy

**POST** `/api/v1/abac/policies`

**Request**:
```json
{
  "policySetId": 1,
  "name": "policy:employee",
  "targetExpression": {
    "type": "LITERAL",
    "spelExpression": "resource.name == 'employee'"
  },
  "combineAlgorithm": "DENY_UNLESS_PERMIT"
}
```

**Flow**:
1. Validate `policySetId` tồn tại
2. Validate `targetExpression.spelExpression` là SpEL hợp lệ (parse check)
3. Persist Expression, sau đó persist Policy

**Response**: `201 Created` — `PolicyView`

**Error**: SpEL parse fail → `400 Bad Request` với message "Invalid SpEL expression: ..."

---

## UC-021-2: Lấy Policy theo ID

**GET** `/api/v1/abac/policies/{id}`

Response: `PolicyView` bao gồm `targetExpression` và danh sách rules (summary)

---

## UC-021-3: Danh sách Policies theo PolicySet

**GET** `/api/v1/abac/policy-sets/{policySetId}/policies`

Response: `List<PolicySummaryView>`

---

## UC-021-4: Cập nhật Policy

**PATCH** `/api/v1/abac/policies/{id}`

Cho phép cập nhật: `targetExpression`, `combineAlgorithm`. Không đổi `name` hoặc `policySetId`.

---

## UC-021-5: Xóa Policy

**DELETE** `/api/v1/abac/policies/{id}`

Cascade xóa tất cả Rules trong Policy này.

**GET** `/api/v1/abac/policies/{id}/delete-preview`  
Response: `{ "ruleCount": 7 }`

---

## Response Models

### `PolicyView`
```json
{
  "id": 1,
  "policySetId": 1,
  "name": "policy:employee",
  "targetExpression": {
    "id": 10,
    "type": "LITERAL",
    "spelExpression": "resource.name == 'employee'"
  },
  "combineAlgorithm": "DENY_UNLESS_PERMIT",
  "rules": [
    { "id": 1, "name": "All actions for HR_ADMIN", "effect": "PERMIT", "orderIndex": 1 },
    { "id": 2, "name": "MANAGER can LIST",          "effect": "PERMIT", "orderIndex": 2 }
  ]
}
```

---

## Ghi chú thiết kế

**SpEL target expression context**:
- `resource` — `Resource` object (có `.name`, `.data`)
- `action` — `Action` object (có `.getAttribute('name')`)
- `subject` — `Subject` object (có `.roles`, `.getAttribute(...)`)
- `environment` — `Environment` object

Khi Phase 2 implement Visual Builder, `targetExpression` vẫn được lưu là raw SpEL nhưng Builder sẽ generate ra. Phase 1 admin nhập thủ công.
