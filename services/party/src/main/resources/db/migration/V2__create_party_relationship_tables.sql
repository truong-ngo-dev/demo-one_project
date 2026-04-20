-- ── PartyRelationship ────────────────────────────────────────────────────────
CREATE TABLE party_relationship (
    id            VARCHAR(36)                              NOT NULL,
    from_party_id VARCHAR(36)                              NOT NULL,
    to_party_id   VARCHAR(36)                              NOT NULL,
    type          ENUM('MEMBER_OF', 'EMPLOYED_BY')         NOT NULL,
    from_role     ENUM('MEMBER', 'HEAD', 'EMPLOYEE', 'EMPLOYER') NOT NULL,
    to_role       ENUM('MEMBER', 'HEAD', 'EMPLOYEE', 'EMPLOYER') NOT NULL,
    status        ENUM('ACTIVE', 'ENDED')                  NOT NULL DEFAULT 'ACTIVE',
    start_date    DATE                                     NOT NULL,
    end_date      DATE,
    PRIMARY KEY (id),
    CONSTRAINT fk_rel_from FOREIGN KEY (from_party_id) REFERENCES party (id),
    CONSTRAINT fk_rel_to   FOREIGN KEY (to_party_id)   REFERENCES party (id)
);
