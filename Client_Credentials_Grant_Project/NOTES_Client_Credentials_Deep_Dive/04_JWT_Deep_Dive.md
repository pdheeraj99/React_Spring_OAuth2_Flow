# 🎟️ 04. JWT Deep Dive - Token Anatomy

## 🤔 JWT enti?

**JWT = JSON Web Token**

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  JWT = A self-contained token that carries information             ║
║                                                                    ║
║  Key Features:                                                     ║
║  • Information token INSIDE undi (no database lookup needed!)      ║
║  • Digitally signed (tamper-proof!)                                ║
║  • Base64 encoded (URL/Header safe)                                ║
║  • Stateless - server state store avasaram ledu                    ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 📦 JWT Structure: 3 Parts

```
eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJvcmRlci1zZXJ2aWNlIn0.SflKxwRJSMeKKF2QT
└────────┬───────────┘└──────────────┬─────────────────┘└─────────┬──────────┘
       HEADER                     PAYLOAD                     SIGNATURE
```

**Remember:** 3 parts, 2 dots!

---

## 1️⃣ HEADER

### Raw JSON

```json
{
  "alg": "RS256",
  "typ": "JWT"
}
```

### Explanation

| Field | Value | Meaning |
|-------|-------|---------|
| `alg` | "RS256" | RSA with SHA-256 algorithm (for signing) |
| `typ` | "JWT" | Token type is JWT |

### Base64 Encoded

```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9
```

---

## 2️⃣ PAYLOAD (The Actual Data!)

### Raw JSON

```json
{
  "sub": "order-service",
  "scope": "read:inventory",
  "iss": "http://localhost:9000",
  "aud": "inventory-service",
  "exp": 1702677059,
  "iat": 1702673459
}
```

### Explanation

| Field | Full Name | Example | Meaning |
|-------|-----------|---------|---------|
| `sub` | Subject | "order-service" | WHO is this token for |
| `scope` | Scope | "read:inventory" | WHAT permissions |
| `iss` | Issuer | "<http://localhost:9000>" | WHO issued this token |
| `aud` | Audience | "inventory-service" | WHO should accept this |
| `exp` | Expiration | 1702677059 | WHEN it expires (Unix timestamp) |
| `iat` | Issued At | 1702673459 | WHEN it was created |

### Base64 Encoded

```
eyJzdWIiOiJvcmRlci1zZXJ2aWNlIiwic2NvcGUiOiJyZWFkOmludmVudG9yeSIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6OTAwMCJ9
```

---

## 3️⃣ SIGNATURE

### How Signature is Created

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  STEP 1: Combine Header + Payload                                  ║
║  ─────────────────────────────────                                 ║
║  encodedHeader + "." + encodedPayload                              ║
║  = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJvcmRlci1zZXJ2aWNlIn0"          ║
║                                                                    ║
║  STEP 2: Apply RSA-SHA256 Signing                                  ║
║  ────────────────────────────────                                  ║
║  RSASHA256(                                                        ║
║    base64UrlEncode(header) + "." + base64UrlEncode(payload),       ║
║    PRIVATE_KEY    ← Only Auth Server has this!                     ║
║  )                                                                 ║
║                                                                    ║
║  STEP 3: Result = Signature                                        ║
║  ─────────────────────────────                                     ║
║  = "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"                   ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 🔗 Complete JWT

```
HEADER.PAYLOAD.SIGNATURE

eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJvcmRlci1zZXJ2aWNlIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV
```

---

## 📖 What is Base64 Encoding?

### Problem

```
JSON: {"name": "Ram"}

These characters cause problems:
- { } : " (special characters)
- Breaks URLs
- Breaks HTTP Headers
- Causes parsing issues
```

### Solution - Base64

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  Base64 = Convert any text to SAFE characters only                 ║
║                                                                    ║
║  Allowed characters: A-Z, a-z, 0-9, +, /, =                        ║
║  (These work everywhere - URLs, Headers, etc.)                     ║
║                                                                    ║
║  Example:                                                          ║
║  ─────────                                                         ║
║  Original: {"alg":"RS256"}                                         ║
║  Base64:   eyJhbGciOiJSUzI1NiJ9                                     ║
║                                                                    ║
║  ⚠️ IMPORTANT:                                                      ║
║  Base64 is ENCODING, not ENCRYPTION!                               ║
║  Anyone can DECODE it back to original!                            ║
║  It's just a FORMAT change, not HIDING!                            ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

### Real-World Analogy

```
Base64 = Writing Telugu in Roman script

Original: నమస్తే
Encoded:  Namaste

Same content, different FORMAT!
Anyone who knows Telugu can read "Namaste" as "నమస్తే"

Similarly:
Original: {"sub":"order-service"}
Encoded:  eyJzdWIiOiJvcmRlci1zZXJ2aWNlIn0
Anyone can decode it back!
```

---

## 🔐 Can Anyone Read JWT Data?

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  ✅ YES! JWT payload is READABLE by anyone!                         ║
║                                                                    ║
║  JWT is NOT encrypted!                                             ║
║  It's just signed (tamper-proof, not secret!)                      ║
║                                                                    ║
║  HEADER:    Anyone can decode and read ✅                           ║
║  PAYLOAD:   Anyone can decode and read ✅                           ║
║  SIGNATURE: Cannot recreate without private key ✅                  ║
║                                                                    ║
║  ⚠️ Never put sensitive data (passwords, credit cards) in JWT!     ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 🧪 Try It Yourself

Go to: **<https://jwt.io>**

1. Paste any JWT token
2. See it decoded in real-time!
3. Try modifying payload - signature becomes invalid!

---

## 🆚 Auth Code Grant vs Client Credentials JWT

| Field | Auth Code Grant | Client Credentials |
|-------|-----------------|-------------------|
| `sub` | User email/ID | **App ID** (order-service) |
| `name` | User's name | ❌ Not present |
| `email` | User's email | ❌ Not present |
| `picture` | User's photo | ❌ Not present |
| `scope` | User's permissions | App's permissions |

**Key Insight:**

```
Auth Code JWT     = Represents a PERSON
Client Creds JWT  = Represents an APPLICATION
```

---

## 💡 Why JWT? Why Not Just Random Token?

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  RANDOM TOKEN (Opaque):                                            ║
║  ──────────────────────                                            ║
║  Token: "abc123xyz789"                                             ║
║  • Contains NO information                                         ║
║  • Resource Server must call Auth Server every time to validate    ║
║  • More network calls = Slower!                                    ║
║                                                                    ║
║  JWT TOKEN:                                                        ║
║  ──────────                                                        ║
║  Token: "eyJ..." (contains data inside!)                           ║
║  • Contains all necessary info (sub, scope, exp)                   ║
║  • Resource Server validates LOCALLY (just check signature)        ║
║  • No network call needed = Faster!                                ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 🎯 Summary

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                     │
│  JWT = HEADER.PAYLOAD.SIGNATURE                                     │
│                                                                     │
│  HEADER:    Algorithm info (RS256)                                  │
│  PAYLOAD:   Your data (sub, scope, exp, etc.)                       │
│  SIGNATURE: Proof of authenticity (signed with private key)         │
│                                                                     │
│  Base64:    Format change (not encryption!)                         │
│  Signing:   Proof of who created it                                 │
│  Readable:  Yes! Anyone can read payload                            │
│  Tamper:    No! Changing anything breaks signature                  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

**Next:** [05_Cryptography_Basics.md](./05_Cryptography_Basics.md) - Public/Private Keys Deep Dive
