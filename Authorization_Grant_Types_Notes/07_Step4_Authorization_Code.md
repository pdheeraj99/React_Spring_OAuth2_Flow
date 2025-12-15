# 07 - Step 4: Authorization Code Returned

> 📌 Google sends back a temporary "ticket" - the Authorization Code!

---

## 📬 What Happens?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    STEP 4: AUTHORIZATION CODE                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   User clicked "Allow" at Google.                                            │
│                                                                              │
│   Google responds with HTTP 302 redirect:                                    │
│   ────────────────────────────────────────                                   │
│   Location: http://localhost:8080/login/oauth2/code/google?                  │
│             code=4/0AX4XfWh8CnlM6Gx...                                       │
│             &state=abc123xyz                                                 │
│                                                                              │
│   Browser automatically goes to this URL!                                    │
│   Your backend receives the code!                                            │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 The Callback URL Breakdown

```
http://localhost:8080/login/oauth2/code/google?
  code=4/0AX4XfWh8CnlM6GxBptYv...
  &state=abc123xyz
```

| Parameter | Value | Meaning |
|-----------|-------|---------|
| `code` | `4/0AX4XfWh...` | The Authorization Code! |
| `state` | `abc123xyz` | Must match what we sent in Step 2 |

---

## 🎫 What is the Authorization Code?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    AUTHORIZATION CODE EXPLAINED                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   The Code is like a MOVIE TICKET:                                           │
│                                                                              │
│   🎫 Ticket says: "Admit one for Avatar 3, Seat A12"                         │
│      Code says: "This app can get tokens for dheeraj@gmail.com"              │
│                                                                              │
│   Properties:                                                                │
│   ─────────────                                                              │
│   • ONE-TIME USE: Use it once, it becomes invalid                            │
│   • SHORT-LIVED: Expires in ~10 minutes                                      │
│   • BOUND TO CLIENT: Only YOUR app's client_id can use it                    │
│   • BOUND TO REDIRECT_URI: Must match what was registered                    │
│                                                                              │
│   Format:                                                                    │
│   ─────────                                                                  │
│   • Opaque string (not JWT!)                                                 │
│   • Example: "4/0AX4XfWh8CnlM6GxBptYvQTy..."                                 │
│   • Only Google knows what's inside                                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔐 State Verification (CSRF Protection)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    STATE PARAMETER CHECK                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   When backend receives the callback:                                        │
│                                                                              │
│   1. Extract state from URL: "abc123xyz"                                     │
│   2. Get stored state from session: "abc123xyz"                              │
│   3. Compare: Do they match?                                                 │
│                                                                              │
│   ✅ MATCH: Proceed to Step 5                                                │
│   ❌ NO MATCH: Reject! Possible CSRF attack!                                 │
│                                                                              │
│   Why this matters:                                                          │
│   ──────────────────                                                         │
│   Without state check, attacker could:                                       │
│   1. Start OAuth with their own account                                      │
│   2. Intercept the callback URL (with their code)                            │
│   3. Send it to victim                                                       │
│   4. Victim's app exchanges code                                             │
│   5. Victim logs in as ATTACKER! 😱                                          │
│                                                                              │
│   With state check:                                                          │
│   → Attacker's state ≠ Victim's session state                                │
│   → Attack fails! ✅                                                         │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## ⚠️ Why Code and Not Directly Token?

This is the MOST IMPORTANT security question!

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    WHY NOT SEND TOKEN IN URL?                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   If Google sent token in URL:                                               │
│   http://localhost:8080/callback?access_token=ya29.xxxxx                     │
│                                                                              │
│   PROBLEMS:                                                                  │
│   ──────────                                                                 │
│   ❌ Browser history stores the URL → Token leaked!                          │
│   ❌ Browser extensions can read URL → Token stolen!                         │
│   ❌ Server logs might record URL → Token exposed!                           │
│   ❌ Referer header to other sites → Token leaked!                           │
│                                                                              │
│   WITH AUTHORIZATION CODE:                                                   │
│   ─────────────────────────                                                  │
│   ✅ Code alone is USELESS (needs client_secret!)                            │
│   ✅ Code is one-time use (even if stolen, already used!)                    │
│   ✅ Code expires quickly (~10 minutes)                                      │
│   ✅ Token is exchanged in BACK CHANNEL (Step 5)                             │
│                                                                              │
│   ⭐ The two-step process (code → token) is a SECURITY FEATURE!              │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔧 What Spring Security Does

```java
// Spring Security's OAuth2LoginAuthenticationFilter catches:
// GET /login/oauth2/code/google?code=xxx&state=yyy

// Internally:
1. Extract code and state from URL
2. Validate state matches session state
3. If valid, proceed to Step 5 (token exchange)
4. If invalid, throw OAuth2AuthenticationException

// You don't write this code! Spring handles it!
```

---

## 📊 Timeline So Far

```
STEP 1: User clicks Login
        → Browser goes to /oauth2/authorization/google

STEP 2: Backend redirects to Google
        → accounts.google.com/...?client_id=...&scope=...&state=abc123

STEP 3: User logs in and approves
        → Google verifies password and consent

STEP 4: Google redirects back (YOU ARE HERE!)
        → localhost:8080/...?code=4/0AX4...&state=abc123
        → Backend validates state, has the code
        → Ready for Step 5!
```

---

## 🤔 Beginner Check

1. What two parameters are in the callback URL?
2. What happens if state doesn't match?
3. Why is code one-time use important?
4. Can someone steal the code and use it? (Think about what they're missing)

Answers:

1. `code` and `state`
2. Request rejected as possible CSRF attack
3. Even if intercepted, attacker can't use it again
4. They can steal it, but without `client_secret`, it's useless!

---

**Next:** [08_Step5_Token_Exchange.md](./08_Step5_Token_Exchange.md)
