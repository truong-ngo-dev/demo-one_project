-- UC-012: Thêm device_id vào login_activities để liên kết trực tiếp với device
-- Thay thế việc correlate qua composite_hash khi cần lấy deviceId trong response

ALTER TABLE login_activities
    ADD COLUMN device_id VARCHAR(36) NULL AFTER composite_hash,
    ADD INDEX idx_login_activities_device_id (device_id);
