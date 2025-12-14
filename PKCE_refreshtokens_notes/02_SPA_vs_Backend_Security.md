# 02 - SPA vs Backend Security

> 📌 Understanding why SPAs are called "Public Clients"

---

## 🏗️ Two Types of OAuth Clients

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   CONFIDENTIAL CLIENT (Backend Apps)                                         │
│   ────────────────────────────────────                                       │
│   • Spring Boot, Node.js, Django, etc.                                       │
│   • Code runs on YOUR server                                                 │
│   • User NEVER sees server code                                              │
│   • Can safely store client_secret                                           │
│   • Example: application.yaml with secret on server                          │
│                                                                              │
│   PUBLIC CLIENT (SPAs, Mobile Apps)                                          │
│   ──────────────────────────────────                                         │
│   • React, Angular, Vue (in browser)                                         │
│   • Android, iOS apps                                                        │
│   • Code runs on USER'S device                                               │
│   • User CAN see all the code                                                │
│   • CANNOT safely store client_secret                                        │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔍 Visual Comparison

```
CONFIDENTIAL CLIENT (Spring Boot):
──────────────────────────────────

   User's Browser              Your Server (Spring Boot)
   ┌─────────────┐            ┌─────────────────────────┐
   │             │            │  application.yaml:       │
   │  React UI   │─────────►  │  client-secret: xxx     │
   │             │  Session   │  (User CAN'T see this!) │
   │  (No secrets│  Cookie    │                         │
   │   here!)    │            │  OAuth happens HERE!    │
   └─────────────┘            └─────────────────────────┘

   • React only shows UI
   • All OAuth logic is on server
   • client_secret never leaves server
   • SAFE! ✅


PUBLIC CLIENT (React Only):
───────────────────────────

   User's Browser
   ┌───────────────────────────────────────────────────┐
   │                                                   │
   │  React App (Everything runs here!)                │
   │                                                   │
   │  const CLIENT_ID = "xxx";                         │
   │  const CLIENT_SECRET = "yyy";  // 👀 User sees!   │
   │                                                   │
   │  // OAuth logic here                              │
   │  // Tokens stored in localStorage                 │
   │  // Everything visible in DevTools!               │
   │                                                   │
   └───────────────────────────────────────────────────┘

   • No server to hide secrets
   • All code is downloadable
   • DevTools shows everything
   • NOT SAFE without PKCE! ❌ → ✅ (with PKCE)
```

---

## 🎯 The Confusion Clarified

Your original question: *"SPA lo client_secret petteyochu kadha?"*

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   Q: Can I put client_secret in React code?                                  │
│   A: Technically YES, but it defeats the purpose!                            │
│                                                                              │
│   • The "secret" is no longer secret!                                        │
│   • Anyone can open DevTools and find it                                     │
│   • Google even says: "SPAs should NOT use client_secret"                    │
│   • That's why PKCE exists - to replace client_secret!                       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Quick Reference

| Aspect | Backend App | SPA (Browser Only) |
|--------|-------------|-------------------|
| Code visibility | Private (server) | Public (browser) |
| client_secret | ✅ Safe to use | ❌ NOT safe! |
| Token storage | HttpSession (server) | localStorage (risky!) |
| OAuth client type | Confidential | Public |
| PKCE needed? | Optional (extra security) | **REQUIRED!** |

---

**Next:** [03_PKCE_Flow_Explained.md](./03_PKCE_Flow_Explained.md)
