# OAuth2 Flow Initiation

> 🎓 **Login Click → Google Redirect**

---

## 🚀 Flow Overview

User clicks "Sign in with Google" → Browser redirects to Google Authorization Server

```
┌──────────┐     ┌─────────────┐     ┌─────────────┐
│  React   │────►│ Client-App  │────►│   Google    │
│   UI     │     │   (BFF)     │     │   OAuth     │
└──────────┘     └─────────────┘     └─────────────┘
   Click         Generate state       Show login
   button        Create redirect       page
```

---

## 📍 Step-by-Step Breakdown

### STEP 1: User Clicks Login Button

```jsx
// React UI - Login button
<a href="http://localhost:8080/oauth2/authorization/google">
  Sign in with Google
</a>
```

**Browser sends:**
```http
GET /oauth2/authorization/google HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=440421F8...
```

---

### STEP 2: OAuth2AuthorizationRequestRedirectFilter Intercepts

From our logs:
```
🚀 REQUEST #12 - GET /oauth2/authorization/google
════════════════════════════════════════════════════════════════════════════════

📥 STEP 1: REQUEST VACHINDI (Incoming Request)
────────────────────────────────────────────────────────────────────────────────
   🌐 URI: /oauth2/authorization/google
   📝 Method: GET

⚙️ STEP 4: EE REQUEST NI EVARU HANDLE CHESTARU?
────────────────────────────────────────────────────────────────────────────────
   🎯 TARGET: OAuth2AuthorizationRequestRedirectFilter
   📍 LOCATION: Spring Security internal filter
   
   🔥 EM JARUGUTUNDI:
      1️⃣ State parameter generate chestundi (CSRF protection)
      2️⃣ State ni session lo save chestundi
      3️⃣ Google Authorization URL build chestundi
      4️⃣ 302 Redirect response istundi → Google ki vellipothav
   
   📤 REDIRECT TO: https://accounts.google.com/o/oauth2/v2/auth
```

---

### STEP 3: Filter Builds Authorization URL

**Internal processing:**

```java
// OAuth2AuthorizationRequestRedirectFilter internally does:

// 1. Generate random state (CSRF protection)
String state = UUID.randomUUID().toString();  // e.g., "abc123xyz"

// 2. Save state in session (for later validation)
session.setAttribute("oauth2_auth_request", authorizationRequest);

// 3. Build Google authorization URL
String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
    "response_type=code" +                     // We want authorization code
    "&client_id=450472639030-g4g6r5..." +     // Our registered app ID
    "&scope=openid+email+profile" +            // What permissions we need
    "&state=abc123xyz" +                       // CSRF protection
    "&redirect_uri=http://localhost:8080/login/oauth2/code/google" +
    "&nonce=XMwoJXG0prWX90z0lh0Az4JaGEqgzWZuwmBr2U5trHA";
```

---

### STEP 4: Server Sends Redirect Response

From logs:
```
📤 STEP 5: RESPONSE READY (Filter chain complete)
────────────────────────────────────────────────────────────────────────────────
   📊 Status Code: 302
   🔄 REDIRECT HAPPENING!
   📍 Location: https://accounts.google.com/o/oauth2/v2/auth?...
   💡 Google Login page ki redirect avtunnaav!
```

**HTTP Response:**
```http
HTTP/1.1 302 Found
Location: https://accounts.google.com/o/oauth2/v2/auth?
    response_type=code&
    client_id=450472639030-g4g6r5terpsr6i9eo5bfhmfedcf33387.apps.googleusercontent.com&
    scope=openid%20email%20profile&
    state=XMwoJXG0prWX90z0lh0Az4JaGEqgzWZuwmBr2U5trHA&
    redirect_uri=http://localhost:8080/login/oauth2/code/google&
    nonce=XMwoJXG0prWX90z0lh0Az4JaGEqgzWZuwmBr2U5trHA
Set-Cookie: JSESSIONID=440421F8...; Path=/; HttpOnly
```

---

### STEP 5: Browser Redirects to Google

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         GOOGLE AUTHORIZATION PAGE                            │
│  URL: https://accounts.google.com/o/oauth2/v2/auth?...                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────┐      │
│   │                                                                  │      │
│   │         [Google Logo]                                           │      │
│   │                                                                  │      │
│   │     Sign in with your Google Account                            │      │
│   │                                                                  │      │
│   │     "PhotoVault Pro" wants to:                                  │      │
│   │       ✓ View your email address                                 │      │
│   │       ✓ View your basic profile info                            │      │
│   │                                                                  │      │
│   │     [Choose Account: Dheeraj]                                   │      │
│   │                                                                  │      │
│   └─────────────────────────────────────────────────────────────────┘      │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Authorization URL Parameters Explained

```
https://accounts.google.com/o/oauth2/v2/auth?
    response_type=code&          ← We want authorization CODE
    client_id=450472...&         ← Our app's registered ID with Google
    scope=openid+email+profile&  ← What permissions we're requesting
    state=XMwoJXG0...&           ← CSRF protection (random string)
    redirect_uri=http://...&     ← Where Google should send the code
    nonce=XMwoJXG0...            ← Replay attack protection
```

| Parameter | Value | Purpose |
|-----------|-------|---------|
| `response_type` | `code` | Request authorization code (not token directly) |
| `client_id` | `450472...` | Identifies our registered application |
| `scope` | `openid email profile` | Permissions we need |
| `state` | Random UUID | CSRF protection - we verify this later |
| `redirect_uri` | `/login/oauth2/code/google` | Where to send the code |
| `nonce` | Random string | Prevent replay attacks in ID token |

---

## 🔐 State Parameter - CSRF Protection

### Why State?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ WITHOUT State (Vulnerable to CSRF):                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Attacker creates link: /login/oauth2/code/google?code=ATTACKER_CODE      │
│   Victim clicks → Victim's account linked to attacker's Google!            │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ WITH State (Protected):                                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   1. We generate: state=abc123 → Save in session                            │
│   2. Google returns: code=xxx&state=abc123                                  │
│   3. We verify: session.state == returned.state?                            │
│   4. If mismatch → REJECT! (CSRF attack detected)                           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 Session State After This Step

```
SESSION {
    id: "440421F8...",
    
    // OAuth2 authorization request saved for verification
    "oauth2_auth_request": {
        authorizationUri: "https://accounts.google.com/...",
        clientId: "450472639030-...",
        redirectUri: "http://localhost:8080/login/oauth2/code/google",
        scopes: ["openid", "email", "profile"],
        state: "XMwoJXG0prWX90z0lh0Az4JaGEqgzWZuwmBr2U5trHA",
        nonce: "XMwoJXG0prWX90z0lh0Az4JaGEqgzWZuwmBr2U5trHA"
    }
}
```

---

## 📋 Summary

| Step | What Happens | Who Does It |
|------|--------------|-------------|
| 1 | User clicks "Sign in with Google" | Browser |
| 2 | Request reaches `/oauth2/authorization/google` | Browser → Server |
| 3 | Generate state, save in session | OAuth2AuthorizationRequestRedirectFilter |
| 4 | Build Google authorization URL | OAuth2AuthorizationRequestRedirectFilter |
| 5 | Send 302 redirect to Google | Server → Browser |
| 6 | User sees Google login page | Google |

---

> 📖 **Next:** [03-callback-processing.md](./03-callback-processing.md) - Google Callback → Token Exchange
