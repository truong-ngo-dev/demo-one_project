-- Thêm cột idp_session_id để liên kết OAuth Session với HttpSession (IdP Session)
-- Phục vụ việc invalidate IdP session khi thực hiện remote logout (UC-008)

ALTER TABLE oauth_sessions
    ADD COLUMN idp_session_id VARCHAR(100) NULL AFTER device_id,
    ADD INDEX idx_oauth_sessions_idp_session_id (idp_session_id);
