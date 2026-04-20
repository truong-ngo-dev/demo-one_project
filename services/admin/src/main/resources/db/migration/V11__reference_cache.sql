CREATE TABLE building_reference (
    building_id     VARCHAR(36) PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    managing_org_id VARCHAR(36),
    cached_at       DATETIME NOT NULL
);

CREATE TABLE org_reference (
    org_id    VARCHAR(36) PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    org_type  VARCHAR(20) NOT NULL,
    cached_at DATETIME NOT NULL
);
