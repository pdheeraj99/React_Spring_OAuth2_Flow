# 06 - Refresh Tokens - Why They Exist

> 📌 Solving the token expiry problem

---

## 🤔 The Problem

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    THE TOKEN EXPIRY PROBLEM                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   User logs in → Gets access_token (valid for 1 hour)                        │
│   User happily uses the app...                                               │
│                                                                              │
│   After 1 hour:                                                              │
│   ──────────────                                                             │
│   App tries to call API with access_token                                    │
│   API: "Token expired! 401 Unauthorized!" ❌                                 │
│                                                                              │
│   What now?                                                                  │
│   • Show "Please login again" ? 😤 Bad UX!                                   │
│   • User was in the middle of something!                                     │
│   • Imagine: Writing email in Gmail → "Session expired!" → Lost work! 😱    │
│                                                                              │
│   Refresh Token = Solution! 🎉                                               │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 💡 Refresh Token = Long-Lived Token to Get Short-Lived Tokens

```
Initial Login Response:
{
    "access_token": "ya29.xxx...",     ← Short-lived (1 hour)
    "id_token": "eyJhbGci...",         ← Short-lived (1 hour)
    "refresh_token": "1//0eXxx...",    ← Long-lived (6 months!)
    "expires_in": 3600
}
```

---

## 🔄 Refresh Token Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    REFRESH TOKEN FLOW                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   TIMELINE:                                                                  │
│                                                                              │
│   T=0 (Login):                                                               │
│   User logs in → Gets access_token + refresh_token                           │
│                                                                              │
│   T=30min:                                                                   │
│   App calls API with access_token → Works! ✅                                │
│                                                                              │
│   T=61min (access_token expired!):                                           │
│   App calls API → 401 Unauthorized! ❌                                       │
│                                                                              │
│   App silently refreshes:                                                    │
│   ────────────────────────                                                   │
│   POST https://oauth2.googleapis.com/token                                   │
│   {                                                                          │
│       "grant_type": "refresh_token",   ← Different grant type!              │
│       "refresh_token": "1//0eXxx...",                                        │
│       "client_id": "xxx",                                                    │
│       "client_secret": "yyy"                                                 │
│   }                                                                          │
│                                                                              │
│   Google responds:                                                           │
│   {                                                                          │
│       "access_token": "ya29.NEW...",   ← Fresh token!                        │
│       "id_token": "eyJhbG.NEW...",     ← Fresh id_token too!                 │
│       "expires_in": 3600               ← Another hour!                       │
│   }                                                                          │
│                                                                              │
│   App retries API call with NEW token → Works! ✅                            │
│   USER DIDN'T NOTICE ANYTHING! Silent refresh! 🎉                            │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Token Comparison

| Aspect | Access Token | ID Token | Refresh Token |
|--------|--------------|----------|---------------|
| Lifetime | ~1 hour | ~1 hour | ~6 months |
| Purpose | Call APIs | User identity | Get new tokens |
| Sent to | Resource Server, APIs | Your backend | Authorization Server only |
| If stolen | 1 hour damage | 1 hour damage | ⚠️ Serious! Ongoing access! |

---

## 🔐 Security Warning

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              REFRESH TOKEN SECURITY                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Refresh tokens are POWERFUL and DANGEROUS!                                 │
│                                                                              │
│   If stolen:                                                                 │
│   • Hacker can get unlimited access tokens                                   │
│   • Access continues for months until revoked!                               │
│                                                                              │
│   Storage rules:                                                             │
│   • NEVER in browser (localStorage, cookies)                                 │
│   • ALWAYS on server (HttpSession, Redis, DB)                                │
│   • This is why BFF pattern matters!                                         │
│                                                                              │
│   Revocation:                                                                │
│   • User can revoke: https://myaccount.google.com/permissions                │
│   • All tokens become invalid immediately                                    │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔧 Getting Refresh Token from Google

By default, Google doesn't give refresh tokens! You must ask:

```yaml
# application.yaml

spring:
  security:
    oauth2:
      client:
        provider:
          google:
            # Add these parameters to authorization URL
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth?access_type=offline&prompt=consent
```

- `access_type=offline` → "I need tokens for when user is away"
- `prompt=consent` → "Show consent screen" (required to get refresh token)

---

**Next:** [07_Session_vs_Token_Expiry.md](./07_Session_vs_Token_Expiry.md)
