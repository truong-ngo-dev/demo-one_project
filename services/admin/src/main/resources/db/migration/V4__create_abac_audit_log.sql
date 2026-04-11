CREATE TABLE abac_audit_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(30)  NOT NULL,
    entity_id   BIGINT       NOT NULL,
    entity_name VARCHAR(255),
    action_type VARCHAR(20)  NOT NULL,
    performed_by VARCHAR(100),
    changed_at  BIGINT       NOT NULL,
    snapshot_json TEXT,
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_performer (performed_by),
    INDEX idx_audit_changed_at (changed_at)
);
