CREATE TABLE employment (
    id              VARCHAR(36)                              NOT NULL,
    relationship_id VARCHAR(36)                              NOT NULL,
    employee_id     VARCHAR(36)                              NOT NULL,
    org_id          VARCHAR(36)                              NOT NULL,
    employment_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT') NOT NULL,
    status          ENUM('ACTIVE', 'TERMINATED')             NOT NULL DEFAULT 'ACTIVE',
    start_date      DATE                                     NOT NULL,
    end_date        DATE,
    PRIMARY KEY (id),
    UNIQUE KEY uq_employment_relationship (relationship_id),
    CONSTRAINT fk_emp_relationship FOREIGN KEY (relationship_id) REFERENCES party_relationship (id),
    CONSTRAINT fk_emp_employee     FOREIGN KEY (employee_id)     REFERENCES person (party_id),
    CONSTRAINT fk_emp_org          FOREIGN KEY (org_id)          REFERENCES organization (party_id)
);

CREATE TABLE position_assignment (
    id            VARCHAR(36)                                                                         NOT NULL,
    employment_id VARCHAR(36)                                                                         NOT NULL,
    position      ENUM('MANAGER', 'DEPUTY_MANAGER', 'FINANCE', 'TECHNICAL',
                       'SECURITY', 'RECEPTIONIST', 'STAFF')                                          NOT NULL,
    department    VARCHAR(100),
    start_date    DATE                                                                                NOT NULL,
    end_date      DATE,
    PRIMARY KEY (id),
    CONSTRAINT fk_pos_employment FOREIGN KEY (employment_id) REFERENCES employment (id)
);
