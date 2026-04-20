CREATE TABLE occupancy_agreement (
    id              VARCHAR(36) PRIMARY KEY,
    party_id        VARCHAR(36) NOT NULL,
    party_type      ENUM('PERSON', 'HOUSEHOLD', 'ORGANIZATION') NOT NULL,
    asset_id        VARCHAR(36) NOT NULL,
    agreement_type  ENUM('OWNERSHIP', 'LEASE') NOT NULL,
    status          ENUM('PENDING', 'ACTIVE', 'TERMINATED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    start_date      DATE NOT NULL,
    end_date        DATE,
    contract_ref    VARCHAR(100),
    created_at      DATETIME NOT NULL,
    updated_at      DATETIME NOT NULL,
    FOREIGN KEY (asset_id) REFERENCES fixed_asset(id)
);
