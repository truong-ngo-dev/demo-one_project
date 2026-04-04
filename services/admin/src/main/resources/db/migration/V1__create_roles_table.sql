-- UC-R01: Tạo role
CREATE TABLE IF NOT EXISTS roles
(
    id          CHAR(36)     NOT NULL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  BIGINT       NOT NULL,
    updated_at  BIGINT,
    created_by  CHAR(36),
    updated_by  CHAR(36)
);
