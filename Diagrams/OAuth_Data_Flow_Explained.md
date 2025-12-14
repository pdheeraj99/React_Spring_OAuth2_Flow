# OAuth 2.0 Data Flow - What Google Sends at Each Step

## 🔄 Complete Flow Overview

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   React UI  │───▶│  Backend    │───▶│   Google    │───▶│  Resource   │
│  (5173)     │    │  (8080)     │    │  (Auth)     │    │  Server(8081)│
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

---

## 📋 Step-by-Step Data Flow

### Step 1️⃣: User Clicks "Login with Google"

**React sends:**

```
GET http://localhost:8080/oauth2/authorization/google
```

**Backend creates Authorization URL:**

```
https://accounts.google.com/o/oauth2/v2/auth?
  response_type=code
  &client_id=YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com
  &scope=openid email profile
  &state=abc123xyz (CSRF protection)
  &redirect_uri=http://localhost:8080/login/oauth2/code/google
```

**Google receives:**

| Parameter | Value | Purpose |
|-----------|-------|---------|
| `client_id` | `YOUR_CLIENT_ID...` | Identifies YOUR app |
| `scope` | `openid email profile` | What data you want |
| `redirect_uri` | `http://localhost:8080/login/oauth2/code/google` | Where to send user back |
| `state` | Random string | CSRF protection |

---

### Step 2️⃣: User Logs in at Google

Google shows login page → User enters credentials → Google validates.

**NO data sent to your app yet!**

---

### Step 3️⃣: Google Redirects Back with Authorization Code

**Google sends to Backend:**

```
GET http://localhost:8080/login/oauth2/code/google?
  code=4/0AXxxxxxxxxxxxxxxxxxxxxx
  &state=abc123xyz
```

| Parameter | Value | Purpose |
|-----------|-------|---------|
| `code` | `4/0AXxxx...` | **Authorization Code** (temporary, one-time use) |
| `state` | `abc123xyz` | Must match what we sent (CSRF check) |

---

### Step 4️⃣: Backend Exchanges Code for Tokens (Back Channel)

**Backend sends to Google (HTTPS POST - invisible to browser):**

```
POST https://oauth2.googleapis.com/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&code=4/0AXxxxxxxxxxxxxxxxxxxxxx
&client_id=YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com
&client_secret=YOUR_GOOGLE_CLIENT_SECRET
&redirect_uri=http://localhost:8080/login/oauth2/code/google
```

**Google returns (JSON):**

```json
{
  "access_token": "ya29.a0AWY7Ckxxxxxxxxxxxxxxxxxxxxxxxx",
  "expires_in": 3599,
  "scope": "openid email profile",
  "token_type": "Bearer",
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI4MTUxOTU4MzczNjQtaGkxOXQ1Y2RrYjB1Mm1rcHQ5bHVlYmk5cmx1MjhvOW0uYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI4MTUxOTU4MzczNjQtaGkxOXQ1Y2RrYjB1Mm1rcHQ5bHVlYmk5cmx1MjhvOW0uYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTI0MTYwMzYzMzcwOTQ0Mzk1NjIiLCJlbWFpbCI6ImRoZWVyYWpwMDI5OUBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6Inh4eHh4eCIsIm5hbWUiOiJEaGVlcmFqIiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hL3h4eHgiLCJnaXZlbl9uYW1lIjoiRGhlZXJhaiIsImlhdCI6MTcwMjI4NTM5OCwiZXhwIjoxNzAyMjg4OTk4fQ.signature"
}
```

---

### Step 5️⃣: Decoding the ID Token (JWT)

The `id_token` is a JWT with 3 parts: `header.payload.signature`

**Decoded Payload (What Google tells us about the user):**

```json
{
  "iss": "https://accounts.google.com",      // Issuer
  "azp": "815195837364-...apps.googleusercontent.com", // Client ID
  "aud": "815195837364-...apps.googleusercontent.com", // Audience (your app)
  "sub": "112416036337094439562",            // 🔥 USER ID (permanent, unique)
  "email": "dheerajp0299@gmail.com",         // User's email
  "email_verified": true,                     // Email is verified
  "name": "Dheeraj",                          // Full name
  "picture": "https://lh3.googleusercontent.com/a/xxx", // Profile picture URL
  "given_name": "Dheeraj",                    // First name
  "iat": 1702285398,                          // Issued at (timestamp)
  "exp": 1702288998                           // Expires at (timestamp)
}
```

---

## 🧠 40LPA Key Insights

### What Each Token Is For

| Token | Type | Purpose | Where Used |
|-------|------|---------|------------|
| **Authorization Code** | Opaque | Exchange for tokens | One-time, expires in seconds |
| **Access Token** | Opaque (Google) | Call Google APIs | Not used in our app |
| **ID Token** | JWT | User identity | Sent to Resource Server |

### Why ID Token, Not Access Token?

```
Google Access Token = "ya29.xxx" (OPAQUE - not JWT!)
  → Cannot be validated by Resource Server
  → Only Google can decode this

Google ID Token = "eyJhbG..." (JWT!)
  → Can be validated by Resource Server
  → Contains user info in payload
  → Signature verified using Google's public keys
```

---

## 📊 Data Available in Spring Security

```java
@GetMapping("/api/user-status")
public Map<String, Object> getUser(@AuthenticationPrincipal OidcUser user) {
    // All this data comes from ID Token!
    user.getSubject();     // "112416036337094439562"
    user.getEmail();       // "dheerajp0299@gmail.com"
    user.getFullName();    // "Dheeraj"
    user.getPicture();     // "https://lh3.googleusercontent.com/..."
    user.getIdToken();     // Full JWT token
    user.getClaims();      // All claims as Map
}
```

---

## 🔒 Security Summary

| Step | Who Sees What |
|------|---------------|
| Authorization Code | Browser URL (but useless alone) |
| Access Token | **Only Backend** (never browser!) |
| ID Token | **Only Backend** (never browser!) |
| Client Secret | **Only Backend** (never browser!) |

**This is why BFF pattern is secure - tokens never reach the browser!**
