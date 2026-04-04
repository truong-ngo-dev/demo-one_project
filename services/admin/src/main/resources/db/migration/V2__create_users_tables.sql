-- UC-U01: Tạo users, user_roles, user_social_connections

CREATE TABLE IF NOT EXISTS users
(
    id                    CHAR(36)     NOT NULL PRIMARY KEY,
    username              VARCHAR(255) UNIQUE,
    email                 VARCHAR(255) UNIQUE,
    phone_number          VARCHAR(255) UNIQUE,
    full_name             VARCHAR(255),
    hashed_password       VARCHAR(255),
    status                VARCHAR(50)  NOT NULL,
    username_changed      BOOLEAN      NOT NULL DEFAULT FALSE,
    locked_at             DATETIME(6),
    created_at            DATETIME(6)  NOT NULL,
    updated_at            DATETIME(6)
);

CREATE TABLE IF NOT EXISTS user_roles
(
    user_id CHAR(36) NOT NULL,
    role_id CHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE IF NOT EXISTS user_social_connections
(
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id      CHAR(36)     NOT NULL,
    provider     VARCHAR(50)  NOT NULL,
    social_id    VARCHAR(255) NOT NULL,
    email        VARCHAR(255),
    connected_at DATETIME(6)  NOT NULL,
    UNIQUE KEY uq_user_provider (user_id, provider),
    CONSTRAINT fk_social_connections_user FOREIGN KEY (user_id) REFERENCES users (id)
);
