# Session Storage & SecurityContext Complete Guide

> 📌 **Prerequisite**: Read [00_Glossary_and_Basics.md](./00_Glossary_and_Basics.md) first!
>
> This file explains **where your login data is stored** on the server.

---

## 🎯 What Gets Stored Where?

After successful OAuth login, Spring stores data in the user's HTTP Session:

```
What is HTTP Session?
─────────────────────
When you login, server creates a "folder" to keep your data.
This folder has a unique ID (like JSESSIONID=ABC123).
Browser keeps this ID in a cookie and sends it with every request.
Server uses this ID to find YOUR folder and get YOUR data!
```

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            HttpSession                                       │
│                      (Server RAM / Redis)                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Session ID: JSESSIONID = "ABC123XYZ789"                                    │
│   (Sent to browser as cookie)                                                │
│                                                                              │
│   ═══════════════════════════════════════════════════════════════════════    │
│   ATTRIBUTE 1: SPRING_SECURITY_CONTEXT                                       │
│   ─────────────────────────────────────                                      │
│   Contains: SecurityContext → OAuth2AuthenticationToken → OidcUser           │
│   Access via: @AuthenticationPrincipal OidcUser                              │
│                                                                              │
│   ═══════════════════════════════════════════════════════════════════════    │
│   ATTRIBUTE 2: AUTHORIZED_CLIENTS                                            │
│   ─────────────────────────────────                                          │
│   Contains: Map of OAuth2AuthorizedClient (tokens)                           │
│   Access via: @RegisteredOAuth2AuthorizedClient("google")                    │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Complete Session Structure (With Dummy Values)

```
HttpSession {
    id: "ABC123XYZ789JSESSIONID",
    creationTime: 2024-12-13T06:50:00Z,
    maxInactiveInterval: 1800,  // 30 minutes
    
    attributes: {
    
        // ═══════════════════════════════════════════════════════════════════
        // ATTRIBUTE 1: Security Context (User Identity)
        // ═══════════════════════════════════════════════════════════════════
        
        "SPRING_SECURITY_CONTEXT": SecurityContext {
            authentication: OAuth2AuthenticationToken {
                principal: OidcUser {
                    idToken: OidcIdToken {
                        tokenValue: "eyJhbGciOiJSUzI1NiIs...",
                        claims: {
                            "iss": "https://accounts.google.com",
                            "sub": "112416036337094439562",
                            "email": "dheerajp0299@gmail.com",
                            "email_verified": true,
                            "name": "Dheeraj",
                            "picture": "https://lh3.googleusercontent.com/...",
                            "given_name": "Dheeraj",
                            "iat": 1702285398,
                            "exp": 1702288998
                        }
                    },
                    authorities: [
                        SimpleGrantedAuthority("SCOPE_openid"),
                        SimpleGrantedAuthority("SCOPE_email"),
                        SimpleGrantedAuthority("SCOPE_profile")
                    ]
                },
                authorities: [SCOPE_openid, SCOPE_email, SCOPE_profile],
                authenticated: true,
                authorizedClientRegistrationId: "google",
                name: "112416036337094439562"
            }
        },
        
        // ═══════════════════════════════════════════════════════════════════
        // ATTRIBUTE 2: Authorized Clients Map (Tokens)
        // ═══════════════════════════════════════════════════════════════════
        
        "org.springframework...AUTHORIZED_CLIENTS": {
            "google": OAuth2AuthorizedClient {
                clientRegistration: ClientRegistration {
                    registrationId: "google",
                    clientId: "815195837364-...",
                    clientSecret: "GOCSPX-...",
                    clientName: "Google Login",
                    scopes: ["openid", "email", "profile"],
                    authorizationGrantType: "authorization_code",
                    redirectUri: "{baseUrl}/login/oauth2/code/{registrationId}",
                    providerDetails: {
                        authorizationUri: "https://accounts.google.com/o/oauth2/v2/auth",
                        tokenUri: "https://oauth2.googleapis.com/token",
                        userInfoUri: "https://www.googleapis.com/oauth2/v3/userinfo",
                        jwkSetUri: "https://www.googleapis.com/oauth2/v3/certs"
                    }
                },
                principalName: "112416036337094439562",
                accessToken: OAuth2AccessToken {
                    tokenValue: "ya29.a0AWY7Ckl5N8qH3xK9mP2wR4tY...",
                    issuedAt: 2024-12-13T06:50:00Z,
                    expiresAt: 2024-12-13T07:50:00Z,
                    tokenType: "Bearer",
                    scopes: ["openid", "email", "profile"]
                },
                refreshToken: null
            }
        }
    }
}
```

---

## 🔄 Request Flow: How Session Is Used

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    EVERY REQUEST FLOW                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Browser sends: Cookie: JSESSIONID=ABC123XYZ789                             │
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────┐        │
│   │         SecurityContextHolderFilter (FIRST FILTER!)             │        │
│   │                                                                  │        │
│   │   1. Get session ID from cookie                                  │        │
│   │   2. Look up session in server memory                            │        │
│   │                                                                  │        │
│   │   Session exists?                                                │        │
│   │      │                                                           │        │
│   │      ├── YES → Load SPRING_SECURITY_CONTEXT from session         │        │
│   │      │         Put in SecurityContextHolder (ThreadLocal)        │        │
│   │      │         User is AUTHENTICATED! ✅                         │        │
│   │      │                                                           │        │
│   │      └── NO  → SecurityContextHolder is EMPTY                    │        │
│   │                User is ANONYMOUS ❌                              │        │
│   │                Redirect to login!                                │        │
│   │                                                                  │        │
│   └─────────────────────────────────────────────────────────────────┘        │
│                                                                              │
│   ↓ (If authenticated)                                                       │
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────┐        │
│   │                    Your Controller                               │        │
│   │                                                                  │        │
│   │   @GetMapping("/api/user")                                       │        │
│   │   public void handle(                                            │        │
│   │       @AuthenticationPrincipal OidcUser user,                    │        │
│   │       @RegisteredOAuth2AuthorizedClient("google") client         │        │
│   │   ) {                                                            │        │
│   │       // User and client data available here!                    │        │
│   │   }                                                              │        │
│   │                                                                  │        │
│   └─────────────────────────────────────────────────────────────────┘        │
│                                                                              │
│   After response: SecurityContextHolder is CLEARED (cleanup)                 │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Two Annotations, Two Paths

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   @AuthenticationPrincipal OidcUser user                                     │
│   ────────────────────────────────────                                       │
│   Path: Session → SPRING_SECURITY_CONTEXT → authentication → principal      │
│   Gets: OidcUser (email, name, picture, subject)                             │
│                                                                              │
│   Internal code:                                                             │
│   OAuth2AuthenticationToken token = SecurityContextHolder                    │
│       .getContext().getAuthentication();                                     │
│   OidcUser user = (OidcUser) token.getPrincipal();                          │
│                                                                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client  │
│   ───────────────────────────────────────────────────────────────────────    │
│   Path: Session → AUTHORIZED_CLIENTS → "google"                              │
│   Gets: OAuth2AuthorizedClient (accessToken, clientRegistration)             │
│                                                                              │
│   Internal code:                                                             │
│   OAuth2AuthorizedClient client = session                                    │
│       .getAttribute("AUTHORIZED_CLIENTS")                                    │
│       .get("google");                                                        │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📦 What Browser Gets

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    BROWSER SIDE                                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Browser receives ONLY:                                                     │
│   ─────────────────────                                                      │
│   Set-Cookie: JSESSIONID=ABC123XYZ789JSESSIONID; Path=/; HttpOnly            │
│                                                                              │
│   In browser:                                                                │
│   ───────────                                                                │
│   • Tokens: NONE ❌                                                          │
│   • User data: NONE ❌                                                       │
│   • Just session pointer: ✅                                                 │
│                                                                              │
│   This is BFF Pattern - tokens stay on server! 🔐                            │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 SecurityContextHolder - ThreadLocal Magic

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    THREAD LOCAL EXPLAINED                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   What is ThreadLocal?                                                       │
│   ─────────────────────                                                      │
│   Variable that is UNIQUE to each thread!                                    │
│   Thread 1's SecurityContext ≠ Thread 2's SecurityContext                   │
│                                                                              │
│   Why needed?                                                                │
│   ────────────                                                               │
│   Server handles 100 requests simultaneously (100 threads)                   │
│   Each thread has its OWN current user in SecurityContextHolder!             │
│                                                                              │
│   ┌────────────┐  ┌────────────┐  ┌────────────┐                            │
│   │  Thread 1  │  │  Thread 2  │  │  Thread 3  │                            │
│   │ User: John │  │ User: Jane │  │ User: Bob  │                            │
│   └────────────┘  └────────────┘  └────────────┘                            │
│                                                                              │
│   Each thread knows ONLY its user!                                           │
│                                                                              │
│   Lifecycle:                                                                 │
│   ──────────                                                                 │
│   Request Start → SecurityContext loaded from session into ThreadLocal      │
│   Request End   → SecurityContext cleared from ThreadLocal (cleanup!)       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🛡️ BFF Pattern (Backend For Frontend)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    BFF PATTERN EXPLAINED                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   What is BFF?                                                               │
│   ────────────                                                               │
│   Backend For Frontend - A backend that acts as proxy for frontend!          │
│                                                                              │
│   Why?                                                                       │
│   ─────                                                                      │
│   • Tokens stay on SERVER (not in browser!)                                  │
│   • Browser only gets session cookie (JSESSIONID)                            │
│   • No XSS attack can steal tokens!                                          │
│                                                                              │
│   Our Architecture:                                                          │
│   ──────────────────                                                         │
│   ┌───────────┐    ┌─────────────┐    ┌──────────────────┐                  │
│   │ React UI  │ ── │ Spring BFF  │ ── │ Resource Server  │                  │
│   │ (Browser) │    │ (Port 8080) │    │   (Port 8081)    │                  │
│   └───────────┘    └─────────────┘    └──────────────────┘                  │
│        │                 │                      │                            │
│   Only cookie!     Has all tokens!        Validates JWT!                     │
│   (JSESSIONID)     (access_token,                                            │
│                     id_token in                                              │
│                     session)                                                 │
│                                                                              │
│   Browser NEVER sees tokens! Secure! 🔐                                      │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 Summary

| Storage Location | Contains | Access Method |
|-----------------|----------|---------------|
| `SPRING_SECURITY_CONTEXT` | OidcUser (user identity) | `@AuthenticationPrincipal` |
| `AUTHORIZED_CLIENTS` | OAuth2AuthorizedClient (tokens) | `@RegisteredOAuth2AuthorizedClient` |
| Browser cookie | Just session ID | Automatic |
