-- DataCleanPro Database Schema
-- MySQL 8.0+

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS datacleanpro DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE datacleanpro;

-- 1. Imported data files metadata
CREATE TABLE IF NOT EXISTS data_file (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name       VARCHAR(255) NOT NULL,
    file_path       VARCHAR(500) NOT NULL,
    file_type       VARCHAR(10) NOT NULL,
    row_count       INT DEFAULT 0,
    column_count    INT DEFAULT 0,
    import_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    status          VARCHAR(20) DEFAULT 'IMPORTED',
    description     VARCHAR(500),
    INDEX idx_file_type (file_type),
    INDEX idx_status (status),
    INDEX idx_import_time (import_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Imported data rows (JSON-based flexible storage)
CREATE TABLE IF NOT EXISTS data_row (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id         BIGINT NOT NULL,
    row_index       INT NOT NULL,
    row_data        JSON NOT NULL,
    is_deleted      BOOLEAN DEFAULT FALSE,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (file_id) REFERENCES data_file(id) ON DELETE CASCADE,
    INDEX idx_file_id (file_id),
    INDEX idx_row_index (row_index),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Cleaning tasks
CREATE TABLE IF NOT EXISTS clean_task (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id         BIGINT NOT NULL,
    task_type       VARCHAR(50) NOT NULL,
    status          VARCHAR(20) DEFAULT 'PENDING',
    rows_affected   INT DEFAULT 0,
    detail          TEXT,
    start_time      DATETIME,
    end_time        DATETIME,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (file_id) REFERENCES data_file(id) ON DELETE CASCADE,
    INDEX idx_file_id (file_id),
    INDEX idx_task_type (task_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Validation rules
CREATE TABLE IF NOT EXISTS validation_rule (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name       VARCHAR(100) NOT NULL,
    rule_type       VARCHAR(50) NOT NULL,
    target_column   VARCHAR(100),
    expression      VARCHAR(500),
    error_message   VARCHAR(255),
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rule_type (rule_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Validation results
CREATE TABLE IF NOT EXISTS validation_result (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id         BIGINT NOT NULL,
    rule_id         BIGINT NOT NULL,
    row_index       INT NOT NULL,
    column_name     VARCHAR(100),
    cell_value      VARCHAR(1000),
    is_passed       BOOLEAN NOT NULL,
    error_message   VARCHAR(255),
    validated_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (file_id) REFERENCES data_file(id) ON DELETE CASCADE,
    FOREIGN KEY (rule_id) REFERENCES validation_rule(id) ON DELETE CASCADE,
    INDEX idx_file_id (file_id),
    INDEX idx_rule_id (rule_id),
    INDEX idx_is_passed (is_passed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Task history (audit log)
CREATE TABLE IF NOT EXISTS task_history (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id         BIGINT,
    action          VARCHAR(50) NOT NULL,
    target          VARCHAR(255),
    detail          TEXT,
    status          VARCHAR(20) DEFAULT 'SUCCESS',
    error_message   TEXT,
    execution_time  BIGINT,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_action (action),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Imported table data (for batch DB import)
CREATE TABLE IF NOT EXISTS imported_table_data (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id         BIGINT NOT NULL,
    table_name      VARCHAR(100) NOT NULL,
    column_definitions JSON NOT NULL,
    row_data        JSON NOT NULL,
    row_index       INT NOT NULL,
    imported_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (file_id) REFERENCES data_file(id) ON DELETE CASCADE,
    INDEX idx_file_id (file_id),
    INDEX idx_table_name (table_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default validation rules
INSERT INTO validation_rule (rule_name, rule_type, target_column, expression, error_message, is_active) VALUES
('手机号格式', 'PHONE', 'phone', '^1[3-9]\\d{9}$', '手机号格式不正确', TRUE),
('邮箱格式', 'EMAIL', 'email', '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$', '邮箱格式不正确', TRUE),
('必填字段', 'REQUIRED', NULL, NULL, '该字段为必填项', TRUE),
('年龄范围', 'NUMBER_RANGE', 'age', '0-150', '年龄范围应在0-150之间', TRUE);
