# 07 - Session vs Token Expiry

> 📌 **CRITICAL CONCEPT:** These are DIFFERENT things!

---

## 🤔 Your Original Confusion

*"Mana app ki access_token enduku kavali? Manam only id_token use chestunnam kadha?"*

*"App enduku sign out avtundi? Token expire aithe problem enti?"*

These questions reveal a common confusion: **mixing up sessions and tokens!**

---

## 💡 Two Different Expiry Mechanisms

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              SESSION vs TOKEN - COMPLETELY DIFFERENT!                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   TOKEN EXPIRY (Google Controls):                                            │
│   ────────────────────────────────                                           │
│   • access_token: expires in ~1 hour                                         │
│   • id_token: expires in ~1 hour                                             │
│   • Set by GOOGLE, you can't change it                                       │
│                                                                              │
│   SESSION EXPIRY (YOUR SERVER Controls):                                     │
│   ─────────────────────────────────────                                      │
│   • HttpSession: default 30 min idle                                         │
│   • Set by YOUR application.yaml                                             │
│   • Can be 1 hour, 1 day, 1 week - whatever you want!                        │
│                                                                              │
│   THEY ARE INDEPENDENT!                                                      │
│   ─────────────────────                                                      │
│   Token expired ≠ Session expired                                            │
│   Session active ≠ Token still valid                                         │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 What Happens After Login?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              POST-LOGIN FLOW                                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   1. User logs in with Google                                                │
│                                                                              │
│   2. Spring gets tokens from Google:                                         │
│      • access_token (1 hour)                                                 │
│      • id_token (1 hour)                                                     │
│                                                                              │
│   3. Spring does these things:                                               │
│      • Decodes id_token                                                      │
│      • Extracts: email, name, picture                                        │
│      • Creates: OidcUser object                                              │
│      • Stores in: HttpSession                                                │
│      • Sends: JSESSIONID cookie to browser                                   │
│                                                                              │
│   4. From now on:                                                            │
│      • Every request has JSESSIONID cookie                                   │
│      • Server finds session by JSESSIONID                                    │
│      • User info is already in session!                                      │
│      • NO TOKEN NEEDED for displaying user info!                             │
│                                                                              │
│   5. After 1 hour (token expired):                                           │
│      • Session is STILL ACTIVE!                                              │
│      • User can still browse protected pages!                                │
│      • User info still available from session!                               │
│                                                                              │
│   Token expired BUT user still logged in! 🎉                                 │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Visual Timeline

```
TIME    TOKEN STATUS       SESSION STATUS      USER EXPERIENCE
─────   ────────────       ──────────────      ───────────────
0 min   ✅ access+id valid  ✅ Session active    Logged in! ✅
30 min  ✅ access+id valid  ✅ Session active    Logged in! ✅
60 min  ❌ TOKENS EXPIRED!  ✅ Session active    Still logged in! ✅
90 min  ❌ Tokens expired   ✅ Session active    Still logged in! ✅
...     (tokens useless)    (session works!)     (user browses happily)

SESSION TIMEOUT (idle 30 min OR max 8 hours):
──────────────────────────────────────────────
480 min ❌ Tokens expired   ❌ SESSION EXPIRED   "Please login again"
```

---

## 🎯 When Do Things Expire?

| Thing | When Does It Expire? | Controlled By |
|-------|---------------------|---------------|
| access_token | ~1 hour from issue | Google |
| id_token | ~1 hour from issue | Google |
| refresh_token | ~6 months | Google |
| HttpSession | Idle timeout OR max age | YOUR app! |

```yaml
# application.yaml - YOU control session!

server:
  servlet:
    session:
      timeout: 8h  # Session lasts 8 hours (or until idle)
```

---

## 🤔 So When Does Token Expiry Matter?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   TOKEN EXPIRY MATTERS WHEN:                                                 │
│   ────────────────────────────                                               │
│                                                                              │
│   1. You call GOOGLE APIs (Drive, Photos, Gmail)                             │
│      → Need access_token                                                     │
│      → Expired? Need to refresh!                                             │
│                                                                              │
│   2. You send id_token to RESOURCE SERVER                                    │
│      → Resource Server validates JWT                                         │
│      → Checks "exp" claim                                                    │
│      → Expired? 401 Rejected!                                                │
│                                                                              │
│   TOKEN EXPIRY DOESN'T MATTER WHEN:                                          │
│   ──────────────────────────────────                                         │
│                                                                              │
│   1. Just showing user's name/email on your site                             │
│      → Already stored in session!                                            │
│      → Token not needed!                                                     │
│                                                                              │
│   2. Browsing protected pages on your app                                    │
│      → Session handles authentication                                        │
│      → Token was only needed once at login!                                  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 💡 Key Insight

```
If your app ONLY does "Login with Google":
• Token used ONCE at login to get user info
• Session keeps user logged in
• Token expiry doesn't affect user!
• You probably DON'T need refresh tokens!

If your app ALSO calls Google APIs OR sends JWT to Resource Server:
• Token needed for each API call
• Token expiry = API failure
• You NEED refresh tokens!
```

---

**Next:** [08_When_Refresh_Needed.md](./08_When_Refresh_Needed.md)
