# Callback Processing

> 🎓 **Google Callback → Token Exchange**

---

## 🚀 Flow Overview

After user logs in at Google, Google redirects back to our app with an authorization code.

```
┌──────────┐     ┌─────────────┐     ┌─────────────┐
│  Google  │────►│ Client-App  │────►│   Google    │
│  Login   │     │   (BFF)     │     │   Token     │
│  Page    │     │             │     │   Endpoint  │
└──────────┘     └─────────────┘     └─────────────┘
   User          Receives code       Exchanges for
   approves      + validates         tokens
```

---

## 📍 Step-by-Step Breakdown

### STEP 1: Google Redirects Back with Authorization Code

After user approves, Google redirects to our callback URL:

```http
GET /login/oauth2/code/google?
    code=4/0AQSTgQF8Jhq7kN2X...&
    state=XMwoJXG0prWX90z0lh0Az4JaGEqgzWZuwmBr2U5trHA&
    scope=email+profile+openid&
    authuser=0&
    prompt=none
HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=440421F8...
```

From our logs:
```
🚀 REQUEST #14 - GET /login/oauth2/code/google
════════════════════════════════════════════════════════════════════════════════

📥 STEP 1: REQUEST VACHINDI (Incoming Request)
────────────────────────────────────────────────────────────────────────────────
   🌐 URI: /login/oauth2/code/google
   📝 Method: GET
   📎 Query Params: code=4/0AQSTg...&state=XMwoJXG0...&scope=...

   🎫 AUTHORIZATION CODE FOUND!
   💡 Idi Google ichina 'Temporary Pass' - idi tokens ki exchange avtundi
   
   🔐 STATE PARAMETER FOUND!
   💡 Idi CSRF protection kosam - manam pampina state match avtundo check chestaru
```

---

### STEP 2: OAuth2LoginAuthenticationFilter Intercepts

```
⚙️ STEP 4: EE REQUEST NI EVARU HANDLE CHESTARU?
────────────────────────────────────────────────────────────────────────────────
   🎯 TARGET: OAuth2LoginAuthenticationFilter
   📍 LOCATION: Spring Security internal filter
   
   🔥 EM JARUGUTUNDI:
      1️⃣ URL lo state parameter extract chestundi
      2️⃣ Session lo saved state tho compare chestundi (CSRF check)
      3️⃣ Authorization code extract chestundi
      4️⃣ Google Token Endpoint ki POST request chestundi:
         - URL: https://oauth2.googleapis.com/token
         - Body: client_id, client_secret, code, redirect_uri
      5️⃣ Google nundi tokens receive chestundi:
         - Access Token (opaque)
         - ID Token (JWT!)
      6️⃣ Tokens ni session lo save chestundi
      7️⃣ OidcUser object create chestundi
      8️⃣ 302 Redirect istundi → React ki vellipothav
```

---

### STEP 3: State Validation (CSRF Check)

```java
// Internal validation:
String sessionState = session.getAttribute("oauth2_auth_request").getState();
String returnedState = request.getParameter("state");

if (!sessionState.equals(returnedState)) {
    throw new OAuth2AuthenticationException("State mismatch - CSRF attack!");
}
// ✅ State matches - proceed with token exchange
```

---

### STEP 4: Token Exchange (Server-to-Server Call)

**This is the CRITICAL step - invisible to browser!**

From our TokenResponseLoggerConfig logs:

```
╔══════════════════════════════════════════════════════════════════════════════╗
║     🔥 GOOGLE TOKEN EXCHANGE - SERVER-TO-SERVER CALL                        ║
║     (Idi Browser Network tab lo kanipinchadu!)                              ║
╚══════════════════════════════════════════════════════════════════════════════╝

📤 STEP 1: REQUEST TO GOOGLE TOKEN ENDPOINT
────────────────────────────────────────────────────────────────────────────────
   🌐 URL: https://oauth2.googleapis.com/token
   📝 Method: POST
   📋 Content-Type: application/x-www-form-urlencoded

   📦 REQUEST BODY (form data):
   ┌─────────────────────────────────────────────────────────────────────────┐
   │ grant_type      = authorization_code                                   │
   │ code            = 4/0AQSTgQF8Jhq7kN2X...                               │
   │ redirect_uri    = http://localhost:8080/login/oauth2/code/google       │
   │ client_id       = 450472639030-g4g6r5...                               │
   │ client_secret   = ******** (SECRET - never log this fully!)            │
   └─────────────────────────────────────────────────────────────────────────┘

⏳ Calling Google Token Endpoint...
```

---

### STEP 5: Google Returns Tokens

```
📥 STEP 2: RESPONSE FROM GOOGLE (Token Exchange Successful!)
────────────────────────────────────────────────────────────────────────────────

   ✅ GOOGLE RESPONSE RECEIVED!

   📦 RESPONSE DATA:
   ┌─────────────────────────────────────────────────────────────────────────┐
   │ 1️⃣ ACCESS TOKEN (Opaque - for Google APIs only)                        │
   │    Value: ya29.a0ARW5m7...                                              │
   │    Type:  Bearer                                                        │
   │    Expires In: 3599 seconds                                             │
   │    Scopes: [openid, email, profile]                                     │
   │                                                                         │
   │ 2️⃣ ID TOKEN (JWT! - This is what we use for Resource Server)          │
   │    ⭐ THIS IS THE IMPORTANT ONE FOR OUR USE CASE!                       │
   │    Value: eyJhbGciOiJSUzI1NiIsImtpZCI6ImQ1NDNl...                      │
   │    Format: header.payload.signature (JWT)                               │
   │    ✓ Valid JWT with 3 parts                                             │
   │    Header: {"alg":"RS256","kid":"d543e21a0273efc..."}                  │
   │                                                                         │
   │ 3️⃣ REFRESH TOKEN: Not provided (need access_type=offline)             │
   └─────────────────────────────────────────────────────────────────────────┘

╔══════════════════════════════════════════════════════════════════════════════╗
║     ✅ TOKEN EXCHANGE COMPLETE - Tokens will be saved in HTTP Session       ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

### STEP 6: Create OidcUser from ID Token

```java
// Internal processing:
// 1. Decode ID Token (JWT)
String idToken = tokenResponse.get("id_token");
JWT jwt = decode(idToken);

// 2. Extract claims
Map<String, Object> claims = jwt.getClaims();
// claims = {
//     sub: "112416036337094439562",
//     name: "Dheeraj",
//     email: "dheerajp0299@gmail.com",
//     picture: "https://lh3.googleusercontent.com/...",
//     email_verified: true,
//     iss: "https://accounts.google.com",
//     iat: 1733823205,
//     exp: 1733826805
// }

// 3. Create OidcUser object
OidcUser user = new DefaultOidcUser(
    authorities,      // [OIDC_USER, SCOPE_email, SCOPE_profile, SCOPE_openid]
    new OidcIdToken(idToken, issuedAt, expiresAt, claims),
    "sub"            // Name attribute
);
```

---

### STEP 7: Save to Session

Two things are saved:

```java
// 1. SecurityContext (contains user)
SecurityContext context = new SecurityContextImpl();
context.setAuthentication(new OAuth2AuthenticationToken(
    user,           // OidcUser
    authorities,    // [OIDC_USER, SCOPE_email, ...]
    "google"        // Registration ID
));
session.setAttribute("SPRING_SECURITY_CONTEXT", context);

// 2. AuthorizedClient (contains tokens)
OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(
    registration,   // ClientRegistration "google"
    user.getName(), // Principal name
    accessToken,    // Access Token
    refreshToken    // Refresh Token (null in our case)
);
session.setAttribute("oauth2AuthorizedClient_google", client);
```

---

### STEP 8: Redirect to React Dashboard

From logs:
```
📤 STEP 5: RESPONSE READY (Filter chain complete)
────────────────────────────────────────────────────────────────────────────────
   📊 Status Code: 302
   🔄 REDIRECT HAPPENING!
   📍 Location: http://localhost:5173/dashboard
   💡 React UI ki redirect avtunnaav!
   🎉 LOGIN SUCCESSFUL - React app lo dashboard chustav!
```

**HTTP Response:**
```http
HTTP/1.1 302 Found
Location: http://localhost:5173/dashboard
Set-Cookie: JSESSIONID=440421F8...; Path=/; HttpOnly
```

---

## 📊 Complete Token Exchange Request/Response

### Request to Google:

```http
POST https://oauth2.googleapis.com/token HTTP/1.1
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&
code=4/0AQSTgQF8Jhq7kN2X...&
redirect_uri=http://localhost:8080/login/oauth2/code/google&
client_id=450472639030-g4g6r5terpsr6i9eo5bfhmfedcf33387.apps.googleusercontent.com&
client_secret=${GOOGLE_CLIENT_SECRET}
```

### Response from Google:

```json
{
    "access_token": "ya29.a0ARW5m7...",
    "expires_in": 3599,
    "scope": "openid https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile",
    "token_type": "Bearer",
    "id_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6ImQ1NDNlMjFhMDI3M2VmYz..."
}
```

---

## 📋 Session State After Login

```
SESSION {
    id: "440421F8...",
    
    // Key 1: User data and authentication
    "SPRING_SECURITY_CONTEXT": SecurityContextImpl {
        authentication: OAuth2AuthenticationToken {
            principal: OidcUser {
                sub: "112416036337094439562",
                name: "Dheeraj",
                email: "dheerajp0299@gmail.com",
                picture: "https://lh3.googleusercontent.com/...",
                idToken: OidcIdToken { tokenValue: "eyJhbG..." }
            },
            authorities: [OIDC_USER, SCOPE_email, SCOPE_profile, SCOPE_openid],
            authenticated: true
        }
    },
    
    // Key 2: Tokens for API calls
    "oauth2AuthorizedClient_google": OAuth2AuthorizedClient {
        accessToken: "ya29.a0ARW5m7...",
        refreshToken: null,
        expiresAt: 2025-12-10T11:13:25Z
    }
}
```

---

## 📋 Summary

| Step | What Happens | Where |
|------|--------------|-------|
| 1 | Google redirects with code | Browser → Server |
| 2 | Filter intercepts | OAuth2LoginAuthenticationFilter |
| 3 | Validate state | Server (CSRF check) |
| 4 | Exchange code for tokens | Server → Google Token Endpoint |
| 5 | Receive tokens | Google → Server |
| 6 | Create OidcUser | Server (decode JWT) |
| 7 | Save to session | Server (SecurityContext + Tokens) |
| 8 | Redirect to React | Server → Browser |

---

> 📖 **Next:** [04-token-deep-dive.md](./04-token-deep-dive.md) - Access Token vs ID Token (JWT)
