# Development Journal

> A chronological engineering log documenting AI-assisted development decisions, architectural debates, verification methods, and key debugging incidents.  
> Ordered from **most recent to oldest**.

---

## 📅 2026-03-22 — Settlement Transaction Centre (Optimistic Lock + Cross-Account Transfer)

### Prompt Strategy

- **Initial Directive:** Provided the complete development checklist for the "1.0 Settlement Centre", requesting implementation of `@Transactional` transfer logic, optimistic lock comparison, double-entry bookkeeping debit/credit, and immutable audit trail writing to `Time_Ledgers`.
- **Iterative Corrections:**
  - **[Domain Logic Clarification]:** When generated code threw a `setTransactionType` not-found error, I rejected the blind addition of a new field. Through business scenario analysis, I established that `ExecutionId` alone sufficiently traces the activity source — the redundant `TransactionType` field was discarded.
  - **[Data Type & Formula Alignment]:** Discovered the AI was conflating `time_spent` with `AssetAmount`. I referenced the ER Model and formally defined the core formula: `Credited Asset = Time Spent × Conversion Multiplier`. Also challenged whether storing minutes as `int` risked overflow.
  - **[Critical Bug — Ledger Imbalance]:** During 1.5× multiplier testing, I noticed the console printed `3.00` for leisure assets, but the database `asset_accounts` only stored `2.00`. Independent code review revealed a refactoring oversight: the credit side was still using `request.getTimeSpent()` instead of the post-conversion `finalAssetAmount`. I personally fixed the logic to enforce the strict sequence: **Debit → Currency Conversion → Credit & Lock**.
  - **[Architecture Flow Verification]:** Mid-implementation, I described the full Controller → Service → Repository control flow to the AI to verify my understanding of MVC and Three-Tier Architecture was correct.
  - **[Final Checklist Audit]:** After initial completion, re-submitted the original spec checklist to verify every requirement had been fulfilled.

### Verification

- **Database Constraint Validation:** The system threw `Column 'time_spent' cannot be null` and `a foreign key constraint fails... constraint [fk_ledger_execution]`. This **confirmed** the schema-level safeguards were working correctly, blocking invalid dirty data writes.
- **Concurrency Defence Test:** Used Postman to intentionally submit a stale `version` value, verifying the system correctly rejected the transaction and triggered Rollback — proving ACID defence integrity.

---

## 📅 2026-03-15 — Registration & Account Opening Logic (ACID Transaction Test)

### Prompt Strategy

- **Initial Directive:** Implement "Module 0.0 Registration Logic" — create `RegisterRequest` DTO, implement BCrypt password hashing in `AuthService`, open `@Transactional`, write user to `Identities` table, and co-create initial `Asset_Accounts`.
- **Iterative Corrections:**
  - **[Mechanism Deep-Dive]:** Questioned the `@Transactional` annotation behaviour to confirm Spring Boot's automatic Rollback on unchecked exception is a built-in framework mechanism, not custom code.
  - **[Security Standard Enforcement]:** Before testing, identified that password hashing was not yet implemented. Halted feature sign-off and required BCrypt implementation to complete first. Also addressed `jackson-core` vulnerability warnings and `Field injection is not recommended` warnings with production-standard constructor injection solutions.

### Verification

- **API Endpoint Test:** Used Postman to `POST http://localhost:8080/api/auth/register`.
- **Log Trace & Concurrency Debugging:** Received `500 Internal Server Error` instead of `200 OK`. Log inspection revealed the root cause: `ObjectOptimisticLockingFailureException: Row was already updated or deleted by another transaction`. This **confirmed** the Optimistic Locking mechanism and `version` field were functioning as designed, while exposing a concurrent write conflict for subsequent investigation.

---

## 📅 2026-03-14 — Entity Classes & Repository Interface Setup

### Prompt Strategy

- **Initial Directive:** After database creation, requested AI to generate Spring Boot `Entity` and `Repository` interfaces for all 7 tables.
- **Iterative Corrections:**
  - **[Knowledge Consolidation First]:** Refused to copy-paste blindly. Required structured `What/Why/How` explanations of ORM concepts, the Repository Design Pattern, and `@Entity` / `@Table` annotations before accepting any code.
  - **[Architecture Standard Enforcement]:** Required strict adherence to a Three-Tier directory structure, converting all tables into ORM-compliant Java classes in a single pass for structural consistency.

### Verification

- **Compiler Static Check:** Pasted code into IntelliJ IDEA and resolved Package name path mismatches that caused red-line compilation errors.
- **Runtime Validation & Debugging:** Spring Boot threw `SchemaManagementException: missing column [proof_image_url]`. Independent diagnosis (without AI) revealed the SQL column had been renamed to `album_image_url` without syncing the Java Entity. After manual sync across both layers, the system reported `Tomcat started on port 8080`, successfully passing local Hibernate auto-schema validation.

---

## 📅 2026-03-08 — Core MySQL Database Setup (`schema.sql`)

### Prompt Strategy

- **Initial Directive:** Provided self-designed column definitions for all 7 core relational tables (Identities, Asset_Accounts, Time_Ledgers, etc.) and requested corresponding MySQL DDL output.
- **Iterative Corrections:**
  - **[Architecture Alignment]:** Identified that the AI omitted the database initialization block and that FK relations did not match the original ER Model design. Issued explicit corrections to enforce blueprint compliance.
  - **[Data Integrity Deep-Dive]:** Challenged the AI's default `ON DELETE CASCADE` on `Executions` and `Asset_Accounts`. Established that deleting activity records or accounts must **never** cascade to `Time_Ledgers` — financial audit trails are absolutely inviolable.
  - **[Final Architecture Decision]:** Personally decided to **abandon hard-delete in favour of Soft Delete** — setting `is_active = false` via the UI rather than deleting rows, preserving complete historical data integrity for all referenced ledger entries.

### Verification

- **Manual Logic Review:** Questioned every `VARCHAR` length decision, the rationale for `UUID (VARCHAR(36))`, and the `TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` default behaviour — refused to accept any definition without full understanding.
- **Execution Validation:** After confirming all FK constraints matched business logic, executed the full `schema.sql` in the database management tool and confirmed `0 errors` with all 7 tables and relations created successfully.

---

## 📅 2026-02-25 — System Prompt Setup & Development Boundaries

### Prompt Strategy

- **Role Injection:** Assigned the AI the role of "Senior Software Engineer & Professor" with deep emphasis on financial-grade systems.
- **Constraints Defined:** Explicitly required all architectural proposals to conform to Double-Entry Bookkeeping principles and ACID compliance standards from the outset.

### Verification

- **Architecture Boundary Audit:** Throughout all subsequent development sessions, every AI proposal was audited against the "Modular Monolith" constraint. Any suggestion of microservices decomposition or over-engineering was immediately rejected to maintain project deliverability within the academic timeline.
