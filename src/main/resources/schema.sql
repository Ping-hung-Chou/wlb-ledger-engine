-- ===============================================================
-- Project Chronicle — WLB Double-Entry Ledger Engine · Core Schema (v1.0)
-- Key Features: RBAC dual-role identity · ACID double-entry accounting · Optimistic-lock defence · 3-day grace-period protection
-- ===============================================================

-- Table 1. Identities
-- Strategic intent: Implements RBAC by storing dual-role permissions as a JSON array.
CREATE TABLE Identities
(
    identity_id VARCHAR(36) PRIMARY KEY,       -- UUID fixed length (36 chars)
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role JSON NOT NULL,                        -- Role permissions (e.g. '["USER", "ARCHITECT"]')
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Record creation time
    updated_at TIMESTAMP                            -- Record last-modified time
                         DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP

) COMMENT='Identity principal & RBAC access control';

-- Table 2. Asset_Accounts
-- Strategic intent: Core vault. 1-to-N design allows users to dynamically create accounts
--   (e.g. "Available Time Pool", "DB Study Account", "Leisure Drawing Account").
CREATE TABLE Asset_Accounts
(
    account_id VARCHAR(36) PRIMARY KEY,
    identity_id VARCHAR(36) NOT NULL,
    account_name VARCHAR(100) NOT NULL,        -- Account label (e.g. "Available Time Pool", "DB Study Account")
    account_type VARCHAR(20) NOT NULL,         -- Account role: SOURCE (debit) or TARGET (credit)
    balance DECIMAL(10,2) DEFAULT 0.00,        -- Current balance of this account
    version INT DEFAULT 0,                     -- Optimistic-lock version (guards against high-concurrency double-submit)
    updated_at TIMESTAMP
                          DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_asset_identity
        FOREIGN KEY (identity_id)
            REFERENCES Identities(identity_id)
            ON DELETE CASCADE                      -- Cascade delete

) COMMENT='Dynamically scalable financial-grade asset accounts';

-- Table 3. Milestone_Nodes
-- Strategic intent: Goal nodes for project and life management.
CREATE TABLE Milestone_Nodes
(
    node_id VARCHAR(36) PRIMARY KEY,
    identity_id VARCHAR(36) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    milestone_type ENUM('WORK', 'LEISURE') NOT NULL,   -- Milestone category (WORK / LEISURE)
    target_minutes INT NOT NULL,                        -- Target time commitment in minutes
    actual_minutes INT DEFAULT 0,                       -- Actual time invested in minutes
    time_multiplier DECIMAL(4,2) DEFAULT 1.00,          -- Time multiplier (inflation / deflation adjustment)
    status ENUM('PENDING','IN_PROGRESS','PAUSED','COMPLETED','ARCHIVED')
        DEFAULT 'PENDING',                              -- Lifecycle status
    started_at TIMESTAMP NULL,                          -- Timestamp when milestone was started
    completed_at TIMESTAMP NULL,                        -- Timestamp when milestone was completed
    paused_at TIMESTAMP NULL,                           -- Timestamp when milestone was last paused
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_milestone_identity
        FOREIGN KEY (identity_id)
            REFERENCES Identities(identity_id)
            ON DELETE CASCADE

) COMMENT='Milestone goals for life balance and academic progress';

-- Table 4. Executions
-- Strategic intent: Defines activity templates and their asset conversion multipliers (inflation / deflation).
CREATE TABLE Executions
(
    execution_id VARCHAR(36) PRIMARY KEY,
    activity_name VARCHAR(100) NOT NULL,
    conversion_multiplier DECIMAL(5,2) DEFAULT 1.00, -- ⭐ Asset conversion multiplier
    is_active BOOLEAN DEFAULT TRUE

) COMMENT='Activity templates with asset-conversion-rate configuration';

-- Table 5. Time_Ledgers
-- Strategic intent: True double-entry bookkeeping — explicitly records fund flow from source to destination account.
CREATE TABLE Time_Ledgers
(
    ledger_id VARCHAR(36) PRIMARY KEY,
    identity_id VARCHAR(36) NOT NULL,
    from_account_id VARCHAR(36) NOT NULL,      -- ⭐ Source account (debit: available-time pool)
    to_account_id VARCHAR(36) NOT NULL,        -- ⭐ Destination account (credit: focus / leisure asset)
    execution_id VARCHAR(36) NOT NULL,
    milestone_id VARCHAR(36) NULL,             -- 🟡 Optional milestone linkage (nullable)
    time_spent INT NOT NULL,                   -- 🔴 Immutable once committed (financial lock)
    asset_amount DECIMAL(10,2) NOT NULL,       -- 🔴 Immutable once committed (financial lock)
    reflection_memo TEXT,                      -- 🟢 Editable within 3-day grace period
    album_image_url VARCHAR(255),              -- 🟢 Editable within 3-day grace period
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
                         DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_ledger_identity
        FOREIGN KEY (identity_id)
            REFERENCES Identities(identity_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_ledger_from_acc
        FOREIGN KEY (from_account_id)
            REFERENCES Asset_Accounts(account_id),
    -- Preserves transfer history; prevents accidental deletion
    CONSTRAINT fk_ledger_to_acc
        FOREIGN KEY (to_account_id)
            REFERENCES Asset_Accounts(account_id),

    CONSTRAINT fk_ledger_execution
        FOREIGN KEY (execution_id)
            REFERENCES Executions(execution_id),
    -- Preserves activity history; prevents accidental deletion
    CONSTRAINT fk_ledger_milestone
        FOREIGN KEY (milestone_id)
            REFERENCES Milestone_Nodes(node_id)
    -- Preserves ledger trail; survives milestone deletion

) COMMENT='Immutable double-entry audit trail';

-- Table 6. AI_Weekly_Insights
-- Strategic intent: Persists the output of RAG-scheduled jobs and LLM graceful-degradation fallbacks.
CREATE TABLE AI_Weekly_Insights
(
    insight_id VARCHAR(36) PRIMARY KEY,
    identity_id VARCHAR(36) NOT NULL,
    year_week VARCHAR(10) NOT NULL,
    ai_summary TEXT NOT NULL,                  -- ⭐ AI-generated weekly letter
    token_used INT DEFAULT 0,                  -- Token cost tracking
    fallback_used BOOLEAN DEFAULT FALSE,       -- ⭐ Flags whether fallback degradation was triggered
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Scheduled-job monitoring anchor

    CONSTRAINT fk_ai_identity
        FOREIGN KEY (identity_id)
            REFERENCES Identities(identity_id)
            ON DELETE CASCADE

) COMMENT='AI weekly insights and RAG fallback cache';

-- Table 7. System_Configs
-- Strategic intent: YAGNI exception — provides high-flexibility global configuration (e.g. AI prompt templates).
CREATE TABLE System_Configs (
                                config_key VARCHAR(50) PRIMARY KEY,        -- Config key (e.g. 'AI_PROMPT_TONE')
                                config_value TEXT NOT NULL,                -- Config value (e.g. 'You are now a warm and wise mentor...')
                                description VARCHAR(255),                  -- Human-readable description (for the architect's reference)
                                updated_at TIMESTAMP
                                    DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP

) COMMENT='Global system configuration and AI prompt settings';

-- Seed: default AI prompt tone (Initialization)
INSERT INTO System_Configs (config_key, config_value, description)
VALUES ('AI_PROMPT_TONE', '你現在是一位溫嚴並濟的母親，請根據使用者的日記，給予充滿情緒價值的鼓勵。', 'AI weekly report generation prompt');