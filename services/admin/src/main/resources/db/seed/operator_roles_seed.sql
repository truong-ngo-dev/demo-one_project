-- Seed: OPERATOR-scoped roles cho BQL
-- Chạy thủ công sau khi migration V9 đã apply.
-- Idempotent: INSERT IGNORE, không fail khi chạy lại.

INSERT IGNORE INTO roles (id, name, description, scope, created_at, updated_at) VALUES
('a1000000-0000-0000-0000-000000000001',
 'BQL_MANAGER',
 'Trưởng ban quản lý — toàn quyền trong phạm vi tòa nhà',
 'OPERATOR',
 UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),

('a1000000-0000-0000-0000-000000000002',
 'BQL_STAFF',
 'Nhân viên BQL — xem và xử lý yêu cầu vận hành',
 'OPERATOR',
 UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),

('a1000000-0000-0000-0000-000000000003',
 'BQL_ACCOUNTANT',
 'Kế toán BQL — quản lý hợp đồng và tài chính',
 'OPERATOR',
 UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),

('a1000000-0000-0000-0000-000000000004',
 'BQL_SECURITY',
 'Bảo vệ — xem danh sách cư dân và kiểm soát ra vào',
 'OPERATOR',
 UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);
