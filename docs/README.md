# Project Chronicle — State Flow Diagrams

> A WLB (Work-Life Balance) double-entry ledger engine built on Java Spring Boot + MySQL.
> The following diagrams cover all four core state modules of the system.

---

## Module 1 · Onboarding (Authentication & Account Opening)

```mermaid
stateDiagram-v2
    direction TB
    [*] --> Auth_Gateway : user opens the application

    state Auth_Gateway {
        direction TB
        Select_Action --> Existing_User_Login : enter credentials
        Select_Action --> New_User_Registration : submit new credentials
        New_User_Registration --> Username_Conflict_Check
        Username_Conflict_Check --> Registration_Rejected : username already taken
        Username_Conflict_Check --> Password_Hashing : username available
        Password_Hashing --> Account_Opening_Transaction

        state "Account Opening Transaction (@Transactional)" as Account_Opening_Transaction {
            direction LR
            Write_Identities --> Provision_Asset_Accounts : provision time & recharge accounts
        }
    }

    Existing_User_Login --> WLB_Dashboard : auth success (issue JWT)
    Account_Opening_Transaction --> WLB_Dashboard : account opened successfully
```

---

## Module 2 · WLB Dashboard

```mermaid
stateDiagram-v2
    direction TB
    [*] --> WLB_Dashboard

    WLB_Dashboard --> Reading_AI_Weekly_Report : click "View AI Feedback"
    Reading_AI_Weekly_Report --> WLB_Dashboard : back to dashboard
    WLB_Dashboard --> History_Review : click "Review History"
    History_Review --> WLB_Dashboard : back to dashboard

    WLB_Dashboard --> Draft_Intercept_Check : click "Start Focus / Retroactive Entry"
    WLB_Dashboard --> Pending_Settlement : click "Resume Draft" (auto-loaded)
    WLB_Dashboard --> Logout_and_Disconnect : click "Logout"

    state Draft_Intercept_Check <<choice>>
    Draft_Intercept_Check --> Milestone_Selection : no draft / click "Discard Draft" (live check-in)
    Draft_Intercept_Check --> Retroactive_Entry : no draft / click "Discard Draft" (retroactive entry)
    Draft_Intercept_Check --> Pending_Settlement : draft exists → click "No, keep draft" (rescued)
```

---

## Module 3 · Asset Calculation & Settlement

```mermaid
stateDiagram-v2
    direction TB
    Milestone_Selection --> Active_Focus : start timer
    Active_Focus --> Pending_Settlement : stop timer, log reflection (image/text)
    Retroactive_Entry --> Pending_Settlement : manually input start/end time, log reflection

    Pending_Settlement --> Asset_Transfer_Transaction : click "Confirm & Settle"
    Pending_Settlement --> Logout_and_Disconnect : close browser (frontend auto-saves draft)
    Active_Focus --> Logout_and_Disconnect : close browser (frontend auto-saves timer)

    state "Asset Transfer Transaction (@Transactional)" as Asset_Transfer_Transaction {
        direction TB
        Begin_Transaction --> Optimistic_Lock_Check
        Optimistic_Lock_Check --> Cross_Account_Transfer : success (available time → target asset)
        Cross_Account_Transfer --> Write_Time_Ledger : audit trail (Time_Ledgers)
        Write_Time_Ledger --> Commit_Transaction : Commit
        Optimistic_Lock_Check --> Abort_Exception : failure (concurrent double-submit / insufficient balance)
    }

    Asset_Transfer_Transaction --> Settlement_Verdict : transaction committed
    Asset_Transfer_Transaction --> Pending_Settlement : transaction failed (Rollback)

    Settlement_Verdict --> Milestone_Achieved_State : Cond-A — milestone target reached
    Settlement_Verdict --> WLB_Dashboard : Cond-B — milestone in progress / leisure only

    state Milestone_Achieved_State {
        direction TB
        Show_Achievement_Screen --> Update_Milestone_Status : record achievement unlock
    }
    Milestone_Achieved_State --> WLB_Dashboard : settlement complete, return to home
```

---

## Module 4 · AI Weekly Report

```mermaid
stateDiagram-v2
    direction TB
    state "AI Weekly Report (Cron Job)" as AI_Cron_Job {
        direction TB
        Awaiting_Weekly_Trigger --> Aggregate_Weekly_Diary : every Sunday at 20h00
        Aggregate_Weekly_Diary --> Call_LLM_Generate_Letter : inject RAG context

        state Call_LLM_Generate_Letter <<fork>>
        Call_LLM_Generate_Letter --> Write_AI_Weekly_Insights : generation succeeded
        Call_LLM_Generate_Letter --> Log_Error : API outage or rate limit

        Log_Error --> Graceful_Degradation : trigger fault-tolerance mechanism
        Graceful_Degradation --> Write_AI_Weekly_Insights : persist static fallback letter
        Write_AI_Weekly_Insights --> Awaiting_Weekly_Trigger
    }
```
