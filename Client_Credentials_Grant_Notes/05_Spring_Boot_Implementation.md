# 05 - Spring Boot Implementation

> 📌 Complete code for both the CLIENT (requestor) and the RESOURCE SERVER (validator)!

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    TWO SPRING BOOT APPS                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌─────────────────┐              ┌─────────────────┐                       │
│   │  Order Service  │              │ Inventory Service│                      │
│   │  (OAuth Client) │  ─────────►  │ (Resource Server)│                      │
│   │     :8080       │  access_token│      :8081       │                      │
│   └─────────────────┘              └─────────────────┘                       │
│          │                                  │                                │
│          │  Get token                       │  Validate token                │
│          ▼                                  ▼                                │
│   ┌─────────────────────────────────────────────────────────────────┐        │
│   │                    Auth Server (Keycloak/Auth0)                 │        │
│   │                           :9000                                 │        │
│   └─────────────────────────────────────────────────────────────────┘        │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📦 Part 1: The Client (Order Service)

### Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- OAuth2 Client - For getting tokens -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>
    
    <!-- WebFlux for WebClient -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
</dependencies>
```

### Configuration (application.yaml)

```yaml
spring:
  application:
    name: order-service
    
  security:
    oauth2:
      client:
        registration:
          inventory-api:                    # Registration name
            client-id: order-service
            client-secret: ${ORDER_SERVICE_SECRET}
            authorization-grant-type: client_credentials  # THE KEY!
            scope: read:inventory,write:orders
            
        provider:
          inventory-api:
            token-uri: http://localhost:9000/oauth/token
            
server:
  port: 8080
```

### WebClient Configuration (with OAuth2)

```java
package com.example.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {
        
        OAuth2AuthorizedClientProvider authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()  // Enable Client Credentials!
                .build();
        
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
            new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);
        
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        
        return authorizedClientManager;
    }

    @Bean
    public WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        
        // Default to our registered client
        oauth2Client.setDefaultClientRegistrationId("inventory-api");
        
        return WebClient.builder()
            .apply(oauth2Client.oauth2Configuration())
            .build();
    }
}
```

### Using the WebClient (Auto-attaches token!)

```java
package com.example.orderservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class InventoryService {

    private final WebClient webClient;
    
    public InventoryService(WebClient webClient) {
        this.webClient = webClient;
    }

    public StockInfo getStock(String productId) {
        // WebClient automatically:
        // 1. Gets token from auth server (if not cached)
        // 2. Adds Authorization: Bearer xxx header
        // 3. Makes the request!
        
        return webClient.get()
            .uri("http://localhost:8081/api/stock/{productId}", productId)
            .retrieve()
            .bodyToMono(StockInfo.class)
            .block();
    }
}
```

---

## 📦 Part 2: The Resource Server (Inventory Service)

### Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- OAuth2 Resource Server - For validating tokens -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
</dependencies>
```

### Configuration (application.yaml)

```yaml
spring:
  application:
    name: inventory-service
    
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9000
          # OR specify jwk-set-uri directly:
          # jwk-set-uri: http://localhost:9000/.well-known/jwks.json
          
server:
  port: 8081
```

### Security Configuration

```java
package com.example.inventoryservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Require specific scope for inventory endpoints
                .requestMatchers("/api/stock/**").hasAuthority("SCOPE_read:inventory")
                .requestMatchers("/api/orders/**").hasAuthority("SCOPE_write:orders")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})  // Validate JWT tokens
            );
            
        return http.build();
    }
}
```

### Protected Controller

```java
package com.example.inventoryservice.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @GetMapping("/{productId}")
    public StockInfo getStock(
            @PathVariable String productId,
            @AuthenticationPrincipal Jwt jwt) {
        
        // jwt.getSubject() = "order-service" (the calling app!)
        // jwt.getClaim("scope") = "read:inventory write:orders"
        
        System.out.println("Request from: " + jwt.getSubject());
        
        return new StockInfo(productId, 100, "In Stock");
    }
}
```

---

## 🎨 Complete Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    COMPLETE CLIENT CREDENTIALS FLOW                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   1. Order Service needs stock data                                          │
│      webClient.get().uri("/api/stock/123")...                                │
│                                                                              │
│   2. WebClient's OAuth2 filter checks: "Do I have a valid token?"            │
│      First time: No → Request token!                                         │
│                                                                              │
│   3. WebClient automatically calls Auth Server:                              │
│      POST /oauth/token                                                       │
│      grant_type=client_credentials                                           │
│      client_id=order-service                                                 │
│      client_secret=xxx                                                       │
│                                                                              │
│   4. Auth Server returns token                                               │
│      { "access_token": "eyJhbG...", "expires_in": 3600 }                     │
│                                                                              │
│   5. WebClient caches token and adds to request:                             │
│      GET /api/stock/123                                                      │
│      Authorization: Bearer eyJhbG...                                         │
│                                                                              │
│   6. Inventory Service validates token:                                      │
│      - Signature valid? ✓                                                    │
│      - Not expired? ✓                                                        │
│      - Has read:inventory scope? ✓                                           │
│                                                                              │
│   7. Inventory Service returns stock data!                                   │
│                                                                              │
│   8. Next request: WebClient reuses cached token (until it expires)          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 💡 Key Points

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT MAGIC                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   CLIENT SIDE (Order Service):                                               │
│   • spring-boot-starter-oauth2-client                                        │
│   • authorization-grant-type: client_credentials                             │
│   • WebClient with OAuth2 filter = Auto token management!                    │
│                                                                              │
│   RESOURCE SERVER (Inventory Service):                                       │
│   • spring-boot-starter-oauth2-resource-server                               │
│   • jwt.issuer-uri points to auth server                                     │
│   • @AuthenticationPrincipal Jwt = Access token claims                       │
│                                                                              │
│   ⭐ You write almost no OAuth code - Spring handles it!                     │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🤔 Beginner Check

1. Which dependency does the CLIENT (token requestor) need?
2. Which dependency does the RESOURCE SERVER need?
3. What config property specifies "client_credentials" flow?
4. How do you check scope in Resource Server?

Answers:

1. `spring-boot-starter-oauth2-client`
2. `spring-boot-starter-oauth2-resource-server`
3. `authorization-grant-type: client_credentials`
4. `.hasAuthority("SCOPE_read:inventory")`

---

**Next:** [06_Common_Use_Cases.md](./06_Common_Use_Cases.md)
