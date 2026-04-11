-- Resource & Action Catalogue (UC-019)
CREATE TABLE resource_definition (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL UNIQUE,
    description  VARCHAR(500),
    service_name VARCHAR(100) NOT NULL,
    created_at   BIGINT       NOT NULL,
    updated_at   BIGINT       NOT NULL
);

CREATE TABLE action_definition (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_standard BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_action_resource FOREIGN KEY (resource_id)
        REFERENCES resource_definition (id) ON DELETE CASCADE,
    CONSTRAINT uq_action_per_resource UNIQUE (resource_id, name)
);

-- Expressions — used by Policy/Rule (Batch 2)
CREATE TABLE abac_expression (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    type             ENUM ('LITERAL', 'COMPOSITION') NOT NULL,
    spel_expression  TEXT,
    combination_type ENUM ('AND', 'OR'),
    parent_id        BIGINT,
    CONSTRAINT fk_expression_parent FOREIGN KEY (parent_id)
        REFERENCES abac_expression (id)
);

-- Policy Set / Policy / Rule (Batch 2)
CREATE TABLE policy_set (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(200) NOT NULL UNIQUE,
    scope             ENUM ('OPERATOR', 'TENANT') NOT NULL DEFAULT 'OPERATOR',
    combine_algorithm VARCHAR(50)  NOT NULL DEFAULT 'DENY_OVERRIDES',
    is_root           BOOLEAN      NOT NULL DEFAULT FALSE,
    tenant_id         VARCHAR(100),
    created_at        BIGINT       NOT NULL,
    updated_at        BIGINT       NOT NULL
);

CREATE TABLE policy (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_set_id        BIGINT       NOT NULL,
    name                 VARCHAR(200) NOT NULL,
    target_expression_id BIGINT,
    combine_algorithm    VARCHAR(50)  NOT NULL DEFAULT 'DENY_UNLESS_PERMIT',
    created_at           BIGINT       NOT NULL,
    updated_at           BIGINT       NOT NULL,
    CONSTRAINT fk_policy_set FOREIGN KEY (policy_set_id)
        REFERENCES policy_set (id) ON DELETE CASCADE,
    CONSTRAINT fk_policy_target FOREIGN KEY (target_expression_id)
        REFERENCES abac_expression (id)
);

CREATE TABLE rule (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_id               BIGINT       NOT NULL,
    name                    VARCHAR(200) NOT NULL,
    description             TEXT,
    target_expression_id    BIGINT,
    condition_expression_id BIGINT,
    effect                  ENUM ('PERMIT', 'DENY') NOT NULL,
    order_index             INT          NOT NULL DEFAULT 0,
    created_at              BIGINT       NOT NULL,
    updated_at              BIGINT       NOT NULL,
    CONSTRAINT fk_rule_policy FOREIGN KEY (policy_id)
        REFERENCES policy (id) ON DELETE CASCADE,
    CONSTRAINT fk_rule_target FOREIGN KEY (target_expression_id)
        REFERENCES abac_expression (id),
    CONSTRAINT fk_rule_condition FOREIGN KEY (condition_expression_id)
        REFERENCES abac_expression (id)
);

-- UIElement Registry (Batch 3)
CREATE TABLE ui_element (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    element_id    VARCHAR(200) NOT NULL UNIQUE,
    label         VARCHAR(200) NOT NULL,
    type          ENUM ('BUTTON', 'TAB', 'MENU_ITEM') NOT NULL,
    element_group VARCHAR(100),
    order_index   INT          NOT NULL DEFAULT 0,
    resource_id   BIGINT       NOT NULL,
    action_id     BIGINT       NOT NULL,
    CONSTRAINT fk_ui_resource FOREIGN KEY (resource_id)
        REFERENCES resource_definition (id),
    CONSTRAINT fk_ui_action FOREIGN KEY (action_id)
        REFERENCES action_definition (id)
);
