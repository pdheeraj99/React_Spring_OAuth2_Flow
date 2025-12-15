# 03 - The Flow: Just 2 Steps!

> 📌 The simplest OAuth flow - because there's no user to redirect!

---

## 🎯 The Entire Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    CLIENT CREDENTIALS FLOW - JUST 2 STEPS!                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   STEP 1: Request Token                                                      │
│   ─────────────────────                                                      │
│                                                                              │
│   Your App                                 Auth Server                       │
│       │                                        │                             │
│       │  POST /oauth/token                     │                             │
│       │  {                                     │                             │
│       │    grant_type: "client_credentials",   │                             │
│       │    client_id: "order-service",         │                             │
│       │    client_secret: "super-secret",      │                             │
│       │    scope: "read:inventory"             │                             │
│       │  }                                     │                             │
│       │───────────────────────────────────────►│                             │
│       │                                        │                             │
│                                                                              │
│   STEP 2: Receive Token                                                      │
│   ─────────────────────                                                      │
│                                                                              │
│       │  Response:                             │                             │
│       │  {                                     │                             │
│       │    "access_token": "eyJhbGci...",      │                             │
│       │    "token_type": "Bearer",             │                             │
│       │    "expires_in": 3600                  │                             │
│       │  }                                     │                             │
│       │◄───────────────────────────────────────│                             │
│       │                                        │                             │
│                                                                              │
│   DONE! Now use the token to call APIs!                                      │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Compare: Auth Code (6 steps) vs Client Credentials (2 steps)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   AUTHORIZATION CODE GRANT (6 Steps):                                        │
│   ───────────────────────────────────                                        │
│   Step 1: User clicks "Login with Google"                                    │
│   Step 2: App redirects to Google                                            │
│   Step 3: User authenticates at Google                                       │
│   Step 4: Google redirects back with code                                    │
│   Step 5: App exchanges code for tokens                                      │
│   Step 6: App uses tokens                                                    │
│                                                                              │
│   All those steps because USER must approve!                                 │
│                                                                              │
│   ──────────────────────────────────────────────────────────────────────────│
│                                                                              │
│   CLIENT CREDENTIALS GRANT (2 Steps):                                        │
│   ────────────────────────────────────                                       │
│   Step 1: App sends credentials → Auth Server                                │
│   Step 2: Auth Server sends back token                                       │
│                                                                              │
│   Just 2 steps because NO USER to approve!                                   │
│   App already has its own credentials!                                       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎨 Visual Flow Diagram

```
    YOUR SERVICE                    AUTH SERVER                   TARGET API
   (order-service)                 (Keycloak/Auth0)              (inventory)
         │                              │                              │
         │                              │                              │
         │  1. POST /token              │                              │
         │     client_id + secret       │                              │
         │─────────────────────────────►│                              │
         │                              │                              │
         │  2. access_token             │                              │
         │◄─────────────────────────────│                              │
         │                              │                              │
         │                              │                              │
         │  3. GET /api/stock           │                              │
         │     Authorization: Bearer xxx│                              │
         │─────────────────────────────────────────────────────────────►
         │                              │                              │
         │  4. Stock data               │                              │
         │◄─────────────────────────────────────────────────────────────
         │                              │                              │
```

---

## 📋 Step 1: Token Request Details

```http
POST https://auth-server.com/oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id=order-service
&client_secret=super-secret-123
&scope=read:inventory write:orders
```

| Parameter | Value | Description |
|-----------|-------|-------------|
| `grant_type` | `client_credentials` | Tells auth server which flow |
| `client_id` | `order-service` | Your app's identifier |
| `client_secret` | `super-secret-123` | Proves you're the real app |
| `scope` | `read:inventory` | What permissions you need |

---

## 📋 Step 2: Token Response Details

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "read:inventory write:orders"
}
```

| Field | Description |
|-------|-------------|
| `access_token` | The token to use for API calls! |
| `token_type` | Always "Bearer" (use in Authorization header) |
| `expires_in` | Token valid for 3600 seconds (1 hour) |
| `scope` | Permissions granted |

⚠️ **Notice:** No `id_token`! There's no user identity!

---

## 🔧 Using the Token

```http
GET https://inventory-service.com/api/stock/product-123
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   The inventory-service:                                                     │
│   1. Receives the request                                                    │
│   2. Extracts token from Authorization header                                │
│   3. Validates token (signature, expiry, issuer)                             │
│   4. Checks scope: "Does this token have read:inventory?"                    │
│   5. If valid: Returns stock data                                            │
│   5. If invalid: Returns 401 Unauthorized                                    │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 💡 Why So Simple?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    WHY CLIENT CREDENTIALS IS SIMPLER                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Authorization Code Grant needs complexity because:                         │
│   • User must be redirected to auth server                                   │
│   • User must login and consent                                              │
│   • Auth server must redirect back                                           │
│   • Code must be exchanged securely                                          │
│   • User's browser is involved (front channel = risky!)                      │
│                                                                              │
│   Client Credentials is simple because:                                      │
│   • No user to redirect                                                      │
│   • No consent to obtain                                                     │
│   • No browser involved                                                      │
│   • Direct server-to-server (back channel = secure!)                         │
│   • App already has its credentials                                          │
│                                                                              │
│   ⭐ No user = No complexity!                                                │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Token Refresh Strategy

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    HOW TO HANDLE TOKEN EXPIRY                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Client Credentials tokens typically DON'T have refresh tokens!             │
│                                                                              │
│   Why not?                                                                   │
│   • You already have client_id + client_secret                               │
│   • Just request a new token when the old one expires!                       │
│   • No need for refresh token mechanism                                      │
│                                                                              │
│   Strategy:                                                                  │
│   ─────────                                                                  │
│   1. Get token                                                               │
│   2. Cache token (e.g., for 55 minutes if expires_in = 3600)                 │
│   3. When token expires: Just request a new one!                             │
│   4. No refresh token dance needed!                                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🤔 Beginner Check

1. How many steps are in Client Credentials flow?
2. What 4 parameters are sent in the token request?
3. Is there an id_token in the response?
4. Why is there no refresh_token needed?

Answers:
1. Just 2 steps!
2. grant_type, client_id, client_secret, scope
3. NO! There's no user identity
4. You can just request a new token with client_id + client_secret

---

**Next:** [04_The_Token_Request.md](./04_The_Token_Request.md)
