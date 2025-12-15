# 08 - Step 5: Token Exchange (The Secure Part!)

> 📌 The MOST CRITICAL step - exchanging code for tokens in the BACK CHANNEL!

---

## 🔐 What Happens?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    STEP 5: TOKEN EXCHANGE                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ⭐ This happens SERVER-TO-SERVER!                                          │
│   ⭐ User's browser is NOT involved!                                         │
│   ⭐ This is the BACK CHANNEL!                                               │
│                                                                              │
│   Your Backend                                          Google               │
│       │                                                    │                 │
│       │                                                    │                 │
│       │  POST https://oauth2.googleapis.com/token          │                 │
│       │  {                                                 │                 │
│       │    grant_type: "authorization_code",               │                 │
│       │    code: "4/0AX4XfWh...",                          │                 │
│       │    client_id: "YOUR_CLIENT_ID",                    │                 │
│       │    client_secret: "YOUR_SECRET",  ← THE SECRET!    │                 │
│       │    redirect_uri: "http://localhost:8080/..."       │                 │
│       │  }                                                 │                 │
│       │────────────────────────────────────────────────────►                 │
│       │                                                    │                 │
│       │  Response:                                         │                 │
│       │  {                                                 │                 │
│       │    "access_token": "ya29.xxx...",                  │                 │
│       │    "id_token": "eyJhbGci...",  ← JWT!              │                 │
│       │    "expires_in": 3599,                             │                 │
│       │    "token_type": "Bearer"                          │                 │
│       │  }                                                 │                 │
│       │◄────────────────────────────────────────────────────                 │
│       │                                                    │                 │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 The Token Request

```http
POST https://oauth2.googleapis.com/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&code=4/0AX4XfWh8CnlM6GxBptYvQTy...
&client_id=YOUR_CLIENT_ID.apps.googleusercontent.com
&client_secret=YOUR_GOOGLE_CLIENT_SECRET
&redirect_uri=http://localhost:8080/login/oauth2/code/google
```

---

## 📊 Parameter Explanation

| Parameter | Value | Why Needed? |
|-----------|-------|-------------|
| `grant_type` | `authorization_code` | Tells Google which flow we're using |
| `code` | `4/0AX4XfWh...` | The code from Step 4 |
| `client_id` | `YOUR_CLIENT_ID` | Identifies your app |
| `client_secret` | `GOCSPX-xxx` | **PROVES** you're the real app! |
| `redirect_uri` | `http://...` | Must match Step 2 exactly! |

---

## 🔑 Why client_secret is CRITICAL

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    THE CLIENT_SECRET                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Scenario: Hacker intercepts the authorization code                         │
│   ──────────────────────────────────────────────────                         │
│                                                                              │
│   Hacker has:                                                                │
│   ✅ code (from URL interception)                                            │
│   ✅ client_id (public, in the URL)                                          │
│   ✅ redirect_uri (public, in the URL)                                       │
│   ❌ client_secret (NEVER left your server!)                                 │
│                                                                              │
│   Hacker tries to exchange code:                                             │
│   {                                                                          │
│     code: "4/0AX4XfWh...",                                                   │
│     client_id: "xxx",                                                        │
│     client_secret: ??? ← DOESN'T HAVE THIS!                                  │
│   }                                                                          │
│                                                                              │
│   Google: "Invalid client_secret! Request denied!" ❌                        │
│                                                                              │
│   ⭐ client_secret = The difference between authorized and unauthorized!     │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📬 The Token Response

```json
{
  "access_token": "ya29.a0AWY7Ckl5HqXjklasdf...",
  "expires_in": 3599,
  "scope": "openid email profile",
  "token_type": "Bearer",
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjEyMyJ9.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI4MTUxOTU4MzczNjQiLCJhdWQiOiI4MTUxOTU4MzczNjQiLCJzdWIiOiIxMTI0MTYwMzYzMzcwIiwiZW1haWwiOiJkaGVlcmFqQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhdF9oYXNoIjoiYWJjMTIzIiwibmFtZSI6IkRoZWVyYWoiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EveHh4IiwiZ2l2ZW5fbmFtZSI6IkRoZWVyYWoiLCJpYXQiOjE3MDIyODUzOTgsImV4cCI6MTcwMjI4ODk5OH0.signature"
}
```

---

## 📊 Token Comparison

| Token | Type | Purpose | Format |
|-------|------|---------|--------|
| `access_token` | Bearer | Call Google APIs | Opaque (`ya29.xxx`) |
| `id_token` | JWT | User identity | JWT (`eyJhbG...`) |

---

## 🧩 Decoding the id_token (JWT)

The id_token has 3 parts: `header.payload.signature`

```json
// HEADER
{
  "alg": "RS256",
  "typ": "JWT",
  "kid": "key-id-123"
}

// PAYLOAD (User Info!)
{
  "iss": "https://accounts.google.com",     // Issuer
  "sub": "112416036337094439562",           // User's unique ID
  "aud": "YOUR_CLIENT_ID",                  // Your app
  "email": "dheeraj@gmail.com",             // User's email
  "email_verified": true,
  "name": "Dheeraj",                        // User's name
  "picture": "https://lh3.googleusercontent.com/a/xxx",
  "given_name": "Dheeraj",
  "iat": 1702285398,                        // Issued at
  "exp": 1702288998                         // Expires at
}

// SIGNATURE
// Signed by Google's private key
// Verified using Google's public key (from jwks endpoint)
```

---

## 🔧 Spring Security Automatic Handling

```java
// Spring does all of this internally!

// 1. Build token request
// 2. Send POST to token endpoint
// 3. Receive tokens
// 4. Validate id_token signature
// 5. Extract user info from id_token
// 6. Create OidcUser object
// 7. Store in SecurityContext

// You get the user like this:
@GetMapping("/user")
public String getUser(@AuthenticationPrincipal OidcUser user) {
    return "Hello, " + user.getFullName();  // "Hello, Dheeraj"
}
```

---

## 🔐 Security: Back Channel is Key

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    WHY BACK CHANNEL IS SECURE                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   FRONT CHANNEL (Steps 1-4):                                                 │
│   ───────────────────────────                                                │
│   ❌ Goes through user's browser                                             │
│   ❌ URLs visible in history                                                 │
│   ❌ Extensions can intercept                                                │
│   ❌ Only public info (client_id, code)                                      │
│                                                                              │
│   BACK CHANNEL (Step 5):                                                     │
│   ──────────────────────                                                     │
│   ✅ Server-to-server HTTPS                                                  │
│   ✅ User's browser NOT involved                                             │
│   ✅ client_secret stays hidden                                              │
│   ✅ Tokens never touch the browser!                                         │
│                                                                              │
│   This is why Authorization Code Grant is more secure than Implicit Grant!   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🤔 Beginner Check

1. Who sends the token request? (Browser or Backend?)
2. What's the one thing hacker can't have even if they intercept everything in the browser?
3. What's the difference between access_token and id_token format?
4. Which token has the user's email inside it?

Answers:

1. BACKEND! (That's why it's secure)
2. `client_secret` (never leaves the server)
3. access_token is opaque (`ya29.xxx`), id_token is JWT (`eyJhbG...`)
4. id_token (JWT payload contains user info)

---

**Next:** [09_Step6_Using_Tokens.md](./09_Step6_Using_Tokens.md)
