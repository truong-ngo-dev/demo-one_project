# Phase 4 — ABAC Management UI

## Bối cảnh

Đây là Admin Portal Angular (stack: Angular 21, Angular Material, Tailwind CSS, Signals).
Backend ABAC đã fully implemented — tất cả UC-019 đến UC-036 đều ở trạng thái Implemented.
Phase 4 chỉ build **Angular UI** — không sửa backend.

Angular gọi API qua Web Gateway (không bao giờ gọi trực tiếp admin service).
Auth dùng SESSION cookie — Angular không lưu/đọc token.

Phase 2 (Angular Shell) đã hoàn tất: có routing `/admin/**`, layout shell (sidebar + header),
auth flow, UIElement visibility map, route guard, và 403 handler.
Phase 3 (User & Role UI) đã hoàn tất: có user/role management screens.

---

## Đọc bắt buộc trước khi implement

| File | Mục đích |
|------|----------|
| `web/CLAUDE.md` | Angular conventions (Standalone, Signals, Material, Tailwind, auth rules) |
| `services/admin/SERVICE_MAP.md` §Presentation | Toàn bộ API endpoints ABAC |
| `services/admin/docs/domains/abac.md` | Domain model, response shapes, error codes |
| UC tương ứng từng task (xem bên dưới) | Flow, request/response chi tiết |

---

## Task 4.1 — Resource + Action CRUD

**Route**: `/admin/abac/resources`

**Backend API** (qua Web Gateway):
```
GET    /api/v1/abac/resources?keyword=&page=&size=   → Page<ResourceSummaryView>
POST   /api/v1/abac/resources
GET    /api/v1/abac/resources/{id}                   → detail kèm danh sách actions
PUT    /api/v1/abac/resources/{id}                   → update description/serviceName
DELETE /api/v1/abac/resources/{id}                   → guard: 409 nếu có UIElement ref
POST   /api/v1/abac/resources/{resourceId}/actions
PATCH  /api/v1/abac/resources/{resourceId}/actions/{actionId}
DELETE /api/v1/abac/resources/{resourceId}/actions/{actionId}  → guard: 409 nếu có UIElement ref
```

**UC tham khảo**: `services/admin/docs/use-cases/UC-019_resource_action_catalogue.md`

**UI requirements**:
- Trang list resources: paginated table + search keyword. Mỗi row: name, serviceName, actionCount, actions (edit/delete).
- Click row → expand panel hoặc navigate detail: hiển thị danh sách actions của resource đó.
- Create resource: dialog form (name, description, serviceName). `name` immutable sau khi tạo.
- Create action trong resource: inline form hoặc dialog (name, description, isStandard).
- `name` của cả resource và action là immutable — form edit không cho sửa name field.
- Error 409 (`RESOURCE_IN_USE`, `ACTION_IN_USE`) → snackbar "Không thể xóa — đang được UIElement tham chiếu".

---

## Task 4.2 — PolicySet / Policy / Rule CRUD + Reorder

**Routes**:
- `/admin/abac/policy-sets` → list policy sets
- `/admin/abac/policy-sets/:id` → detail: hiển thị policies của set
- `/admin/abac/policy-sets/:policySetId/policies/:policyId` → detail: hiển thị rules của policy

**Backend API**:
```
# PolicySet (UC-020)
GET    /api/v1/abac/policy-sets
GET    /api/v1/abac/policy-sets/{id}
POST   /api/v1/abac/policy-sets
PUT    /api/v1/abac/policy-sets/{id}          (scope, combineAlgorithm, isRoot)
DELETE /api/v1/abac/policy-sets/{id}
GET    /api/v1/abac/policy-sets/{id}/delete-preview  → { policyCount, ruleCount }

# Policy (UC-021)
GET    /api/v1/abac/policy-sets/{policySetId}/policies
GET    /api/v1/abac/policies/{id}
POST   /api/v1/abac/policies
PUT    /api/v1/abac/policies/{id}             (targetExpression, combineAlgorithm)
DELETE /api/v1/abac/policies/{id}
GET    /api/v1/abac/policies/{id}/delete-preview  → { ruleCount }

# Rule (UC-022)
GET    /api/v1/abac/policies/{policyId}/rules
POST   /api/v1/abac/rules
PUT    /api/v1/abac/rules/{id}
DELETE /api/v1/abac/rules/{id}
PATCH  /api/v1/abac/policies/{policyId}/rules/order   → reorder
```

**UC tham khảo**: `UC-020_policy_set_management.md`, `UC-021_policy_management.md`, `UC-022_rule_management.md`

**UI requirements**:
- PolicySet list: badge hiển thị isRoot. Chỉ 1 set được đánh dấu isRoot.
- Delete preview: dialog confirm hiển thị "Sẽ xóa X policies, Y rules".
- Policy detail: list rules ordered by `orderIndex` ASC. Hỗ trợ **drag-and-drop reorder** → gọi `PATCH /rules/order`. Quan trọng với FIRST_APPLICABLE algorithm.
- Rule form: raw SpEL textarea cho `targetExpression` và `conditionExpression`.
  - Cả hai đều **optional** (xem UC-022 ghi chú thiết kế).
  - Validate SpEL phía server (400 + message) — hiển thị lỗi inline dưới textarea.
  - Hint: bảng SpEL variables available (`subject`, `action`, `resource`/`object`, `env`).
- `combineAlgorithm` dropdown: `DENY_OVERRIDES`, `PERMIT_OVERRIDES`, `DENY_UNLESS_PERMIT`, `PERMIT_UNLESS_DENY`, `FIRST_APPLICABLE`, `ONLY_ONE_APPLICABLE`.
- Rule effect radio: `PERMIT` / `DENY`.

---

## Task 4.3 — UIElement Registry + Coverage Indicator

**Route**: `/admin/abac/ui-elements`

**Backend API**:
```
GET    /api/v1/abac/ui-elements?resourceId=&type=&group=&page=&size=
GET    /api/v1/abac/ui-elements/{id}       → kèm hasPolicyCoverage
GET    /api/v1/abac/ui-elements/uncovered  → list uncovered elements
POST   /api/v1/abac/ui-elements
PUT    /api/v1/abac/ui-elements/{id}
DELETE /api/v1/abac/ui-elements/{id}
```

**UC tham khảo**: `UC-023_ui_element_registry.md`, `UC-036_ui_element_policy_coverage.md`

**UI requirements**:
- List table với filter: resourceId dropdown, type dropdown (BUTTON/TAB/MENU_ITEM), group text input.
- Column `hasPolicyCoverage`: badge `COVERED` (xanh) / `UNCOVERED` (đỏ).
- Tab hoặc button "Xem uncovered" → gọi `/uncovered` → hiển thị danh sách rút gọn alert-style.
- Create/Edit form:
  - `elementId`: string, immutable sau khi tạo. Convention hint: `{type}:{resource}:{action-slug}`.
  - `resourceId` dropdown → sau khi chọn, `actionId` dropdown filter theo resource đó.
  - `type`: BUTTON / TAB / MENU_ITEM.
  - `group`: optional text (tên màn hình chứa element).
- Error 409 (`UI_ELEMENT_ID_DUPLICATE`) → form inline error.

---

## Task 4.4 — Policy Simulator

**Route**: `/admin/abac/simulator`

**Backend API**:
```
POST /api/v1/abac/simulate
```

Request:
```json
{
  "subject": { "userId": null, "roles": ["MANAGER"], "attributes": {} },
  "resource": { "name": "employee", "data": null },
  "action": "READ",
  "policySetId": null
}
```

Response kèm `trace[]` per-rule:
```json
{
  "decision": "PERMIT",
  "policySetName": "bql-root",
  "details": [...],
  "trace": [
    {
      "ruleId": "3",
      "ruleDescription": "MANAGER read own dept only",
      "effect": "PERMIT",
      "targetMatched": true,
      "conditionMatched": true,
      "wasDeciding": true
    }
  ]
}
```

**UC tham khảo**: `UC-024_policy_simulate.md`, `UC-033_evaluation_trace.md`

**UI requirements**:
- Form trái: nhập virtual subject (roles: chip input, attributes: key-value builder), resource name (dropdown từ `/api/v1/abac/resources`), action (dropdown filter theo resource đã chọn), resource.data (JSON textarea — optional).
- `userId` optional: nếu nhập → server merge DB user data.
- `policySetId` optional: nếu để trống → dùng root PolicySet.
- Kết quả phải:
  - Badge lớn `PERMIT` (xanh) / `DENY` (đỏ).
  - Trace table: mỗi rule một row — ruleName, effect, targetMatched ✓/✗, conditionMatched ✓/✗/— (null khi target không match), wasDeciding (bold nếu true).
  - `conditionMatched = null` → hiển thị "—".
- `resource.data = null` → navigation-mode (không có instance data). Note này hiển thị trong UI.

---

## Task 4.5 — Navigation Simulate

**Route**: `/admin/abac/simulator/navigation` (tab hoặc sub-route của Simulator)

**Backend API**:
```
POST /api/v1/abac/simulate/navigation
GET  /api/v1/abac/simulate/reverse?resourceName=X&actionName=Y&policySetId=N
```

Navigation simulate request:
```json
{
  "subject": { "roles": ["MANAGER"], "attributes": {} },
  "resourceName": "employee",
  "policySetId": null
}
```

Response:
```json
{
  "data": {
    "resourceName": "employee",
    "decisions": [
      { "action": "CREATE", "decision": "DENY",   "matchedRuleName": null },
      { "action": "READ",   "decision": "PERMIT",  "matchedRuleName": "MANAGER read own dept" }
    ]
  }
}
```

Reverse lookup response:
```json
{
  "resourceName": "employee",
  "actionName": "READ",
  "permitRules": [
    {
      "ruleName": "MANAGER read own dept only",
      "policyName": "policy:employee",
      "effect": "PERMIT",
      "requiredRoles": ["MANAGER"],
      "requiredAttributes": ["managedDepartments"],
      "hasInstanceCondition": true,
      "userCountByRole": 5,
      "userCountNote": null
    }
  ],
  "denyRules": []
}
```

**UC tham khảo**: `UC-031_navigation_simulate.md`, `UC-034_reverse_lookup.md`

**UI requirements**:
- Form: subject builder (roles chip input + attributes key-value) + resource dropdown. Submit → bảng actions × PERMIT/DENY.
- `matchedRuleName` hiển thị dưới decision badge (nếu có).
- "Xem reverse lookup" button tại mỗi row action → side panel hoặc dialog gọi `/simulate/reverse`.
- Reverse lookup panel: 2 cột permitRules / denyRules. Mỗi rule: policyName, requiredRoles (chips), requiredAttributes (chips), hasInstanceCondition (badge), userCountByRole.
- Subject builder có thể tái dùng component đã tạo ở Task 4.4.

---

## Task 4.6 — Audit Log Viewer

**Route**: `/admin/abac/audit-log`

**Backend API**:
```
GET /api/v1/abac/audit-log?entityType=&entityId=&performedBy=&page=&size=
```

Response `Page<AuditLogEntry>`:
```json
{
  "id": 1,
  "entityType": "RULE",
  "entityId": 42,
  "entityName": "MANAGER read own dept only",
  "actionType": "UPDATED",
  "performedBy": "admin@example.com",
  "changedAt": 1712345678000,
  "snapshotJson": "{\"name\":\"...\",\"effect\":\"PERMIT\"}"
}
```

**UC tham khảo**: `UC-035_admin_change_audit_log.md`

**UI requirements**:
- Paginated table, default sort `changedAt` DESC.
- Filter bar: entityType dropdown (POLICY_SET / POLICY / RULE / UI_ELEMENT), entityId number input, performedBy text input.
- `actionType` column: badge màu — CREATED (xanh), UPDATED (cam), DELETED (đỏ).
- `changedAt`: format `dd/MM/yyyy HH:mm:ss`.
- Click row → expand inline hoặc dialog hiển thị `snapshotJson` dạng formatted JSON (pre/code block).
- `snapshotJson = null` (DELETED actions) → hiển thị "Không có snapshot".

---

## Conventions bắt buộc (từ web/CLAUDE.md)

- **Standalone components** — không dùng NgModule.
- **Signals** làm state mặc định. RxJS chỉ cho HTTP calls.
- **Lazy loading** với `loadComponent()`.
- **Angular Material** cho form, table, dialog, chip, badge, snackbar.
- **Tailwind** cho layout/spacing — không override Material styles.
- Theme: Light, minimal. Primary color: Slate `#475569`.
- **Không lưu token**. Mọi API qua Web Gateway.
- **401** → redirect về login, không retry.

---

## Thứ tự implement

```
4.1 → 4.2 → 4.3 → 4.4 → 4.5 → 4.6
```

**Dependency**:
- 4.2 cần resource list từ 4.1 (action dropdown trong Rule form).
- 4.4 cần resource list từ 4.1 (resource/action dropdown trong Simulator form).
- 4.5 có thể tái dùng subject-builder component từ 4.4.
