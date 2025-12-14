# Google Response → Spring Session Storage

## 🎯 The Big Picture

```
Google Token Response (JSON)
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│              SPRING SECURITY PROCESSING                      │
│                                                              │
│  1. DefaultAuthorizationCodeTokenResponseClient              │
│     └─→ Calls Google's token endpoint                        │
│     └─→ Gets JSON response                                   │
│                                                              │
│  2. OAuth2AccessTokenResponse                                │
│     └─→ Parses JSON into Java object                         │
│                                                              │
│  3. OidcIdTokenDecoderFactory                                │
│     └─→ Decodes JWT (id_token)                               │
│     └─→ Validates signature using Google's public keys       │
│                                                              │
│  4. OidcUserService                                          │
│     └─→ Creates OidcUser object from ID Token claims         │
│                                                              │
│  5. OAuth2AuthorizedClient                                   │
│     └─→ Wraps access_token + refresh_token + user            │
│                                                              │
│  6. HttpSessionOAuth2AuthorizedClientRepository              │
│     └─→ Stores OAuth2AuthorizedClient in HttpSession         │
│                                                              │
│  7. SecurityContextHolder                                    │
│     └─→ Stores Authentication (with OidcUser) in session     │
│                                                              │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
    HttpSession (Server RAM / Redis)
```

---

## 📦 Step 1: Google's Raw JSON Response

```json
{
  "access_token": "ya29.a0AWY7Ckxxxxxxxx",
  "expires_in": 3599,
  "scope": "openid email profile",
  "token_type": "Bearer",
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.xxxxx.signature"
}
```

---

## 🔄 Step 2: DefaultAuthorizationCodeTokenResponseClient

**Class:** `org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient`

```java
// This class calls Google's token endpoint
public OAuth2AccessTokenResponse getTokenResponse(
    OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
    
    // Makes HTTP POST to https://oauth2.googleapis.com/token
    // Returns OAuth2AccessTokenResponse object
}
```

**Output:** `OAuth2AccessTokenResponse` object containing:
```java
OAuth2AccessTokenResponse {
    accessToken: OAuth2AccessToken {
        tokenValue: "ya29.a0AWY7Ckxxxxxxxx",
        issuedAt: Instant,
        expiresAt: Instant,
        scopes: ["openid", "email", "profile"]
    },
    refreshToken: null,  // (only if access_type=offline)
    additionalParameters: {
        "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.xxxxx"
    }
}
```

---

## 🔐 Step 3: OidcIdTokenDecoderFactory (JWT Decoding)

**Class:** `org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory`

```java
// Decodes the JWT id_token
public JwtDecoder createDecoder(ClientRegistration clientRegistration) {
    // 1. Fetches Google's public keys from:
    //    https://www.googleapis.com/oauth2/v3/certs
    
    // 2. Validates JWT signature
    
    // 3. Returns decoded Jwt object
}
```

**Output:** `Jwt` object:
```java
Jwt {
    tokenValue: "eyJhbGciOiJSUzI1NiIs...",
    headers: {alg: "RS256", typ: "JWT", kid: "d543e21a0273efc66a47500"},
    claims: {
        "iss": "https://accounts.google.com",
        "sub": "112416036337094439562",
        "email": "dheerajp0299@gmail.com",
        "name": "Dheeraj",
        "picture": "https://lh3.googleusercontent.com/...",
        "iat": 1702285398,
        "exp": 1702288998
    }
}
```

---

## 👤 Step 4: OidcUserService (Creates OidcUser)

**Class:** `org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService`

```java
public OidcUser loadUser(OidcUserRequest userRequest) {
    // 1. Gets ID Token claims
    // 2. Optionally calls Google's userinfo endpoint for more data
    // 3. Creates OidcUser object
    
    return new DefaultOidcUser(authorities, idToken, userInfo);
}
```

**Output:** `OidcUser` (implements `OAuth2User`):
```java
DefaultOidcUser {
    // From ID Token
    idToken: OidcIdToken {
        tokenValue: "eyJhbGciOiJSUzI1NiIs...",
        claims: {sub, email, name, picture, ...}
    },
    
    // User Info (optional, from userinfo endpoint)
    userInfo: OidcUserInfo {
        claims: {email, name, picture, ...}
    },
    
    // Authorities (roles)
    authorities: [SCOPE_openid, SCOPE_email, SCOPE_profile],
    
    // Methods available:
    getSubject()   → "112416036337094439562"
    getEmail()     → "dheerajp0299@gmail.com"
    getFullName()  → "Dheeraj"
    getPicture()   → "https://lh3.googleusercontent.com/..."
    getClaims()    → Map of all claims
    getIdToken()   → OidcIdToken object
}
```

---

## 🎫 Step 5: OAuth2AuthorizedClient (Token Container)

**Class:** `org.springframework.security.oauth2.client.OAuth2AuthorizedClient`

```java
// Created by OAuth2LoginAuthenticationProvider
OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
    clientRegistration,  // Google config from application.yaml
    principalName,       // "112416036337094439562" (user ID)
    accessToken,         // OAuth2AccessToken object
    refreshToken         // null (unless access_type=offline)
);
```

**Structure:**
```java
OAuth2AuthorizedClient {
    clientRegistration: ClientRegistration {
        registrationId: "google",
        clientId: "815195837364-xxx.apps.googleusercontent.com",
        clientSecret: "GOCSPX-xxx",
        authorizationUri: "https://accounts.google.com/o/oauth2/v2/auth",
        tokenUri: "https://oauth2.googleapis.com/token"
    },
    
    principalName: "112416036337094439562",
    
    accessToken: OAuth2AccessToken {
        tokenValue: "ya29.a0AWY7Ckxxxxxxxx",
        expiresAt: 2023-12-11T15:30:00Z
    },
    
    refreshToken: null  // Only present if access_type=offline
}
```

---

## 💾 Step 6: Storing in HttpSession

### A) OAuth2AuthorizedClient Storage

**Class:** `org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository`

```java
public void saveAuthorizedClient(
    OAuth2AuthorizedClient authorizedClient,
    Authentication principal,
    HttpServletRequest request,
    HttpServletResponse response) {
    
    HttpSession session = request.getSession();
    
    // Key format: "org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository.AUTHORIZED_CLIENTS"
    Map<String, OAuth2AuthorizedClient> clients = 
        (Map) session.getAttribute(SESSION_ATTRIBUTE_NAME);
    
    clients.put("google", authorizedClient);
    session.setAttribute(SESSION_ATTRIBUTE_NAME, clients);
}
```

### B) Authentication (OidcUser) Storage

**Class:** `org.springframework.security.web.context.HttpSessionSecurityContextRepository`

```java
public void saveContext(SecurityContext context, 
    HttpServletRequest request, HttpServletResponse response) {
    
    HttpSession session = request.getSession();
    
    // Key: "SPRING_SECURITY_CONTEXT"
    session.setAttribute("SPRING_SECURITY_CONTEXT", context);
}
```

---

## 🗄️ Final Session Contents

```java
HttpSession {
    // Session ID (sent to browser as cookie)
    JSESSIONID: "ABC123XYZ789"
    
    // Attribute 1: Security Context (contains OidcUser)
    "SPRING_SECURITY_CONTEXT": SecurityContext {
        authentication: OAuth2AuthenticationToken {
            principal: DefaultOidcUser {
                subject: "112416036337094439562",
                email: "dheerajp0299@gmail.com",
                name: "Dheeraj",
                idToken: OidcIdToken {...}
            },
            authorities: [SCOPE_openid, SCOPE_email, SCOPE_profile],
            authorizedClientRegistrationId: "google"
        }
    },
    
    // Attribute 2: Authorized Clients (contains tokens)
    "...AUTHORIZED_CLIENTS": {
        "google": OAuth2AuthorizedClient {
            accessToken: "ya29.a0AWY7Ckxxxxxxxx",
            refreshToken: null,
            principalName: "112416036337094439562"
        }
    }
}
```

---

## 🔄 How You Access These in Controller

```java
@GetMapping("/api/user")
public Map<String, Object> getUser(
    // This comes from session → SecurityContext → Authentication → Principal
    @AuthenticationPrincipal OidcUser user,
    
    // This comes from session → AUTHORIZED_CLIENTS → "google"
    @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client
) {
    // OidcUser has user info
    String email = user.getEmail();           // From ID Token claims
    String name = user.getFullName();         // From ID Token claims
    String userId = user.getSubject();        // From ID Token "sub" claim
    OidcIdToken idToken = user.getIdToken();  // Full JWT
    
    // OAuth2AuthorizedClient has tokens
    String accessToken = client.getAccessToken().getTokenValue();
    OAuth2RefreshToken refreshToken = client.getRefreshToken(); // null usually
    
    return Map.of("email", email, "name", name);
}
```

---

## 📊 Class Responsibility Summary

| Class | Responsibility |
|-------|----------------|
| `DefaultAuthorizationCodeTokenResponseClient` | Calls Google's token endpoint |
| `OAuth2AccessTokenResponse` | Holds raw token response |
| `OidcIdTokenDecoderFactory` | Decodes & validates JWT |
| `OidcUserService` | Creates `OidcUser` from claims |
| `OAuth2AuthorizedClient` | Wraps tokens + client info |
| `HttpSessionOAuth2AuthorizedClientRepository` | Stores `OAuth2AuthorizedClient` in session |
| `HttpSessionSecurityContextRepository` | Stores `Authentication` (with `OidcUser`) in session |

---

## 🧠 40LPA Key Insight

```
Browser only gets: JSESSIONID cookie (random string)
Session contains: Everything (tokens, user info, etc.)

This is why BFF pattern is secure!
→ Tokens NEVER leave the server
→ Browser just has a session pointer
```
