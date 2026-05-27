-- AWS RDS MySQL schema script.
-- Run this manually in MySQL Workbench or another DB tool when rebuilding the AWS DB.
-- Caution: DROP DATABASE deletes all existing data.

DROP DATABASE IF EXISTS datasample;
CREATE DATABASE datasample DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE datasample;

DROP TABLE IF EXISTS resident_inquiry;
DROP TABLE IF EXISTS manager_inquiry;
DROP TABLE IF EXISTS waiting_list;
DROP TABLE IF EXISTS settings;
DROP TABLE IF EXISTS device_info;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS parking_history;
DROP TABLE IF EXISTS registered_cars;
DROP TABLE IF EXISTS parking_zone;
DROP TABLE IF EXISTS parking_lot;
DROP TABLE IF EXISTS car;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS apartment_manager;
DROP TABLE IF EXISTS web_manager;
DROP TABLE IF EXISTS apartments;

CREATE TABLE apartments (
    a_no INT NOT NULL AUTO_INCREMENT,
    a_name VARCHAR(30) NOT NULL,
    a_pwd VARCHAR(30) NOT NULL,
    a_address VARCHAR(255) NOT NULL,
    a_detail_address VARCHAR(255),
    PRIMARY KEY (a_no),
    UNIQUE KEY uk_apartments_name (a_name),
    UNIQUE KEY uk_apartments_password (a_pwd)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE web_manager (
    w_no INT NOT NULL AUTO_INCREMENT,
    w_id VARCHAR(255) NOT NULL,
    w_pwd VARCHAR(100) NOT NULL,
    PRIMARY KEY (w_no),
    UNIQUE KEY uk_web_manager_id (w_id),
    UNIQUE KEY uk_web_manager_password (w_pwd)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE apartment_manager (
    m_no INT NOT NULL AUTO_INCREMENT,
    a_no INT NOT NULL,
    m_id VARCHAR(20) NOT NULL,
    m_pwd VARCHAR(255) NOT NULL,
    m_email VARCHAR(255) NOT NULL,
    m_phone VARCHAR(20),
    m_address VARCHAR(150),
    m_name VARCHAR(30),
    picture VARCHAR(255) NOT NULL,
    approval_status VARCHAR(20) NOT NULL,
    reject_reason VARCHAR(255),
    requested_at DATETIME(6),
    approved_at DATETIME(6),
    PRIMARY KEY (m_no),
    UNIQUE KEY uk_apartment_manager_login_id (m_id),
    CONSTRAINT fk_apartment_manager_apartment
        FOREIGN KEY (a_no) REFERENCES apartments (a_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user` (
    u_no INT NOT NULL AUTO_INCREMENT,
    u_id VARCHAR(20) NOT NULL,
    u_pwd VARCHAR(255) NOT NULL,
    u_name VARCHAR(30) NOT NULL,
    u_email VARCHAR(255) NOT NULL,
    u_phone VARCHAR(20),
    p_date DATETIME(6),
    u_dong VARCHAR(20) NOT NULL,
    u_ho VARCHAR(20) NOT NULL,
    a_no INT,
    approval_status VARCHAR(20) NOT NULL,
    reject_reason VARCHAR(255),
    PRIMARY KEY (u_no),
    UNIQUE KEY uk_user_login_id (u_id),
    CONSTRAINT fk_user_apartment
        FOREIGN KEY (a_no) REFERENCES apartments (a_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE car (
    c_no INT NOT NULL AUTO_INCREMENT,
    c_name VARCHAR(50),
    c_number VARCHAR(255) NOT NULL,
    c_kind VARCHAR(30),
    c_note VARCHAR(255),
    c_date DATETIME(6),
    u_no INT,
    PRIMARY KEY (c_no),
    CONSTRAINT fk_car_user
        FOREIGN KEY (u_no) REFERENCES `user` (u_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE parking_lot (
    pl_no INT NOT NULL AUTO_INCREMENT,
    a_no INT NOT NULL,
    pl_name VARCHAR(255) NOT NULL,
    pl_floor VARCHAR(20) NOT NULL,
    total_spaces INT NOT NULL,
    used_spaces INT NOT NULL,
    PRIMARY KEY (pl_no),
    CONSTRAINT fk_parking_lot_apartment
        FOREIGN KEY (a_no) REFERENCES apartments (a_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE parking_zone (
    pz_no INT NOT NULL AUTO_INCREMENT,
    pl_no INT NOT NULL,
    area_number VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    layout_row INT,
    layout_column INT,
    status_change_reason VARCHAR(255),
    current_car_number VARCHAR(255),
    PRIMARY KEY (pz_no),
    CONSTRAINT fk_parking_zone_parking_lot
        FOREIGN KEY (pl_no) REFERENCES parking_lot (pl_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE registered_cars (
    v_no INT NOT NULL AUTO_INCREMENT,
    u_no INT NOT NULL,
    c_number VARCHAR(50) NOT NULL,
    reg_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    park_time DATETIME,
    expire_date DATETIME,
    PRIMARY KEY (v_no),
    CONSTRAINT fk_registered_cars_user
        FOREIGN KEY (u_no) REFERENCES `user` (u_no) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE parking_history (
    history_id INT NOT NULL AUTO_INCREMENT,
    pz_no INT,
    c_no INT,
    v_no INT,
    history_zone VARCHAR(255) NOT NULL,
    history_plate VARCHAR(50) NOT NULL,
    history_entry_time DATETIME(6) NOT NULL,
    history_exit_time DATETIME(6),
    history_status VARCHAR(20) NOT NULL,
    PRIMARY KEY (history_id),
    INDEX idx_parking_history_zone (pz_no),
    INDEX idx_parking_history_car (c_no),
    INDEX idx_parking_history_visitor_car (v_no),
    INDEX idx_parking_history_plate (history_plate),
    CONSTRAINT fk_parking_history_zone
        FOREIGN KEY (pz_no) REFERENCES parking_zone (pz_no) ON DELETE SET NULL,
    CONSTRAINT fk_parking_history_car
        FOREIGN KEY (c_no) REFERENCES car (c_no) ON DELETE SET NULL,
    CONSTRAINT fk_parking_history_visitor_car
        FOREIGN KEY (v_no) REFERENCES registered_cars (v_no) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
    noti_no INT NOT NULL AUTO_INCREMENT,
    u_no INT NOT NULL,
    noti_type VARCHAR(20) DEFAULT 'system',
    noti_title VARCHAR(100) NOT NULL,
    noti_message VARCHAR(255) NOT NULL,
    is_read TINYINT(1) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (noti_no),
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (u_no) REFERENCES `user` (u_no) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE device_info (
    device_id VARCHAR(100) NOT NULL,
    u_no INT NOT NULL,
    fcm_token VARCHAR(255),
    os_type VARCHAR(20),
    last_login DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (device_id),
    CONSTRAINT fk_device_info_user
        FOREIGN KEY (u_no) REFERENCES `user` (u_no) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE settings (
    setting_no INT NOT NULL AUTO_INCREMENT,
    device_id VARCHAR(100) NOT NULL,
    alert_push TINYINT(1) DEFAULT 1,
    theme_mode VARCHAR(20) DEFAULT 'light',
    PRIMARY KEY (setting_no),
    UNIQUE KEY uk_settings_device_id (device_id),
    CONSTRAINT fk_settings_device
        FOREIGN KEY (device_id) REFERENCES device_info (device_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE waiting_list (
    wait_no INT NOT NULL AUTO_INCREMENT,
    u_no INT NOT NULL,
    target_slot_id VARCHAR(10),
    is_notified TINYINT(1) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (wait_no),
    CONSTRAINT fk_waiting_list_user
        FOREIGN KEY (u_no) REFERENCES `user` (u_no) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE manager_inquiry (
    inquiry_no INT NOT NULL AUTO_INCREMENT,
    m_no INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    answer VARCHAR(2000),
    created_at DATETIME(6) NOT NULL,
    answered_at DATETIME(6),
    PRIMARY KEY (inquiry_no),
    CONSTRAINT fk_manager_inquiry_manager
        FOREIGN KEY (m_no) REFERENCES apartment_manager (m_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE resident_inquiry (
    inquiry_no INT NOT NULL AUTO_INCREMENT,
    u_no INT NOT NULL,
    c_no INT,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    answer VARCHAR(2000),
    created_at DATETIME(6) NOT NULL,
    answered_at DATETIME(6),
    PRIMARY KEY (inquiry_no),
    CONSTRAINT fk_resident_inquiry_user
        FOREIGN KEY (u_no) REFERENCES `user` (u_no),
    CONSTRAINT fk_resident_inquiry_car
        FOREIGN KEY (c_no) REFERENCES car (c_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
