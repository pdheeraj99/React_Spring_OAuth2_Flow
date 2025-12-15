# 07 - Security Considerations

> 📌 Client Credentials is simple, but security risks still exist!

---

## ⚠️ Risk 1: client_secret Exposure

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    THE BIGGEST RISK: SECRET EXPOSURE                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   If client_secret is exposed:                                               │
│   → Attacker can impersonate your service!                                   │
│   → Attacker gets full access to everything your service can access!         │
│   → No user approval needed - just credentials!                              │
│                                                                              │
│   EXPOSURE VECTORS:                                                          │
│   ──────────────────                                                         │
│   ❌ Hardcoded in source code → Leaked in GitHub                             │
│   ❌ In docker-compose.yml → Committed to repo                               │
│   ❌ In browser JavaScript → Anyone can view!                                │
│   ❌ In mobile app → Can be decompiled                                       │
│   ❌ In logs → Printed during debugging                                      │
│                                                                              │
│   PROTECTION:                                                                │
│   ────────────                                                               │
│   ✅ Use environment variables: ${ORDER_SERVICE_SECRET}                      │
│   ✅ Use secrets managers (Vault, AWS Secrets Manager)                       │
│   ✅ Never log credentials!                                                  │
│   ✅ Use .gitignore for local config files                                   │
│   ✅ Rotate secrets regularly                                                │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## ⚠️ Risk 2: Over-Privileged Scopes

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    PRINCIPLE OF LEAST PRIVILEGE                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   PROBLEM:                                                                   │
│   Service registers with scope: "admin:everything"                           │
│   → If compromised, attacker has FULL access!                                │
│                                                                              │
│   SOLUTION:                                                                  │
│   Request ONLY what you need!                                                │
│                                                                              │
│   ❌ BAD:                                                                    │
│   scope: "admin:all read:all write:all delete:all"                           │
│                                                                              │
│   ✅ GOOD:                                                                   │
│   Order Service: scope: "read:inventory"                                     │
│   Backup Service: scope: "read:database"                                     │
│   Notification Service: scope: "send:email"                                  │
│                                                                              │
│   Each service has ONLY what it needs!                                       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## ⚠️ Risk 3: No User Context

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    AUDIT TRAIL CHALLENGES                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   PROBLEM:                                                                   │
│   With Client Credentials, you only know WHICH SERVICE made a call,          │
│   not WHICH USER caused that service to make the call.                       │
│                                                                              │
│   Example:                                                                   │
│   Order Service deletes a record.                                            │
│   Log says: "order-service deleted record #123"                              │
│   But WHO triggered this? Which user? 🤷                                     │
│                                                                              │
│   SOLUTION:                                                                  │
│   Pass user context in request (not in token):                               │
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐    │
│   │  POST /api/orders                                                   │    │
│   │  Authorization: Bearer <service_token>                              │    │
│   │  X-User-Id: dheeraj@example.com     ← Custom header                 │    │
│   │  X-Request-Id: abc-123-xyz          ← For tracing                   │    │
│   │                                                                     │    │
│   │  {                                                                  │    │
│   │    "action": "delete",                                              │    │
│   │    "recordId": 123,                                                 │    │
│   │    "initiatedBy": "dheeraj@example.com"  ← In body                  │    │
│   │  }                                                                  │    │
│   └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
│   Now you know: Service = order-service, Triggered by = dheeraj             │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## ⚠️ Risk 4: Token Theft

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ACCESS TOKEN SECURITY                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   If access_token is stolen:                                                 │
│   → Attacker can use it until it expires!                                    │
│   → Usually 1 hour, but still dangerous                                      │
│                                                                              │
│   MITIGATION:                                                                │
│   ────────────                                                               │
│   • Short token expiry (15-60 minutes)                                       │
│   • Token binding (tie token to specific IP/fingerprint)                     │
│   • HTTPS everywhere (prevent interception)                                  │
│   • Monitor for unusual access patterns                                      │
│   • Ability to revoke tokens if breach detected                              │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## ✅ Security Best Practices

### 1. Secret Management

```yaml
# ❌ NEVER do this
client-secret: my-super-secret-password-123

# ✅ Use environment variables
client-secret: ${ORDER_SERVICE_SECRET}

# ✅ Or use Spring Cloud Config with Vault
spring:
  cloud:
    vault:
      token: ${VAULT_TOKEN}
```

### 2. Scope Restriction

```yaml
# ❌ Too broad
scope: admin

# ✅ Specific permissions
scope: read:inventory,read:pricing
```

### 3. Token Validation (Resource Server)

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            // Validate specific scopes for each endpoint
            .requestMatchers(HttpMethod.GET, "/api/stock/**")
                .hasAuthority("SCOPE_read:inventory")
            .requestMatchers(HttpMethod.POST, "/api/stock/**")
                .hasAuthority("SCOPE_write:inventory")
            .requestMatchers("/api/admin/**")
                .hasAuthority("SCOPE_admin")
            .anyRequest().denyAll()  // Deny by default!
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
    
    return http.build();
}
```

### 4. Logging (Without Secrets!)

```java
// ❌ NEVER log tokens or secrets
log.info("Token: " + accessToken);  // BAD!
log.info("Secret: " + clientSecret);  // TERRIBLE!

// ✅ Log safely
log.info("Request from client: {}", jwt.getSubject());
log.info("Scopes: {}", jwt.getClaim("scope"));
log.info("Token expires: {}", jwt.getExpiresAt());
```

### 5. Network Security

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    NETWORK LEVEL SECURITY                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   • HTTPS everywhere (TLS 1.2+)                                              │
│   • Internal services behind firewall                                        │
│   • Service mesh (Istio) for mTLS                                            │
│   • IP whitelisting for sensitive APIs                                       │
│   • Rate limiting to prevent abuse                                           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Security Comparison: Auth Code vs Client Credentials

| Risk | Auth Code Grant | Client Credentials |
|------|----------------|-------------------|
| User impersonation | Possible (steal user token) | N/A (no users) |
| Service impersonation | Less likely | ⚠️ High risk if secret exposed |
| Scope abuse | Limited by user consent | ⚠️ Depends on registration |
| Audit trail | Clear (user identified) | ⚠️ Only service identified |
| Token in browser | Avoidable (BFF pattern) | N/A (server-only) |

---

## 🤔 Beginner Check

1. What's the biggest security risk in Client Credentials?
2. Why is "admin:all" a bad scope?
3. How do you track which USER triggered a service call?
4. Where should client_secret be stored?

Answers:

1. client_secret exposure (attacker can impersonate service)
2. Violates least privilege - too much access if compromised
3. Pass user context in custom headers or request body
4. Environment variables or secrets manager (never in code!)

---

## 🎉 Congratulations

You've completed the Client Credentials Grant learning guide!

### Quick Recap

| Aspect | Client Credentials |
|--------|-------------------|
| User involved? | ❌ No |
| Steps | 2 (request → token) |
| Use case | Machine-to-machine |
| Main risk | Secret exposure |
| Token content | App identity only |

### What's Next?

1. **Practice**: Set up Keycloak and test the flow
2. **Implement**: Add Client Credentials to your microservices
3. **Next Grant**: Refresh Token Grant (coming soon!)

---

**Back to:** [README.md](./README.md)
