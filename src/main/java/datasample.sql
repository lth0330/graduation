DROP DATABASE IF EXISTS datasample;
CREATE DATABASE datasample DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE datasample;

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
    u_pwd VARCHAR(20) NOT NULL,
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
    c_name VARCHAR(30) NOT NULL,
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
    PRIMARY KEY (pz_no),
    CONSTRAINT fk_parking_zone_parking_lot
        FOREIGN KEY (pl_no) REFERENCES parking_lot (pl_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
