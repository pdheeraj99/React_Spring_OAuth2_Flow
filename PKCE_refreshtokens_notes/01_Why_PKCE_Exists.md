# 01 - Why PKCE Exists

> 📌 PKCE = **P**roof **K**ey for **C**ode **E**xchange (Pronounced "Pixie" 🧚)

---

## 🤔 The Problem

You know Authorization Code Grant, right? Here's the flow:

```
1. User clicks "Login with Google"
2. Google redirects back with AUTHORIZATION CODE in URL
3. Your app exchanges CODE + CLIENT_SECRET for tokens
4. Done!
```

This works great for **backend apps** because `client_secret` is safe on the server.

**But what about apps that run entirely in the browser (SPAs)?**

---

## 💔 The SPA Problem

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              SPA = Single Page Application (React, Angular, Vue)             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   SPA runs 100% in user's browser!                                           │
│                                                                              │
│   Your React code:                                                           │
│   const CLIENT_SECRET = "GOCSPX-super-secret";  // 🚨 ANYONE CAN SEE!        │
│                                                                              │
│   How? User opens DevTools (F12):                                            │
│   • Sources tab → Find your .js files                                        │
│   • Search for "secret" → FOUND! 😱                                          │
│   • Network tab → See all requests                                           │
│   • Right-click → View Page Source                                           │
│                                                                              │
│   BROWSER CODE = PUBLIC CODE                                                 │
│   There's no way to hide client_secret in a SPA!                             │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 💡 PKCE = The Solution

PKCE replaces `client_secret` with a **dynamic, one-time secret** for each login!

```
Traditional (with client_secret):
─────────────────────────────────
Exchange: code + client_secret → tokens

PKCE (without client_secret):
─────────────────────────────
Exchange: code + code_verifier → tokens

The difference:
• client_secret = STATIC (same every time, stored in code)
• code_verifier = DYNAMIC (new random value each login!)
```

---

## 🎯 Key Insight

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   PKCE exists because:                                                       │
│                                                                              │
│   • SPAs can't keep secrets (all code is visible!)                           │
│   • Mobile apps can't keep secrets (can be decompiled!)                      │
│   • We need a way to prove "I'm the one who started this login"              │
│   • Without storing a permanent secret in the code!                          │
│                                                                              │
│   PKCE = Dynamic secret per request = Safe even in public code!              │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

**Next:** [02_SPA_vs_Backend_Security.md](./02_SPA_vs_Backend_Security.md)
