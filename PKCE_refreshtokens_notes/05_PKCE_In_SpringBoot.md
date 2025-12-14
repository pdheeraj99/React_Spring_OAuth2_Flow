# 05 - PKCE in Spring Boot

> 📌 How to enable PKCE in your backend application

---

## 🤔 Your Question

*"Backend lo automatic ga PKCE handle avtunda? Or manual ga code raayala?"*

**Answer: FULLY AUTOMATIC in Spring Boot!** 🎉

---

## SPA vs Spring Boot - Who Does the Work?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              PKCE IMPLEMENTATION COMPARISON                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   SPA (React Only):                                                          │
│   ─────────────────                                                          │
│   YOU write code to:                                                         │
│   • Generate code_verifier ✍️                                                │
│   • Hash to create code_challenge ✍️                                         │
│   • Store in sessionStorage ✍️                                               │
│   • Add to authorization URL ✍️                                              │
│   • Send in token exchange ✍️                                                │
│                                                                              │
│   (See our PKCE_demo React app for example!)                                 │
│                                                                              │
│   ───────────────────────────────────────────────────────────────────────    │
│                                                                              │
│   SPRING BOOT:                                                               │
│   ─────────────                                                              │
│   Spring Security does EVERYTHING automatically!                             │
│   • Generate code_verifier ✅ (Spring does it!)                              │
│   • Hash to create code_challenge ✅ (Spring does it!)                       │
│   • Store in session ✅ (Spring does it!)                                    │
│   • Add to authorization URL ✅ (Spring does it!)                            │
│   • Send in token exchange ✅ (Spring does it!)                              │
│                                                                              │
│   YOU just enable it! One config!                                            │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔧 Enabling PKCE in Spring Boot

### Option 1: Java Config

```java
// SecurityConfig.java

@Configuration
public class SecurityConfig {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(auth -> auth
                    .authorizationRequestResolver(pkceResolver())
                )
            );
        return http.build();
    }

    // Enable PKCE
    private OAuth2AuthorizationRequestResolver pkceResolver() {
        DefaultOAuth2AuthorizationRequestResolver resolver = 
            new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, 
                "/oauth2/authorization"
            );
        
        // This one line enables PKCE!
        resolver.setAuthorizationRequestCustomizer(
            OAuth2AuthorizationRequestCustomizers.withPkce()
        );
        
        return resolver;
    }
}
```

That's it! After this, Spring automatically:

1. Generates code_verifier (random 128 chars)
2. Creates code_challenge (SHA256)
3. Adds code_challenge to Google authorization URL
4. Stores code_verifier in session
5. Sends code_verifier during token exchange

---

## 🎯 What Changes in the URL?

```
WITHOUT PKCE:
─────────────
https://accounts.google.com/o/oauth2/v2/auth?
    client_id=xxx
    &response_type=code
    &redirect_uri=http://localhost:8080/login/oauth2/code/google
    &scope=openid email profile

WITH PKCE (Spring adds automatically):
──────────────────────────────────────
https://accounts.google.com/o/oauth2/v2/auth?
    client_id=xxx
    &response_type=code
    &redirect_uri=http://localhost:8080/login/oauth2/code/google
    &scope=openid email profile
    &code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM  ← ADDED!
    &code_challenge_method=S256                                   ← ADDED!
```

---

## 🤔 Bonus: client_authentication_method

You asked about `client-authentication-method: client_secret_post`

This is NOT about PKCE! It's about how client_secret is sent:

```yaml
# application.yaml

spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-authentication-method: client_secret_basic  # Default
            # OR
            client-authentication-method: client_secret_post
```

```
client_secret_basic (Default):
──────────────────────────────
Secret goes in HTTP Header (Base64 encoded)
Authorization: Basic base64(client_id:client_secret)

client_secret_post:
───────────────────
Secret goes in POST body
{ "client_id": "xxx", "client_secret": "yyy", ... }

BOTH work! Just different ways to send the secret.
NOT related to PKCE!
```

---

## 📊 Summary

| Question | Answer |
|----------|--------|
| Spring lo PKCE manual raayala? | NO! Automatic! |
| Just enable cheyala? | YES! One resolver config! |
| client_authentication_method = PKCE? | NO! Unrelated! |
| PKCE with client_secret? | YES! Double protection! |

---

**Next:** [06_Refresh_Tokens_Why.md](./06_Refresh_Tokens_Why.md)
