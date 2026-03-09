-- =====================================================================
-- V2__budget_audit_tables.sql — Flyway migration: budget tracking + audit log
--
-- New capabilities:
--   - budget: per-category monthly spending limits with alerting
--   - audit_log: immutable event log for all user actions (event sourcing lite)
-- =====================================================================

-- Budget: per-category monthly spending limits
CREATE TABLE IF NOT EXISTS budget (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    category       VARCHAR(100) NOT NULL,
    monthly_limit  DECIMAL(15, 2) NOT NULL,
    alert_threshold DECIMAL(5, 2) NOT NULL DEFAULT 80.00,  -- Alert at 80% by default
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_budget_category UNIQUE (category)
);

CREATE INDEX idx_budget_category ON budget(category);

-- Audit log: immutable event trail for all user actions
CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type  VARCHAR(50)  NOT NULL,  -- TRANSACTION_CREATED, RULE_ADDED, CSV_IMPORTED, etc.
    entity_type VARCHAR(50),            -- TRANSACTION, RULE, BUDGET, etc.
    entity_id   BIGINT,                 -- ID of affected entity
    details     VARCHAR(1000),          -- Human-readable description
    metadata    VARCHAR(2000),          -- JSON-encoded extra context
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_event_type ON audit_log(event_type);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
CREATE INDEX idx_audit_log_entity     ON audit_log(entity_type, entity_id);
