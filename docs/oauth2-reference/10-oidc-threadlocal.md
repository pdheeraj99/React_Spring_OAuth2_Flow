# OIDC & SecurityContextHolder Deep Dive

> 🎓 **OpenID Connect and Thread-Safe Security Context**

---

## 1️⃣ What is OIDC (OpenID Connect)?

### OAuth2 vs OIDC:

**OAuth2** = Authorization (permission to access resources)
**OpenID Connect (OIDC)** = OAuth2 + **Identity** (who is the user)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   OAuth2 alone:                                                              │
│   ─────────────                                                              │
│   "Here's an access token, you can access Google Drive"                     │
│   ❌ But WHO is the user? No info!                                          │
│                                                                              │
│   OAuth2 + OIDC:                                                            │
│   ──────────────                                                             │
│   "Here's an access token AND an ID Token (JWT) that tells you:            │
│    - sub: 112416036337094439562                                             │
│    - name: Dheeraj                                                          │
│    - email: dheerajp0299@gmail.com                                          │
│    - picture: https://..."                                                  │
│   ✅ Now you know WHO the user is!                                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### OIDC adds these to OAuth2:

| What | Purpose |
|------|---------|
| **ID Token** | JWT containing user identity claims |
| **UserInfo Endpoint** | API to get user details |
| **Standard Claims** | sub, name, email, picture, etc. |
| **Standard Scopes** | openid, profile, email |

---

## 2️⃣ OidcUser in Spring Security

### What is OidcUser?

```java
// OidcUser = User object created from ID Token (JWT) claims
public interface OidcUser extends OAuth2User, IdTokenClaimAccessor {
    
    OidcIdToken getIdToken();      // The actual JWT
    OidcUserInfo getUserInfo();    // User details
    
    // From IdTokenClaimAccessor:
    String getSubject();           // "112416036337094439562"
    String getEmail();             // "dheerajp0299@gmail.com"
    String getFullName();          // "Dheeraj"
    String getPicture();           // Photo URL
    // ... more claims
}
```

### OAuth2User vs OidcUser:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   OAuth2User                          OidcUser                              │
│   ───────────                         ────────                              │
│   - Access Token only                 - Access Token + ID Token (JWT)       │
│   - Basic attributes                  - Full identity claims                │
│   - Needs /userinfo call              - Claims in JWT directly             │
│                                                                              │
│   OAuth2User ────────────────►  Basic user info                             │
│         │                                                                    │
│         │ + ID Token (JWT)                                                  │
│         ▼                                                                    │
│   OidcUser ──────────────────►  Complete user identity!                     │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Our OidcUser (Real Data):

```java
OidcUser {
    // Identity (from ID Token claims)
    subject: "112416036337094439562",      // Unique Google user ID
    fullName: "Dheeraj",                   // Display name
    email: "dheerajp0299@gmail.com",       // Email
    emailVerified: true,                    // Google verified this email
    picture: "https://lh3.googleusercontent.com/...",  // Profile photo
    
    // Token info
    issuer: "https://accounts.google.com", // Who issued this token
    issuedAt: 2025-12-10T10:13:25Z,       // When token was created
    expiresAt: 2025-12-10T11:13:25Z,      // When token expires
    
    // The actual JWT
    idToken: OidcIdToken {
        tokenValue: "eyJhbGciOiJSUzI1NiIs..."  // Full JWT string
    },
    
    // All claims from JWT
    claims: {
        "sub": "112416036337094439562",
        "name": "Dheeraj",
        "email": "dheerajp0299@gmail.com",
        "picture": "https://...",
        "email_verified": true,
        "iss": "https://accounts.google.com",
        "aud": ["450472639030-g4g6r5..."],
        "iat": 1733823205,
        "exp": 1733826805,
        "nonce": "XMwoJXG0prWX90z0...",
        "at_hash": "4aao0hkss3WWo5qauK8wow"
    },
    
    // Permissions
    authorities: [
        "OIDC_USER",                        // OpenID Connect user
        "SCOPE_openid",
        "SCOPE_email",
        "SCOPE_profile"
    ]
}
```

### How to Use in Controller:

#### 1. Get User Identity (`@AuthenticationPrincipal`):

```java
@GetMapping("/api/user")
public Map<String, Object> getUser(@AuthenticationPrincipal OidcUser user) {
    
    // User identity
    String userId = user.getSubject();        // "112416036337094439562"
    String name = user.getFullName();         // "Dheeraj"
    String email = user.getEmail();           // "dheerajp0299@gmail.com"
    String picture = user.getPicture();       // Photo URL
    
    // Get any claim
    Boolean emailVerified = user.getClaim("email_verified");  // true
    
    // Get the JWT for Resource Server calls
    String jwt = user.getIdToken().getTokenValue();
    
    return Map.of(
        "id", userId,
        "name", name,
        "email", email,
        "picture", picture
    );
}
```

#### 2. Get OAuth Tokens (`@RegisteredOAuth2AuthorizedClient`):

```java
@GetMapping("/api/tokens")
public Map<String, Object> getTokens(
    @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client
) {
    // Access Token (opaque - for Google APIs)
    OAuth2AccessToken accessToken = client.getAccessToken();
    String tokenValue = accessToken.getTokenValue();      // "ya29.a0ARW5m7..."
    String tokenType = accessToken.getTokenType().getValue();  // "Bearer"
    Instant expiresAt = accessToken.getExpiresAt();       // 2025-12-10T12:32:36Z
    Set<String> scopes = accessToken.getScopes();         // [openid, email, profile]
    
    // Refresh Token (if requested)
    OAuth2RefreshToken refreshToken = client.getRefreshToken();  // null in our case
    
    // Client Registration info
    ClientRegistration registration = client.getClientRegistration();
    String clientId = registration.getClientId();         // "450472639030-..."
    String registrationId = registration.getRegistrationId();  // "google"
    
    return Map.of(
        "accessToken", tokenValue.substring(0, 30) + "...",
        "tokenType", tokenType,
        "scopes", scopes,
        "expiresAt", expiresAt.toString()
    );
}
```

#### 3. Use Both Together (Common Pattern):

```java
@GetMapping("/api/photos")
public String getPhotos(
    @AuthenticationPrincipal OidcUser user,  // User identity + ID Token
    @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client  // Access Token
) {
    // Log who is making the request
    log.info("User {} is fetching photos", user.getFullName());
    
    // Get ID Token (JWT) for our Resource Server
    String idToken = user.getIdToken().getTokenValue();
    // Use this to call: Authorization: Bearer <idToken>
    
    // Get Access Token for Google APIs (if needed)
    String accessToken = client.getAccessToken().getTokenValue();
    // Use this to call Google Drive, Gmail, etc.
    
    // Call Resource Server with ID Token
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(idToken);  // ID Token for our RS!
    
    return restTemplate.exchange(
        "http://localhost:8081/photos",
        HttpMethod.GET,
        new HttpEntity<>(headers),
        String.class
    ).getBody();
}
```

### Where Each Token Comes From:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   @AuthenticationPrincipal OidcUser user                                    │
│   ─────────────────────────────────────                                      │
│   Source: SecurityContextHolder (ThreadLocal)                               │
│                                                                              │
│   Contains:                                                                  │
│   ├── User identity (name, email, picture)                                  │
│   └── ID Token (JWT) ← for OUR Resource Server                             │
│                                                                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client │
│   ──────────────────────────────────────────────────────────────────────────│
│   Source: HttpSession directly (oauth2AuthorizedClient_google key)          │
│                                                                              │
│   Contains:                                                                  │
│   ├── Access Token (opaque) ← for GOOGLE APIs (Drive, Gmail)               │
│   └── Refresh Token ← for getting new tokens (if requested)                │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### When to Use Which:

| Annotation | Use When |
|------------|----------|
| `@AuthenticationPrincipal OidcUser` | Need user info OR calling **our** Resource Server |
| `@RegisteredOAuth2AuthorizedClient` | Calling **Google** APIs (Drive, Gmail, Calendar) |
| Both together | Need user context AND external API calls |



## 3️⃣ SecurityContextHolder & ThreadLocal

### Why ThreadLocal?

Server lo multiple users simultaneously requests pathinchochu. Thread-safety kavali!

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         SERVER (Tomcat)                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Thread Pool (e.g., 200 threads)                                           │
│                                                                              │
│   ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐           │
│   │ Thread-1        │  │ Thread-2        │  │ Thread-3        │           │
│   │                 │  │                 │  │                 │           │
│   │ ThreadLocal:    │  │ ThreadLocal:    │  │ ThreadLocal:    │           │
│   │ ┌─────────────┐ │  │ ┌─────────────┐ │  │ ┌─────────────┐ │           │
│   │ │SecurityCtx  │ │  │ │SecurityCtx  │ │  │ │SecurityCtx  │ │           │
│   │ │User: Dheeraj│ │  │ │User: Ravi   │ │  │ │User: Kumar  │ │           │
│   │ └─────────────┘ │  │ └─────────────┘ │  │ └─────────────┘ │           │
│   │                 │  │                 │  │                 │           │
│   │ Processing:     │  │ Processing:     │  │ Processing:     │           │
│   │ GET /api/photos │  │ GET /api/user   │  │ POST /api/data  │           │
│   │                 │  │                 │  │                 │           │
│   └─────────────────┘  └─────────────────┘  └─────────────────┘           │
│                                                                              │
│   ✅ Each thread has its OWN SecurityContext!                               │
│   ✅ Thread-1 cannot see Thread-2's user!                                   │
│   ✅ No data leakage between users!                                         │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Problem Without ThreadLocal:

```
┌──────────────────────────────────────┐
│  Static variable (shared):           │
│                                      │
│  static User currentUser;            │
│                                      │
│  Thread-1 sets: currentUser = Dheeraj│
│  Thread-2 sets: currentUser = Ravi   │  ← OVERWRITES!
│                                      │
│  Thread-1 reads: currentUser = Ravi  │  ← WRONG USER!
│                                      │
│  ❌ Race condition! User sees        │
│     someone else's data! DISASTER!   │
└──────────────────────────────────────┘
```

### Solution With ThreadLocal:

```
┌──────────────────────────────────────┐
│  ThreadLocal<User> currentUser;      │
│                                      │
│  Thread-1: currentUser.set(Dheeraj)  │  → Thread-1's box
│  Thread-2: currentUser.set(Ravi)     │  → Thread-2's box
│                                      │
│  Thread-1: currentUser.get() = Dheeraj ✅
│  Thread-2: currentUser.get() = Ravi    ✅
│                                      │
│  ✅ Each thread is isolated!         │
│  ✅ No data mixing!                  │
└──────────────────────────────────────┘
```

---

## 4️⃣ SecurityContextHolder Internals

### How It Works:

```java
public class SecurityContextHolder {
    
    // ThreadLocal - each thread gets its own copy!
    private static final ThreadLocal<SecurityContext> contextHolder = 
        new ThreadLocal<>();
    
    // Save context for current thread
    public static void setContext(SecurityContext context) {
        contextHolder.set(context);  // Save in THIS thread's storage
    }
    
    // Get context for current thread
    public static SecurityContext getContext() {
        SecurityContext ctx = contextHolder.get();
        if (ctx == null) {
            ctx = createEmptyContext();
            contextHolder.set(ctx);
        }
        return ctx;  // Get from THIS thread's storage
    }
    
    // Clean up after request
    public static void clearContext() {
        contextHolder.remove();  // Remove from THIS thread's storage
    }
}
```

### Request Lifecycle:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  Request from Dheeraj:                                                       │
│  Cookie: JSESSIONID=ABC123                                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1️⃣ Request arrives → Tomcat assigns Thread-5                              │
│     Thread-5's ThreadLocal is EMPTY at this point                           │
│                                                                              │
│  2️⃣ SecurityContextHolderFilter runs:                                      │
│     ┌────────────────────────────────────────────────────────────────────┐ │
│     │ // Load from session using JSESSIONID                              │ │
│     │ HttpSession session = getSession("ABC123");                        │ │
│     │ SecurityContext context =                                          │ │
│     │     session.getAttribute("SPRING_SECURITY_CONTEXT");               │ │
│     │                                                                    │ │
│     │ // Store in Thread-5's ThreadLocal                                │ │
│     │ SecurityContextHolder.setContext(context);                         │ │
│     └────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│  3️⃣ Controller executes:                                                   │
│     ┌────────────────────────────────────────────────────────────────────┐ │
│     │ @GetMapping("/api/user")                                           │ │
│     │ public String getUser(@AuthenticationPrincipal OidcUser user) {    │ │
│     │     // Spring internally does:                                     │ │
│     │     // SecurityContext ctx = SecurityContextHolder.getContext();   │ │
│     │     // OidcUser user = (OidcUser) ctx.getAuthentication()         │ │
│     │     //                              .getPrincipal();               │ │
│     │                                                                    │ │
│     │     return user.getName();  // "Dheeraj" from Thread-5's storage  │ │
│     │ }                                                                  │ │
│     └────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│  4️⃣ Response sent to browser                                               │
│                                                                              │
│  5️⃣ SecurityContextHolderFilter cleanup (after response):                  │
│     ┌────────────────────────────────────────────────────────────────────┐ │
│     │ // Clear Thread-5's ThreadLocal                                   │ │
│     │ SecurityContextHolder.clearContext();                              │ │
│     │                                                                    │ │
│     │ // Thread-5 is now clean and ready for next request              │ │
│     │ // (could be a different user)                                    │ │
│     └────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│  6️⃣ Thread-5 returns to pool, ready for next user's request               │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 5️⃣ Visual Summary

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          THE COMPLETE PICTURE                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   PERMANENT STORAGE: HttpSession (Server RAM)                               │
│   ─────────────────────────────────────────────                              │
│   ┌───────────────────────────────────────────────────────────────────┐    │
│   │  Session ABC123                      Session XYZ789              │    │
│   │  ┌─────────────────────┐            ┌─────────────────────┐     │    │
│   │  │ User: Dheeraj       │            │ User: Ravi          │     │    │
│   │  │ Tokens: {...}       │            │ Tokens: {...}       │     │    │
│   │  └─────────────────────┘            └─────────────────────┘     │    │
│   └───────────────────────────────────────────────────────────────────┘    │
│                    │                               │                        │
│        ┌───────────┘                               └───────────┐            │
│        │ Load via JSESSIONID                                   │            │
│        ▼                                                       ▼            │
│   TEMPORARY STORAGE: ThreadLocal (Per Request)                              │
│   ───────────────────────────────────────────                                │
│   ┌─────────────────────┐            ┌─────────────────────┐               │
│   │ Thread-1            │            │ Thread-2            │               │
│   │ SecurityContextHolder│           │ SecurityContextHolder│              │
│   │ ┌─────────────────┐ │           │ ┌─────────────────┐ │               │
│   │ │ ctx: Dheeraj    │ │           │ │ ctx: Ravi       │ │               │
│   │ └─────────────────┘ │           │ └─────────────────┘ │               │
│   └─────────────────────┘            └─────────────────────┘               │
│        │                                     │                              │
│        ▼                                     ▼                              │
│   ┌─────────────────────┐            ┌─────────────────────┐               │
│   │ Controller          │            │ Controller          │               │
│   │ @AuthenticationPrincipal         │ @AuthenticationPrincipal            │
│   │ OidcUser = Dheeraj  │            │ OidcUser = Ravi     │               │
│   └─────────────────────┘            └─────────────────────┘               │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 Quick Reference

| Concept | What It Is | Lifespan |
|---------|------------|----------|
| **HttpSession** | Permanent storage in server RAM | Until logout/timeout |
| **ThreadLocal/SecurityContextHolder** | Temporary per-request cache | Single request only |
| **OidcUser** | User object from ID Token | Stored in both |
| **ID Token** | JWT with user identity | ~1 hour |

| Annotation | Gets From | Contains |
|------------|-----------|----------|
| `@AuthenticationPrincipal OidcUser` | SecurityContextHolder (ThreadLocal) | User identity, ID Token |
| `@RegisteredOAuth2AuthorizedClient` | HttpSession directly | Access Token, Refresh Token |

---

## 📋 Key Takeaways

1. **OIDC** = OAuth2 + Identity. Gives us ID Token (JWT) with user info.

2. **OidcUser** = Spring's representation of OIDC user with all claims.

3. **ThreadLocal** = Each thread has isolated storage. Thread-1 can't see Thread-2's data.

4. **SecurityContextHolder** = Uses ThreadLocal internally for thread-safe user access.

5. **Lifecycle**:
   - Session = Permanent (until logout)
   - ThreadLocal = Per-request (cleared after response)

---

> 📖 **Related:** [05-session-storage.md](./05-session-storage.md) - How Session Storage Works
