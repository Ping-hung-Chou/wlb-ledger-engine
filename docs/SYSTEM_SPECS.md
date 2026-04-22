# System Specifications

> This document covers the system's data flow architecture (DFD) and the detailed Input-Process-Output (IPO) specifications for each core processing module.

---

## Part 1 — Data Flow Architecture
![System Data Flow Diagram](diagrams/dfd.png)
### Design Philosophy: Pipelined High-Concurrency Defence

The core design principle is **boundary enforcement** — completely isolating unpredictable external risks (user double-clicks, LLM API timeouts) from the absolutely safe internal vault (the database). The system's layout maps directly to a real bank's physical defence layers.

### The Three-Layer Defence Model

| Layer | Bank Analogy | System Components |
|---|---|---|
| **Left — External World** | Bank lobby (Untrusted Zone) | Users submitting settlement sessions; external LLM API calls |
| **Centre — Processing Hub** | Bulletproof-glass counter | 1.0 Settlement Transaction Centre · 3.0 AI Weekly Report Scheduler |
| **Right — Internal Vault** | Underground safe (Absolute Security Zone) | D1–D6 logical data stores |

### Why This Architecture?

- **Prevents Single Point of Failure:** The background scheduler and graceful degradation (Fallback) keep the system alive even when external APIs fail. `AI_Weekly_Insights` is always populated — with a generated letter or a static fallback.
- **Blocks Race Conditions (Optimistic Locking):** Every settlement request is treated as potentially concurrent. The final defence is placed immediately before the database write, ensuring asset credits are never double-issued.
- **Guarantees Financial Auditability:** `Time_Ledgers` is a dedicated, immutable store. Every financial movement permanently records both the debit and credit sides (Double-Entry Bookkeeping).
- **Separation of Concerns (Decoupling):** Grouping the data stores and external entities into explicit subgraph boundaries demonstrates modular boundary thinking — satisfying both software engineering professors and enterprise architecture reviewers.

### Logical Data Store → Physical Table Mapping

| DFD Store | Physical Table(s) | Notes |
|---|---|---|
| **D1: Identity & Asset** | `Identities` + `Asset_Accounts` | Atomically co-created on account opening (1-to-N relationship) |
| **D2: Time Ledger Trail** | `Time_Ledgers` | Immutable double-entry audit trail with 3-day grace period on memo/image fields |
| **D3: Milestone Nodes** | `Milestone_Nodes` | Project and life goal tracking with full lifecycle status |
| **D4: AI Weekly Insights** | `AI_Weekly_Insights` | Scheduled-job output and fallback degradation cache |
| **D5: Activity Templates** | `Executions` | Activity definitions and asset conversion multipliers (soft-delete via `is_active`) |
| **D6: System Config** | `System_Configs` | Global configuration — AI prompt hot-reload and WLB parameter tuning |

---

## Part 2 — IPO Module Specifications

### Module 0.0 — User Registration & Account Opening

| Field | Detail |
|---|---|
| **Module** | User Registration & Account Initialization |
| **Input** | `username` (string), `password` (plaintext string) |
| **Process** | **Phase 1 — Security Defence**<br>1. Query `Identities` table to check for duplicate `username`.<br>2. Hash `password` using BCrypt algorithm.<br><br>**Phase 2 — Financial-Grade Account Opening (ACID Zone)**<br>3. Start `@Transactional`.<br>4. `INSERT INTO Identities` — store username, hashed password, and `["USER", "ARCHITECT"]` dual RBAC roles.<br>5. `INSERT INTO Asset_Accounts` — provision "Available Time" and "Recharge Asset" accounts with `version = 0`.<br>6. Commit on full success; Rollback on any error. |
| **Output** | **Success:** `201 Created` — frontend redirects to login screen, JWT issued on next login.<br>**Failure:** `409 Conflict` (username taken) or `500 Internal Server Error`. |

---

### Module 1.0 — WLB Settlement Transaction Centre

| Field | Detail |
|---|---|
| **Module** | WLB Transaction Processing Centre |
| **Input** | `identity_id`, `execution_id`, `time_spent`, `reflection_memo`, `album_image_url`, `clientVersion` |
| **Process** | **Phase 1 — Validation**<br>1. Read `Milestone_Nodes` and `Executions` to confirm the activity is valid and retrieve the asset conversion multiplier.<br><br>**Phase 2 — Financial-Grade Cross-Account Transfer (ACID Zone)**<br>2. Start `@Transactional`.<br>3. Read `Asset_Accounts` for Optimistic Lock comparison (`clientVersion` vs. stored `version`).<br>4. On match: deduct available time, credit target asset, increment `version`.<br>5. Write immutable record to `Time_Ledgers` — lock `time_spent` and `asset_amount`; grant 3-day grace period on `reflection_memo` and `album_image_url`.<br>6. Commit.<br><br>**Phase 3 — Milestone Verdict**<br>7. Check whether the milestone target has been reached; update `Milestone_Nodes` status if achieved. |
| **Output** | **Success (200 OK):** Returns latest account balances and new version number — frontend clears localStorage draft.<br>**Failure (400/409):** Insufficient balance or optimistic lock conflict — prompt user to reload and retry. |

---

### Module 3.0 — AI Weekly Report & RAG Scheduling

| Field | Detail |
|---|---|
| **Module** | AI Weekly Report & RAG Scheduling Centre (Cron Job Daemon) |
| **Input** | System clock trigger — every Sunday at 20:00 (`@Scheduled(cron = "0 0 20 * * SUN")`) |
| **Process** | **Phase 1 — Async Aggregation & Prompt Injection**<br>1. Spring Boot `@Scheduled` background thread initiates.<br>2. Aggregate this week's journal entries from `Time_Ledgers`.<br>3. Read `System_Configs` to retrieve the latest AI prompt tone and WLB target ratio.<br>4. Randomly select a motivational quote from the pre-loaded static list.<br><br>**Phase 2 — LLM Call & Graceful Degradation**<br>5. Call external LLM API (OpenAI / Gemini).<br>6. **Success:** Parse response JSON and `INSERT INTO AI_Weekly_Insights` (`fallback_used = FALSE`).<br>7. **Fallback:** On API timeout or rate limit, log the error and `INSERT` a pre-loaded static letter (`fallback_used = TRUE`). |
| **Output** | No immediate frontend response. Side effect: `AI_Weekly_Insights` is updated and available for the user to read the following morning. |

---

### Why Module 2.0 (Admin Panel) Has No IPO Table

The Admin Panel's operations are direct CRUD actions on `Milestone_Nodes`, `Executions`, and `System_Configs`. The logic is linear with no complex state transitions or algorithmic processing. Its specification is fully captured in the DFD data store mappings and `ADMIN_PANEL_DESIGN.md`, deliberately avoiding over-engineering (YAGNI principle).
