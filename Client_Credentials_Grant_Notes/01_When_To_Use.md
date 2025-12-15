# 01 - When To Use Client Credentials Grant

> 📌 The right tool for the right job - when apps talk to apps!

---

## 🤔 The Key Question

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    WHICH GRANT TYPE DO I NEED?                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Ask yourself: "Is a HUMAN USER involved?"                                  │
│                                                                              │
│   ✅ YES, a user is logging in                                               │
│      → Use AUTHORIZATION CODE GRANT                                          │
│      → "Login with Google" for dheeraj@gmail.com                             │
│                                                                              │
│   ❌ NO, it's machine-to-machine                                             │
│      → Use CLIENT CREDENTIALS GRANT                                          │
│      → Service A calling Service B automatically                             │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Client Credentials Grant Scenarios

### ✅ PERFECT Use Cases

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    USE CLIENT CREDENTIALS WHEN...                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   1. MICROSERVICE TO MICROSERVICE                                            │
│      ─────────────────────────────                                           │
│      Order Service → Inventory Service                                       │
│      "Check if product is in stock"                                          │
│      No user involved, just services talking!                                │
│                                                                              │
│   2. SCHEDULED JOBS / CRON TASKS                                             │
│      ─────────────────────────────                                           │
│      Nightly backup job → Storage API                                        │
│      "Upload today's data"                                                   │
│      Runs at 2 AM, no user is awake!                                         │
│                                                                              │
│   3. BACKGROUND WORKERS                                                      │
│      ───────────────────                                                     │
│      Email Worker → Notification Service                                     │
│      "Send queued emails"                                                    │
│      Processing happens in background!                                       │
│                                                                              │
│   4. CLI TOOLS / SCRIPTS                                                     │
│      ────────────────────                                                    │
│      Admin Script → Management API                                           │
│      "Clean up old records"                                                  │
│      Script runs with app credentials, not user login!                       │
│                                                                              │
│   5. THIRD-PARTY INTEGRATIONS                                                │
│      ────────────────────────                                                │
│      Your CRM → Your API                                                     │
│      "Sync customer data"                                                    │
│      CRM app authenticates itself, not a user!                               │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### ❌ DO NOT Use Client Credentials When

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    DON'T USE CLIENT CREDENTIALS WHEN...                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ❌ User needs to login                                                     │
│      "Login with Google" → Use Authorization Code Grant!                     │
│                                                                              │
│   ❌ You need user's identity                                                │
│      "Show dheeraj's photos" → Needs user consent!                           │
│                                                                              │
│   ❌ You're accessing user-specific data                                     │
│      "Get user's Drive files" → User must authorize!                         │
│                                                                              │
│   ❌ ANYTHING involving end-user data                                        │
│      Client Credentials = App's own permissions                              │
│      NOT user's permissions!                                                 │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎨 Visual: Architecture Examples

### Example 1: E-Commerce Microservices

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    MICROSERVICES ARCHITECTURE                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   User → [Frontend] → [Order Service] ← Client Credentials → [Inventory]    │
│                              │                                               │
│                              │  Client Credentials                           │
│                              ▼                                               │
│                       [Payment Service]                                      │
│                              │                                               │
│                              │  Client Credentials                           │
│                              ▼                                               │
│                       [Notification Service]                                 │
│                                                                              │
│   ⭐ User logged in via Auth Code Grant at Frontend                         │
│   ⭐ Services talk to each other via Client Credentials Grant               │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Example 2: Cron Job

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    SCHEDULED JOB                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Every night at 2:00 AM:                                                    │
│                                                                              │
│   ┌──────────────┐    Client Credentials    ┌────────────────────┐          │
│   │  Backup Job  │ ─────────────────────► │  Cloud Storage API  │          │
│   │  (cron)      │                          │  (protected)        │          │
│   └──────────────┘                          └────────────────────┘          │
│                                                                              │
│   No user is involved!                                                       │
│   No login screen!                                                           │
│   Just machine-to-machine!                                                   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Decision Matrix

| Scenario | User Involved? | Grant Type |
|----------|----------------|------------|
| "Login with Google" button | ✅ Yes | Authorization Code |
| Order Service → Inventory API | ❌ No | Client Credentials |
| Show user's Google Photos | ✅ Yes | Authorization Code |
| Nightly data sync job | ❌ No | Client Credentials |
| User views their dashboard | ✅ Yes | Authorization Code |
| Microservice health check | ❌ No | Client Credentials |
| Third-party webhook | ❌ No | Client Credentials |

---

## 💡 Key Insight

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   ⭐ Client Credentials = "The APP authenticates ITSELF"                     │
│                                                                              │
│   In Authorization Code Grant:                                               │
│   → App acts on BEHALF OF USER                                               │
│   → Token represents: "dheeraj@gmail.com authorized this app"                │
│                                                                              │
│   In Client Credentials Grant:                                               │
│   → App acts as ITSELF                                                       │
│   → Token represents: "order-service is authorized"                          │
│   → No user identity at all!                                                 │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🤔 Beginner Check

1. Your cron job needs to call a protected API at 3 AM. Which grant type?
2. A user clicks "Login with GitHub". Which grant type?
3. Microservice A needs to check stock in Inventory Service. Which grant type?
4. What's the main question to ask when choosing grant type?

Answers:

1. Client Credentials (no user at 3 AM!)
2. Authorization Code (user is logging in)
3. Client Credentials (just services talking)
4. "Is a human USER involved?"

---

**Next:** [02_No_User_Involved.md](./02_No_User_Involved.md)
