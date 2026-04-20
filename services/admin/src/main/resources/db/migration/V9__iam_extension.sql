-- Extend roles table
ALTER TABLE roles ADD COLUMN scope ENUM('ADMIN','OPERATOR','TENANT','RESIDENT') NOT NULL DEFAULT 'ADMIN';
ALTER TABLE roles DROP INDEX name;
ALTER TABLE roles ADD UNIQUE KEY uq_role_name_scope (name, scope);

-- Extend user_role_context table
ALTER TABLE user_role_context
    ADD COLUMN status   ENUM('ACTIVE','REVOKED') NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN org_type ENUM('PARTY','FIXED_ASSET');

-- Extend users table
ALTER TABLE users ADD COLUMN party_id VARCHAR(36);
