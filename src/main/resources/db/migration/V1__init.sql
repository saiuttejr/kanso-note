-- =====================================================================
-- V1__init.sql — Flyway migration: initial schema for Kanso offline DB
-- Interview talking point: Flyway gives us repeatable, version-controlled
-- migrations so the schema is always consistent across dev/demo runs.
--
-- Tables: profile, category_rule, transaction, uploaded_file
-- Design decisions:
--   - Single profile row (single-user local app, no multi-user auth).
--   - category_rule has priority + pattern_type for deterministic resolution.
--   - transaction stores the matched_rule_id for explainability.
--   - uploaded_file tracks CSV uploads for audit / re-import.
-- =====================================================================

-- Profile: single-user settings (passphrase salt stored here if encryption enabled)
CREATE TABLE IF NOT EXISTS profile (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    display_name VARCHAR(100)  NOT NULL DEFAULT 'Default User',
    -- encryption_salt is stored as hex; NULL means encryption not configured.
    encryption_salt VARCHAR(64),
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed a default profile so the app works out-of-the-box
INSERT INTO profile (display_name) VALUES ('Default User');

-- Category rule: drives the deterministic rule engine
-- pattern_type: 'KEYWORD' (substring match) or 'REGEX' (Java regex)
-- priority: higher integer wins; on tie, longest pattern wins
CREATE TABLE IF NOT EXISTS category_rule (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    pattern_type VARCHAR(10)  NOT NULL DEFAULT 'KEYWORD',
    pattern      VARCHAR(255) NOT NULL,
    category     VARCHAR(100) NOT NULL,
    priority     INT          NOT NULL DEFAULT 0,
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    is_default   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed default rules useful for beginner bank users
INSERT INTO category_rule (pattern_type, pattern, category, priority, enabled, is_default) VALUES
    ('KEYWORD', 'walmart',     'Groceries',     10, TRUE, TRUE),
    ('KEYWORD', 'target',      'Groceries',     10, TRUE, TRUE),
    ('KEYWORD', 'costco',      'Groceries',     10, TRUE, TRUE),
    ('KEYWORD', 'kroger',      'Groceries',     10, TRUE, TRUE),
    ('KEYWORD', 'uber',        'Transport',     10, TRUE, TRUE),
    ('KEYWORD', 'lyft',        'Transport',     10, TRUE, TRUE),
    ('KEYWORD', 'shell',       'Fuel',          10, TRUE, TRUE),
    ('KEYWORD', 'chevron',     'Fuel',          10, TRUE, TRUE),
    ('KEYWORD', 'electric',    'Utilities',      8, TRUE, TRUE),
    ('KEYWORD', 'water bill',  'Utilities',      8, TRUE, TRUE),
    ('KEYWORD', 'internet',    'Utilities',      8, TRUE, TRUE),
    ('KEYWORD', 'netflix',     'Subscriptions', 10, TRUE, TRUE),
    ('KEYWORD', 'spotify',     'Subscriptions', 10, TRUE, TRUE),
    ('KEYWORD', 'rent',        'Housing',       15, TRUE, TRUE),
    ('KEYWORD', 'mortgage',    'Housing',       15, TRUE, TRUE),
    ('KEYWORD', 'salary',      'Income',        20, TRUE, TRUE),
    ('KEYWORD', 'payroll',     'Income',        20, TRUE, TRUE),
    ('KEYWORD', 'amazon',      'Shopping',       5, TRUE, TRUE),
    ('KEYWORD', 'restaurant',  'Dining',        10, TRUE, TRUE),
    ('KEYWORD', 'cafe',        'Dining',        10, TRUE, TRUE);

-- Transaction: the core financial record
CREATE TABLE IF NOT EXISTS transaction (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    date            DATE           NOT NULL,
    description     VARCHAR(500)   NOT NULL,
    amount          DECIMAL(15, 2) NOT NULL,
    category        VARCHAR(100)   NOT NULL DEFAULT 'Uncategorized',
    matched_rule_id BIGINT,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_rule FOREIGN KEY (matched_rule_id)
        REFERENCES category_rule(id) ON DELETE SET NULL
);

CREATE INDEX idx_transaction_date     ON transaction(date);
CREATE INDEX idx_transaction_category ON transaction(category);

-- Uploaded file: tracks CSV uploads for audit and optional re-import
CREATE TABLE IF NOT EXISTS uploaded_file (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name  VARCHAR(255) NOT NULL,
    stored_path    VARCHAR(500) NOT NULL,
    encrypted      BOOLEAN      NOT NULL DEFAULT FALSE,
    row_count      INT          NOT NULL DEFAULT 0,
    uploaded_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
