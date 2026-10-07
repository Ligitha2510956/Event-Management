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

