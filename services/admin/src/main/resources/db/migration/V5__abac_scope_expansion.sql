-- V5: Expand scope enum + add scope column to ui_element

-- Expand policy_set.scope enum to include ADMIN and RESIDENT
ALTER TABLE policy_set
    MODIFY COLUMN scope ENUM('ADMIN', 'OPERATOR', 'TENANT', 'RESIDENT') NOT NULL DEFAULT 'OPERATOR';

-- Add scope column to ui_element; backfill existing rows to ADMIN
ALTER TABLE ui_element
    ADD COLUMN scope ENUM('ADMIN', 'OPERATOR', 'TENANT', 'RESIDENT') NOT NULL DEFAULT 'ADMIN'
    AFTER type;
