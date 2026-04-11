Prompt: Phase 3 Batch 2 (BE) — Admin Change Audit Log + UIElement Coverage (UC-035, UC-036)

Vai trò: Bạn là Senior Backend Engineer thực hiện Phase 3 Batch 2.
Phase 3 Batch 1 đã xong (Trace, Reverse Lookup).
Batch 2 implement: (1) Audit Log theo dõi thay đổi của admin trong ABAC console,
(2) UIElement policy coverage indicator.

Tài liệu căn cứ:
  1. Design: @docs/business_analysis/abac_admin_console_design.md (Section 6)
  2. Service Map: @services/admin/SERVICE_MAP.md
  3. Convention: @docs/conventions/ddd-structure.md
  4. Rule domain: @services/admin/src/main/java/vn/truongngo/apartcom/one/service/admin/domain/abac/policy/RuleDefinition.java
  5. CreateRule use case: @services/admin/src/main/java/vn/truongngo/apartcom/one/service/admin/application/rule/command/create_rule/CreateRule.java
  6. UIElement domain: tìm UIElementDefinition.java trong services/admin domain
  7. GetRuleImpactPreview: @services/admin/src/main/java/vn/truongngo/apartcom/one/service/admin/application/rule/query/impact_preview/GetRuleImpactPreview.java
  8. PolicyRepository: @services/admin/src/main/java/vn/truongngo/apartcom/one/service/admin/domain/abac/policy/PolicyRepository.java
  9. libs/common EventDispatcher pattern: @libs/common/CLAUDE.md

Context quan trọng:
  - Admin identity: lấy từ `SecurityContextHolder.getContext().getAuthentication().getName()`
    → đây là userId (UUID string) được set bởi JWT filter của Web Gateway
  - Convention: dùng domain event publish qua EventDispatcher khi có mutation, handler lắng nghe + ghi log
  - Migration file: bắt buộc có, đặt tại `services/admin/src/main/resources/db/migration/`
    — đọc các file migration hiện có để xác định version tiếp theo

---

## UC-035 — Admin Change Audit Log

### Mô tả
Ghi lại mọi thay đổi admin thực hiện trên: PolicySet, Policy, Rule, UIElement.
Mỗi entry: entityType, entityId, entityName, actionType (CREATED/UPDATED/DELETED), performedBy (adminId), changedAt, snapshotJson.

### Nhiệm vụ

  1. Domain — `domain/abac/audit/`
     - `AuditActionType.java` (enum): `CREATED, UPDATED, DELETED`
     - `AuditEntityType.java` (enum): `POLICY_SET, POLICY, RULE, UI_ELEMENT`
     - `AbacAuditLog.java` (JPA Entity):
       ```java
       @Entity @Table(name = "abac_audit_log")
       public class AbacAuditLog {
           @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
           private Long id;
           @Enumerated(EnumType.STRING)
           private AuditEntityType entityType;
           private Long entityId;
           private String entityName;
           @Enumerated(EnumType.STRING)
           private AuditActionType actionType;
           private String performedBy;  // userId string
           private Long changedAt;      // epoch millis
           @Column(columnDefinition = "TEXT")
           private String snapshotJson; // JSON của entity sau thay đổi
       }
       ```
     - `AbacAuditLogRepository.java` (Spring Data JPA):
       - `findAllByOrderByChangedAtDesc(Pageable pageable)`
       - `findByEntityType(AuditEntityType type, Pageable pageable)`
       - `findByEntityTypeAndEntityId(AuditEntityType type, Long id, Pageable pageable)`
       - `findByPerformedBy(String performedBy, Pageable pageable)`
     - `AbacAuditLogEvent.java` (domain event, không phải entity):
       ```java
       public record AbacAuditLogEvent(
           AuditEntityType entityType,
           Long entityId,
           String entityName,
           AuditActionType actionType,
           String performedBy,
           String snapshotJson
       ) {}
       ```

  2. Migration file: `V{N}__create_abac_audit_log.sql`
     ```sql
     CREATE TABLE abac_audit_log (
         id BIGINT AUTO_INCREMENT PRIMARY KEY,
         entity_type VARCHAR(30) NOT NULL,
         entity_id BIGINT NOT NULL,
         entity_name VARCHAR(255),
         action_type VARCHAR(20) NOT NULL,
         performed_by VARCHAR(100),
         changed_at BIGINT NOT NULL,
         snapshot_json TEXT,
         INDEX idx_audit_entity (entity_type, entity_id),
         INDEX idx_audit_performer (performed_by),
         INDEX idx_audit_changed_at (changed_at)
     );
     ```

  3. Infrastructure — `AuditLogEventHandler.java`:
     - Lắng nghe `AbacAuditLogEvent` (Spring @EventListener)
     - Persist vào `AbacAuditLogRepository`
     - Đặt `changedAt = System.currentTimeMillis()` khi persist (không từ event, tránh clock skew)

  4. Publish event — bổ sung vào các Command Handlers hiện có:
     - `CreateRule.Handler`, `UpdateRule.Handler`, `DeleteRule.Handler`
     - `CreatePolicy.Handler`, `UpdatePolicy.Handler`, `DeletePolicy.Handler`
     - `CreatePolicySet.Handler`, `UpdatePolicySet.Handler`, `DeletePolicySet.Handler`
     - `CreateUIElement.Handler`, `UpdateUIElement.Handler`, `DeleteUIElement.Handler`

     Trong mỗi handler sau khi commit thành công:
     ```java
     eventDispatcher.dispatch(new AbacAuditLogEvent(
         AuditEntityType.RULE,
         savedRule.getId().getValue(),
         savedRule.getName(),
         AuditActionType.CREATED,
         SecurityContextHolder.getContext().getAuthentication().getName(),
         objectMapper.writeValueAsString(ruleSnapshot)  // simple DTO, không cần toàn bộ graph
     ));
     ```
     `snapshotJson`: chỉ cần các field key (name, effect, targetExpression, conditionExpression) —
     không serialize toàn bộ entity graph.

  5. Application Layer — `application/audit/list_audit_log/ListAuditLog.java`:
     - Query:
       ```java
       public record Query(
           AuditEntityType entityType,  // null = tất cả
           Long entityId,               // null = tất cả
           String performedBy,          // null = tất cả
           int page, int size
       ) {}
       ```
     - Result: Page<AuditLogEntry> where AuditLogEntry is a record View type

  6. Presentation Layer — `AuditLogController.java`:
     ```
     GET /api/v1/abac/audit-log
     Query params: entityType, entityId, performedBy, page (0), size (20)
     Response: PagedApiResponse<AuditLogEntry>
     ```

  7. UC Doc: `UC-035_audit_log.md`, cập nhật `UC-000_index.md`

---

## UC-036 — UIElement Policy Coverage

### Mô tả
Với mỗi UIElement, xác định liệu có ít nhất một PERMIT Rule trong hệ thống cover `resource + action` của nó.
Dùng để cảnh báo admin: element mới → không có rule → mặc định DENY → user không thấy button.

### Nhiệm vụ

  1. Application Layer — bổ sung vào `GetUIElement.Handler` và `ListUIElements.Handler`:
     - Thêm field `hasPolicyCoverage: boolean` vào `UIElementView` (hoặc tạo `UIElementCoverageView` riêng)
     - Logic xác định coverage:
       a. Lấy `resourceName` + `actionName` của UIElement
       b. Load tất cả rules qua `PolicyRepository.findAll()`
       c. Với mỗi rule, parse `targetExpression` bằng SpEL AST walker
       d. Check: `specificActions.contains(actionName)` (hoặc specificActions rỗng = áp dụng tất cả)
          VÀ rule.effect = PERMIT
       → nếu tìm được ít nhất 1 rule → `hasPolicyCoverage = true`
     - Tái dùng SpEL AST walker static helper từ GetRuleImpactPreview (UC-032)

  2. Thêm endpoint liệt kê UIElement thiếu coverage:
     ```
     GET /api/v1/abac/ui-elements/uncovered
     Response: ApiResponse<List<UIElementView>>  // chỉ trả elements có hasPolicyCoverage = false
     ```

  3. UC Doc: `UC-036_uielement_coverage.md`, cập nhật `UC-000_index.md`

---

## Yêu cầu kỹ thuật chung
  - `ObjectMapper` inject trong handlers cần audit — dùng Jackson, đã có trong Spring Boot
  - Migration version: đọc file cao nhất hiện có trong `src/main/resources/db/migration/` → tăng +1
  - `SecurityContextHolder` available trong handlers vì request đi qua filter chain
  - Tách logic coverage check thành static helper trong package `application/rule/` hoặc `domain/abac/`
    để tái dùng giữa GetRuleImpactPreview, GetReverseLookup, và UIElement coverage
  - `mvn clean compile -DskipTests` phải thành công

---

## Yêu cầu Handoff (Bắt buộc)

  PHASE 3 BATCH 2 BE CONTEXT BLOCK
    - Migration file version thực tế
    - Endpoint list:
      GET /api/v1/abac/audit-log (params)
      GET /api/v1/abac/ui-elements/uncovered
    - UIElementView shape sau khi thêm hasPolicyCoverage

  FRONTEND CONTEXT BLOCK — Phase 3 Batch 2
    TypeScript interfaces:
      AuditLogEntry {
        id: number;
        entityType: 'POLICY_SET' | 'POLICY' | 'RULE' | 'UI_ELEMENT';
        entityId: number;
        entityName: string | null;
        actionType: 'CREATED' | 'UPDATED' | 'DELETED';
        performedBy: string | null;
        changedAt: number;
        snapshotJson: string | null;
      }
      AuditLogPage { data: AuditLogEntry[]; meta: { page, size, total } }

    UIElementView: thêm `hasPolicyCoverage: boolean`

    Endpoints:
      GET /api/admin/v1/abac/audit-log?entityType=RULE&page=0&size=20 → AuditLogPage
      GET /api/admin/v1/abac/ui-elements/uncovered → ApiResponse<UIElementView[]>
