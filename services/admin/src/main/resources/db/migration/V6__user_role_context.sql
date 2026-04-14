-- V6: RoleContext model — multi-portal role assignments per user

CREATE TABLE user_role_context
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36)     NOT NULL,
    scope   ENUM('ADMIN', 'OPERATOR', 'TENANT', 'RESIDENT') NOT NULL,
    -- org_id stored as '' for ADMIN scope (NULL would break UNIQUE constraint in MySQL)
    org_id  VARCHAR(100) NOT NULL DEFAULT '',
    CONSTRAINT fk_urc_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_user_role_context UNIQUE (user_id, scope, org_id)
);

CREATE TABLE user_role_context_roles
(
    context_id BIGINT   NOT NULL,
    role_id    CHAR(36) NOT NULL,
    PRIMARY KEY (context_id, role_id),
    CONSTRAINT fk_urcr_context FOREIGN KEY (context_id) REFERENCES user_role_context (id) ON DELETE CASCADE,
    CONSTRAINT fk_urcr_role FOREIGN KEY (role_id) REFERENCES roles (id)
);
