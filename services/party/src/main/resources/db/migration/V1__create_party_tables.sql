-- ── Party (supertype) ────────────────────────────────────────────────────────
CREATE TABLE party (
    id         VARCHAR(36)                              NOT NULL,
    type       ENUM('PERSON','ORGANIZATION','HOUSEHOLD') NOT NULL,
    name       VARCHAR(255)                             NOT NULL,
    status     ENUM('ACTIVE','INACTIVE')                NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6)                              NOT NULL,
    updated_at DATETIME(6)                              NOT NULL,
    PRIMARY KEY (id)
);

-- ── Person ───────────────────────────────────────────────────────────────────
CREATE TABLE person (
    party_id   VARCHAR(36)                    NOT NULL,
    first_name VARCHAR(100)                   NOT NULL,
    last_name  VARCHAR(100)                   NOT NULL,
    dob        DATE,
    gender     ENUM('MALE','FEMALE','OTHER'),
    PRIMARY KEY (party_id),
    CONSTRAINT fk_person_party FOREIGN KEY (party_id) REFERENCES party (id)
);

-- ── Organization ─────────────────────────────────────────────────────────────
CREATE TABLE organization (
    party_id        VARCHAR(36)                          NOT NULL,
    org_type        ENUM('BQL','TENANT','VENDOR','OTHER') NOT NULL,
    tax_id          VARCHAR(20),
    registration_no VARCHAR(50),
    PRIMARY KEY (party_id),
    CONSTRAINT fk_organization_party FOREIGN KEY (party_id) REFERENCES party (id)
);

-- ── Household ────────────────────────────────────────────────────────────────
CREATE TABLE household (
    party_id       VARCHAR(36) NOT NULL,
    head_person_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (party_id),
    CONSTRAINT fk_household_party  FOREIGN KEY (party_id)       REFERENCES party  (id),
    CONSTRAINT fk_household_head   FOREIGN KEY (head_person_id) REFERENCES person (party_id)
);

-- ── PartyIdentification ──────────────────────────────────────────────────────
CREATE TABLE party_identification (
    id          VARCHAR(36)                                      NOT NULL,
    party_id    VARCHAR(36)                                      NOT NULL,
    type        ENUM('CCCD','TAX_ID','PASSPORT','BUSINESS_REG')  NOT NULL,
    value       VARCHAR(100)                                     NOT NULL,
    issued_date DATE,
    PRIMARY KEY (id),
    CONSTRAINT fk_identification_party FOREIGN KEY (party_id) REFERENCES party (id),
    UNIQUE KEY uq_identification (type, value)
);
