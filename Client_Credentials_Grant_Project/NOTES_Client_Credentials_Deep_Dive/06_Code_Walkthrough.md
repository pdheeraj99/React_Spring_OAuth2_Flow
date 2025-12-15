# 💻 06. Code Walkthrough - Line by Line

## 📁 Files We'll Cover

```
Option1_Spring_Auth_Server/
├── auth-server/
│   └── AuthorizationServerConfig.java     ← Token issuing
├── order-service/
│   ├── application.yaml                   ← Client config
│   └── WebClientConfig.java               ← Token auto-attach
└── inventory-service/
    ├── application.yaml                   ← JWT validation config
    └── SecurityConfig.java                ← Access control
```

---

# 🔐 AUTH-SERVER Code

## File: `AuthorizationServerConfig.java`

### Section 1: Registered Clients (Lines 81-113)

```java
@Bean
public RegisteredClientRepository registeredClientRepository() {
    // ⭐ ORDER-SERVICE: Uses Client Credentials Grant
    RegisteredClient orderService = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("order-service")                         // ← Username for the app
            .clientSecret("{noop}order-service-secret")        // ← Password for the app
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)  // ← Send in Header
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)           // ← THE KEY!
            .scope("read:inventory")                           // ← What permissions allowed
            .scope("write:orders")
            .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofHours(1)) // ← Token expiry
                    .build())
            .clientSettings(ClientSettings.builder()
                    .requireAuthorizationConsent(false)         // ← No consent popup
                    .build())
            .build();

    return new InMemoryRegisteredClientRepository(orderService, notificationService);
}
```

### 🔍 Line-by-Line Explanation

| Line | Code | Meaning |
|------|------|---------|
| `.clientId("order-service")` | App ka username | Unique identifier for this app |
| `.clientSecret("{noop}order-service-secret")` | App ka password | `{noop}` = plain text (only for demo!) |
| `.clientAuthenticationMethod(CLIENT_SECRET_BASIC)` | How to send credentials | Base64 encode and send in Header |
| `.authorizationGrantType(CLIENT_CREDENTIALS)` | Grant type | **THE KEY CONFIG!** App tokens, no user |
| `.scope("read:inventory")` | Permissions | What this app is allowed to access |
| `.accessTokenTimeToLive(Duration.ofHours(1))` | Token expiry | Token valid for 1 hour |
| `InMemoryRegisteredClientRepository` | Storage | In RAM (demo only, production uses DB!) |

---

### Section 2: JWK Source - Key Generation (Lines 118-143)

```java
@Bean
public JWKSource<SecurityContext> jwkSource() {
    KeyPair keyPair = generateRsaKey();                              // ← Generate RSA key pair
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();     // ← Public (share this!)
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate(); // ← Private (keep secret!)

    RSAKey rsaKey = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())   // ← Unique key ID
            .build();

    JWKSet jwkSet = new JWKSet(rsaKey);
    return new ImmutableJWKSet<>(jwkSet);          // ← Spring uses this for signing
}

private static KeyPair generateRsaKey() {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);             // ← 2048-bit key (secure!)
    return keyPairGenerator.generateKeyPair();
}
```

### 🔍 What This Does

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  1. Generate RSA Key Pair (2048 bits)                              ║
║     - Private Key: For SIGNING tokens                              ║
║     - Public Key: For VERIFYING tokens                             ║
║                                                                    ║
║  2. Wrap in JWKSet                                                 ║
║     - JWK = JSON Web Key (standard format)                         ║
║                                                                    ║
║  3. Spring automatically:                                          ║
║     - Uses private key to sign JWTs                                ║
║     - Exposes public key at /oauth2/jwks endpoint                  ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

### Section 3: Authorization Server Settings (Lines 148-153)

```java
@Bean
public AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder()
            .issuer("http://localhost:9000")   // ← IMPORTANT!
            .build();
}
```

### 🔍 Why Issuer is Important

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  ISSUER = "http://localhost:9000"                                  ║
║                                                                    ║
║  This value goes INTO the JWT token as "iss" claim!                ║
║                                                                    ║
║  Resource Server (Inventory) checks:                               ║
║  "Token's issuer == My configured issuer-uri?"                     ║
║  If not matching → REJECT TOKEN!                                   ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

# 📦 ORDER-SERVICE Code

## File: `application.yaml`

```yaml
server:
  port: 8080

spring:
  security:
    oauth2:
      client:
        registration:
          inventory-client:                                    # ← Registration name
            client-id: order-service                           # ← Same as Auth Server
            client-secret: order-service-secret                # ← Same as Auth Server
            authorization-grant-type: client_credentials       # ← THE KEY!
            scope: read:inventory                              # ← Request this scope
            
        provider:
          inventory-client:
            token-uri: http://localhost:9000/oauth2/token      # ← Where to get token

inventory:
  service:
    url: http://localhost:8081                                 # ← Inventory URL
```

### 🔍 Line-by-Line Explanation

| Line | Meaning |
|------|---------|
| `registration: inventory-client` | A name for this OAuth2 client config |
| `client-id: order-service` | Must match what's in Auth Server! |
| `client-secret: order-service-secret` | Must match what's in Auth Server! |
| `authorization-grant-type: client_credentials` | Tell Spring to use Client Credentials flow |
| `scope: read:inventory` | Request this permission |
| `token-uri` | Auth Server's token endpoint |

---

## File: `WebClientConfig.java`

```java
@Configuration
public class WebClientConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        // ⭐ Enable Client Credentials grant type
        OAuth2AuthorizedClientProvider authorizedClientProvider = 
            OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()       // ← THE KEY LINE!
                .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = 
            new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    public WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = 
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        // Default to our "inventory-client" registration
        oauth2Client.setDefaultClientRegistrationId("inventory-client");

        return WebClient.builder()
                .apply(oauth2Client.oauth2Configuration())   // ← Magic happens here!
                .build();
    }
}
```

### 🔍 What Each Part Does

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  authorizedClientManager:                                          ║
║  ─────────────────────────                                         ║
║  • Manages fetching tokens from Auth Server                        ║
║  • Caches tokens (doesn't fetch new one every time!)               ║
║  • Automatically refreshes when expired                            ║
║                                                                    ║
║  .clientCredentials():                                             ║
║  ─────────────────────                                             ║
║  • Tells Spring we're using Client Credentials flow                ║
║  • Will send client_id + secret to Auth Server                     ║
║                                                                    ║
║  oauth2Client.oauth2Configuration():                               ║
║  ─────────────────────────────────────                             ║
║  • MAGIC! Automatically attaches Bearer token to all requests      ║
║  • Developer doesn't need to manually add Authorization header     ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

# 🔒 INVENTORY-SERVICE Code

## File: `application.yaml`

```yaml
server:
  port: 8081

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9000   # ← Auth Server URL
```

### 🔍 What Happens at Startup

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  Inventory-Service starts...                                       ║
║                                                                    ║
║  1. Reads issuer-uri: http://localhost:9000                        ║
║                                                                    ║
║  2. Calls: http://localhost:9000/.well-known/openid-configuration  ║
║     Gets back:                                                     ║
║     {                                                              ║
║       "issuer": "http://localhost:9000",                           ║
║       "jwks_uri": "http://localhost:9000/oauth2/jwks",             ║
║       "token_endpoint": "http://localhost:9000/oauth2/token"       ║
║     }                                                              ║
║                                                                    ║
║  3. Fetches PUBLIC KEYS from jwks_uri                              ║
║                                                                    ║
║  4. Caches keys for JWT validation                                 ║
║                                                                    ║
║  Now ready to validate tokens!                                     ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## File: `SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // ⭐ Require specific scope for stock endpoints
                .requestMatchers("/api/stock/**").hasAuthority("SCOPE_read:inventory")
                .anyRequest().authenticated())
            // ⭐ Enable Resource Server with JWT validation
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {}));

        return http.build();
    }
}
```

### 🔍 Line-by-Line Explanation

| Line | Meaning |
|------|---------|
| `.requestMatchers("/api/stock/**")` | For any URL starting with /api/stock/ |
| `.hasAuthority("SCOPE_read:inventory")` | Token must have this scope! |
| `.oauth2ResourceServer().jwt()` | Enable JWT token validation |

### 🔍 SCOPE_ Prefix

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  JWT TOKEN contains:                                               ║
║  { "scope": "read:inventory" }                                     ║
║                                                                    ║
║  Spring AUTOMATICALLY converts to:                                 ║
║  Authority: "SCOPE_read:inventory"                                 ║
║             ↑                                                      ║
║             Spring adds this prefix!                               ║
║                                                                    ║
║  So in code, you check: hasAuthority("SCOPE_read:inventory")       ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 🎯 Summary: What Each Service Does

| Service | Role | Key Config |
|---------|------|------------|
| **Auth-Server** | Issues tokens | `RegisteredClientRepository` + `JWKSource` |
| **Order-Service** | Gets & uses tokens | `WebClientConfig` + `application.yaml` |
| **Inventory-Service** | Validates tokens | `SecurityConfig` + `issuer-uri` |

---

## 💡 Magic Behind the Scenes

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  WHAT WE WROTE:                                                    ║
║  ──────────────                                                    ║
║  • Configuration classes                                           ║
║  • YAML properties                                                 ║
║                                                                    ║
║  WHAT SPRING DOES AUTOMATICALLY:                                   ║
║  ─────────────────────────────────                                 ║
║  • Creates /oauth2/token endpoint                                  ║
║  • Creates /oauth2/jwks endpoint                                   ║
║  • Generates RSA keys                                              ║
║  • Signs JWTs                                                      ║
║  • Fetches tokens                                                  ║
║  • Attaches tokens to requests                                     ║
║  • Validates JWTs                                                  ║
║  • Checks scopes                                                   ║
║                                                                    ║
║  WE CONFIGURE, SPRING IMPLEMENTS!                                  ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

**Next:** [07_Common_Doubts.md](./07_Common_Doubts.md) - All FAQs and explanations
