-- Spring Authorization Server tables
-- Source: https://github.com/spring-projects/spring-authorization-server/blob/main/oauth2-authorization-server/src/main/resources/org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql

CREATE TABLE oauth2_registered_client (
    id VARCHAR(100) NOT NULL,
    client_id VARCHAR(100) NOT NULL,
    client_id_issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    client_secret VARCHAR(200) DEFAULT NULL,
    client_secret_expires_at TIMESTAMP DEFAULT NULL,
    client_name VARCHAR(200) NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types VARCHAR(1000) NOT NULL,
    redirect_uris VARCHAR(1000) DEFAULT NULL,
    post_logout_redirect_uris VARCHAR(1000) DEFAULT NULL,
    scopes VARCHAR(1000) NOT NULL,
    client_settings VARCHAR(2000) NOT NULL,
    token_settings VARCHAR(2000) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE oauth2_authorization_consent (
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorities VARCHAR(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);

CREATE TABLE oauth2_authorization (
    id VARCHAR(100) NOT NULL,
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorization_grant_type VARCHAR(100) NOT NULL,
    authorized_scopes VARCHAR(1000) DEFAULT NULL,
    attributes BLOB DEFAULT NULL,
    state VARCHAR(500) DEFAULT NULL,
    authorization_code_value BLOB DEFAULT NULL,
    authorization_code_issued_at TIMESTAMP DEFAULT NULL,
    authorization_code_expires_at TIMESTAMP DEFAULT NULL,
    authorization_code_metadata BLOB DEFAULT NULL,
    access_token_value BLOB DEFAULT NULL,
    access_token_issued_at TIMESTAMP DEFAULT NULL,
    access_token_expires_at TIMESTAMP DEFAULT NULL,
    access_token_metadata BLOB DEFAULT NULL,
    access_token_type VARCHAR(100) DEFAULT NULL,
    access_token_scopes VARCHAR(1000) DEFAULT NULL,
    oidc_id_token_value BLOB DEFAULT NULL,
    oidc_id_token_issued_at TIMESTAMP DEFAULT NULL,
    oidc_id_token_expires_at TIMESTAMP DEFAULT NULL,
    oidc_id_token_metadata BLOB DEFAULT NULL,
    oidc_id_token_claims BLOB DEFAULT NULL,
    refresh_token_value BLOB DEFAULT NULL,
    refresh_token_issued_at TIMESTAMP DEFAULT NULL,
    refresh_token_expires_at TIMESTAMP DEFAULT NULL,
    refresh_token_metadata BLOB DEFAULT NULL,
    user_code_value BLOB DEFAULT NULL,
    user_code_issued_at TIMESTAMP DEFAULT NULL,
    user_code_expires_at TIMESTAMP DEFAULT NULL,
    user_code_metadata BLOB DEFAULT NULL,
    device_code_value BLOB DEFAULT NULL,
    device_code_issued_at TIMESTAMP DEFAULT NULL,
    device_code_expires_at TIMESTAMP DEFAULT NULL,
    device_code_metadata BLOB DEFAULT NULL,
    PRIMARY KEY (id)
);

-- RSA key pairs for JWT signing
CREATE TABLE rsa_key_pairs (
    id VARCHAR(100) NOT NULL,
    private_key MEDIUMTEXT NOT NULL,
    public_key TEXT NOT NULL,
    created DATE NOT NULL,
    PRIMARY KEY (id)
);

-- Devices
CREATE TABLE devices (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    client_hash VARCHAR(500),
    user_agent VARCHAR(500),
    accept_language VARCHAR(200),
    composite_hash VARCHAR(64) NOT NULL,
    device_name VARCHAR(200),
    device_type VARCHAR(50),
    trusted BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL,
    registered_at DATETIME(6) NOT NULL,
    last_seen_at DATETIME(6),
    last_ip_address VARCHAR(50),
    PRIMARY KEY (id),
    INDEX idx_devices_user_id (user_id),
    INDEX idx_devices_composite_hash (composite_hash)
);

-- OAuth sessions
CREATE TABLE oauth_sessions (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    device_id VARCHAR(36) NOT NULL,
    spring_authorization_id VARCHAR(100) NOT NULL,
    ip_address VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE INDEX idx_oauth_sessions_authorization_id (spring_authorization_id),
    INDEX idx_oauth_sessions_user_id (user_id),
    INDEX idx_oauth_sessions_device_id (device_id)
);

-- Login activities (append-only)
CREATE TABLE login_activities (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36),
    username VARCHAR(200),
    result VARCHAR(30) NOT NULL,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    composite_hash VARCHAR(64),
    session_id VARCHAR(100),
    provider VARCHAR(20),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_login_activities_user_id (user_id),
    INDEX idx_login_activities_created_at (created_at)
);
