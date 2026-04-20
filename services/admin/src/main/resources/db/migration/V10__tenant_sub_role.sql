CREATE TABLE tenant_sub_role_assignment (
    id          VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36) NOT NULL,
    org_id      VARCHAR(36) NOT NULL,
    sub_role    ENUM('TENANT_MANAGER','TENANT_FINANCE','TENANT_HR') NOT NULL,
    assigned_by VARCHAR(36) NOT NULL,
    assigned_at DATETIME NOT NULL,
    UNIQUE KEY uq_sub_role (user_id, org_id, sub_role),
    FOREIGN KEY (user_id)     REFERENCES users(id),
    FOREIGN KEY (assigned_by) REFERENCES users(id)
);
