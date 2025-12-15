# 12 - Common Confusions & FAQ

> 📌 Questions that trip up beginners (and even some seniors!)

---

## 🤔 Confusion 1: OAuth vs OIDC

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    OAuth 2.0 vs OpenID Connect                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   QUESTION:                                                                  │
│   "Is this OAuth or OIDC? What's the difference?"                            │
│                                                                              │
│   ANSWER:                                                                    │
│   ───────                                                                    │
│   OAuth 2.0 = AUTHORIZATION framework                                        │
│   → "Can this app access my photos?"                                         │
│   → Returns: access_token                                                    │
│   → Doesn't tell you WHO the user is!                                        │
│                                                                              │
│   OpenID Connect (OIDC) = AUTHENTICATION layer ON TOP of OAuth 2.0          │
│   → "Who is this user?"                                                      │
│   → Returns: id_token (JWT with user info)                                   │
│   → Uses OAuth 2.0 flow + adds identity!                                     │
│                                                                              │
│   We use OIDC (which uses OAuth 2.0 under the hood):                         │
│   → scope: openid ← Makes it OIDC!                                           │
│   → We get id_token (user identity)                                          │
│   → We use Authorization Code flow (from OAuth 2.0)                          │
│                                                                              │
│   OIDC = OAuth 2.0 + Identity                                                │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🤔 Confusion 2: Why Do We Need access_token?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    access_token vs id_token                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   QUESTION:                                                                  │
│   "We just need user info, why do we get access_token too?"                  │
│                                                                              │
│   ANSWER:                                                                    │
│   ───────                                                                    │
│   Google always sends both when you use OIDC.                                │
│                                                                              │
│   access_token = For calling Google APIs                                     │
│   → Get Google Drive files                                                   │
│   → Read Google Photos                                                       │
│   → Access Gmail                                                             │
│   → If you don't call Google APIs, you IGNORE this token!                    │
│                                                                              │
│   id_token = For knowing WHO the user is                                     │
│   → User's email, name, picture                                              │
│   → JWT format (can be validated)                                            │
│   → THIS is what we use for "Login with Google"!                             │
│                                                                              │
│   In our app:                                                                │
│   → We use: id_token ✅                                                      │
│   → We ignore: access_token (not calling Google APIs)                        │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🤔 Confusion 3: Client = User?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    "Client" Terminology                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   QUESTION:                                                                  │
│   "In OAuth, is 'Client' the user?"                                          │
│                                                                              │
│   ANSWER:                                                                    │
│   ───────                                                                    │
│   NO! This is a COMMON confusion!                                            │
│                                                                              │
│   In everyday usage:                                                         │
│   Client = Customer = User                                                   │
│                                                                              │
│   In OAuth:                                                                  │
│   Client = YOUR APPLICATION                                                  │
│   User = "Resource Owner"                                                    │
│                                                                              │
│   The naming is confusing because OAuth was designed for                     │
│   machine-to-machine scenarios first!                                        │
│                                                                              │
│   ┌────────────────┬─────────────────────────────────────┐                   │
│   │ OAuth Term     │ Real World Meaning                  │                   │
│   ├────────────────┼─────────────────────────────────────┤                   │
│   │ Client         │ Your Spring Boot app                │                   │
│   │ Resource Owner │ The human user                      │                   │
│   │ Resource Server│ API server (Google or yours)        │                   │
│   │ Auth Server    │ Google's login system               │                   │
│   └────────────────┴─────────────────────────────────────┘                   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🤔 Confusion 4: Token in Browser?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    Where Are Tokens?                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   QUESTION:                                                                  │
│   "Can I see the tokens in browser DevTools?"                                │
│                                                                              │
│   ANSWER:                                                                    │
│   ───────                                                                    │
│   NO! And that's by design!                                                  │
│                                                                              │
│   What browser HAS:                                                          │
│   → JSESSIONID cookie (points to server session)                             │
│   → DOM, JavaScript variables (nothing about tokens)                         │
│                                                                              │
│   What browser DOESN'T HAVE:                                                 │
│   → access_token                                                             │
│   → id_token                                                                 │
│   → refresh_token                                                            │
│   → client_secret                                                            │
│                                                                              │
│   Where tokens ARE:                                                          │
│   → Your Spring Boot server's HttpSession                                    │
│   → Never sent to browser!                                                   │
│                                                                              │
│   This is the BFF (Backend For Frontend) pattern!                            │
│   Frontend has session cookie, backend has tokens.                           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🤔 Confusion 5: Session vs Token Expiry

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    When Do Things Expire?                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   QUESTION:                                                                  │
│   "Token expires in 1 hour, does user get logged out?"                       │
│                                                                              │
│   ANSWER:                                                                    │
│   ───────                                                                    │
│   NO! Session and token expiry are DIFFERENT!                                │
│                                                                              │
│   ┌─────────────────┬────────────────┬──────────────────────────────────┐    │
│   │ Thing           │ Expiry         │ Controlled By                    │    │
│   ├─────────────────┼────────────────┼──────────────────────────────────┤    │
│   │ access_token    │ ~1 hour        │ Google                           │    │
│   │ id_token        │ ~1 hour        │ Google                           │    │
│   │ HttpSession     │ 30 min idle    │ YOUR server (configurable!)      │    │
│   │ refresh_token   │ ~6 months      │ Google                           │    │
│   └─────────────────┴────────────────┴──────────────────────────────────┘    │
│                                                                              │
│   Scenario:                                                                  │
│   - User logs in at 10:00 AM                                                 │
│   - Token expires at 11:00 AM                                                │
│   - Session configured for 8 hours                                           │
│   - User browses at 11:30 AM                                                 │
│   - → Token expired, BUT session active = User STILL logged in!             │
│                                                                              │
│   Token expiry matters ONLY when:                                            │
│   - Calling Google APIs (need valid access_token)                            │
│   - Sending id_token to Resource Server (checks exp claim)                   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🤔 Confusion 6: Why Two Tokens?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    Two Tokens, Two Purposes                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   QUESTION:                                                                  │
│   "Why not just one token for everything?"                                   │
│                                                                              │
│   ANSWER:                                                                    │
│   ───────                                                                    │
│   Different purposes, different designs!                                     │
│                                                                              │
│   access_token:                                                              │
│   • Purpose: Access resources (photos, files)                                │
│   • Who validates: GOOGLE (resource server)                                  │
│   • Format: Opaque (only Google understands)                                 │
│   • Contains: Permissions (scopes) - machine readable                        │
│                                                                              │
│   id_token:                                                                  │
│   • Purpose: Identify user (who is this?)                                    │
│   • Who validates: YOUR APP (or resource server)                             │
│   • Format: JWT (anyone with public key can validate)                        │
│   • Contains: User info (email, name) - human readable                       │
│                                                                              │
│   Why separation?                                                            │
│   → Security: id_token can be validated anywhere                             │
│   → Flexibility: access_token format can change                              │
│   → Purpose: Clear what each token does                                      │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🤔 Confusion 7: What is "Scope"?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    Understanding Scopes                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   QUESTION:                                                                  │
│   "What exactly are scopes?"                                                 │
│                                                                              │
│   ANSWER:                                                                    │
│   ───────                                                                    │
│   Scopes = Permissions you're requesting                                     │
│                                                                              │
│   Like asking: "Can I please have access to..."                              │
│                                                                              │
│   ┌─────────────────────┬────────────────────────────────────────────────┐   │
│   │ Scope               │ What You Get                                   │   │
│   ├─────────────────────┼────────────────────────────────────────────────┤   │
│   │ openid              │ id_token with user ID (sub)                    │   │
│   │ email               │ User's email added to id_token                 │   │
│   │ profile             │ Name, picture added to id_token                │   │
│   │ drive.readonly      │ Read Google Drive files (access_token use)     │   │
│   │ photos.readonly     │ Read Google Photos (access_token use)          │   │
│   └─────────────────────┴────────────────────────────────────────────────┘   │
│                                                                              │
│   Our scopes: openid email profile                                           │
│   → We get: id_token with email, name, picture                               │
│   → User sees: "App wants to see your email and profile" ✅                  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📝 Quick Reference: Common Terms

| Term | Meaning |
|------|---------|
| Grant Type | The "flow" or method of getting tokens |
| Authorization Code | Temporary code exchanged for tokens |
| Client ID | Public identifier for your app |
| Client Secret | Private proof you're the real app |
| Scope | Permissions being requested |
| State | Random value for CSRF protection |
| Redirect URI | Where to send user after login |
| Front Channel | Communication through browser |
| Back Channel | Server-to-server communication |
| BFF | Backend For Frontend pattern |
| OIDC | OpenID Connect (identity layer on OAuth) |
| JWT | JSON Web Token (self-contained token) |
| Opaque Token | Token only the issuer understands |

---

## 🎉 Congratulations

You've completed the Authorization Code Grant learning guide!

### What's Next?

1. **Review**: Go back and re-read any confusing sections
2. **Practice**: Run the Spring Boot app and watch the flow
3. **Debug**: Enable TRACE logging to see every step
4. **Next Grant**: Move to [Client Credentials Grant](../02_Client_Credentials_Grant/) (coming soon!)

---

**Back to:** [README.md](./README.md)
