-- ── FixedAsset ───────────────────────────────────────────────────────────────
CREATE TABLE fixed_asset (
    id              VARCHAR(36)                                                                                              NOT NULL,
    parent_id       VARCHAR(36),
    path            VARCHAR(500)                                                                                             NOT NULL,
    type            ENUM('BUILDING','FLOOR','RESIDENTIAL_UNIT','COMMERCIAL_SPACE','COMMON_AREA',
                         'FACILITY','MEETING_ROOM','PARKING_SLOT','EQUIPMENT')                                               NOT NULL,
    name            VARCHAR(255)                                                                                             NOT NULL,
    code            VARCHAR(50),
    sequence_no     INT                                                                                                      NOT NULL DEFAULT 0,
    status          ENUM('ACTIVE','INACTIVE','UNDER_MAINTENANCE')                                                            NOT NULL DEFAULT 'ACTIVE',
    managing_org_id VARCHAR(36),
    created_at      DATETIME(6)                                                                                              NOT NULL,
    updated_at      DATETIME(6)                                                                                              NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_fixed_asset_parent FOREIGN KEY (parent_id) REFERENCES fixed_asset (id)
);
