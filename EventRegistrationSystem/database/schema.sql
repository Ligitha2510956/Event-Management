-- Event Registration System - Database Schema
-- Run this once in MySQL Workbench (or mysql CLI) before running the app.

CREATE DATABASE IF NOT EXISTS event_registration;
USE event_registration;

CREATE TABLE IF NOT EXISTS events (
    event_id     INT AUTO_INCREMENT PRIMARY KEY,
    event_name   VARCHAR(100) NOT NULL,
    event_date   DATE NOT NULL,
    venue        VARCHAR(100) NOT NULL,
    capacity     INT NOT NULL
);

CREATE TABLE IF NOT EXISTS participants (
    participant_id   INT AUTO_INCREMENT PRIMARY KEY,
    event_id         INT NOT NULL,
    participant_name VARCHAR(100) NOT NULL,
    email            VARCHAR(100) NOT NULL,
    phone            VARCHAR(15) NOT NULL,
    FOREIGN KEY (event_id) REFERENCES events(event_id)
        ON DELETE CASCADE
);

-- Optional: a couple of sample rows so the app has something to show on first run.
-- Comment these out if you'd rather start empty.
INSERT INTO events (event_name, event_date, venue, capacity)
VALUES ('Tech Symposium', '2026-11-10', 'Main Auditorium', 150);

use parking_violation_db;
CREATE TABLE vehicle_directory (
    directory_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(20) UNIQUE NOT NULL,
    owner_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    mobile_number VARCHAR(15)
);
