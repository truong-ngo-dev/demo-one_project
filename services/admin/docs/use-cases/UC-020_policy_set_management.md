# UC-020: Policy Set Management

## Tóm tắt
Admin quản lý Policy Set — root container của cây policy ABAC.

## Actor
BQL SUPER_ADMIN

## Trạng thái
Planned

---

## UC-020-1: Tạo Policy Set

**POST** `/api/v1/abac/policy-sets`

**Request**:
```json
{
  "name": "bql-root",
  "scope": "OPERATOR",
  "combineAlgorithm": "DENY_OVERRIDES",
  "isRoot": true
}
```

**Invariant**: `name` unique. Chỉ 1 PolicySet được đánh dấu `isRoot = true` tại một thời điểm.

**Flow**:
1. Validate name unique
2. Nếu `isRoot = true`: unset `isRoot` trên PolicySet hiện tại (nếu có)
3. Persist

**Response**: `201 Created` — `PolicySetView`

---

## UC-020-2: Lấy Policy Set theo ID

**GET** `/api/v1/abac/policy-sets/{id}`

Response: `PolicySetView` bao gồm danh sách `policies` (summary — không load rule chi tiết)

---

## UC-020-3: Danh sách Policy Sets

**GET** `/api/v1/abac/policy-sets?keyword=&page=&size=`

Response: `Page<PolicySetSummaryView>` — name, scope, combineAlgorithm, isRoot, policyCount

---

## UC-020-4: Cập nhật Policy Set

**PATCH** `/api/v1/abac/policy-sets/{id}`

Cho phép cập nhật: `scope`, `combineAlgorithm`, `isRoot`. Không đổi `name`.

---

## UC-020-5: Xóa Policy Set

**DELETE** `/api/v1/abac/policy-sets/{id}`

Guard: Xóa cascade tất cả Policies và Rules trong PolicySet này (DB ON DELETE CASCADE).  
Cần confirm trước khi xóa — response preview số policies/rules bị xóa.

**GET** `/api/v1/abac/policy-sets/{id}/delete-preview`  
Response: `{ "policyCount": 3, "ruleCount": 15 }`

---

## Response Models

### `PolicySetView`
```json
{
  "id": 1,
  "name": "bql-root",
  "scope": "OPERATOR",
  "combineAlgorithm": "DENY_OVERRIDES",
  "isRoot": true,
  "tenantId": null,
  "policies": [
    { "id": 1, "name": "policy:employee", "ruleCount": 7 },
    { "id": 2, "name": "policy:user", "ruleCount": 5 }
  ]
}
```

### `PolicySetSummaryView`
```json
{
  "id": 1,
  "name": "bql-root",
  "scope": "OPERATOR",
  "combineAlgorithm": "DENY_OVERRIDES",
  "isRoot": true,
  "policyCount": 3
}
```

---

## Ghi chú thiết kế

- `combineAlgorithm` map trực tiếp sang `CombineAlgorithmName` trong libs/abac
- Giá trị hợp lệ: `DENY_OVERRIDES`, `PERMIT_OVERRIDES`, `DENY_UNLESS_PERMIT`, `PERMIT_UNLESS_DENY`, `FIRST_APPLICABLE`, `ONLY_ONE_APPLICABLE`
- `scope` dự phòng cho multi-tenant — Phase 1 chỉ dùng `OPERATOR`
