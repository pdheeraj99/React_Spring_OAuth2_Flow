# OAuth2 BFF Reference Guide

> 🎓 **Complete reference for OAuth2 Authorization Code Flow with BFF Pattern**  
> Validated from live logs - Every detail verified from actual running system

---

## 📚 Table of Contents

| # | Topic | Description |
|---|-------|-------------|
| 01 | [Session Fundamentals](./01-session-fundamentals.md) | JSESSIONID, creation, storage, scaling |
| 02 | [OAuth2 Flow Initiation](./02-oauth2-initiation.md) | Login click → Google redirect |
| 03 | [Callback Processing](./03-callback-processing.md) | Code → Token exchange |
| 04 | [Token Deep Dive](./04-token-deep-dive.md) | Access Token vs ID Token (JWT) |
| 05 | [Session Storage](./05-session-storage.md) | SecurityContext, AuthorizedClient |
| 06 | [Filter Chain](./06-filter-chain.md) | All 17 filters explained |
| 07 | [Subsequent Requests](./07-subsequent-requests.md) | Session retrieval, data injection |
| 08 | [Resource Server](./08-resource-server.md) | JWT validation, protected APIs |
| 09 | [Complete Flow](./09-complete-flow.md) | End-to-end with all diagrams |
| 10 | [OIDC & ThreadLocal](./10-oidc-threadlocal.md) | OpenID Connect, SecurityContextHolder |


---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              BROWSER (React App)                            │
│                            http://localhost:5174                            │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  Cookies:                                                            │   │
│  │    JSESSIONID = 440421F8...  (HttpOnly, Secure)                     │   │
│  │                                                                      │   │
│  │  ⚠️ No tokens stored here! Only Session ID                          │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         CLIENT BACKEND (BFF)                                │
│                         http://localhost:8080                               │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  SESSION (Server Memory):                                            │   │
│  │    Key: SPRING_SECURITY_CONTEXT                                      │   │
│  │      → OAuth2AuthenticationToken                                     │   │
│  │         → Principal: OidcUser { name, email, picture, idToken }     │   │
│  │                                                                      │   │
│  │    Key: oauth2AuthorizedClient                                       │   │
│  │      → accessToken: "ya29.a0..."                                    │   │
│  │      → refreshToken: null                                           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          RESOURCE SERVER                                    │
│                         http://localhost:8081                               │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  JWT Validation:                                                     │   │
│  │    → Receives: Authorization: Bearer eyJhbGciOiJSUzI1NiIs...        │   │
│  │    → Validates: Signature using Google JWKS                         │   │
│  │    → Extracts: User claims (sub, email, name)                       │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                               GOOGLE                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  Authorization: https://accounts.google.com/o/oauth2/v2/auth        │   │
│  │  Token:         https://oauth2.googleapis.com/token                 │   │
│  │  JWKS:          https://www.googleapis.com/oauth2/v3/certs          │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔑 Key Concepts Quick Reference

| Term | Definition |
|------|------------|
| **JSESSIONID** | Browser cookie that maps to server-side session |
| **Session** | Server-side storage (RAM by default) |
| **SecurityContext** | Contains Authentication object with user details |
| **OidcUser** | User object with claims from ID Token |
| **Access Token** | Opaque token for Google APIs (not JWT) |
| **ID Token** | JWT containing user identity claims |
| **BFF** | Backend-for-Frontend - proxy pattern for security |

---

## 🚀 Quick Start

1. Start all services:
   ```bash
   # Terminal 1: Client Backend
   cd client-app && ./mvnw spring-boot:run
   
   # Terminal 2: Resource Server  
   cd resource-server && ./mvnw spring-boot:run
   
   # Terminal 3: React UI
   cd client-ui && npm run dev
   ```

2. Open http://localhost:5173 and click "Sign in with Google"

3. After login, check session: http://localhost:8080/debug/session

---

> 📖 **Start Reading:** [01-session-fundamentals.md](./01-session-fundamentals.md)
