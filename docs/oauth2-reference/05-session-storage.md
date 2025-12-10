# Session Storage Details

> 🎓 **SecurityContext, Session, and How Data Reaches Controller**

---

## 🎯 First: Confusion Clear Chesta

Mawa, 3 different concepts unnayi - mix cheyoddu:

| Term | Type | Role |
|------|------|------|
| `HttpSession` | **Storage** (Locker) | Server RAM lo actual storage |
| `SecurityContextImpl` | **Data Object** (Bag) | What's stored inside session |
| `HttpSessionSecurityContextRepository` | **Manager** (Bank Employee) | Saves/Loads from session |
| `SecurityContextHolder` | **Thread-local cache** | Quick access during request |

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   Think of it like a BANK LOCKER:                                           │
│                                                                              │
│   🏦 Bank Vault (Server RAM) = Where all lockers are                        │
│   🔐 Locker = HttpSession                                                   │
│   🎒 Bag inside locker = SecurityContextImpl (user data)                    │
│   👨‍💼 Bank Employee = HttpSessionSecurityContextRepository (saves/retrieves)  │
│   🔑 Locker Number = JSESSIONID cookie                                      │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔥 Session Transformation: Before & After Login

### BEFORE Login (Empty Session):

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    BEFORE LOGIN (After /logout or new user)                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   HttpSession {                                                              │
│       id: "SOME_SESSION_ID",                                                │
│       created: "Wed Dec 10 17:32:00 IST 2025",                             │
│                                                                              │
│       attributes: {                                                          │
│           // EMPTY! No user data!                                           │
│           // Maybe just CSRF token for forms                                │
│       }                                                                      │
│   }                                                                          │
│                                                                              │
│   SecurityContext = EMPTY (no authenticated user)                           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Google nundi Token Response vachindi:

```json
{
    "access_token": "ya29.A0Aa7pCA_9Yk6zdgqnUwtBSj212...",
    "id_token": "eyJhbGciOiJSUzI1NiIs...",
    "expires_in": 3599,
    "token_type": "Bearer"
}
```

### Transformation Process:

```
GOOGLE RESPONSE
      │
      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  OAuth2LoginAuthenticationFilter processes response:                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1️⃣ DECODE id_token (JWT) → Get user claims:                               │
│     {                                                                        │
│         "sub": "112416036337094439562",                                     │
│         "name": "Dheeraj",                                                  │
│         "email": "dheerajp0299@gmail.com",                                  │
│         "picture": "https://lh3..."                                         │
│     }                                                                        │
│                        │                                                     │
│                        ▼                                                     │
│  2️⃣ CREATE OidcUser object from claims                                     │
│                        │                                                     │
│                        ▼                                                     │
│  3️⃣ WRAP OidcUser in OAuth2AuthenticationToken                             │
│                        │                                                     │
│                        ▼                                                     │
│  4️⃣ WRAP Token in SecurityContextImpl                                      │
│                        │                                                     │
│                        ▼                                                     │
│  5️⃣ HttpSessionSecurityContextRepository.saveContext()                     │
│     → session.setAttribute("SPRING_SECURITY_CONTEXT", context)              │
│                        │                                                     │
│                        ▼                                                     │
│  6️⃣ Also SAVE access_token in OAuth2AuthorizedClient                       │
│     → session.setAttribute("oauth2AuthorizedClient_google", client)         │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### AFTER Login (Session Populated):

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    AFTER LOGIN (Session Filled!)                             │
│                    Real Data from our server:                                │
│                    Session ID: C0C3F7FB4F429F5196056FC1B62C682B             │
│                    Created: Wed Dec 10 17:32:37 IST 2025                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   HttpSession {                                                              │
│       id: "C0C3F7FB4F429F5196056FC1B62C682B",                              │
│                                                                              │
│       attributes: {                                                          │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │  KEY: "SPRING_SECURITY_CONTEXT"                                      │  │
│  │                                                                       │  │
│  │  VALUE: SecurityContextImpl {                                        │  │
│  │      authentication: OAuth2AuthenticationToken {                     │  │
│  │          principal: OidcUser {                                       │  │
│  │              subject: "112416036337094439562",                       │  │
│  │              name: "Dheeraj",                                        │  │
│  │              email: "dheerajp0299@gmail.com",                        │  │
│  │              picture: "https://lh3.googleusercontent.com/...",       │  │
│  │              idToken: OidcIdToken {                                  │  │
│  │                  tokenValue: "eyJhbGciOiJSUzI1NiIs..."              │  │
│  │              }                                                       │  │
│  │          },                                                          │  │
│  │          authorities: [OIDC_USER, SCOPE_email, SCOPE_profile],      │  │
│  │          authenticated: true                                        │  │
│  │      }                                                               │  │
│  │  }                                                                   │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │  KEY: "oauth2AuthorizedClient_google"                                │  │
│  │                                                                       │  │
│  │  VALUE: OAuth2AuthorizedClient {                                     │  │
│  │      accessToken: {                                                  │  │
│  │          value: "ya29.A0Aa7pCA_9Yk6zdgqnUwtBSj212eeNBoIVxVd...",    │  │
│  │          type: "Bearer",                                             │  │
│  │          expiresAt: 2025-12-10T12:32:36Z                            │  │
│  │      },                                                              │  │
│  │      refreshToken: null                                              │  │
│  │  }                                                                   │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│       }                                                                      │
│   }                                                                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔑 How JSESSIONID Works with SecurityContextHolder

**Mawa, idhi important question!**

`SecurityContextHolder` directly JSESSIONID check cheyadu. Process veeru:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    REQUEST PROCESSING FLOW                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   BROWSER sends request:                                                     │
│   GET /api/user-status                                                       │
│   Cookie: JSESSIONID=C0C3F7FB4F429F5196056FC1B62C682B                       │
│                                                                              │
│                         │                                                    │
│                         ▼                                                    │
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │ STEP 1: TOMCAT (Servlet Container)                                  │   │
│   │                                                                      │   │
│   │   "Cookie lo JSESSIONID kanipinchindi!"                             │   │
│   │   "Naa session store lo ee ID tho session undo chusta..."          │   │
│   │                                                                      │   │
│   │   HttpSession session = sessionStore.get("C0C3F7FB...");           │   │
│   │   // Found! This session has attributes                             │   │
│   │                                                                      │   │
│   │   request.session = session;  // Attach to request                  │   │
│   │                                                                      │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                         │                                                    │
│                         ▼                                                    │
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │ STEP 2: SecurityContextHolderFilter                                 │   │
│   │                                                                      │   │
│   │   "Now I'll load SecurityContext from this session..."             │   │
│   │                                                                      │   │
│   │   // Uses HttpSessionSecurityContextRepository internally          │   │
│   │   SecurityContext context =                                         │   │
│   │       session.getAttribute("SPRING_SECURITY_CONTEXT");              │   │
│   │                                                                      │   │
│   │   // Put it in thread-local for quick access                       │   │
│   │   SecurityContextHolder.setContext(context);                        │   │
│   │                                                                      │   │
│   │   // NOW context is available via SecurityContextHolder!           │   │
│   │                                                                      │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                         │                                                    │
│                         ▼                                                    │
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │ STEP 3: Your Controller                                             │   │
│   │                                                                      │   │
│   │   @GetMapping("/api/user-status")                                   │   │
│   │   public String getUser(@AuthenticationPrincipal OidcUser user) {   │   │
│   │       // Spring gets user from SecurityContextHolder               │   │
│   │       return user.getName();  // "Dheeraj"                         │   │
│   │   }                                                                 │   │
│   │                                                                      │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Summary - Who Does What:

| Component | Role |
|-----------|------|
| **Tomcat** | Reads JSESSIONID cookie, finds session object |
| **SecurityContextHolderFilter** | Reads "SPRING_SECURITY_CONTEXT" from session, puts in SecurityContextHolder |
| **SecurityContextHolder** | Thread-local cache - stores context for current request thread |
| **@AuthenticationPrincipal** | Reads from SecurityContextHolder, injects into controller |

---

## 🎯 How Values Reach Controller Annotations

### @AuthenticationPrincipal Flow:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    @AuthenticationPrincipal OidcUser user                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   When your controller method is called:                                     │
│                                                                              │
│   @GetMapping("/api/user")                                                  │
│   public String getUser(@AuthenticationPrincipal OidcUser user) {           │
│       // How does 'user' get populated?                                     │
│   }                                                                          │
│                                                                              │
│   ───────────────────────────────────────────────────────────────────────   │
│                                                                              │
│   STEP 1: Spring sees @AuthenticationPrincipal annotation                   │
│           │                                                                  │
│           ▼                                                                  │
│   STEP 2: AuthenticationPrincipalArgumentResolver is called                 │
│           │                                                                  │
│           ▼                                                                  │
│   STEP 3: It calls SecurityContextHolder.getContext()                       │
│           │                                                                  │
│           ▼                                                                  │
│   STEP 4: Gets Authentication from context                                  │
│           │                                                                  │
│           ▼                                                                  │
│   STEP 5: Extracts Principal from Authentication                            │
│           │                                                                  │
│           ▼                                                                  │
│   STEP 6: Casts to OidcUser and injects into your method                   │
│                                                                              │
│   ───────────────────────────────────────────────────────────────────────   │
│                                                                              │
│   In code:                                                                   │
│                                                                              │
│   SecurityContext context = SecurityContextHolder.getContext();             │
│   Authentication auth = context.getAuthentication();                        │
│   OidcUser user = (OidcUser) auth.getPrincipal();                          │
│   // This 'user' is injected into your controller parameter!               │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### @RegisteredOAuth2AuthorizedClient Flow:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│           @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient│
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   @GetMapping("/api/photos")                                                │
│   public String getPhotos(                                                  │
│       @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient c  │
│   ) {                                                                       │
│       // How does 'c' get populated?                                        │
│   }                                                                          │
│                                                                              │
│   ───────────────────────────────────────────────────────────────────────   │
│                                                                              │
│   STEP 1: Spring sees @RegisteredOAuth2AuthorizedClient annotation          │
│           │                                                                  │
│           ▼                                                                  │
│   STEP 2: OAuth2AuthorizedClientArgumentResolver is called                  │
│           │                                                                  │
│           ▼                                                                  │
│   STEP 3: It uses OAuth2AuthorizedClientRepository (session-backed)        │
│           │                                                                  │
│           ▼                                                                  │
│   STEP 4: Reads from session: getAttribute("oauth2AuthorizedClient_google") │
│           │                                                                  │
│           ▼                                                                  │
│   STEP 5: Returns OAuth2AuthorizedClient to your method                    │
│                                                                              │
│   ───────────────────────────────────────────────────────────────────────   │
│                                                                              │
│   In code (internally):                                                      │
│                                                                              │
│   HttpSession session = request.getSession();                               │
│   Map<String, OAuth2AuthorizedClient> clients =                             │
│       session.getAttribute("...AUTHORIZED_CLIENTS");                        │
│   OAuth2AuthorizedClient client = clients.get("google");                   │
│   // This 'client' is injected into your controller!                       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Complete Picture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              BROWSER                                         │
│   Cookie: JSESSIONID=C0C3F7FB4F429F5196056FC1B62C682B                       │
└─────────────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              TOMCAT                                          │
│   "JSESSIONID dekha, session nikalta hoon..."                               │
│   session = sessionStore.get("C0C3F7FB...")                                 │
└─────────────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     SecurityContextHolderFilter                              │
│   context = session.getAttribute("SPRING_SECURITY_CONTEXT")                 │
│   SecurityContextHolder.setContext(context)   ←── Thread-local lo save     │
└─────────────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          CONTROLLER                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   @GetMapping("/api/user")                                                  │
│   public String getUser(                                                    │
│       @AuthenticationPrincipal OidcUser user,  ◄── From SecurityContextHolder│
│       @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient c  │
│   ) {                                              ▲                        │
│       user.getName();        // "Dheeraj"          │                        │
│       user.getEmail();       // "dheerajp0299@..." │ From Session directly  │
│       user.getIdToken();     // JWT                │                        │
│       c.getAccessToken();    // Access Token ──────┘                        │
│   }                                                                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 Quick Reference

| What You Want | Annotation | Source |
|---------------|------------|--------|
| User name, email, picture | `@AuthenticationPrincipal OidcUser` | SecurityContextHolder |
| ID Token (JWT) | `user.getIdToken()` | Inside OidcUser |
| Access Token | `@RegisteredOAuth2AuthorizedClient` | Session directly |
| Refresh Token | `client.getRefreshToken()` | Session directly |

---

## 📋 Summary Table

| Concept | What It Is | When Used |
|---------|------------|-----------|
| `HttpSession` | Server-side storage (RAM) | Always - stores all data |
| `SecurityContextImpl` | Java object holding user auth | Stored in session |
| `HttpSessionSecurityContextRepository` | Class that saves/loads context | During login & every request |
| `SecurityContextHolder` | Thread-local quick access | During request processing |
| `JSESSIONID` | Cookie linking browser to session | Every request |
| `@AuthenticationPrincipal` | Gets user from SecurityContextHolder | In controller |
| `@RegisteredOAuth2AuthorizedClient` | Gets tokens from session | In controller |

---

> 📖 **Next:** [06-filter-chain.md](./06-filter-chain.md) - All 17 Spring Security Filters
