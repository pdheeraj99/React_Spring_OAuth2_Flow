# 08 - When Refresh Tokens Are Needed

> 📌 Decision guide: Does YOUR app need refresh tokens?

---

## 🤔 Your Key Questions

*"Mana app ki access_token avasaram ledu kadha?"*  
*"Manam only Google APIs use chestene problem?"*  
*"id_token expire aithe em cheyyali?"*

Let's answer all of these!

---

## 📊 Decision Matrix

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    DO YOU NEED REFRESH TOKENS?                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   SCENARIO 1: Just "Login with Google"                                       │
│   ────────────────────────────────────                                       │
│   • User clicks "Login with Google"                                          │
│   • You show their name and email                                            │
│   • They browse your protected pages                                         │
│   • THAT'S IT - no external API calls                                        │
│                                                                              │
│   Refresh token needed? ❌ NO!                                               │
│   Why: Session keeps user logged in, tokens used only once.                  │
│                                                                              │
│   ───────────────────────────────────────────────────────────────────────    │
│                                                                              │
│   SCENARIO 2: Calling Google APIs                                            │
│   ─────────────────────────────────                                          │
│   • Get files from Google Drive                                              │
│   • Fetch photos from Google Photos                                          │
│   • Send email via Gmail API                                                 │
│                                                                              │
│   Refresh token needed? ✅ YES!                                              │
│   Why: access_token sent to Google APIs expires, need fresh ones.            │
│                                                                              │
│   ───────────────────────────────────────────────────────────────────────    │
│                                                                              │
│   SCENARIO 3: Sending id_token to Resource Server                            │
│   ──────────────────────────────────────────────────                         │
│   • Your Client App → Your Resource Server                                   │
│   • Resource Server validates JWT                                            │
│   • JWT has "exp" claim - gets checked!                                      │
│                                                                              │
│   Refresh token needed? ✅ YES!                                              │
│   Why: Expired id_token gets rejected by Resource Server!                    │
│                                                                              │
│   ───────────────────────────────────────────────────────────────────────    │
│                                                                              │
│   SCENARIO 4: Background jobs accessing user data                            │
│   ───────────────────────────────────────────────                            │
│   • Sync user's calendar at midnight                                         │
│   • Backup user's drive files weekly                                         │
│   • Process when user is NOT actively using app                              │
│                                                                              │
│   Refresh token needed? ✅ YES! (This is "offline access")                   │
│   Why: User not present to login again, need refresh_token.                  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Quick Decision Chart

| Your App's Use Case | Refresh Token? |
|---------------------|----------------|
| Login with Google, show user info, done | ❌ NO |
| Access Google Drive/Photos/Calendar | ✅ YES |
| Send JWT to your Resource Server | ✅ YES |
| Background jobs when user offline | ✅ YES |

---

## 🤔 id_token Refresh - Surprise Answer

Your question: *"access_token ki refresh token use chestam, id_token ki emcheyyali?"*

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              REFRESH TOKEN GIVES BOTH!                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   When you use refresh_token, Google returns:                                │
│   {                                                                          │
│       "access_token": "ya29.NEW...",  ← New access_token!                    │
│       "id_token": "eyJhbG.NEW...",    ← New id_token too! 🎉                 │
│       "expires_in": 3600                                                     │
│   }                                                                          │
│                                                                              │
│   ONE refresh = BOTH tokens refreshed!                                       │
│   No separate refresh for id_token!                                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📝 Our App's Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              OUR APP SETUP                                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   React (UI)  ──Session Cookie──►  Spring Boot (Client App)                  │
│                                          │                                   │
│                                          │ id_token (JWT)                    │
│                                          ▼                                   │
│                                   Resource Server                            │
│                                          │                                   │
│                                          ▼                                   │
│                                   JWT Validated!                             │
│                                   (checks "exp" claim)                       │
│                                                                              │
│   Since we send id_token to Resource Server:                                 │
│   → id_token CAN expire                                                      │
│   → Resource Server WILL reject expired JWT                                  │
│   → We NEED refresh tokens!                                                  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔧 Enabling Refresh Token for Our App

```yaml
# client-app/src/main/resources/application.yaml

spring:
  security:
    oauth2:
      client:
        provider:
          google:
            # Add access_type=offline to get refresh token
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth?access_type=offline&prompt=consent
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid, email, profile
```

Spring will automatically:

1. Request refresh_token from Google
2. Detect when access_token/id_token expired
3. Use refresh_token to get new ones
4. Retry the failed request with new token

---

## 🎉 Summary

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   REFRESH TOKENS - FINAL ANSWER:                                             │
│   ───────────────────────────────                                            │
│                                                                              │
│   • For just login (no API calls): NOT needed                                │
│   • For Google API access: NEEDED                                            │
│   • For sending JWT to Resource Server: NEEDED                               │
│   • For background/offline jobs: NEEDED                                      │
│                                                                              │
│   id_token refresh: Same refresh_token, gives BOTH tokens!                   │
│   Spring handling: AUTOMATIC - just enable in config!                        │
│                                                                              │
│   🎯 OUR APP: Sends id_token to Resource Server → NEEDS refresh tokens!     │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔗 Congratulations

You've completed the PKCE & Refresh Tokens learning guide! 🎉

**Related Demo:** See [../PKCE_demo/](../PKCE_demo/) for a live React demo of PKCE flow!
