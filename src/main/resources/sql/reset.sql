-- Reset script for demo deployments.
-- Runs before data.sql only when spring.sql.init.mode=always.
-- WARNING: This removes all existing application data in the connected database.

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM plate_correction_review;
DELETE FROM gate_entry_log;
DELETE FROM resident_inquiry;
DELETE FROM manager_inquiry;
DELETE FROM waiting_list;
DELETE FROM settings;
DELETE FROM device_info;
DELETE FROM notifications;
DELETE FROM manager_notification;
DELETE FROM parking_history;
DELETE FROM registered_cars;
DELETE FROM parking_zone;
DELETE FROM parking_lot;
DELETE FROM car;
DELETE FROM `user`;
DELETE FROM apartment_manager;
DELETE FROM web_manager;
DELETE FROM apartments;

ALTER TABLE plate_correction_review AUTO_INCREMENT = 1;
ALTER TABLE gate_entry_log AUTO_INCREMENT = 1;
ALTER TABLE resident_inquiry AUTO_INCREMENT = 1;
ALTER TABLE manager_inquiry AUTO_INCREMENT = 1;
ALTER TABLE waiting_list AUTO_INCREMENT = 1;
ALTER TABLE settings AUTO_INCREMENT = 1;
ALTER TABLE notifications AUTO_INCREMENT = 1;
ALTER TABLE manager_notification AUTO_INCREMENT = 1;
ALTER TABLE parking_history AUTO_INCREMENT = 1;
ALTER TABLE registered_cars AUTO_INCREMENT = 1;
ALTER TABLE parking_zone AUTO_INCREMENT = 1;
ALTER TABLE parking_lot AUTO_INCREMENT = 1;
ALTER TABLE car AUTO_INCREMENT = 1;
ALTER TABLE `user` AUTO_INCREMENT = 1;
ALTER TABLE apartment_manager AUTO_INCREMENT = 1;
ALTER TABLE web_manager AUTO_INCREMENT = 1;
ALTER TABLE apartments AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;
