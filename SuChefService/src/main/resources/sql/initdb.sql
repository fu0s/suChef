-- Drop database if exists and create new one
DROP DATABASE IF EXISTS suchef;

CREATE DATABASE suchef;

USE suchef;

-- Users Table
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Documents Table
CREATE TABLE documents (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    date DATETIME NOT NULL,
    uploaded_at DATETIME NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Initial Data (Optional - for testing)
INSERT INTO
    users (id, email, name, password)
VALUES (
        '1',
        'admin@suchef.com',
        'Admin User',
        '$2a$10$e8FpYDht0p2ZpHyzDdClYOvp3OLtnogHl81acAFFG8Bv6Y/oryPoG'
    ),
    (
        '2',
        'user@suchef.com',
        'Regular User',
        '$2a$10$e8FpYDht0p2ZpHyzDdClYOvp3OLtnogHl81acAFFG8Bv6Y/oryPoG'
    );