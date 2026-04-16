Prompt: Expression System — Backend Refactor (BE)

Vai trò: Bạn là Senior Backend Engineer thực hiện refactor Expression System trong `services/admin`.
ABAC Phase 1–3 đã hoàn thành. Nhiệm vụ này thay thế `ExpressionVO` flat string bằng expression tree
có khả năng tái sử dụng và đúng chuẩn DDD.

Tài liệu căn cứ:
  1. Design spec: @docs/context/final_design/08_expression_system_design.md
  2. ABAC domain hiện tại: @services/admin/docs/domains/abac.md
  3. Convention: @docs/conventions/ddd-structure.md
  4. Service map: @services/admin/SERVICE_MAP.md

Files cần đọc trước khi implement:
  - @services/admin/src/main/java/.../domain/abac/policy/ExpressionVO.java
  - @services/admin/src/main/java/.../domain/abac/policy/RuleDefinition.java
  - @services/admin/src/main/java/.../domain/abac/policy/PolicyDefinition.java
  - @services/admin/src/main/java/.../infrastructure/persistence/abac/expression/AbacExpressionJpaEntity.java
  - @services/admin/src/main/java/.../infrastructure/persistence/abac/expression/AbacExpressionJpaRepository.java
  - @services/admin/src/main/java/.../infrastructure/persistence/abac/policy/PolicyMapper.java
  - @services/admin/src/main/java/.../application/rule/create_rule/CreateRule.java
  - @services/admin/src/main/java/.../application/rule/update_rule/UpdateRule.java
  - @services/admin/src/main/java/.../application/rule/delete_rule/DeleteRule.java
  - @services/admin/src/main/java/.../infrastructure/adapter/abac/AdminPolicyProvider.java
  - @services/admin/src/main/resources/db/migration/V3__abac_schema.sql

---

Phase 1 — DB + Domain model

  1. Migration V8
      File: `services/admin/src/main/resources/db/migration/V8__named_expression_and_tree_ref.sql`

      ```sql
      CREATE TABLE named_expression (
          id    BIGINT AUTO_INCREMENT PRIMARY KEY,
          name  VARCHAR(200) NOT NULL UNIQUE,
          spel  TEXT NOT NULL
      );

      ALTER TABLE abac_expression
        ADD COLUMN name                VARCHAR(200) NULL,
        ADD COLUMN named_expression_id BIGINT       NULL,
        ADD CONSTRAINT fk_expr_named
          FOREIGN KEY (named_expression_id) REFERENCES named_expression(id);
      ```

  2. Domain: `domain/abac/expression/` — package mới

      2a. `NamedExpressionId.java` — Value Object
          ```java
          public record NamedExpressionId(Long value) {
              public static NamedExpressionId of(Long value) { ... }
          }
          ```

      2b. `NamedExpression.java` — Aggregate Root
          Fields: id (NamedExpressionId), name (String), spel (String)
          Factory methods: `create(String name, String spel)`, `reconstitute(...)`
          Mutation methods: `rename(String newName)`, `updateSpel(String newSpel)` — trả instance mới (immutable style)

      2c. `NamedExpressionRepository.java` — domain interface
          ```java
          Optional<NamedExpression> findById(NamedExpressionId id);
          Optional<NamedExpression> findBySpel(String spel);
          List<NamedExpression> findAll();
          NamedExpression save(NamedExpression expr);
          void delete(NamedExpressionId id);
          boolean isInUse(NamedExpressionId id);  // check policy + rule FK refs
          ```

  3. Domain: `domain/abac/policy/ExpressionNode.java` — thay thế ExpressionVO

      ```java
      public sealed interface ExpressionNode
          permits ExpressionNode.Inline, ExpressionNode.LibraryRef, ExpressionNode.Composition {

          record Inline(String name, String spel) implements ExpressionNode {
              public boolean isNamed() { return name != null; }
          }
          record LibraryRef(NamedExpressionId refId) implements ExpressionNode {}
          record Composition(CombinationType operator,
                             List<ExpressionNode> children) implements ExpressionNode {}
      }
      ```

      Xóa `ExpressionVO.java`.

  4. Cập nhật `RuleDefinition.java`
      - `ExpressionVO targetExpression` → `ExpressionNode targetExpression`
      - `ExpressionVO conditionExpression` → `ExpressionNode conditionExpression`
      - Cập nhật `create()`, `reconstitute()`, `update()`

  5. Cập nhật `PolicyDefinition.java`
      - `ExpressionVO targetExpression` → `ExpressionNode targetExpression`
      - Cập nhật `reconstitute()`, update methods

---

Phase 2 — Infrastructure

  6. `NamedExpressionJpaEntity.java`
      Package: `infrastructure/persistence/abac/expression/`
      Fields: id, name, spel. Annotation: `@Entity @Table(name = "named_expression")`.

  7. `NamedExpressionJpaRepository.java`
      ```java
      public interface NamedExpressionJpaRepository extends JpaRepository<NamedExpressionJpaEntity, Long> {
          Optional<NamedExpressionJpaEntity> findBySpel(String spel);
      }
      ```

  8. `NamedExpressionPersistenceAdapter.java`
      Package: `infrastructure/adapter/repository/abac/`
      Implements `NamedExpressionRepository`.
      `isInUse()`: query bảng rule và policy xem có FK nào trỏ vào named_expression_id này không
      (qua `abac_expression.named_expression_id`).

  9. Cập nhật `AbacExpressionJpaEntity.java`
      Thêm:
      ```java
      @Column(name = "name", length = 200)
      private String name;

      @Column(name = "named_expression_id")
      private Long namedExpressionId;
      ```

  10. Cập nhật `AbacExpressionJpaRepository.java`
      Thêm:
      ```java
      Optional<AbacExpressionJpaEntity> findBySpelExpressionAndParentIdIsNull(String spel);
      List<AbacExpressionJpaEntity> findAllByParentId(Long parentId);
      List<AbacExpressionJpaEntity> findAllByNamedExpressionId(Long namedExpressionId);
      ```

---

Phase 3 — Application layer

  11. `ExpressionTreeService.java`
      Package: `application/expression/`

      ```java
      @Service
      public class ExpressionTreeService {

          /**
           * Persist cây ExpressionNode. Trả về DB root id (Long) để lưu vào rule/policy.
           * Logic:
           *   Inline: findBySpelExpressionAndParentIdIsNull → reuse id nếu tìm thấy; INSERT nếu không
           *   LibraryRef: INSERT row (type=LITERAL, named_expression_id=refId)
           *   Composition: INSERT row (type=COMPOSITION, combination_type=op), recurse children với parentId
           */
          Long persist(ExpressionNode node, Long parentId);

          /**
           * Resolve cây từ DB root id → SpEL string thuần để dùng ở PDP.
           * LibraryRef: load named_expression.spel qua namedExpressionId
           * Inline: dùng spel_expression trực tiếp
           * Composition: resolve children rồi join bằng op
           */
          String resolveSpel(Long rootId);

          /**
           * Delete cây bắt đầu từ rootId (selective):
           *   LITERAL, named_expression_id != null → DELETE row (LibraryRef row riêng, không xóa AR)
           *   LITERAL, named_expression_id == null, name == null → DELETE (anonymous inline)
           *   LITERAL, name != null → SKIP (named inline, có thể shared)
           *   COMPOSITION → recurse children, DELETE row
           */
          void deleteTree(Long rootId);

          /**
           * Reconstruct ExpressionNode tree từ DB root id — dùng bởi PolicyMapper.
           */
          ExpressionNode loadTree(Long rootId);
      }
      ```

  12. Cập nhật `CreateRule.Handler`
      - `Command` thêm `ExpressionNode targetExpression`, `ExpressionNode conditionExpression`
        thay vì `String targetExpression`, `String conditionExpression`
      - Validate SpEL trước khi persist: `SpelValidator.validate(ExpressionNode.resolveSpelForValidation(node))`
        (helper method — resolve toàn cây để lấy SpEL string, dùng LibraryRef.spel từ DB)
      - `Long targetId = expressionTreeService.persist(command.targetExpression(), null)`
      - Dùng id này khi tạo RuleDefinition

  13. Cập nhật `UpdateRule.Handler`
      - Tương tự CreateRule
      - Sau khi save thành công: cleanup expression tree cũ
        ```java
        if (oldTargetId != null && !Objects.equals(oldTargetId, newTargetId))
            expressionTreeService.deleteTree(oldTargetId);
        ```

  14. Cập nhật `DeleteRule.Handler`
      - Sau khi `policyRepository.save(updated)`:
        ```java
        if (rule.getTargetExpression() != null)
            expressionTreeService.deleteTree(/* root id từ rule */);
        if (rule.getConditionExpression() != null)
            expressionTreeService.deleteTree(/* root id */);
        ```
      - Lưu ý: phải lấy root id từ infrastructure layer trước khi remove rule khỏi aggregate

  15. Cập nhật `UpdatePolicy.Handler` và `DeletePolicy.Handler`
      - Cùng pattern với Rule — cleanup old target expression tree khi update/delete

  16. Cập nhật `PolicyMapper.java`
      - `toExprVO()` → `toExprNode(Long rootId)`: gọi `expressionTreeService.loadTree(rootId)`
      - Inject `ExpressionTreeService` vào mapper (hoặc inject vào `PolicyPersistenceAdapter` và truyền vào mapper)

  17. Cập nhật `AdminPolicyProvider.java`
      - Khi map expression: gọi `expressionTreeService.resolveSpel(rootId)` thay vì đọc `spelExpression` trực tiếp
      - Kết quả SpEL string vẫn đưa vào `Expression(type=LITERAL, spel)` của libs/abac — không thay đổi eval path

  18. Use case: `ListNamedExpressions.java`
      Package: `application/expression/list_named/`
      ```java
      record Result(List<NamedExpressionView> items) {}
      record NamedExpressionView(Long id, String name, String spel) {}
      ```
      Gọi `NamedExpressionRepository.findAll()`.

  19. Use case: `DeleteNamedExpression.java`
      Package: `application/expression/delete_named/`
      ```java
      record Command(Long id) {}
      ```
      - `namedExpressionRepository.isInUse(id)` → nếu true throw lỗi mới: `NAMED_EXPRESSION_IN_USE` (30014)
      - Xóa: `namedExpressionRepository.delete(id)`

  20. Thêm error code `NAMED_EXPRESSION_IN_USE = 30014` vào `PolicyErrorCode` (hoặc tạo enum riêng).

---

Phase 4 — Presentation layer

  21. Request model mới: `ExpressionNodeRequest.java`
      Package: `presentation/abac/model/`
      ```java
      public record ExpressionNodeRequest(
          String type,         // "INLINE" | "LIBRARY_REF" | "COMPOSITION"
          String name,         // nullable — cho INLINE
          String spel,         // nullable — cho INLINE
          Long refId,          // nullable — cho LIBRARY_REF
          String operator,     // nullable — cho COMPOSITION: "AND" | "OR"
          List<ExpressionNodeRequest> children  // nullable — cho COMPOSITION
      ) {
          public ExpressionNode toDomain() { /* map recursive */ }
      }
      ```

  22. Response model mới: `ExpressionNodeView.java`
      ```java
      public record ExpressionNodeView(
          String type,
          String name,
          String resolvedSpel,
          Long refId,
          String operator,
          List<ExpressionNodeView> children
      ) {}
      ```

  23. Cập nhật `CreateRuleRequest.java`, `UpdateRuleRequest.java`
      - `String targetExpression` → `ExpressionNodeRequest targetExpression`
      - `String conditionExpression` → `ExpressionNodeRequest conditionExpression`

  24. Cập nhật `UpdatePolicyRequest.java`
      - `String targetExpression` → `ExpressionNodeRequest targetExpression`

  25. Cập nhật response trong `GetPolicy`, `GetRule` — trả `ExpressionNodeView` thay vì raw SpEL string.
      `RuleView.targetExpression` và `conditionExpression` từ `String` → `ExpressionNodeView`.

  26. Controller mới: `ExpressionController.java`
      ```
      GET    /api/admin/v1/abac/expressions        → ListNamedExpressions.Result
      DELETE /api/admin/v1/abac/expressions/{id}   → 204 hoặc 409 nếu in-use
      ```

---

Ràng buộc kỹ thuật:
  - Jackson 3.x (`tools.jackson.databind`) — không dùng `com.fasterxml`
  - Không dùng `@SpringBootTest` cho unit test domain
  - `ExpressionNode.resolveSpelForValidation()` phải resolve `LibraryRef` qua DB trước khi validate SpEL
  - Compile check: `mvn clean compile -DskipTests` phải pass trước khi handoff

---

Yêu cầu Handoff (Bắt buộc):
  Sau khi xong, cung cấp 2 block:

  EXPRESSION SYSTEM BE CONTEXT BLOCK
    - Package paths thực tế của tất cả files mới/sửa
    - `ExpressionNode` sealed interface — full code (để FE reference type mapping)
    - `ExpressionTreeService` — method signatures thực tế
    - Error codes mới

  FRONTEND CONTEXT BLOCK
    - TypeScript interfaces đầy đủ:
        ExpressionNodeRequest { type, name?, spel?, refId?, operator?, children? }
        ExpressionNodeView { type, name, resolvedSpel, refId?, operator?, children }
        NamedExpressionView { id, name, spel }
    - Tất cả endpoints mới/thay đổi với method, path, request/response shape:
        GET/DELETE /api/admin/v1/abac/expressions
        Updated: POST/PUT /api/admin/v1/abac/policies/{id}/rules
        Updated: PUT /api/admin/v1/abac/policies/{id}
    - Breaking changes trong existing response shape (RuleView, PolicyView)
    - UI notes:
        ExpressionNodeView.type: "INLINE" | "LIBRARY_REF" | "COMPOSITION"
        LIBRARY_REF: hiển thị name từ NamedExpressionRepository (refId → name trong resolvedSpel context)
        resolvedSpel luôn có → dùng cho SpEL preview và reverse-parse detection
