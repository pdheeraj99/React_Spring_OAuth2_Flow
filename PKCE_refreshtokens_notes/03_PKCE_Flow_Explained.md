# 03 - PKCE Flow Explained

> 📌 Step-by-step PKCE mechanism with visual diagrams

---

## 🔑 Two Key Values in PKCE

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   1. code_verifier                                                           │
│      ─────────────                                                           │
│      • Random string (64-128 characters)                                     │
│      • Generated at START of login                                           │
│      • STAYS IN YOUR APP (never sent in URL!)                                │
│      • Example: "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"                 │
│                                                                              │
│   2. code_challenge                                                          │
│      ───────────────                                                         │
│      • SHA256 hash of code_verifier                                          │
│      • Sent to Google in authorization URL                                   │
│      • It's just a HASH - cannot be reversed!                                │
│      • Example: "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"                 │
│                                                                              │
│   Relationship:                                                              │
│   code_verifier ──SHA256──► code_challenge                                   │
│   (secret)       (hash)      (public, safe to expose)                        │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 PKCE Flow (5 Steps)

### Step 1: Generate code_verifier

```
App generates random string:
code_verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"

Store it safely (sessionStorage in SPA, session in backend)
```

### Step 2: Create code_challenge (hash)

```
code_challenge = SHA256(code_verifier) = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"

Key insight:
• SHA256 is ONE-WAY
• Can't reverse: hash → original
• Safe to expose in URL!
```

### Step 3: Redirect to Google with code_challenge

```
https://accounts.google.com/o/oauth2/v2/auth?
    client_id=xxx
    &response_type=code
    &redirect_uri=http://localhost:3000/callback
    &scope=openid email profile
    &code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM  ← HASH only!
    &code_challenge_method=S256

Google stores: "This login request has code_challenge = E9Melhoa..."
```

### Step 4: Google redirects back with authorization code

```
http://localhost:3000/callback?code=4/0AY0e-abc123xyz

Your app receives the code.
```

### Step 5: Exchange code + code_verifier for tokens

```
POST https://oauth2.googleapis.com/token

{
    "code": "4/0AY0e-abc123xyz",
    "code_verifier": "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk",  ← NOW we send it!
    "client_id": "xxx",
    "redirect_uri": "http://localhost:3000/callback",
    "grant_type": "authorization_code"
}

Google does: SHA256(code_verifier) == stored code_challenge?
If YES → Issue tokens! ✅
If NO → Reject! ❌
```

---

## 🎨 Visual Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         PKCE COMPLETE FLOW                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Your App                              Google                               │
│      │                                     │                                 │
│      │  1. Generate code_verifier          │                                 │
│      │     (random, keep secret!)          │                                 │
│      │                                     │                                 │
│      │  2. Create code_challenge           │                                 │
│      │     = SHA256(code_verifier)         │                                 │
│      │                                     │                                 │
│      │  3. Redirect with code_challenge    │                                 │
│      │─────────────────────────────────────►                                 │
│      │     (hash in URL, safe!)            │                                 │
│      │                                     │  Google stores:                 │
│      │                                     │  "code_challenge = E9Me..."     │
│      │                                     │                                 │
│      │  4. Redirect back with code         │                                 │
│      │◄─────────────────────────────────────                                 │
│      │     ?code=abc123                    │                                 │
│      │                                     │                                 │
│      │  5. Exchange code + verifier        │                                 │
│      │─────────────────────────────────────►                                 │
│      │     { code, code_verifier }         │                                 │
│      │                                     │  Google checks:                 │
│      │                                     │  SHA256(verifier) == challenge? │
│      │                                     │                                 │
│      │  6. Tokens!                         │                                 │
│      │◄─────────────────────────────────────                                 │
│      │     { access_token, id_token }      │                                 │
│      │                                     │                                 │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 💡 Why This Works

```
At no point is code_verifier in the URL!

Step 3: Only code_challenge (hash) is in URL → Can't reverse it!
Step 5: code_verifier goes in POST body → HTTPS encrypts it!

Even if hacker sees the URL with code_challenge,
they can't figure out code_verifier!
```

---

**Next:** [04_Attack_Prevention.md](./04_Attack_Prevention.md)
