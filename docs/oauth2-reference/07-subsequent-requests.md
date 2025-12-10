# Subsequent Requests Flow

> 🎓 **Session Retrieval and Data Injection**

---

## 🔄 After Login - What Happens on Each Request?

Once user is logged in, every subsequent request follows this flow:

```
Browser ──► Client Backend ──► Controller
   │              │
   │ JSESSIONID   │ Load from
   │ Cookie       │ Session
   └──────────────┘
```

---

## 📍 Step-by-Step: GET /api/user-status

### STEP 1: Browser Sends Request

```http
GET /api/user-status HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=440421F81FB0CD37C26E757DADE4CBAB
Origin: http://localhost:5174
```

From logs:
```
🚀 REQUEST #10 - GET /api/user-status
════════════════════════════════════════════════════════════════════════════════

📥 STEP 1: REQUEST VACHINDI (Incoming Request)
────────────────────────────────────────────────────────────────────────────────
   🌐 URI: /api/user-status
   📝 Method: GET

📦 STEP 2: SESSION CHECK (Server-side storage)
────────────────────────────────────────────────────────────────────────────────
   ✅ Session EXISTS
   🆔 Session ID: 440421F8...
   ⏰ Created: Wed Dec 10 15:40:30 IST 2025
```

---

### STEP 2: SecurityContextHolderFilter Loads User

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  SecurityContextHolderFilter                                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1. Get JSESSIONID from cookie:                                             │
│     Cookie: JSESSIONID=440421F81FB0CD37C26E757DADE4CBAB                    │
│                                                                              │
│  2. Find session in server memory:                                          │
│     HttpSession session = sessionManager.findSession("440421F8...")         │
│     ✅ Found!                                                               │
│                                                                              │
│  3. Load SecurityContext from session:                                      │
│     SecurityContext context = (SecurityContext)                             │
│         session.getAttribute("SPRING_SECURITY_CONTEXT");                    │
│     ✅ Found! Contains OAuth2AuthenticationToken                            │
│                                                                              │
│  4. Set in SecurityContextHolder:                                           │
│     SecurityContextHolder.setContext(context);                              │
│                                                                              │
│  Now this thread has access to the authenticated user!                      │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

From logs:
```
DEBUG FilterChainProxy : Securing GET /api/user-status
TRACE OAuth2LoginAuthenticationFilter : Did not match request to Ant [pattern='/login/oauth2/code/*']
DEBUG FilterChainProxy : Secured GET /api/user-status

DEBUG HttpSessionSecurityContextRepository : Retrieved SecurityContextImpl [
    Authentication=OAuth2AuthenticationToken [
        Principal=Name: [112416036337094439562], 
        Granted Authorities: [[OIDC_USER, SCOPE_email, SCOPE_profile, SCOPE_openid]], 
        User Attributes: [{
            sub=112416036337094439562, 
            name=Dheeraj, 
            email=dheerajp0299@gmail.com, 
            picture=https://lh3.googleusercontent.com/...
        }], 
        Authenticated=true
    ]
]
```

---

### STEP 3: Controller Receives Request

```java
@GetMapping("/api/user-status")
public Map<String, Object> getUserStatus(
    @AuthenticationPrincipal OidcUser user  // ← Injected from session!
) {
    Map<String, Object> response = new HashMap<>();
    
    if (user != null) {
        response.put("authenticated", true);
        response.put("name", user.getFullName());           // "Dheeraj"
        response.put("email", user.getEmail());             // "dheerajp0299@gmail.com"
        response.put("picture", user.getPicture());         // "https://lh3..."
        response.put("subject", user.getSubject());         // "112416036..."
    } else {
        response.put("authenticated", false);
    }
    
    return response;
}
```

---

### STEP 4: How @AuthenticationPrincipal Works

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  @AuthenticationPrincipal OidcUser user                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1. Spring sees @AuthenticationPrincipal annotation                         │
│                                                                              │
│  2. Gets current authentication:                                            │
│     Authentication auth = SecurityContextHolder                             │
│         .getContext()                                                       │
│         .getAuthentication();                                               │
│                                                                              │
│  3. Extracts principal:                                                     │
│     Object principal = auth.getPrincipal();                                 │
│     // principal is OidcUser                                               │
│                                                                              │
│  4. Casts and injects:                                                      │
│     OidcUser user = (OidcUser) principal;                                  │
│     // Now available in your controller method!                             │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           BROWSER                                            │
│  Cookie: JSESSIONID=440421F8...                                             │
└───────────────────────────────────┬─────────────────────────────────────────┘
                                    │
                            GET /api/user-status
                            Cookie: JSESSIONID=440421F8...
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      CLIENT BACKEND (Port 8080)                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Filter Pipeline:                                                            │
│  ────────────────                                                            │
│                                                                              │
│  1. OAuth2FlowLogger (our custom)                                           │
│     └── Log request details                                                 │
│                                                                              │
│  2. SecurityContextHolderFilter                                             │
│     ├── Extract JSESSIONID from cookie                                      │
│     ├── Find session in memory                                              │
│     ├── Load SecurityContext                                                │
│     └── Set in SecurityContextHolder                                        │
│                                                                              │
│  3. OAuth2AuthorizationRequestRedirectFilter                                │
│     └── Skip (not /oauth2/authorization/*)                                  │
│                                                                              │
│  4. OAuth2LoginAuthenticationFilter                                         │
│     └── Skip (not /login/oauth2/code/*)                                    │
│                                                                              │
│  5. AuthorizationFilter                                                     │
│     └── Check: Is user authenticated? ✅ Pass                               │
│                                                                              │
│  Controller:                                                                 │
│  ───────────                                                                 │
│                                                                              │
│  @GetMapping("/api/user-status")                                            │
│  getUserStatus(@AuthenticationPrincipal OidcUser user)                      │
│                                    │                                         │
│                                    │ Spring injects from                     │
│                                    │ SecurityContextHolder                   │
│                                    ▼                                         │
│  OidcUser {                                                                  │
│      name: "Dheeraj",                                                       │
│      email: "dheerajp0299@gmail.com",                                       │
│      picture: "https://lh3...",                                             │
│      subject: "112416036337094439562"                                       │
│  }                                                                           │
│                                                                              │
└───────────────────────────────────┬─────────────────────────────────────────┘
                                    │
                            HTTP 200 OK
                            {"authenticated": true, "name": "Dheeraj", ...}
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           BROWSER                                            │
│  Display user info on dashboard                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📖 @RegisteredOAuth2AuthorizedClient Usage

For getting tokens (not just user info):

```java
@GetMapping("/api/photos")
public String getPhotos(
    @AuthenticationPrincipal OidcUser user,  // User data
    @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client  // Tokens
) {
    // Get user info
    String userName = user.getFullName();
    
    // Get ID Token (JWT) for Resource Server
    String idToken = user.getIdToken().getTokenValue();
    // "eyJhbGciOiJSUzI1NiIs..."
    
    // Get Access Token (for Google APIs - we don't use this in our BFF)
    String accessToken = client.getAccessToken().getTokenValue();
    // "ya29.a0ARW5m7..."
    
    // Call Resource Server with ID Token
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(idToken);
    // ...
}
```

---

## 📊 Logs Summary

```
🚀 REQUEST #10 - GET /api/user-status
════════════════════════════════════════════════════════════════════════════════

📦 STEP 2: SESSION CHECK (Server-side storage)
────────────────────────────────────────────────────────────────────────────────
   ✅ Session EXISTS
   🆔 Session ID: 440421F8...
   ⏰ Created: Wed Dec 10 15:40:30 IST 2025

DEBUG FilterChainProxy        : Securing GET /api/user-status
TRACE OAuth2LoginAuthFilter   : Did not match request to '/login/oauth2/code/*'
DEBUG FilterChainProxy        : Secured GET /api/user-status

DEBUG HttpSessionSecurityContextRepository : Retrieved SecurityContextImpl [
    Authentication=OAuth2AuthenticationToken [
        Principal=Name: [112416036337094439562]
        Authenticated=true
    ]
]

📤 STEP 5: RESPONSE READY (Filter chain complete)
────────────────────────────────────────────────────────────────────────────────
   📊 Status Code: 200
   ✅ SUCCESS - Data returned!

════════════════════════════════════════════════════════════════════════════════
✅ REQUEST #10 COMPLETE
```

---

## 📋 Summary

| Step | What Happens | Who Does It |
|------|--------------|-------------|
| 1 | Browser sends request with JSESSIONID cookie | Browser |
| 2 | Server finds session by ID | Tomcat |
| 3 | Load SecurityContext from session | SecurityContextHolderFilter |
| 4 | Set in SecurityContextHolder | SecurityContextHolderFilter |
| 5 | Spring injects OidcUser | @AuthenticationPrincipal |
| 6 | Controller uses user data | Your code |

---

> 📖 **Next:** [08-resource-server.md](./08-resource-server.md) - JWT Validation and Protected APIs
