# Architecture Design

> **Pattern:** Three-Tier Architecture (MVC) with Repository Pattern.  
> The transfer mechanism is used here as a representative example — the same pattern governs registration, AI scheduling, and all other modules.

---

## Project Directory Structure

```
wlb-ledger-engine/
└── src/main/java/com/chronicle/wlb/
    │
    ├── WlbLedgerEngineApplication.java       (Spring Boot entry point)
    │
    ├── dto/                                   (Data Transfer Objects — external request/response contracts)
    │    └── CheckoutRequest.java              (Ledger request: accountId, timeSpent, optimistic-lock version)
    │
    ├── controller/                            (Presentation Layer — HTTP interface)
    │   └── LedgerController.java             (Receives POST /api/ledger — double-entry bookkeeping requests)
    │
    ├── service/                              (Business Logic Layer)
    │   └── LedgerService.java                (Optimistic lock verification, double-entry execution, audit trail write)
    │
    └── repository/                           (Data Access Layer — communicates with MySQL via Hibernate ORM)
        ├── IdentityRepository.java           (Looks up accounts by username)
        ├── AssetAccountRepository.java       (Manages account balances)
        ├── TimeLedgerRepository.java         (Writes immutable historical records)
        └── ... (4 additional repositories)
```

---

## Transaction Flow — Step by Step

### Step 1 — Pack the Request (Frontend → DTO)

The frontend packages the time spent, task ID, and optimistic-lock version into a JSON payload. Spring Boot automatically deserializes it into the `CheckoutRequest` DTO.

> During Phase 1 development, IntelliJ's HTTP Client acts as the frontend substitute.

### Step 2 — Hit the Controller (`LedgerController`)

The `@PostMapping("/checkout")` endpoint intercepts the request and passes the `CheckoutRequest` to the Service layer.

### Step 3 — Business Logic Execution (`LedgerService` + `@Transactional`)

The service opens a `@Transactional` boundary, then:

1. Reads the submitted `clientVersion`.
2. Delegates to `AssetAccountRepository` to fetch both the source and destination accounts.
3. Performs **Optimistic Lock verification** — compares `clientVersion` against the stored `version`.
4. On match: deducts available time from the source account, credits the calculated asset amount to the target account.
5. Writes a new `TimeLedger` audit record.
6. Commits. On any exception: automatic Rollback.

### Step 4 — Persist to Database (`Repository` → MySQL)

The repositories (`accountRepo`, `timeLedgerRepo`) execute `findById` and `save` operations against MySQL, mediated through Hibernate ORM.

---

## Core Formula

```
Credited Asset = Time Spent × Activity Conversion Multiplier
```

---

## Key Defence Mechanisms

| Mechanism | Implementation | Purpose |
|---|---|---|
| **Optimistic Locking** | `version` field on `Asset_Accounts` + client version comparison | Prevents concurrent double-submit and race conditions |
| **Atomic Transaction** | `@Transactional` on `LedgerService` | Guarantees all-or-nothing: debit, credit, and audit trail succeed together or roll back entirely |
| **Soft Delete** | `is_active` flag on `Executions` | Preserves historical ledger integrity; referenced records are never hard-deleted |
| **Immutable Audit Trail** | No UPDATE/DELETE on `Time_Ledgers` | Financial compliance — every transaction is permanently recorded |
