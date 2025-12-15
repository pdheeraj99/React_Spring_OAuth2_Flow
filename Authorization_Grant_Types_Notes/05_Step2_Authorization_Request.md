# 05 - Step 2: Authorization Request (Redirect to Google)

> 📌 Your backend builds a special URL and sends the user to Google!

---

## 🔄 What Happens?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    STEP 2: REDIRECT TO GOOGLE                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Browser sent:                                                              │
│   GET http://localhost:8080/oauth2/authorization/google                      │
│                                                                              │
│   Spring Boot responds with:                                                 │
│   HTTP 302 Found                                                             │
│   Location: https://accounts.google.com/o/oauth2/v2/auth?                    │
│             response_type=code&                                              │
│             client_id=YOUR_CLIENT_ID&                                        │
│             scope=openid+email+profile&                                      │
│             state=abc123xyz&                                                 │
│             redirect_uri=http://localhost:8080/login/oauth2/code/google      │
│                                                                              │
│   Browser automatically follows this redirect!                               │
│   User now sees Google's login page!                                         │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 The Authorization URL Breakdown

Let's examine each parameter:

```
https://accounts.google.com/o/oauth2/v2/auth?
  response_type=code
  &client_id=YOUR_CLIENT_ID.apps.googleusercontent.com
  &scope=openid+email+profile
  &state=abc123xyz
  &redirect_uri=http://localhost:8080/login/oauth2/code/google
```

---

## 📊 Parameter-by-Parameter Explanation

### 1. `response_type=code`

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   response_type=code                                                         │
│   ───────────────────                                                        │
│                                                                              │
│   Meaning: "I want an Authorization CODE, not a token directly!"             │
│                                                                              │
│   Options:                                                                   │
│   • code → Authorization Code Grant (WHAT WE USE)                            │
│   • token → Implicit Grant (DEPRECATED! Don't use!)                          │
│                                                                              │
│   Why code?                                                                  │
│   → Code is exchanged for token in back channel (Step 5)                     │
│   → More secure than getting token in URL!                                   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 2. `client_id=YOUR_CLIENT_ID`

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   client_id=YOUR_CLIENT_ID                                                   │
│   ─────────────────────────                                                  │
│                                                                              │
│   Meaning: "This is WHO is asking for access!"                               │
│                                                                              │
│   Where it comes from:                                                       │
│   → Google Cloud Console                                                     │
│   → APIs & Services → Credentials → OAuth 2.0 Client IDs                     │
│                                                                              │
│   Format: 815195837364-xxx.apps.googleusercontent.com                        │
│                                                                              │
│   ⚠️ This is PUBLIC! Anyone can see it in the URL.                          │
│   That's OK - client_id alone can't do anything harmful.                     │
│   You need client_secret to exchange code for tokens!                        │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 3. `scope=openid+email+profile`

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   scope=openid+email+profile                                                 │
│   ──────────────────────────                                                 │
│                                                                              │
│   Meaning: "What data/permissions do I want?"                                │
│                                                                              │
│   SCOPE BREAKDOWN:                                                           │
│   ┌───────────┬──────────────────────────────────┬────────────────────────┐  │
│   │ Scope     │ What It Gives                    │ Token Type             │  │
│   ├───────────┼──────────────────────────────────┼────────────────────────┤  │
│   │ openid    │ User's unique ID (sub)           │ id_token (JWT!)        │  │
│   │ email     │ User's email address             │ Added to id_token      │  │
│   │ profile   │ Name, picture, etc.              │ Added to id_token      │  │
│   └───────────┴──────────────────────────────────┴────────────────────────┘  │
│                                                                              │
│   Other possible scopes (not in our app):                                    │
│   • https://www.googleapis.com/auth/drive.readonly → Read Google Drive       │
│   • https://www.googleapis.com/auth/photoslibrary.readonly → Read Photos     │
│   • https://www.googleapis.com/auth/calendar.readonly → Read Calendar        │
│                                                                              │
│   ⭐ openid scope = You get id_token (JWT with user info)                    │
│   ⭐ Without openid = Only access_token, NO id_token!                        │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 4. `state=abc123xyz`

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   state=abc123xyz                                                            │
│   ────────────────                                                           │
│                                                                              │
│   Meaning: "A random value for security!"                                    │
│                                                                              │
│   Purpose: CSRF (Cross-Site Request Forgery) protection                      │
│                                                                              │
│   How it works:                                                              │
│   1. Backend generates random state: "abc123xyz"                             │
│   2. Backend stores it in session                                            │
│   3. Sends to Google in URL                                                  │
│   4. Google sends it BACK in callback (Step 4)                               │
│   5. Backend checks: Does returned state == stored state?                    │
│      → If YES: Proceed ✅                                                    │
│      → If NO: Attack detected! Reject! ❌                                    │
│                                                                              │
│   Prevents: Attacker tricking user into logging in to attacker's account     │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 5. `redirect_uri=http://localhost:8080/login/oauth2/code/google`

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   redirect_uri=http://localhost:8080/login/oauth2/code/google                │
│   ────────────────────────────────────────────────────────────               │
│                                                                              │
│   Meaning: "Where to send user after login!"                                 │
│                                                                              │
│   ⚠️ SECURITY CRITICAL:                                                      │
│   This MUST be registered in Google Cloud Console!                           │
│   Google only redirects to pre-approved URLs.                                │
│                                                                              │
│   Why?                                                                       │
│   Prevents: Attacker changing redirect_uri to their own server               │
│   If allowed: Attacker could steal the authorization code!                   │
│                                                                              │
│   In Google Cloud Console:                                                   │
│   APIs & Services → Credentials → Your OAuth Client                          │
│   → Authorized redirect URIs → Add your URL                                  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎨 Visual: What User Sees

```
After Step 2, user's browser shows:

┌─────────────────────────────────────────────────────────────────────────────┐
│  ← →  🔒 accounts.google.com/o/oauth2/v2/auth?...                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│                         ┌──────────────────────┐                             │
│                         │       Google         │                             │
│                         └──────────────────────┘                             │
│                                                                              │
│                          Sign in with Google                                 │
│                                                                              │
│                    ┌──────────────────────────────┐                          │
│                    │  Email or phone              │                          │
│                    └──────────────────────────────┘                          │
│                                                                              │
│                    ┌──────────────────────────────┐                          │
│                    │       Next                   │                          │
│                    └──────────────────────────────┘                          │
│                                                                              │
│                    Create account   Forgot email?                            │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘

User is NOW at Google's website, not your app!
```

---

## 🔧 Spring Boot Configuration

```yaml
# application.yaml - This configures Step 2!

spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:                        # ← Goes in URL as scope=
              - openid
              - email
              - profile
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            # ↑ This becomes: http://localhost:8080/login/oauth2/code/google
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth
            # ↑ This is where Spring redirects to!
```

---

## 💡 Key Insight

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   Step 2 = Your app saying to Google:                                        │
│                                                                              │
│   "Hey Google! My name is [client_id].                                       │
│    I want [openid, email, profile] access.                                   │
│    When done, send user back to [redirect_uri].                              │
│    Here's a [state] token to verify the response is legit."                  │
│                                                                              │
│   Google will now show its login page to the user!                           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🤔 Beginner Check

| Question | Answer |
|----------|--------|
| What type of HTTP response is Step 2? | 302 Redirect |
| Which parameter identifies your app? | client_id |
| Which parameter prevents CSRF attacks? | state |
| Which scope gets you the id_token? | openid |
| Where must redirect_uri be registered? | Google Cloud Console |

---

**Next:** [06_Step3_User_Authenticates.md](./06_Step3_User_Authenticates.md)
