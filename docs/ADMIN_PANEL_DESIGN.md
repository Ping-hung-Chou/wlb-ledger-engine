# Admin Panel Design

> **Design Principle:** Selective immutability — the UI enforces financial compliance through deliberate access control at the field level.

---

## Core Design Decision: Immutable Audit Trail
![Admin Panel UI Mockup](ui-mockups/admin_panel.png)
A system that exposes full CRUD on `Time_Ledgers` would fail any financial-grade architecture review. Transaction history is **absolutely immutable** by design and regulatory convention. This system deliberately removes Edit and Delete controls from the time ledger interface — the system administrator (including the architect) cannot alter past records.

> *"I implemented Read-only on the time tracking interface for financial compliance. Even as the system admin, I cannot falsify past focus records."*

---

## CRUD Business Logic — Operations Reference

| Operation | Business Scenario | Table(s) | Implementation Highlights |
|---|---|---|---|
| **C / R / U / D** | **Inflation Control & Goal Configuration** | `Executions`<br>`Milestone_Nodes` | **Parameterised Dynamic Pricing:** Adjust the `conversion_multiplier` for any activity at runtime without restarting the system. Demonstrates 1-to-N Foreign Key constraint enforcement and hot-reload capability without code deployment. |
| **R & Partial U** | **Immutable Memory Vault** | `Time_Ledgers` | **Financial Compliance & Anti-Tampering:** Edit/Delete controls for `time_spent` and `asset_amount` are permanently hidden. The **3-day grace period** on `reflection_memo` and `album_image_url` is enforced by evaluating `created_at` in SQL — once 72 hours pass, the edit control greys out automatically. |
| **U (Update)** | **Global Config & AI Prompt Hot-Reload** | `System_Configs` | **SaaS-Grade Extensibility:** Dynamically update the AI prompt tone and WLB ratio targets from the admin interface. Demonstrates extraction of mutable business logic from compiled code into the database — achieving Zero-Downtime Hot Reload. |
| **D (Delete)** | **Identity Deregistration** | `Identities` | **Cascade Delete:** Deleting an identity triggers `ON DELETE CASCADE`, cleanly removing all associated asset accounts, ledger entries, and AI insights — zero orphan data left in the database. |

---

## Field-Level Access Control Summary

| Field | Access Level | Enforcement Mechanism |
|---|---|---|
| `time_spent`, `asset_amount` | **Read-only (permanent)** | No UPDATE endpoint exposed; UI buttons absent |
| `reflection_memo`, `album_image_url` | **Editable within 72 hours** | `created_at` evaluated at query time; UI auto-disables after grace period |
| `Executions.conversion_multiplier` | **Admin write** | Dynamic business rule adjustment via admin panel |
| `Executions.is_active` | **Admin write (soft-delete)** | Hides activity from UI without deleting historical ledger references |
| `System_Configs.config_value` | **Admin write** | Hot-reload of AI prompts and global system parameters |
| `Identities` (row) | **Admin delete only** | Triggers cascading cleanup of all owned data |
