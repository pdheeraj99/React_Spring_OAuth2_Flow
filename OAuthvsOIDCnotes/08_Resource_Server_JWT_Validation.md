# Resource Server & JWT Validation

> 📌 **Prerequisite**: Read [00_Glossary_and_Basics.md](./00_Glossary_and_Basics.md) first!
>
> This file explains how **your backend API validates tokens**.

---

## 🎯 What is Resource Server?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    RESOURCE SERVER                                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Definition: A server that holds protected resources (APIs)                 │
│               and validates JWT tokens before giving access.                 │
│                                                                              │
│   Simple explanation:                                                        │
│   ───────────────────                                                        │
│   Resource Server = Security guard at a concert venue 🎫                     │
│   - Checks your ticket (JWT token)                                           │
│   - If valid → Let you in                                                    │
│   - If invalid → "Sorry, you can't enter!"                                   │
│                                                                              │
│   In our app:                                                                │
│   • Client App (8080) → Handles login, has user session                      │
│   • Resource Server (8081) → Has /photos API, validates JWT                  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 The Flow

```
┌────────────────┐         ┌────────────────┐         ┌────────────────────┐
│   Client App   │  ───▶   │ Resource Server │  ◀───  │ Google (JWKS)      │
│   (Port 8080)  │         │   (Port 8081)   │        │ (Public Keys)      │
└───────┬────────┘         └───────┬────────┘         └────────────────────┘
        │                          │                           │
        │ 1. User logs in          │                           │
        │ 2. Gets JWT (id_token)   │                           │
        │                          │                           │
        │ 3. Call /photos API ─────▶│                           │
        │    Authorization: Bearer │                           │
        │    eyJhbG...             │                           │
        │                          │                           │
        │                          │ 4. Fetch public keys ────▶│
        │                          │    (GET /oauth2/v3/certs)  │
        │                          │◀───────────────────────────│
        │                          │    { keys: [...] }         │
        │                          │                           │
        │                          │ 5. Validate JWT signature │
        │                          │    Check: expiry, issuer  │
        │                          │                           │
        │                          │ 6. If valid → Return data │
        │◀─────────────────────────│                           │
        │    Photos HTML           │                           │
```

---

## 🔧 Resource Server Configuration

### application.yaml

```yaml
server:
  port: 8081  # Different port from Client (8080)

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # Google's public keys endpoint
          jwk-set-uri: https://www.googleapis.com/oauth2/v3/certs
          # Issuer claim that must be present in JWT
          issuer-uri: https://accounts.google.com
```

### SecurityConfig.java

```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()  // All requests need valid JWT
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())  // Enable JWT validation
            );

        return http.build();
    }
}
```

---

## 🔍 JWT Validation Steps

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              RESOURCE SERVER JWT VALIDATION                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Incoming Request:                                                          │
│   Authorization: Bearer eyJhbGciOiJSUzI1NiIs...                              │
│                                                                              │
│   ═══════════════════════════════════════════════════════════════════════    │
│                                                                              │
│   STEP 1: Extract JWT                                                        │
│   ─────────────────────                                                      │
│   Remove "Bearer " prefix → eyJhbGciOiJSUzI1NiIs...                          │
│                                                                              │
│   STEP 2: Decode JWT (3 parts)                                               │
│   ─────────────────────────────                                              │
│   header.payload.signature → Decode Base64                                   │
│                                                                              │
│   STEP 3: Fetch Google's Public Keys                                         │
│   ─────────────────────────────────                                          │
│   GET https://www.googleapis.com/oauth2/v3/certs                             │
│   Response: { keys: [{ kid: "xxx", n: "...", e: "..." }] }                   │
│   (These are cached for performance!)                                        │
│                                                                              │
│   STEP 4: Verify Signature ✅                                                │
│   ─────────────────────────                                                  │
│   Use public key to verify JWT signature                                     │
│   If invalid → 401 Unauthorized ❌                                           │
│                                                                              │
│   STEP 5: Validate Claims ✅                                                 │
│   ────────────────────────                                                   │
│   • iss == "https://accounts.google.com" ✅                                  │
│   • exp > current timestamp (not expired) ✅                                 │
│   • aud == our client ID ✅                                                  │
│                                                                              │
│   STEP 6: Grant Access! ✅                                                   │
│   ────────────────────────                                                   │
│   Request proceeds to controller                                             │
│   Return: Photos HTML                                                        │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📝 Client App Code: Sending JWT

```java
@GetMapping("/api/photos")
public String getPhotosFromResourceServer(
    @AuthenticationPrincipal OidcUser oidcUser
) {
    // 1. Get JWT (id_token) from session
    String idToken = oidcUser.getIdToken().getTokenValue();
    
    // 2. Create HTTP request with Bearer token
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(idToken);  // Sets "Authorization: Bearer eyJhbG..."
    
    HttpEntity<String> entity = new HttpEntity<>(headers);
    
    // 3. Call Resource Server
    ResponseEntity<String> response = restTemplate.exchange(
        "http://localhost:8081/photos",  // Resource Server URL
        HttpMethod.GET,
        entity,
        String.class
    );
    
    return response.getBody();
}
```

---

## ⚠️ Why JWT, Not Access Token?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    WHY JWT (id_token) ?                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ACCESS TOKEN (ya29.xxx):                                                   │
│   ─────────────────────────                                                  │
│   • Opaque - random string                                                   │
│   • Only Google can decode                                                   │
│   • Resource Server CAN'T validate! ❌                                       │
│                                                                              │
│   ID TOKEN (eyJhbG...):                                                      │
│   ───────────────────                                                        │
│   • JWT - structured format                                                  │
│   • Signed with Google's private key                                         │
│   • Anyone with public key can validate! ✅                                  │
│                                                                              │
│   Our Resource Server uses Google's PUBLIC KEYS to validate JWT!             │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔐 Security: Automatic by Spring Security

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    YOU DON'T WRITE VALIDATION CODE!                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Just add:                                                                  │
│   • application.yaml: jwk-set-uri, issuer-uri                                │
│   • SecurityConfig: .oauth2ResourceServer(oauth2 -> oauth2.jwt(...))         │
│                                                                              │
│   Spring Security automatically:                                             │
│   • Adds JwtAuthenticationFilter to filter chain                             │
│   • Fetches & caches public keys                                             │
│   • Validates every incoming request                                         │
│   • Rejects invalid tokens with 401                                          │
│                                                                              │
│   Your controller only runs if JWT is valid!                                 │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 Summary

| Component | Role |
|-----------|------|
| **Client App** | Has session, JWT stored, sends to Resource Server |
| **Resource Server** | Validates JWT, serves protected APIs |
| **Google JWKS** | Provides public keys for validation |
