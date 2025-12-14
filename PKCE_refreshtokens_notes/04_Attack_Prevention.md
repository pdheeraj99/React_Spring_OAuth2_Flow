# 04 - Attack Prevention

> 📌 How PKCE prevents authorization code theft attacks

---

## 🤔 Your Original Question

*"Token exchange lo code_verifier pampistunnapudu hacker adi kuda steal cheyyochu kadha?"*

Great question! Let's analyze the attack window step by step.

---

## ⚔️ Attack Scenario (Without PKCE)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              WITHOUT PKCE - HACKER CAN SUCCEED!                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   1. User starts login                                                       │
│   2. Google redirects back:                                                  │
│      http://myapp.com/callback?code=abc123                                   │
│                                                                              │
│   3. ⚠️ ATTACK WINDOW!                                                       │
│      └── Browser extension reads URL → Gets code!                            │
│      └── Browser history stores URL → Gets code from history!                │
│      └── Malicious app intercepts (mobile) → Gets code!                      │
│                                                                              │
│   4. Hacker now has: code + (can search for) client_secret                   │
│                                                                              │
│   5. Hacker exchanges code → Gets user's tokens! 😱                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🛡️ With PKCE - Hacker Fails

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              WITH PKCE - HACKER BLOCKED!                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   1. User's app generates: code_verifier (stored in memory/session)          │
│                            code_challenge (hash, sent in URL)                │
│                                                                              │
│   2. Google redirects back:                                                  │
│      http://myapp.com/callback?code=abc123                                   │
│                                                                              │
│   3. ⚠️ ATTACK WINDOW!                                                       │
│      └── Hacker intercepts URL → Gets code!                                  │
│      └── Hacker also saw code_challenge earlier → But it's just a hash!     │
│                                                                              │
│   4. Hacker tries to exchange:                                               │
│      {                                                                       │
│        code: "abc123",      ← Has this!                                      │
│        code_verifier: "???" ← DOESN'T HAVE THIS!                             │
│      }                                                                       │
│                                                                              │
│   5. Where is code_verifier?                                                 │
│      → In USER's browser sessionStorage!                                     │
│      → Hacker's browser is DIFFERENT!                                        │
│      → Hacker CAN'T access user's memory!                                    │
│                                                                              │
│   6. Hacker guesses a verifier → Google: SHA256 doesn't match! ❌            │
│                                                                              │
│   ATTACK FAILED! 🎉                                                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔐 Why Token Exchange is Safe

Your question: *"POST request lo code_verifier pampistunnapudu adi steal cheyyochu kadha?"*

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   The POST request (code + verifier → tokens):                               │
│                                                                              │
│   WHY IT'S SAFE:                                                             │
│                                                                              │
│   1. HTTPS Encryption                                                        │
│      ─────────────────                                                       │
│      POST body is encrypted!                                                 │
│      Even on same WiFi, hacker can't read it.                                │
│      Only Google's server can decrypt.                                       │
│                                                                              │
│   2. Different Attack Point                                                  │
│      ───────────────────────                                                 │
│      Hacker intercepted the REDIRECT URL (step 4)                            │
│      Token exchange happens from YOUR browser (step 5)                       │
│      Hacker is NOT in your browser!                                          │
│                                                                              │
│   3. Timing                                                                  │
│      ───────                                                                 │
│      Hacker gets code from your URL                                          │
│      Hacker tries to exchange from THEIR computer                            │
│      But they don't have YOUR code_verifier!                                 │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📍 Attack Window Timeline

```
YOUR BROWSER                  HACKER                    GOOGLE
     │                           │                         │
     │  1. Start login           │                         │
     │   (generate verifier)     │                         │
     │                           │                         │
     │                           │                         │
     │  2. Redirect with hash    │                         │
     │───────────────────────────────────────────────────► │
     │                           │                         │
     │  3. Redirect back         │                         │
     │◄───────────────────────────────────────────────────│
     │   ?code=abc123            │                         │
     │                           │                         │
     │   ⚠️ HACKER INTERCEPTS    │                         │
     │   ─────────────────────── │                         │
     │                           │ "I got the code!"       │
     │                           │                         │
     │  4. YOU exchange          │                         │
     │   (code + verifier)       │                         │
     │───────────────────────────────────────────────────► │
     │   🔒 HTTPS encrypted      │                         │
     │   Hacker can't see!       │                         │
     │                           │                         │
     │                           │  5. HACKER tries        │
     │                           │   (code + ???)          │
     │                           │────────────────────────►│
     │                           │                         │
     │                           │  "Wrong verifier!" ❌   │
     │                           │◄────────────────────────│
     │                           │                         │
     │  6. YOU get tokens! ✅    │                         │
     │◄───────────────────────────────────────────────────│
     │                           │                         │
```

---

## 🎯 Key Insight

```
Hacker can steal the authorization CODE (from URL)
Hacker CANNOT steal the code_VERIFIER (in your memory)

These are in DIFFERENT places:
• Code → URL (visible, interceptable)
• Verifier → Your app's memory (not accessible to hacker)

PKCE = Makes code useless without verifier!
```

---

**Next:** [05_PKCE_In_SpringBoot.md](./05_PKCE_In_SpringBoot.md)
