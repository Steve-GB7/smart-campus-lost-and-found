-- ============================================================
-- Unified Smart Campus Helpdesk — Database Schema
-- College of Engineering Chengannur (CEC)
-- ============================================================

CREATE DATABASE IF NOT EXISTS cec_helpdesk
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE cec_helpdesk;

-- ────────────────────────────────────────────────────────────
-- 1. USERS  (single table, discriminated by role)
-- ────────────────────────────────────────────────────────────
CREATE TABLE users (
    user_id      INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,          -- stored as BCrypt hash
    full_name    VARCHAR(120) NOT NULL,
    email        VARCHAR(120) NOT NULL UNIQUE,
    role         ENUM('STUDENT','STAFF','ADMIN') NOT NULL,
    department   VARCHAR(100),                   -- relevant for STAFF routing
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────
-- 2. LOST & FOUND POSTS
-- ────────────────────────────────────────────────────────────
CREATE TABLE posts (
    post_id      INT AUTO_INCREMENT PRIMARY KEY,
    author_id    INT NOT NULL,
    post_type    ENUM('LOST','FOUND') NOT NULL,
    category     VARCHAR(80)  NOT NULL,          -- Electronics, ID‑Card, Books, etc.
    title        VARCHAR(200) NOT NULL,
    description  TEXT         NOT NULL,
    image_path   VARCHAR(500),                   -- optional image file path
    video_path   VARCHAR(500),                   -- optional video file path
    is_resolved  BOOLEAN DEFAULT FALSE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Safe migration for existing installations:
-- ALTER TABLE posts ADD COLUMN IF NOT EXISTS video_path VARCHAR(500) NULL AFTER image_path;

-- ────────────────────────────────────────────────────────────
-- 3. PRIVATE MESSAGES  (linked to a post)
-- ────────────────────────────────────────────────────────────
CREATE TABLE messages (
    message_id   INT AUTO_INCREMENT PRIMARY KEY,
    post_id      INT NOT NULL,                   -- originating post
    sender_id    INT NOT NULL,
    receiver_id  INT NOT NULL,
    content      TEXT NOT NULL,
    sent_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id)    REFERENCES posts(post_id)  ON DELETE CASCADE,
    FOREIGN KEY (sender_id)  REFERENCES users(user_id)  ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────
-- 4. GRIEVANCE TICKETS
-- ────────────────────────────────────────────────────────────
CREATE TABLE tickets (
    ticket_id    INT AUTO_INCREMENT PRIMARY KEY,
    student_id   INT NOT NULL,
    category     ENUM('IT','ELECTRICAL','GENERAL_MAINTENANCE') NOT NULL,
    subject      VARCHAR(200) NOT NULL,
    description  TEXT         NOT NULL,
    status       ENUM('PENDING','IN_PROGRESS','RESOLVED') DEFAULT 'PENDING',
    staff_id     INT,                            -- assigned staff (auto-routed by dept)
    remarks      TEXT,                           -- staff remarks on resolution
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id)   REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ────────────────────────────────────────────────────────────
-- SEED: default admin account  (password = "admin123")
-- ────────────────────────────────────────────────────────────
INSERT INTO users (username, password, full_name, email, role, department)
VALUES ('admin', 'admin123', 'System Administrator', 'admin@cec.ac.in', 'ADMIN', 'Administration');
