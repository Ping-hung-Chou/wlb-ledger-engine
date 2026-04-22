# API Reference

> **Architecture Pattern:** Single Facade API (external) · Dual-Service Decoupling (internal).

---

## Endpoint

**`POST /api/wlb/execution`**

**Responsibility:** Receives a single request from the frontend and, within a single `@Transactional` boundary, sequentially delegates to `AssetService` and `MilestoneService`.

### Request Body

```json
{
  "identityId": "UUID-1234",
  "executionId": "ACT_COMIC",
  "timeSpent": 120,
  "reflectionMemo": "This week was exhausting debugging, but drawing felt great!",
  "albumImageUrl": "/images/comic_01.png",
  "clientVersion": 3
}
```

---

## Internal Dual-Module Decoupling

Although the frontend calls a single Web API, the backend is strictly split into two independent Service classes that do not interfere with each other.

### Module A — Asset Clearing (`AssetService.java`)

- **Method:** `executeDoubleEntry(identityId, executionId, timeSpent, clientVersion)`
- **Responsibility:**
  1. Query the `available-time` account balance.
  2. Retrieve the `Executions` asset conversion multiplier.
  3. Perform Optimistic Lock verification against the `clientVersion`.
  4. Execute double-entry bookkeeping: debit the source account, credit the target account.
  5. Write an immutable audit record to `Time_Ledgers`.
- **On Failure:** Throws `PaymentException` (insufficient balance or lock conflict), triggering automatic Rollback.

### Module B — Milestone Progress (`MilestoneService.java`)

- **Method:** `updateMilestoneProgress(identityId, executionId)`
- **Responsibility:** Triggered only after Module A commits successfully. Checks `Milestone_Nodes` to determine if the current session meets the milestone target and updates its lifecycle status.

---

## Response Specification

### ✅ 200 OK — Success

```json
{
  "success": true,
  "fromAccountBalance": 480.00,
  "toAccountBalance": 180.00,
  "newVersion": 4,
  "milestoneUpdated": true,
  "message": "Settlement complete. 120 minutes invested, 180 asset points credited."
}
```

### ❌ 409 Conflict / 402 Payment Required — Failure

```json
{
  "success": false,
  "errorCode": "OPTIMISTIC_LOCK_CONFLICT",
  "message": "System state has been updated. Please reload and try again."
}
```

---

## Defence Principles

| Layer | Strategy |
|---|---|
| **External (Network)** | Single API endpoint locks the transaction boundary, preventing race conditions and unauthorised injection. |
| **Internal (Application)** | Dual-Service Decoupling enforces strict separation of financial clearing and milestone management. |
