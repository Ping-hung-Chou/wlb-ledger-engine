# Database Schema

> This schema translates the system's internal vault design into a strict relational structure conforming to Third Normal Form (3NF).  
> It serves as the single source of truth for all data persistence in the WLB Double-Entry Ledger Engine.

---

## Entity Relationship Diagram

```mermaid
erDiagram
    %% [Layout] Hub-and-spoke radial arrangement

    %% 1. Core identity with unlimited accounts (1-to-N)
    Identities ||--o{ Asset_Accounts : "opens multiple accounts (owns)"
    Identities ||--o{ Milestone_Nodes : "sets goals (sets)"
    Identities ||--o{ Time_Ledgers : "generates ledger entries (generates)"
    Identities ||--o{ AI_Weekly_Insights : "receives weekly insights (receives)"

    %% 2. Double-entry ledger relations (explicit fund-flow source and destination)
    Asset_Accounts ||--o{ Time_Ledgers : "debit source (from)"
    Asset_Accounts ||--o{ Time_Ledgers : "credit destination (to)"
    Executions ||--o{ Time_Ledgers : "triggers ledger entries (triggers)"
    Milestone_Nodes ||--o{ Time_Ledgers : "tracks time investment (tracks, nullable)"

    %% ======== Entity Attribute Definitions ========

    Identities {
        varchar identity_id PK
        varchar username
        varchar password_hash
        json role "dual-role RBAC permissions"
        timestamp created_at "record creation time"
        timestamp updated_at "record last-modified time"
    }

    Asset_Accounts {
        varchar account_id PK
        varchar identity_id FK
        varchar account_name "account label (e.g. Available Time, Manga Drawing)"
        varchar account_type "account role (SOURCE / TARGET)"
        decimal balance "current asset balance"
        int version "optimistic-lock version number"
        timestamp updated_at "last updated time"
    }

    Milestone_Nodes {
        varchar node_id PK
        varchar identity_id FK
        varchar title "milestone title"
        text description "detailed description"
        enum milestone_type "milestone category (WORK / LEISURE)"
        int target_minutes "target time commitment (minutes)"
        int actual_minutes "actual time invested (minutes)"
        decimal time_multiplier "time multiplier"
        enum status "lifecycle status (PENDING / IN_PROGRESS / PAUSED / COMPLETED / ARCHIVED)"
        timestamp started_at "started timestamp"
        timestamp completed_at "completed timestamp"
        timestamp paused_at "paused timestamp"
        timestamp created_at "record creation time"
    }

    Executions {
        varchar execution_id PK
        varchar activity_name "activity name"
        decimal conversion_multiplier "asset conversion multiplier"
        boolean is_active "is active (soft-delete flag)"
    }

    Time_Ledgers {
        varchar ledger_id PK
        varchar identity_id FK
        varchar from_account_id FK "debit: available-time account"
        varchar to_account_id FK "credit: leisure / focus asset account"
        varchar execution_id FK
        varchar milestone_id FK "linked milestone (nullable)"
        int time_spent "immutable: time spent"
        decimal asset_amount "immutable: asset amount"
        text reflection_memo "editable within 3-day grace period"
        varchar album_image_url "album image URL"
        timestamp created_at "grace-period anchor timestamp"
        timestamp updated_at "last updated time"
    }

    AI_Weekly_Insights {
        varchar insight_id PK
        varchar identity_id FK
        varchar year_week "year and week number"
        text ai_summary "AI-generated weekly letter"
        int token_used "token cost tracking"
        boolean fallback_used "fallback degradation flag"
        timestamp created_at "scheduled-job monitoring anchor"
    }

    System_Configs {
        varchar config_key PK
        text config_value "system config value and prompt template"
        varchar description "human-readable description"
        timestamp updated_at "last updated time"
    }
```

---

## Design Rationale

### RBAC Single-Account Dual-Role (`role`)

Abandons the novice pattern of separate User/Admin tables. Permissions are stored as a JSON array, enabling clean JWT integration and dynamic frontend route-level access control.

### Unlimited Scalable Double-Entry Ledger (`Asset_Accounts` + `Time_Ledgers`)

The core financial engine. Instead of hardcoding a balance field, a 1-to-N account architecture allows users to dynamically open dedicated accounts (e.g. "Manga Drawing Account", "DB Study Account"). `Time_Ledgers` strictly records `from_account_id` (debit party) and `to_account_id` (credit party), implementing true **Double-Entry Bookkeeping**.

### Partial Immutability with Grace Period (`Time_Ledgers`)

Field-level access control: `time_spent` and `asset_amount` are permanently locked once committed. The `reflection_memo` and `album_image_url` fields are granted a 3-day edit window for UX flexibility without compromising financial integrity.

### System Resilience Flag (`fallback_used`)

The boolean flag in `AI_Weekly_Insights` provides an observable record of system health. Even when the LLM API is unavailable, the table faithfully records whether a graceful degradation fallback was triggered.

### Hot-Reloadable Global Configuration (`System_Configs`)

AI prompt templates and business logic variables are extracted from the codebase into this table, enabling zero-downtime hot updates and defending against future scope creep.

### Third Normal Form (3NF) Compliance

`Time_Ledgers` references `execution_id` via Foreign Key instead of duplicating the activity name string. This eliminates Update Anomalies and satisfies strict normalization requirements.

---

## DDL Reference

See: `../src/main/resources/schema.sql`
