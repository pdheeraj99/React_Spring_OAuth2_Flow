# Spring Security Annotations Internals

> 📌 **Prerequisite**: Read [00_Glossary_and_Basics.md](./00_Glossary_and_Basics.md) first!
>
> This file explains **how Spring magically gives you user data in controllers**.

---

## 🎯 The Two Key Annotations

```java
@AuthenticationPrincipal OidcUser user
@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client
```

What happens internally when you use these?

```
Simple explanation:
───────────────────
These annotations are SHORTCUTS!
Instead of writing 10 lines of code to get user data,
just add @AuthenticationPrincipal and Spring does it for you!

Like auto-complete on your phone - you type less, get same result!
```

---

## 1️⃣ @AuthenticationPrincipal

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    @AuthenticationPrincipal OidcUser user                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   What you write:                                                            │
│   ─────────────────                                                          │
│   @GetMapping("/api/user")                                                   │
│   public void handle(@AuthenticationPrincipal OidcUser user) {               │
│       String email = user.getEmail();                                        │
│   }                                                                          │
│                                                                              │
│   What Spring does internally:                                               │
│   ─────────────────────────────                                              │
│   // Step 1: Get current Authentication from SecurityContextHolder           │
│   Authentication auth = SecurityContextHolder                                │
│       .getContext()                                                          │
│       .getAuthentication();                                                  │
│                                                                              │
│   // Step 2: Extract principal (the user object)                             │
│   OidcUser user = (OidcUser) auth.getPrincipal();                           │
│                                                                              │
│   // Step 3: Inject into your method parameter                               │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Visual Flow

```
SecurityContextHolder (ThreadLocal)
        │
        ▼
┌─────────────────────────┐
│    SecurityContext      │
│           │             │
│           ▼             │
│    authentication       │   ─── OAuth2AuthenticationToken
│           │             │
│           ▼             │
│      principal          │   ─── OidcUser ◄── THIS IS WHAT YOU GET!
│                         │
└─────────────────────────┘
```

---

## 2️⃣ @RegisteredOAuth2AuthorizedClient

```
┌─────────────────────────────────────────────────────────────────────────────┐
│        @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   What you write:                                                            │
│   ─────────────────                                                          │
│   @GetMapping("/api/photos")                                                 │
│   public void handle(                                                        │
│       @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient c   │
│   ) {                                                                        │
│       String token = c.getAccessToken().getTokenValue();                     │
│   }                                                                          │
│                                                                              │
│   What Spring does internally:                                               │
│   ─────────────────────────────                                              │
│   // Step 1: Get registration ID from annotation                             │
│   String registrationId = "google";                                          │
│                                                                              │
│   // Step 2: Get current user's principal name                               │
│   String principalName = SecurityContextHolder                               │
│       .getContext()                                                          │
│       .getAuthentication()                                                   │
│       .getName();  // "112416036337094439562"                                │
│                                                                              │
│   // Step 3: Look up in session's AUTHORIZED_CLIENTS map                     │
│   OAuth2AuthorizedClient client = session                                    │
│       .getAttribute("org.springframework...AUTHORIZED_CLIENTS")              │
│       .get("google");                                                        │
│                                                                              │
│   // Step 4: Inject into your method parameter                               │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Visual Flow

```
HttpSession
        │
        ▼
┌─────────────────────────┐
│   AUTHORIZED_CLIENTS    │   ─── Map<String, OAuth2AuthorizedClient>
│       (attribute)       │
│           │             │
│           ▼             │
│    "google" (key)       │   ─── Uses @RegisteredOAuth2AuthorizedClient("google")
│           │             │
│           ▼             │
│  OAuth2AuthorizedClient │   ◄── THIS IS WHAT YOU GET!
│                         │
└─────────────────────────┘
```

---

## 🔍 Why "google" Parameter?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    MULTIPLE PROVIDERS SUPPORT                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   You might have multiple OAuth providers:                                   │
│                                                                              │
│   # application.yaml                                                         │
│   spring:                                                                    │
│     security:                                                                │
│       oauth2:                                                                │
│         client:                                                              │
│           registration:                                                      │
│             google:        # ← @RegisteredOAuth2AuthorizedClient("google")   │
│               client-id: xxx                                                 │
│             facebook:      # ← @RegisteredOAuth2AuthorizedClient("facebook") │
│               client-id: yyy                                                 │
│             github:        # ← @RegisteredOAuth2AuthorizedClient("github")   │
│               client-id: zzz                                                 │
│                                                                              │
│   The string tells Spring WHICH provider's tokens you want!                  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Comparison

```
┌────────────────────────────────────────────┬────────────────────────────────────────────┐
│   @AuthenticationPrincipal                 │   @RegisteredOAuth2AuthorizedClient       │
├────────────────────────────────────────────┼────────────────────────────────────────────┤
│                                            │                                            │
│   Source: SecurityContextHolder            │   Source: HttpSession                      │
│                                            │                                            │
│   Path: SecurityContext →                  │   Path: AUTHORIZED_CLIENTS →               │
│         Authentication →                   │         registrationId ("google")          │
│         Principal                          │                                            │
│                                            │                                            │
│   Returns: OidcUser                        │   Returns: OAuth2AuthorizedClient          │
│                                            │                                            │
│   Contains: User identity                  │   Contains: Tokens + config                │
│   (email, name, picture)                   │   (accessToken, clientRegistration)        │
│                                            │                                            │
│   Use: Display user info                   │   Use: Call APIs with token                │
│                                            │                                            │
└────────────────────────────────────────────┴────────────────────────────────────────────┘
```

---

## 💡 Code Example

```java
@GetMapping("/api/user")
public Map<String, Object> getUser(
    @AuthenticationPrincipal OidcUser user,
    @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client
) {
    // From OidcUser (user identity)
    String userId = user.getSubject();           // "112416036337094439562"
    String email = user.getEmail();              // "dheerajp0299@gmail.com"
    String name = user.getFullName();            // "Dheeraj"
    String picture = user.getPicture();          // "https://..."
    
    // From OAuth2AuthorizedClient (tokens)
    String accessToken = client.getAccessToken().getTokenValue();  // "ya29.xxx"
    String idToken = user.getIdToken().getTokenValue();            // "eyJhbG..."
    
    return Map.of(
        "userId", userId,
        "email", email,
        "name", name
    );
}
```

---

## 📌 Remember

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   These annotations are SHORTCUTS!                                           │
│                                                                              │
│   Instead of manually:                                                       │
│   • SecurityContextHolder.getContext().getAuthentication()...                │
│   • session.getAttribute("AUTHORIZED_CLIENTS").get("google")...              │
│                                                                              │
│   Just use:                                                                  │
│   • @AuthenticationPrincipal OidcUser                                        │
│   • @RegisteredOAuth2AuthorizedClient("google")                              │
│                                                                              │
│   Spring handles the extraction for you! 🎉                                  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```
