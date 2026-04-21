-- Seed: Operator Portal — Resource, Action, UIElement catalogue
-- Chạy thủ công sau khi migration V11 đã apply.
-- Idempotent: dùng INSERT IGNORE để không fail khi chạy lại.

-- =============================================
-- RESOURCES
-- =============================================

INSERT IGNORE INTO resource_definition (name, description, service_name, created_at, updated_at) VALUES
('building:asset',
 'Fixed assets trong tòa nhà — tầng, căn hộ, đơn vị',
 'property-service',
 UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),

('building:agreement',
 'Hợp đồng sử dụng (thuê/sở hữu) trong tòa nhà',
 'property-service',
 UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),

('building:operator',
 'Danh sách operator được giao quản lý tòa nhà',
 'admin-service',
 UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),

('operator:management',
 'Quản trị operator — gán/thu hồi context, link partyId, cập nhật roles',
 'admin-service',
 UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

-- =============================================
-- ACTIONS
-- =============================================

-- building:asset
INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'view', 'Xem danh sách asset của tòa nhà', TRUE
FROM resource_definition WHERE name = 'building:asset';

-- building:agreement
INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'view', 'Xem danh sách hợp đồng', TRUE
FROM resource_definition WHERE name = 'building:agreement';

INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'create', 'Tạo hợp đồng mới', FALSE
FROM resource_definition WHERE name = 'building:agreement';

-- building:operator
INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'view', 'Xem danh sách operator tại building', TRUE
FROM resource_definition WHERE name = 'building:operator';

-- operator:management
INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'view', 'Xem danh sách operator theo building', TRUE
FROM resource_definition WHERE name = 'operator:management';

INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'assign', 'Gán OPERATOR context cho user tại building', FALSE
FROM resource_definition WHERE name = 'operator:management';

INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'revoke', 'Thu hồi OPERATOR context của user', FALSE
FROM resource_definition WHERE name = 'operator:management';

INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'link-party', 'Gắn partyId vào tài khoản user', FALSE
FROM resource_definition WHERE name = 'operator:management';

INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'assign-roles', 'Cập nhật danh sách roles trong OPERATOR context', FALSE
FROM resource_definition WHERE name = 'operator:management';

-- =============================================
-- UI ELEMENTS — scope = OPERATOR (Operator Portal)
-- =============================================

-- route:operator:dashboard
-- Map sang building:asset/view — gate "có quyền vào portal"
INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT
    'route:operator:dashboard', 'Trang chủ', 'MENU_ITEM', 'OPERATOR', 'operator-nav', 0,
    r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'view'
WHERE r.name = 'building:asset';

-- route:operator:assets
INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT
    'route:operator:assets', 'Tòa nhà & Căn hộ', 'MENU_ITEM', 'OPERATOR', 'operator-nav', 1,
    r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'view'
WHERE r.name = 'building:asset';

-- route:operator:agreements
INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT
    'route:operator:agreements', 'Hợp đồng', 'MENU_ITEM', 'OPERATOR', 'operator-nav', 2,
    r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'view'
WHERE r.name = 'building:agreement';

-- route:operator:operators
INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT
    'route:operator:operators', 'Danh sách Operator', 'MENU_ITEM', 'OPERATOR', 'operator-nav', 3,
    r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'view'
WHERE r.name = 'building:operator';

-- btn:operator:agreement:create
INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT
    'btn:operator:agreement:create', 'Tạo hợp đồng', 'BUTTON', 'OPERATOR', 'operator-agreement', 0,
    r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'create'
WHERE r.name = 'building:agreement';

-- =============================================
-- UI ELEMENTS — scope = ADMIN (Admin Panel Extensions)
-- =============================================

-- route:admin:operators — sidebar nav item
INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT
    'route:admin:operators', 'Quản lý Operator', 'MENU_ITEM', 'ADMIN', 'admin-nav', 10,
    r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'view'
WHERE r.name = 'operator:management';

-- btn:admin:operator:link-party
INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT
    'btn:admin:operator:link-party', 'Gắn Party ID', 'BUTTON', 'ADMIN', 'admin-operator-mgmt', 0,
    r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'link-party'
WHERE r.name = 'operator:management';

-- btn:admin:operator:assign
INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT
    'btn:admin:operator:assign', 'Gán Operator', 'BUTTON', 'ADMIN', 'admin-operator-mgmt', 1,
    r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'assign'
WHERE r.name = 'operator:management';

-- btn:admin:operator:revoke
INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT
    'btn:admin:operator:revoke', 'Thu hồi Operator', 'BUTTON', 'ADMIN', 'admin-operator-mgmt', 2,
    r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'revoke'
WHERE r.name = 'operator:management';

-- btn:admin:operator:assign-roles
INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT
    'btn:admin:operator:assign-roles', 'Cập nhật Roles', 'BUTTON', 'ADMIN', 'admin-operator-mgmt', 3,
    r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'assign-roles'
WHERE r.name = 'operator:management';

-- =============================================
-- RESOURCES — Building Management (Phase 3.5)
-- =============================================

INSERT IGNORE INTO resource_definition (name, description, service_name, created_at, updated_at) VALUES
('property:building',
 'Quản lý tòa nhà và cấu trúc asset (tầng, căn hộ)',
 'property-service',
 UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

-- =============================================
-- ACTIONS — property:building
-- =============================================

INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'view',   'Xem danh sách tòa nhà',     TRUE  FROM resource_definition WHERE name = 'property:building';

INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'create', 'Tạo tòa nhà mới',            FALSE FROM resource_definition WHERE name = 'property:building';

INSERT IGNORE INTO action_definition (resource_id, name, description, is_standard)
SELECT id, 'manage', 'Thêm/sửa tầng và căn hộ',   FALSE FROM resource_definition WHERE name = 'property:building';

-- =============================================
-- UI ELEMENTS — ADMIN scope (Building Management)
-- =============================================

-- route:admin:buildings
INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT
    'route:admin:buildings', 'Tòa nhà', 'MENU_ITEM', 'ADMIN', 'admin-nav', 11,
    r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'view'
WHERE r.name = 'property:building';

-- btn:admin:building:create
INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT
    'btn:admin:building:create', 'Tạo tòa nhà', 'BUTTON', 'ADMIN', 'admin-building', 0,
    r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'create'
WHERE r.name = 'property:building';

-- btn:admin:building:manage-assets
INSERT IGNORE INTO ui_element (element_id, label, type, scope, element_group, order_index, resource_id, action_id)
SELECT
    'btn:admin:building:manage-assets', 'Thêm tầng/căn hộ', 'BUTTON', 'ADMIN', 'admin-building', 1,
    r.id, a.id
FROM resource_definition r
JOIN action_definition a ON a.resource_id = r.id AND a.name = 'manage'
WHERE r.name = 'property:building';
