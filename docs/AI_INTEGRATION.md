# AI Integration

> **Strategy:** Decoupled background Cron Job with graceful degradation fallback.  
> The LLM call is completely isolated from the core ACID transaction flow.

---

## Overview

The server aggregates weekly time ledger data as context and calls an external Large Language Model (LLM) API on a fixed schedule. To maintain development sustainability, this MVP adopts a "Poor Man's RAG" approach — Context Stuffing via static text injection rather than a full vector database.

---

## Why a Decoupled Background Job?

### Resilience Against LLM Outages

If the LLM API goes down or hits a rate limit, the system must not crash. The scheduler captures the exception and automatically falls back to a pre-loaded static letter, storing it in `AI_Weekly_Insights` with `fallback_used = true`. The user's weekly report is guaranteed regardless of external API availability.

### Async Processing — Core Transaction Independence

LLM calls are expensive in both latency and cost. Placing them in the settlement flow would freeze the UI. The background `@Scheduled` daemon thread ensures the core ACID transaction completes in milliseconds, completely decoupled from the AI generation pipeline.

---

## Implementation Spec

### 1. Trigger

```java
@Scheduled(cron = "0 0 20 * * SUN")
```

Executes automatically every Sunday at 20:00. Initiated by a Spring Boot background daemon thread.

### 2. Context Injection — The 3 Pillars

Before sending the LLM request, the backend performs in-memory aggregation from three sources:

| Pillar | Data Source | Content |
|---|---|---|
| **Financial Report** | `Time_Ledgers` | Calculates the actual focus-to-leisure asset ratio for the week |
| **Dynamic Baseline** | `Milestone_Nodes` + `System_Configs` | Retrieves the current milestone goal and user-configured WLB target ratio |
| **Inspirational Quote** | Pre-loaded `List<String>` | Randomly selects a motivational quote for injection |

### 3. LLM Request (Sample)

```json
{
  "model": "gpt-4o-mini",
  "messages": [
    {
      "role": "system",
      "content": "You are a warm and firm mentor. The user's current goal is 'Final Project'. Expected WLB: 80% focus / 20% leisure. Based on the following data and journal, provide emotionally resonant encouragement."
    },
    {
      "role": "user",
      "content": "Actual ratio: 85% focus, 15% leisure. Weekly journal: Spent the whole week debugging and felt exhausted, but watching a movie on Saturday helped me recover."
    }
  ]
}
```

### 4. Output — `AI_Weekly_Insights` Table

| Field | Sample Value |
|---|---|
| `insight_id` (PK) | `UUID-9999` |
| `year_week` | `2026-W11` |
| `ai_summary` (TEXT) | *(AI-generated letter content)* |
| `fallback_used` | `FALSE` |
| `created_at` | `2026-03-08 20:00:05` |

---

## Graceful Degradation (Fallback) Flow

```
Call LLM API
    ├── Success
    │       → Parse response JSON
    │       → INSERT into AI_Weekly_Insights  (fallback_used = FALSE)
    │
    └── Failure (Timeout / Rate Limit)
            → Log Error
            → Select random quote from pre-loaded static list
            → INSERT into AI_Weekly_Insights  (fallback_used = TRUE)
            → System remains fully operational
```
