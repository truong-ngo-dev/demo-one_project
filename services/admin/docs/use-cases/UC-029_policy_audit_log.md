# UC-029: Policy Decision Audit Log

## Tóm tắt
Ghi lại mọi PERMIT/DENY decision từ PepEngine vào bảng `policy_audit_log`. Admin có thể query log để debug policy, trace tại sao một user bị từ chối, và đáp ứng yêu cầu compliance.

## Actor
System (tự động sau mỗi enforcement decision) / BQL SUPER_ADMIN (query log)

## Trạng thái
Planned

---

## UC-029-1: Schema

```sql
-- Migration V4__policy_audit_log.sql
CREATE TABLE policy_audit_log (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      VARCHAR(100)                   NOT NULL,
    resource     VARCHAR(200)                   NOT NULL,
    action       VARCHAR(100)                   NOT NULL,
    decision     ENUM('PERMIT', 'DENY')         NOT NULL,
    policy_set   VARCHAR(200),
    details      TEXT,
    requested_at BIGINT                         NOT NULL,
    INDEX idx_audit_user    (user_id),
    INDEX idx_audit_time    (requested_at),
    INDEX idx_audit_decision (decision)
);
```

---

## UC-029-2: AuditLogService

```java
// application/audit/log_decision/LogPolicyDecision.java
public class LogPolicyDecision {
    public record Command(
        String userId,
        String resource,
        String action,
        String decision,    // "PERMIT" | "DENY"
        String policySet,
        Object details,
        long requestedAt
    ) {}
    // Handler: async @EventListener hoặc direct save sau enforce
}
```

**Ghi async** để không block request thread:

```java
@Async
public void log(LogPolicyDecision.Command command) { ... }
```

---

## UC-029-3: Tích hợp với AuthorizationAspect

Sau khi `pepEngine.enforce()` trả decision:

```java
// Trong AuthorizationAspect (libs/abac) — cần thêm callback hook
// HOẶC: override AuthorizationAspect trong admin service bằng subclass
```

**Cách đơn giản hơn**: tạo `AdminAuthorizationAspect extends AuthorizationAspect` trong admin service, override `preEnforce()` để log sau khi call `super.preEnforce()`.

---

## UC-029-4: Query API

**GET** `/api/v1/abac/audit-log`

Params: `userId`, `resource`, `action`, `decision`, `from` (epoch ms), `to`, `page`, `size`

**Response**:
```json
{
  "data": [
    {
      "id": 123,
      "userId": "abc-xyz",
      "resource": "user",
      "action": "DELETE",
      "decision": "DENY",
      "policySet": "bql-root",
      "requestedAt": "2026-04-07T10:30:00Z"
    }
  ],
  "meta": { "page": 0, "size": 20, "total": 45 }
}
```

---

## UC-029-5: UI — Audit Log page

**Route**: `/admin/abac/audit-log`

**UI**:
- Filter: userId input, resource select, decision select (ALL/PERMIT/DENY), date range
- Table: userId, resource, action, decision badge, timestamp
- Click row → expand details (raw policy evaluation context)

---

## Retention Policy

- Default: giữ 90 ngày.
- Configurable qua `application.yml`: `abac.audit.retention-days=90`
- Cleanup job: Spring `@Scheduled` xóa records cũ hơn retention threshold.

---

## Ghi chú thiết kế

- Audit log là **append-only** — không update, không delete manually.
- Không log evaluate batch (UC-023-6 UIElement evaluate) — quá nhiều traffic, không phải enforcement path thực.
- Simulate (UC-024) không log — đây là test traffic.
- Phase 2: chỉ log admin-service decisions. Khi enforcement mở rộng sang các service khác → centralized audit log (Phase 4+).
