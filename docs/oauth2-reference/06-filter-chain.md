# Filter Chain Deep Dive

> 🎓 **All 17 Spring Security Filters**

---

## 🔗 What is Filter Chain?

Every HTTP request passes through a **chain of filters** before reaching your controller.

```
REQUEST ──► Filter 1 ──► Filter 2 ──► ... ──► Filter 17 ──► Controller
                                                                │
RESPONSE ◄── Filter 1 ◄── Filter 2 ◄── ... ◄── Filter 17 ◄─────┘
```

---

## 📊 Complete Filter Chain (From Logs)

From our server startup logs:

```
DefaultSecurityFilterChain : Will secure any request with filters: 
  DisableEncodeUrlFilter, 
  WebAsyncManagerIntegrationFilter, 
  SecurityContextHolderFilter, 
  HeaderWriterFilter, 
  CorsFilter, 
  CsrfFilter, 
  LogoutFilter, 
  OAuth2AuthorizationRequestRedirectFilter, 
  OAuth2LoginAuthenticationFilter, 
  DefaultResourcesFilter, 
  DefaultLoginPageGeneratingFilter, 
  DefaultLogoutPageGeneratingFilter, 
  RequestCacheAwareFilter, 
  SecurityContextHolderAwareRequestFilter, 
  AnonymousAuthenticationFilter, 
  ExceptionTranslationFilter, 
  AuthorizationFilter
```

---

## 📋 Filter Descriptions

| # | Filter | Purpose |
|---|--------|---------|
| 1 | `DisableEncodeUrlFilter` | Disables URL encoding of Session ID |
| 2 | `WebAsyncManagerIntegrationFilter` | Async request support |
| 3 | **`SecurityContextHolderFilter`** | **Load user from session** |
| 4 | `HeaderWriterFilter` | Add security headers |
| 5 | `CorsFilter` | Handle CORS requests |
| 6 | `CsrfFilter` | CSRF protection |
| 7 | `LogoutFilter` | Handle /logout |
| 8 | **`OAuth2AuthorizationRequestRedirectFilter`** | **Redirect to Google** |
| 9 | **`OAuth2LoginAuthenticationFilter`** | **Handle callback, exchange tokens** |
| 10 | `DefaultResourcesFilter` | Serve static resources |
| 11 | `DefaultLoginPageGeneratingFilter` | Generate login page |
| 12 | `DefaultLogoutPageGeneratingFilter` | Generate logout page |
| 13 | `RequestCacheAwareFilter` | Restore saved request |
| 14 | `SecurityContextHolderAwareRequestFilter` | Wrap request |
| 15 | `AnonymousAuthenticationFilter` | Set anonymous auth if none |
| 16 | `ExceptionTranslationFilter` | Handle security exceptions |
| 17 | `AuthorizationFilter` | Check permissions |

---

## 🔥 Key Filters for OAuth2

### 1. SecurityContextHolderFilter (Position 3)

**When:** Every request (before processing)

**Purpose:** Load authenticated user from session

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  SecurityContextHolderFilter                                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1. Extract JSESSIONID from Cookie header                                   │
│     Cookie: JSESSIONID=440421F81FB0CD37C26E757DADE4CBAB                    │
│                                                                              │
│  2. Find session in server memory                                           │
│     session = sessionRepository.findById("440421F8...")                     │
│                                                                              │
│  3. Load SecurityContext from session                                       │
│     context = session.getAttribute("SPRING_SECURITY_CONTEXT")               │
│                                                                              │
│  4. Set in SecurityContextHolder (thread-local)                             │
│     SecurityContextHolder.setContext(context)                               │
│                                                                              │
│  5. Now EVERYWHERE in this request can access user:                         │
│     Authentication auth = SecurityContextHolder.getContext().getAuth();     │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

From logs:
```
HttpSessionSecurityContextRepository : Retrieved SecurityContextImpl [
    Authentication=OAuth2AuthenticationToken [
        Principal=Name: [112416036337094439562], 
        Granted Authorities: [[OIDC_USER, SCOPE_email, ...]], 
        Authenticated=true
    ]
]
```

---

### 2. OAuth2AuthorizationRequestRedirectFilter (Position 8)

**When:** Request to `/oauth2/authorization/{provider}`

**Purpose:** Redirect to Google login

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  OAuth2AuthorizationRequestRedirectFilter                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  URL Match: /oauth2/authorization/google                                    │
│                                                                              │
│  Actions:                                                                    │
│  1. Generate state (CSRF protection)                                        │
│  2. Generate nonce (replay protection)                                      │
│  3. Save authorization request in session                                   │
│  4. Build Google authorization URL                                          │
│  5. Send 302 redirect to Google                                             │
│                                                                              │
│  Result:                                                                     │
│  HTTP 302                                                                    │
│  Location: https://accounts.google.com/o/oauth2/v2/auth?...                 │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

From logs:
```
📤 STEP 5: RESPONSE READY (Filter chain complete)
   📊 Status Code: 302
   🔄 REDIRECT HAPPENING!
   📍 Location: https://accounts.google.com/o/oauth2/v2/auth?...
```

---

### 3. OAuth2LoginAuthenticationFilter (Position 9)

**When:** Request to `/login/oauth2/code/{provider}`

**Purpose:** Handle Google callback, exchange code for tokens

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  OAuth2LoginAuthenticationFilter                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  URL Match: /login/oauth2/code/google?code=...&state=...                   │
│                                                                              │
│  Actions:                                                                    │
│  1. Extract code and state from URL                                         │
│  2. Validate state against session (CSRF check)                             │
│  3. POST to Google token endpoint                                           │
│     → Exchange code for tokens                                              │
│  4. Receive tokens (Access Token, ID Token)                                 │
│  5. Decode ID Token → Create OidcUser                                       │
│  6. Create OAuth2AuthenticationToken                                        │
│  7. Save SecurityContext to session                                         │
│  8. Save AuthorizedClient to session                                        │
│  9. Redirect to success URL (/dashboard)                                    │
│                                                                              │
│  Result:                                                                     │
│  HTTP 302                                                                    │
│  Location: http://localhost:5173/dashboard                                  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

From logs:
```
🎯 TARGET: OAuth2LoginAuthenticationFilter
📤 REDIRECT TO: http://localhost:5173/dashboard

🔥 EM JARUGUTUNDI:
   1️⃣ URL lo state parameter extract chestundi
   2️⃣ Session lo saved state tho compare chestundi (CSRF check)
   3️⃣ Authorization code extract chestundi
   4️⃣ Google Token Endpoint ki POST request chestundi
   5️⃣ Google nundi tokens receive chestundi
   6️⃣ Tokens ni session lo save chestundi
   7️⃣ OidcUser object create chestundi
   8️⃣ 302 Redirect istundi → React ki vellipothav
```

---

## 📊 Filter Flow for Different Requests

### Request: GET /api/photos (Authenticated)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│  Filters activated:                                                          │
│                                                                              │
│  SecurityContextHolderFilter                                                │
│  ├── Load user from session ✅                                              │
│  │                                                                           │
│  OAuth2AuthorizationRequestRedirectFilter                                   │
│  ├── URL match? /oauth2/authorization/*? ❌ SKIP                           │
│  │                                                                           │
│  OAuth2LoginAuthenticationFilter                                            │
│  ├── URL match? /login/oauth2/code/*? ❌ SKIP                              │
│  │                                                                           │
│  AnonymousAuthenticationFilter                                              │
│  ├── Already authenticated? ✅ SKIP                                        │
│  │                                                                           │
│  AuthorizationFilter                                                        │
│  ├── Has permission? ✅ PASS                                               │
│  │                                                                           │
│  ──► Controller (ClientBackendController.getPhotos)                        │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

From logs:
```
FilterChainProxy : Securing GET /api/photos
OAuth2LoginAuthenticationFilter : Did not match request to Ant [pattern='/login/oauth2/code/*']
HttpSessionSecurityContextRepository : Retrieved SecurityContextImpl [Authentication=...]
FilterChainProxy : Secured GET /api/photos
```

---

### Request: GET /oauth2/authorization/google

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│  Filters activated:                                                          │
│                                                                              │
│  SecurityContextHolderFilter                                                │
│  ├── Load user from session (probably empty) ✅                             │
│  │                                                                           │
│  OAuth2AuthorizationRequestRedirectFilter                                   │
│  ├── URL match? /oauth2/authorization/google? ✅ MATCH!                     │
│  ├── Handle request ✅                                                      │
│  ├── Send 302 redirect to Google                                           │
│  └── Request ENDS here (redirect)                                          │
│                                                                              │
│  Following filters NOT executed                                             │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### Request: GET /login/oauth2/code/google?code=...

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│  Filters activated:                                                          │
│                                                                              │
│  SecurityContextHolderFilter                                                │
│  ├── Load (empty) ✅                                                        │
│  │                                                                           │
│  OAuth2AuthorizationRequestRedirectFilter                                   │
│  ├── URL match? /oauth2/authorization/*? ❌ SKIP                           │
│  │                                                                           │
│  OAuth2LoginAuthenticationFilter                                            │
│  ├── URL match? /login/oauth2/code/google? ✅ MATCH!                        │
│  ├── Validate state ✅                                                      │
│  ├── Exchange code for tokens ✅                                            │
│  ├── Create user ✅                                                         │
│  ├── Save to session ✅                                                     │
│  ├── Redirect to /dashboard                                                 │
│  └── Request ENDS here (redirect)                                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🛑 Our Custom Filter: OAuth2FlowLogger

**Position:** FIRST (before all Spring Security filters)

```java
@Component
@Order(Integer.MIN_VALUE) // Run FIRST
public class OAuth2FlowLogger implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        // Log request before Spring Security
        System.out.println("📥 REQUEST: " + request.getRequestURI());
        
        // Pass to next filter
        chain.doFilter(request, response);
        
        // Log response after Spring Security
        System.out.println("📤 RESPONSE: " + response.getStatus());
    }
}
```

---

## 📋 Summary

| Filter | URL Pattern | Action |
|--------|-------------|--------|
| SecurityContextHolderFilter | ALL | Load user from session |
| OAuth2AuthorizationRequestRedirectFilter | `/oauth2/authorization/*` | Redirect to Google |
| OAuth2LoginAuthenticationFilter | `/login/oauth2/code/*` | Exchange code for tokens |
| LogoutFilter | `/logout` | Clear session |
| AuthorizationFilter | ALL | Check permissions |

---

> 📖 **Next:** [07-subsequent-requests.md](./07-subsequent-requests.md) - Session Retrieval on API Calls
