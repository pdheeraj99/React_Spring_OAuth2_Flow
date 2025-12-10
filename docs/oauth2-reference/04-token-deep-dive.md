# Token Deep Dive

> 🎓 **Access Token vs ID Token (JWT)**

---

## 🎫 Two Tokens - Different Purposes

Google returns TWO tokens after successful authentication:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          GOOGLE TOKEN RESPONSE                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1️⃣ ACCESS TOKEN                    2️⃣ ID TOKEN                            │
│  ─────────────────                   ────────────                            │
│  Format: Opaque String              Format: JWT                              │
│  Value: ya29.a0ARW5m7...           Value: eyJhbGciOiJSUzI1NiIs...           │
│                                                                              │
│  Purpose: Access Google APIs        Purpose: Identify User                  │
│  Use: Gmail, Drive, etc.           Use: Our Resource Server                 │
│                                                                              │
│  ❌ NOT for our use                 ✅ THIS is what we use!                  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Comparison Table

| Feature | Access Token | ID Token |
|---------|--------------|----------|
| **Format** | Opaque (random string) | JWT (structured) |
| **Readable** | ❌ No | ✅ Yes (decode with Base64) |
| **Contains user info** | ❌ No | ✅ Yes (sub, name, email) |
| **Issued by** | Google | Google |
| **For whom** | Google APIs | Our application |
| **We use for** | Nothing (in our BFF) | Resource Server authentication |
| **Verify signature** | Only Google can | Anyone with public key |

---

## 1️⃣ Access Token (Opaque)

### What it looks like:
```
ya29.a0ARW5m7gJsZPtZ8kN2X5qV9rW3hJ7mK1pL4nO2iU6yT8wR...
```

### Properties:
- Random string, no structure
- Only Google understands it
- Used to call Google APIs (Gmail, Drive, etc.)
- Expires in ~1 hour (3599 seconds)

### We DON'T use this for:
- Our Resource Server (it can't validate this)
- User identification (no user info inside)

---

## 2️⃣ ID Token (JWT)

### What it looks like:
```
eyJhbGciOiJSUzI1NiIsImtpZCI6ImQ1NDNlMjFhMDI3M2VmYzNmZDUzZ...
```

### JWT Structure:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              JWT TOKEN                                       │
│                        (THREE parts, dot-separated)                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  eyJhbGciOi...  .  eyJzdWIiOi...  .  rW3hJ7mK1pL4nO2...                     │
│  ─────────────     ─────────────     ─────────────────                      │
│     HEADER            PAYLOAD            SIGNATURE                           │
│                                                                              │
│  (Base64)           (Base64)          (Encrypted)                           │
│  Decode ↓           Decode ↓          Verify with                           │
│  {                  {                 Google's                              │
│   "alg":"RS256",     "sub":"11241..",  public key                           │
│   "kid":"d543.."     "name":"Dheeraj",                                      │
│  }                   "email":"..."                                          │
│                     }                                                        │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔍 ID Token Decoded (From our actual logs)

### Header:
```json
{
    "alg": "RS256",
    "kid": "d543e21a0273efc3fd53z...",
    "typ": "JWT"
}
```

| Field | Value | Meaning |
|-------|-------|---------|
| `alg` | RS256 | Algorithm used to sign |
| `kid` | d543e21a... | Key ID (which key to use for verification) |
| `typ` | JWT | Token type |

### Payload (Claims):
```json
{
    "iss": "https://accounts.google.com",
    "azp": "450472639030-g4g6r5terpsr6i9eo5bfhmfedcf33387.apps.googleusercontent.com",
    "aud": "450472639030-g4g6r5terpsr6i9eo5bfhmfedcf33387.apps.googleusercontent.com",
    "sub": "112416036337094439562",
    "email": "dheerajp0299@gmail.com",
    "email_verified": true,
    "at_hash": "4aao0hkss3WWo5qauK8wow",
    "nonce": "XMwoJXG0prWX90z0lh0Az4JaGEqgzWZuwmBr2U5trHA",
    "name": "Dheeraj",
    "picture": "https://lh3.googleusercontent.com/a/ACg8ocKECHu82YluGxpKzLkqc2kflU7nowt9w4FiOnpeuhTc1xvhHw=s96-c",
    "given_name": "Dheeraj",
    "iat": 1733823205,
    "exp": 1733826805
}
```

| Claim | Value | Meaning |
|-------|-------|---------|
| `iss` | accounts.google.com | Issuer (who created this token) |
| `sub` | 112416036... | Subject (unique user ID) |
| `aud` | 450472639... | Audience (intended recipient - our app) |
| `email` | dheerajp0299@gmail.com | User's email |
| `name` | Dheeraj | User's full name |
| `picture` | https://lh3... | Profile picture URL |
| `iat` | 1733823205 | Issued At (timestamp) |
| `exp` | 1733826805 | Expiration (timestamp) |
| `nonce` | XMwoJXG0... | Replay attack prevention |

### Signature:
```
rW3hJ7mK1pL4nO2iU6yT8wR...
```
- Encrypted hash of Header + Payload
- Created using Google's **private key**
- Verified using Google's **public key** (JWKS)

---

## 🔐 How Resource Server Validates JWT

### Process:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         JWT VALIDATION FLOW                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1️⃣ Receive JWT in Authorization header:                                   │
│     Authorization: Bearer eyJhbGciOiJSUzI1NiIs...                          │
│                                                                              │
│  2️⃣ Extract header to get 'kid' (Key ID):                                  │
│     kid = "d543e21a0273efc..."                                              │
│                                                                              │
│  3️⃣ Fetch Google's public keys (JWKS):                                     │
│     GET https://www.googleapis.com/oauth2/v3/certs                         │
│                                                                              │
│  4️⃣ Find the matching key by 'kid':                                        │
│     publicKey = jwks.keys.find(k => k.kid === "d543e21a...")               │
│                                                                              │
│  5️⃣ Verify signature:                                                       │
│     verify(header + "." + payload, signature, publicKey)                   │
│     ✅ If valid → Token is authentic!                                       │
│     ❌ If invalid → Reject! Token tampered!                                 │
│                                                                              │
│  6️⃣ Check claims:                                                           │
│     - exp > now?  (Not expired)                                            │
│     - iss == "https://accounts.google.com"? (Correct issuer)              │
│     - aud == our-client-id? (Token for us)                                 │
│                                                                              │
│  7️⃣ Extract user info from payload:                                        │
│     sub = "112416036337094439562"                                          │
│     email = "dheerajp0299@gmail.com"                                       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Our BFF Flow with Tokens

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│  Browser ──────────────────────────────────────────────► Client Backend     │
│           GET /api/photos                                                   │
│           Cookie: JSESSIONID=440421F8...                                    │
│                                                                              │
│           ❌ NO TOKENS passed here!                                          │
│           ✅ Only Session ID (cookie)                                        │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  Client Backend                                                              │
│  ─────────────────                                                           │
│                                                                              │
│  1. Get user from session: @AuthenticationPrincipal OidcUser user           │
│  2. Extract ID Token: user.getIdToken().getTokenValue()                     │
│  3. Call Resource Server with JWT                                           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│  Client Backend ────────────────────────────────────► Resource Server       │
│                   GET /photos                                               │
│                   Authorization: Bearer eyJhbGciOiJSUzI1NiIs...            │
│                                                                              │
│           ✅ JWT (ID Token) passed here!                                    │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

From logs:
```
🔑 CALLING RESOURCE SERVER with ID Token (JWT)...
Token (first 50 chars): eyJhbGciOiJSUzI1NiIsImtpZCI6ImQ1NDNlMjFhMDI3M2VmYz...

2025-12-10T15:41:57.166+05:30 DEBUG --- RestTemplate: HTTP GET http://localhost:8081/photos
```

---

## 📋 Token Expiration

| Token | Lifespan | After Expiry |
|-------|----------|--------------|
| Access Token | ~1 hour | Need new token (refresh or re-login) |
| ID Token | ~1 hour | Need new token (refresh or re-login) |
| Refresh Token | Long (30 days+) | Used to get new access/ID tokens |

> ⚠️ **Note:** Our current setup doesn't request refresh token. Add `access_type=offline` to get one.

---

## 📋 Summary

| Token | Format | Contains | Use For |
|-------|--------|----------|---------|
| Access Token | Opaque | Nothing readable | Google APIs (not our use) |
| ID Token | JWT | User identity claims | Our Resource Server |

---

> 📖 **Next:** [05-session-storage.md](./05-session-storage.md) - SecurityContext and AuthorizedClient Details
