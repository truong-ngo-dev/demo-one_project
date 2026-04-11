# UC-035 — Admin Change Audit Log

## Mô tả

Ghi lại mọi thay đổi tạo / cập nhật / xóa đối với PolicySet, Policy, Rule, và UIElement.
Admin có thể query log theo entity type, entity ID, hoặc performer.

---

## Actors

- **System** — tự động ghi log sau mỗi command thành công
- **Admin** — query audit log

---

## Domain Objects

| Object              | Package                                              |
|---------------------|------------------------------------------------------|
| `AbacAuditLog`      | `domain.abac.audit`                                  |
| `AbacAuditLogEvent` | `domain.abac.audit` (extends `AbstractDomainEvent`)  |
| `AuditActionType`   | `domain.abac.audit` (CREATED, UPDATED, DELETED)      |
| `AuditEntityType`   | `domain.abac.audit` (POLICY_SET, POLICY, RULE, UI_ELEMENT) |

---

## Infrastructure

- **`AuditLogEventHandler`** — `EventHandler<AbacAuditLogEvent>`, persists `AbacAuditLog`
- **`AuditHelper`** — `currentPerformedBy()` từ `SecurityContextHolder`
- **Migration** — `V4__create_abac_audit_log.sql`

---

## Command Handlers (audit event dispatch)

Mỗi command handler dưới đây inject `EventDispatcher` + `ObjectMapper`, ghi snapshot JSON, rồi dispatch `AbacAuditLogEvent`:

| Handler                   | Entity       | Action  | Snapshot fields                              |
|---------------------------|--------------|---------|----------------------------------------------|
| `CreatePolicySet.Handler` | POLICY_SET   | CREATED | `{name, combineAlgorithm, isRoot}`           |
| `UpdatePolicySet.Handler` | POLICY_SET   | UPDATED | `{name, combineAlgorithm, isRoot}`           |
| `DeletePolicySet.Handler` | POLICY_SET   | DELETED | null                                         |
| `CreatePolicy.Handler`    | POLICY       | CREATED | `{name, combineAlgorithm}`                   |
| `UpdatePolicy.Handler`    | POLICY       | UPDATED | `{name, combineAlgorithm}`                   |
| `DeletePolicy.Handler`    | POLICY       | DELETED | null                                         |
| `CreateRule.Handler`      | RULE         | CREATED | `{name, effect, targetExpression, conditionExpression}` |
| `UpdateRule.Handler`      | RULE         | UPDATED | `{name, effect, targetExpression, conditionExpression}` |
| `DeleteRule.Handler`      | RULE         | DELETED | null                                         |
| `CreateUIElement.Handler` | UI_ELEMENT   | CREATED | `{elementId, label, type, resourceId, actionId}` |
| `UpdateUIElement.Handler` | UI_ELEMENT   | UPDATED | `{elementId, label, type, resourceId, actionId}` |
| `DeleteUIElement.Handler` | UI_ELEMENT   | DELETED | null                                         |

---

## Query

**Use case:** `application.audit.list_audit_log.ListAuditLog`

**API:** `GET /api/v1/abac/audit-log`

| Param        | Type           | Required | Mô tả                     |
|--------------|----------------|----------|---------------------------|
| `entityType` | `AuditEntityType` | No    | Filter theo entity type   |
| `entityId`   | `Long`         | No       | Filter theo entity ID     |
| `performedBy`| `String`       | No       | Filter theo performer     |
| `page`       | `Integer`      | No       | Default 0                 |
| `size`       | `Integer`      | No       | Default 20                |

**Response:** `Page<AuditLogEntry>` gồm: `id, entityType, entityId, entityName, actionType, performedBy, changedAt, snapshotJson`
