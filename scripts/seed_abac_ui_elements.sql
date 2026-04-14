-- =============================================================================
-- ABAC UI Element Seed — Admin Portal (ADMIN scope)
-- Reset và insert lại toàn bộ: resource_definition, action_definition, ui_element
--
-- Chạy lại file này bất kỳ lúc nào để reset về trạng thái chuẩn.
-- Lưu ý: KHÔNG xóa policy_set / policy / rule — giữ nguyên ABAC policy.
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ── 1. Xoá data cũ ──────────────────────────────────────────────────────────
TRUNCATE TABLE ui_element;
TRUNCATE TABLE action_definition;
TRUNCATE TABLE resource_definition;

SET FOREIGN_KEY_CHECKS = 1;

-- ── 2. Resource Definitions ──────────────────────────────────────────────────
--      Tên resource phải khớp với @ResourceMapping(resource = "...") trong backend

INSERT INTO resource_definition (id, name, description, service_name, created_at, updated_at) VALUES
  (1,  'user',            'User management',                    'admin-service',  UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
  (2,  'role',            'Role management',                    'admin-service',  UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
  (3,  'abac_resource',   'ABAC resource catalogue',            'admin-service',  UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
  (4,  'abac_policy_set', 'ABAC policy set management',         'admin-service',  UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
  (5,  'abac_policy',     'ABAC policy management',             'admin-service',  UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
  (6,  'abac_rule',       'ABAC rule management',               'admin-service',  UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
  (7,  'abac_ui_element', 'ABAC UI element registry',           'admin-service',  UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
  (8,  'abac_simulate',   'ABAC policy simulator',              'admin-service',  UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
  (9,  'abac_audit_log',  'ABAC admin change audit log',        'admin-service',  UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
  (10, 'admin_session',   'Active session management (oauth2)', 'oauth2-service', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
  (11, 'admin_activity',  'Login activity log (oauth2)',         'oauth2-service', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

-- ── 3. Action Definitions ────────────────────────────────────────────────────
--      Tên action phải khớp với @ResourceMapping(action = "...") trong backend

INSERT INTO action_definition (id, resource_id, name, description, is_standard) VALUES
  -- user
  (1,  1, 'CREATE',      'Create new user',              TRUE),
  (2,  1, 'READ',        'View user detail',             TRUE),
  (3,  1, 'LIST',        'List / search users',          TRUE),
  (4,  1, 'LOCK',        'Lock user account',            FALSE),
  (5,  1, 'UNLOCK',      'Unlock user account',          FALSE),
  (6,  1, 'ASSIGN_ROLE', 'Assign role to user',          FALSE),
  (7,  1, 'REMOVE_ROLE', 'Remove role from user',        FALSE),

  -- role
  (8,  2, 'CREATE', 'Create new role',     TRUE),
  (9,  2, 'READ',   'View role detail',    TRUE),
  (10, 2, 'LIST',   'List / search roles', TRUE),
  (11, 2, 'UPDATE', 'Update role',         TRUE),
  (12, 2, 'DELETE', 'Delete role',         TRUE),

  -- abac_resource
  (13, 3, 'CREATE', 'Create resource definition', TRUE),
  (14, 3, 'READ',   'View resource detail',        TRUE),
  (15, 3, 'LIST',   'List resources',              TRUE),
  (16, 3, 'UPDATE', 'Update resource',             TRUE),
  (17, 3, 'DELETE', 'Delete resource',             TRUE),

  -- abac_policy_set
  (18, 4, 'CREATE', 'Create policy set', TRUE),
  (19, 4, 'READ',   'View policy set',   TRUE),
  (20, 4, 'LIST',   'List policy sets',  TRUE),
  (21, 4, 'UPDATE', 'Update policy set', TRUE),
  (22, 4, 'DELETE', 'Delete policy set', TRUE),

  -- abac_policy
  (23, 5, 'CREATE', 'Create policy', TRUE),
  (24, 5, 'READ',   'View policy',   TRUE),
  (25, 5, 'LIST',   'List policies', TRUE),
  (26, 5, 'UPDATE', 'Update policy', TRUE),
  (27, 5, 'DELETE', 'Delete policy', TRUE),

  -- abac_rule
  (28, 6, 'CREATE', 'Create rule',  TRUE),
  (29, 6, 'READ',   'View rule',    TRUE),
  (30, 6, 'LIST',   'List rules',   TRUE),
  (31, 6, 'UPDATE', 'Update rule',  TRUE),
  (32, 6, 'DELETE', 'Delete rule',  TRUE),

  -- abac_ui_element
  (33, 7, 'CREATE',   'Create UI element',          TRUE),
  (34, 7, 'READ',     'View UI element detail',     TRUE),
  (35, 7, 'LIST',     'List UI elements',           TRUE),
  (36, 7, 'UPDATE',   'Update UI element',          TRUE),
  (37, 7, 'DELETE',   'Delete UI element',          TRUE),

  -- abac_simulate
  (38, 8, 'EXECUTE', 'Run policy simulation', FALSE),

  -- abac_audit_log
  (39, 9, 'LIST', 'View audit log', FALSE),

  -- admin_session
  (40, 10, 'LIST',      'List active sessions',    FALSE),
  (41, 10, 'TERMINATE', 'Force terminate session', FALSE),

  -- admin_activity
  (42, 11, 'LIST', 'View login activities', FALSE);

-- ── 4. UI Elements ───────────────────────────────────────────────────────────
--      element_id phải khớp với ADMIN_ROUTE_ELEMENT_IDS trong abac.service.ts
--      và abacGuard(elementId) trong app.routes.ts

INSERT INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id) VALUES

  -- ── Sidebar nav — Management ──────────────────────────────────────────────
  ('route:users',   'Users',  'MENU_ITEM', 'ADMIN', 'sidebar-management', 1,  1,  3),   -- user / LIST
  ('route:roles',   'Roles',  'MENU_ITEM', 'ADMIN', 'sidebar-management', 2,  2, 10),   -- role / LIST

  -- ── Sidebar nav — Access Control ─────────────────────────────────────────
  ('route:abac:resources',   'Resources',   'MENU_ITEM', 'ADMIN', 'sidebar-abac', 1,  3, 15),  -- abac_resource / LIST
  ('route:abac:policy-sets', 'Policy Sets', 'MENU_ITEM', 'ADMIN', 'sidebar-abac', 2,  4, 20),  -- abac_policy_set / LIST
  ('route:abac:ui-elements', 'UI Elements', 'MENU_ITEM', 'ADMIN', 'sidebar-abac', 3,  7, 35),  -- abac_ui_element / LIST
  ('route:abac:simulator',   'Simulator',   'MENU_ITEM', 'ADMIN', 'sidebar-abac', 4,  8, 38),  -- abac_simulate / EXECUTE
  ('route:abac:audit-log',   'Audit Log',   'MENU_ITEM', 'ADMIN', 'sidebar-abac', 5,  9, 39),  -- abac_audit_log / LIST

  -- ── Sidebar nav — Monitoring ──────────────────────────────────────────────
  ('route:active-sessions',  'Sessions',         'MENU_ITEM', 'ADMIN', 'sidebar-monitoring', 1, 10, 40),  -- admin_session / LIST
  ('route:login-activities', 'Login Activities', 'MENU_ITEM', 'ADMIN', 'sidebar-monitoring', 2, 11, 42),  -- admin_activity / LIST

  -- ── User detail page — action buttons ────────────────────────────────────
  ('btn:user:lock',        'Lock User',       'BUTTON', 'ADMIN', 'user-detail-actions', 1, 1, 4),   -- user / LOCK
  ('btn:user:unlock',      'Unlock User',     'BUTTON', 'ADMIN', 'user-detail-actions', 2, 1, 5),   -- user / UNLOCK
  ('btn:user:assign-role', 'Assign Role',     'BUTTON', 'ADMIN', 'user-detail-actions', 3, 1, 6),   -- user / ASSIGN_ROLE
  ('btn:user:remove-role', 'Remove Role',     'BUTTON', 'ADMIN', 'user-detail-actions', 4, 1, 7),   -- user / REMOVE_ROLE
  ('btn:user:create',      'Create User',     'BUTTON', 'ADMIN', 'user-list-actions',   1, 1, 1),   -- user / CREATE

  -- ── Role page — action buttons ────────────────────────────────────────────
  ('btn:role:create', 'Create Role', 'BUTTON', 'ADMIN', 'role-list-actions', 1, 2, 8),   -- role / CREATE
  ('btn:role:update', 'Edit Role',   'BUTTON', 'ADMIN', 'role-list-actions', 2, 2, 11),  -- role / UPDATE
  ('btn:role:delete', 'Delete Role', 'BUTTON', 'ADMIN', 'role-list-actions', 3, 2, 12),  -- role / DELETE

  -- ── Session management ────────────────────────────────────────────────────
  ('btn:session:terminate', 'Terminate Session', 'BUTTON', 'ADMIN', 'session-actions', 1, 10, 41);  -- admin_session / TERMINATE
