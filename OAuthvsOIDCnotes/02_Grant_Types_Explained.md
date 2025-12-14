# OAuth 2.0 Grant Types Explained

> 📌 **Prerequisite**: Read [00_Glossary_and_Basics.md](./00_Glossary_and_Basics.md) first!

---

## 🎯 What is a Grant Type?

```
Grant Type = HOW you request tokens from the Authorization Server

Think of it like: Different ways to get entry pass at a venue!
─────────────────────────────────────────────────────────────────
• Show ID at gate = Authorization Code (normal way)
• Company badge = Client Credentials (employee/machine)
• VIP list = Password Grant (deprecated - too risky!)

Different scenarios need different ways to get tokens!
```

---

## 📊 The 4 Main Grant Types

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         OAuth 2.0 GRANT TYPES                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1️⃣ AUTHORIZATION CODE GRANT ⭐ (Most Common, Most Secure!)                 │
│                                                                              │
│  2️⃣ CLIENT CREDENTIALS GRANT ⭐ (Machine-to-Machine)                        │
│                                                                              │
│  3️⃣ IMPLICIT GRANT ❌ (DEPRECATED - Don't use!)                             │
│                                                                              │
│  4️⃣ PASSWORD GRANT ❌ (DEPRECATED - Don't use!)                             │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 1️⃣ Authorization Code Grant (What We Used!)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    AUTHORIZATION CODE GRANT                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   When: User wants to login (or authorize access)                            │
│   Who: Web apps, Mobile apps                                                 │
│   Security: ⭐⭐⭐ BEST!                                                     │
│                                                                              │
│   Flow:                                                                      │
│   ─────                                                                      │
│   1. User clicks "Login with Google"                                         │
│   2. Redirect to Google login page                                           │
│   3. User logs in & consents                                                 │
│   4. Google redirects back with AUTHORIZATION CODE                           │
│   5. Backend exchanges code for tokens                                       │
│   6. Get: access_token + id_token (if OIDC)                                  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Token Request (Step 5)

```http
POST https://oauth2.googleapis.com/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code        ← THIS DEFINES THE GRANT TYPE!
&code=4/0AXxxxxxxxxxxxxxxxxxxxxx     ← Authorization code from step 4
&client_id=your-client-id
&client_secret=your-secret
&redirect_uri=http://localhost:8080/login/oauth2/code/google
```

### Response

```json
{
  "access_token": "ya29.xxx",
  "expires_in": 3599,
  "scope": "openid email profile",
  "token_type": "Bearer",
  "id_token": "eyJhbG..."
}
```

---

## 2️⃣ Client Credentials Grant (Machine-to-Machine)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    CLIENT CREDENTIALS GRANT                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   When: Service calls another service (NO USER!)                             │
│   Who: Backend microservices, Cron jobs, Lambdas                             │
│   Security: ⭐⭐ Good for server-side                                        │
│                                                                              │
│   Key Point: NO USER INVOLVED! App authenticates ITSELF!                     │
│                                                                              │
│   Flow:                                                                      │
│   ─────                                                                      │
│   1. Service A needs to call Service B                                       │
│   2. Service A sends its clientId + clientSecret to Auth Server              │
│   3. Auth Server gives access_token                                          │
│   4. Service A uses token to call Service B                                  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Token Request

```http
POST https://auth-server.com/oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials     ← DIFFERENT GRANT TYPE!
&client_id=order-service
&client_secret=service-secret
&scope=payment-api
```

### Response

```json
{
  "access_token": "eyJhbG...",
  "expires_in": 3600,
  "token_type": "Bearer"
}
```

⚠️ **Notice: NO id_token!** (No user = no user identity!)

---

## 📊 Authorization Code vs Client Credentials

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         COMPARISON                                           │
├───────────────────────────────┬─────────────────────────────────────────────┤
│   Authorization Code          │   Client Credentials                        │
├───────────────────────────────┼─────────────────────────────────────────────┤
│   User login involved ✅      │   No user ❌                                │
│   Needs authorization code ✅ │   No code needed ❌                         │
│   Needs redirect_uri ✅       │   No redirect ❌                            │
│   Gets id_token (OIDC) ✅     │   No id_token ❌                            │
│   For: User-facing apps       │   For: Server-to-server                     │
└───────────────────────────────┴─────────────────────────────────────────────┘
```

---

## 3️⃣ Implicit Grant ❌ (DEPRECATED!)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    IMPLICIT GRANT (DON'T USE!)                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Old Way: Token returned directly in URL                                    │
│                                                                              │
│   Problem:                                                                   │
│   ─────────                                                                  │
│   Redirect URL: http://app.com/callback#access_token=ya29.xxx                │
│                                        ↑                                     │
│                                        Token visible in URL! 😱              │
│                                        Browser history, logs, etc.           │
│                                                                              │
│   Replaced by: Authorization Code + PKCE                                     │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 4️⃣ Password Grant ❌ (DEPRECATED!)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    PASSWORD GRANT (DON'T USE!)                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Old Way: User gives username/password to YOUR app                          │
│                                                                              │
│   Request:                                                                   │
│   ─────────                                                                  │
│   grant_type=password                                                        │
│   &username=user@gmail.com                                                   │
│   &password=secret123     ← YOUR APP SEES THE PASSWORD! 😱                   │
│                                                                              │
│   Problem: App has access to user's credentials!                             │
│   Only for: Legacy apps that can't be updated                                │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Which Grant Type for Which Scenario?

| Scenario | Grant Type |
|----------|------------|
| "Login with Google" (web/mobile) | **Authorization Code** |
| SPA (React, Angular) | **Authorization Code + PKCE** |
| Backend A → Backend B | **Client Credentials** |
| Cron job accessing API | **Client Credentials** |
| AWS Lambda calling DynamoDB | **Client Credentials** |
| Old legacy system | Password (if no other option) |

---

## 🔐 PKCE (Proof Key for Code Exchange)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    PKCE - For SPAs & Mobile Apps                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Problem: SPAs can't keep client_secret safe (JavaScript is readable!)     │
│                                                                              │
│   Solution: PKCE - One-time secret generated per request!                    │
│                                                                              │
│   Flow:                                                                      │
│   ─────                                                                      │
│   1. SPA generates random code_verifier                                      │
│   2. Creates code_challenge = SHA256(code_verifier)                          │
│   3. Send code_challenge to authorization URL                                │
│   4. After login, exchange code + code_verifier for token                    │
│   5. Auth server verifies: SHA256(code_verifier) == code_challenge           │
│                                                                              │
│   Why secure?                                                                │
│   • Even if hacker intercepts code_challenge, can't reverse SHA256!          │
│   • code_verifier is never transmitted until token exchange!                 │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### PKCE Flow Diagram

```
SPA (React App)                              Google
      │                                         │
      │ 1. Generate:                            │
      │    code_verifier = "abc123random..."   │
      │    code_challenge = SHA256(above)       │
      │                                         │
      │ 2. Redirect with code_challenge ───────▶│
      │    /authorize?code_challenge=xxx        │
      │                                         │
      │ 3. User logs in                         │
      │                                         │
      │◀──────────────────────────────────────  │
      │ 4. Authorization code returned          │
      │                                         │
      │ 5. Exchange code + code_verifier ─────▶│
      │    POST /token                          │
      │    code=xxx                             │
      │    code_verifier=abc123random...        │
      │                                         │
      │                     Google checks:      │
      │                     SHA256(code_verifier) == code_challenge? ✅
      │                                         │
      │◀──────────────────────────────────────  │
      │ 6. Tokens returned!                     │
```

---

## ⚠️ Important Clarification: What's NOT a Grant Type

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   Backend App → Resource Server (passing JWT)                                │
│                                                                              │
│   Is this Client Credentials? ❌ NO!                                         │
│                                                                              │
│   This is just "Bearer Token Authentication"                                 │
│   You're USING a token, not GETTING a new one!                               │
│                                                                              │
│   Grant Types = HOW you GET tokens                                           │
│   Bearer Auth = HOW you USE tokens                                           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📌 What We Did

```
Grant Type: Authorization Code
Protocol: OIDC (openid scope)
Tokens: access_token + id_token

POST https://oauth2.googleapis.com/token
grant_type=authorization_code  ← We used this!
```
