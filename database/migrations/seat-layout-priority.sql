-- Run once against an existing database before starting this version.
-- Existing buses and booking labels retain their original 2+2 layout.
ALTER TABLE buses ADD COLUMN seat_layout ENUM('3+2','2+3','2+2') NOT NULL DEFAULT '2+2';
ALTER TABLE bookings ADD COLUMN seat_layout ENUM('3+2','2+3','2+2') NOT NULL DEFAULT '2+2';
