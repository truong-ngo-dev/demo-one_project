-- =============================================================================
-- ABAC Policy Seed — Admin Portal
-- Tạo policy set ADMIN scope với policy "Admin Do All":
--   - Target: subject.roles.contains('ADMIN') → chỉ áp dụng cho ADMIN user
--   - Rule:   condition = true, effect = PERMIT → admin được phép tất cả
--
-- Tuân theo expression system mới (V8):
--   - Mọi leaf expression là LITERAL row trong abac_expression,
--     trỏ vào named_expression AR qua named_expression_id.
--   - Không có Inline row nào trong abac_expression.
--
-- Chạy lại file này bất kỳ lúc nào để reset về trạng thái chuẩn.
-- Lưu ý: KHÔNG ảnh hưởng đến resource_definition / action_definition / ui_element.
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ── 1. Xoá data cũ ──────────────────────────────────────────────────────────
TRUNCATE TABLE rule;
TRUNCATE TABLE policy;
TRUNCATE TABLE policy_set;
TRUNCATE TABLE abac_expression;
TRUNCATE TABLE named_expression;

SET FOREIGN_KEY_CHECKS = 1;

-- ── 2. Named Expressions (NamedExpression AR) ────────────────────────────────
--      Đây là "library" — mỗi row là một reusable SpEL expression có tên.

INSERT INTO named_expression (id, name, spel) VALUES
  (1, 'Là admin',   "subject.roles.contains('ADMIN')"),
  (2, 'Luôn đúng',  'true');

-- ── 3. Expression tree nodes (LibraryRef leaves) ─────────────────────────────
--      Mỗi row là một LITERAL node trỏ vào named_expression AR.
--      spel_expression = NULL, name = NULL (theo V8 design).

INSERT INTO abac_expression (id, type, named_expression_id, spel_expression, name, parent_id) VALUES
  (1, 'LITERAL', 1, NULL, NULL, NULL),   -- ref → "Là admin"   (dùng làm policy target)
  (2, 'LITERAL', 2, NULL, NULL, NULL);   -- ref → "Luôn đúng"  (dùng làm rule condition)

-- ── 4. Policy Set ─────────────────────────────────────────────────────────────

INSERT INTO policy_set (id, name, scope, combine_algorithm, is_root, tenant_id, created_at, updated_at) VALUES
  (1, 'Admin Portal', 'ADMIN', 'DENY_OVERRIDES', TRUE, NULL,
   UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

-- ── 5. Policy ─────────────────────────────────────────────────────────────────
--      target_expression_id = 1 → abac_expression id=1 → named_expression "Là admin"
--      combine_algorithm = PERMIT_UNLESS_DENY: admin được permit trừ khi có rule DENY tường minh.

INSERT INTO policy (id, policy_set_id, name, target_expression_id, combine_algorithm, created_at, updated_at) VALUES
  (1, 1, 'Admin Do All', 1, 'PERMIT_UNLESS_DENY',
   UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

-- ── 6. Rule ───────────────────────────────────────────────────────────────────
--      target_expression_id  = NULL  (không giới hạn thêm, policy target đã lọc)
--      condition_expression_id = 2   → abac_expression id=2 → named_expression "Luôn đúng"
--      effect = PERMIT, condition = true → admin luôn được phép

INSERT INTO rule (id, policy_id, name, description, target_expression_id, condition_expression_id, effect, order_index, created_at, updated_at) VALUES
  (1, 1, 'Permit All Actions',
   'Cho phép mọi hành động đối với admin user (subject.roles.contains(ADMIN))',
   NULL, 2, 'PERMIT', 0,
   UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);
