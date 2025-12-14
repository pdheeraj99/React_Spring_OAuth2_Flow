# OAuth 2.0 & OpenID Connect Complete Guide

---

## 📚 Table of Contents

This folder contains comprehensive documentation covering OAuth 2.0 and OpenID Connect, organized as a learning flow from basics to advanced concepts.

---

## 🆕 **New to OAuth? Start Here!**

| File | Description |
|------|-------------|
| **[00_Glossary_and_Basics.md](./00_Glossary_and_Basics.md)** | 🚀 **START HERE!** Beginner-friendly glossary with hotel analogy |

---

## 📖 Reading Order

| # | File | Topics Covered |
|---|------|----------------|
| 0 | [00_Glossary_and_Basics.md](./00_Glossary_and_Basics.md) | 🆕 **Prerequisites!** Basic terms, hotel analogy, big picture |
| 1 | [01_OAuth_vs_OIDC_Core_Difference.md](./01_OAuth_vs_OIDC_Core_Difference.md) | OAuth vs OIDC, openid scope, when to use each |
| 2 | [02_Grant_Types_Explained.md](./02_Grant_Types_Explained.md) | 4 grant types, **PKCE for SPAs**, when to use each |
| 3 | [03_Google_Token_Response.md](./03_Google_Token_Response.md) | 5 fields, access_token vs id_token, JWT claims |
| 4 | [04_Token_Formats_Opaque_vs_JWT.md](./04_Token_Formats_Opaque_vs_JWT.md) | Opaque vs JWT, **JWT 3-part structure**, auth server comparison |
| 5 | [05_Spring_Objects_Hierarchy.md](./05_Spring_Objects_Hierarchy.md) | OidcUser, AuthToken, AuthorizedClient hierarchy |
| 6 | [06_Session_Storage_SecurityContext.md](./06_Session_Storage_SecurityContext.md) | Session structure, **ThreadLocal**, **BFF Pattern** |
| 7 | [07_Annotations_Internals.md](./07_Annotations_Internals.md) | @AuthenticationPrincipal, @RegisteredOAuth2AuthorizedClient internals |
| 8 | [08_Resource_Server_JWT_Validation.md](./08_Resource_Server_JWT_Validation.md) | JWT validation flow, JWKS caching, config |
| 9 | [09_Microservices_Auth_Why.md](./09_Microservices_Auth_Why.md) | Why auth between services, 3 security pillars |
| 10 | [10_Multi_Account_Connect_Scenario.md](./10_Multi_Account_Connect_Scenario.md) | User A connects User B's account, login_hint |

---

## 🎯 Quick Reference

### OAuth 2.0 vs OIDC

```
OAuth 2.0 = Authorization ("Can you access?")  → Access Token
OIDC = OAuth 2.0 + Authentication ("Who are you?") → ID Token

Key: Adding "openid" scope switches from OAuth to OIDC!
```

### Grant Types

```
Authorization Code  → User login (web apps) ⭐ USE THIS
Client Credentials → Machine-to-machine (no user) ⭐ USE THIS
Implicit            → DEPRECATED ❌
Password            → DEPRECATED ❌
```

### Token Formats

```
Google: access_token = Opaque, id_token = JWT
Keycloak: Both access_token and id_token = JWT

Grant Type ≠ Token Format!
```

### What You Use in Code

```java
@AuthenticationPrincipal OidcUser user  // User identity
@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client  // Tokens
```

---

## 📂 Related Documentation

- `../Diagrams/` - Visual OAuth flow diagrams
- `../docs/` - Detailed technical documentation

---

## 🔑 Key Concepts Summary

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    COMPLETE OAUTH/OIDC FLOW                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   1. User clicks "Login with Google"                                         │
│      └── Grant Type: Authorization Code + OIDC (openid scope)                │
│                                                                              │
│   2. Google returns:                                                         │
│      ├── access_token (Opaque) → For Google APIs                            │
│      └── id_token (JWT) → For user identity                                 │
│                                                                              │
│   3. Spring creates:                                                         │
│      ├── OidcUser (from id_token) → User details                            │
│      ├── OAuth2AuthorizedClient → Tokens + config                           │
│      └── OAuth2AuthenticationToken → Security wrapper                       │
│                                                                              │
│   4. Stored in HttpSession:                                                  │
│      ├── SPRING_SECURITY_CONTEXT → SecurityContext with AuthToken           │
│      └── AUTHORIZED_CLIENTS → Map of OAuth2AuthorizedClient                 │
│                                                                              │
│   5. Resource Server:                                                        │
│      └── Validates JWT using Google's public keys (JWKS)                    │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

*Last Updated: December 2024*
