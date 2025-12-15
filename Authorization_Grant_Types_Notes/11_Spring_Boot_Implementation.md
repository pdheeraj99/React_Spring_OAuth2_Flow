# 11 - Spring Boot Implementation

> 📌 Complete code walkthrough - from config to working app!

---

## 🏗️ Project Structure

```
client-app/
├── src/main/java/com/oauth/client_app/
│   ├── ClientAppApplication.java      # Main class
│   ├── config/
│   │   └── SecurityConfig.java        # OAuth2 configuration
│   └── controller/
│       └── UserController.java        # Protected endpoints
├── src/main/resources/
│   └── application.yaml               # OAuth2 client config
└── pom.xml                            # Dependencies
```

---

## 📦 Step 1: Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- OAuth2 Client - THE MAGIC! -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>
    
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</dependencies>
```

---

## ⚙️ Step 2: Configuration (application.yaml)

```yaml
spring:
  application:
    name: client-app
  security:
    oauth2:
      client:
        registration:
          google:
            # These come from Google Cloud Console
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            
            # What permissions we want
            scope:
              - openid    # Get id_token (JWT)
              - email     # Get user's email
              - profile   # Get user's name, picture
              
            # Where Google sends user after login
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            
            # Name shown on login page
            client-name: Google Login
            
        provider:
          google:
            # Google's OAuth endpoints
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
            user-name-attribute: sub

server:
  port: 8080
```

### Configuration Explained

| Property | Purpose |
|----------|---------|
| `client-id` | Identifies your app to Google |
| `client-secret` | Proves you're the real app (Step 5) |
| `scope` | What data you want access to |
| `redirect-uri` | Where Google redirects after login |
| `authorization-uri` | Google's login page (Step 2) |
| `token-uri` | Where to exchange code for tokens (Step 5) |

---

## 🔐 Step 3: Security Configuration

```java
package com.oauth.client_app.config;

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
            // URL Authorization Rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no login needed)
                .requestMatchers("/", "/public/**", "/error").permitAll()
                
                // Everything else requires login
                .anyRequest().authenticated()
            )
            
            // Enable OAuth2 Login
            .oauth2Login(oauth2 -> oauth2
                // Custom login page (optional)
                .loginPage("/login")
                
                // Where to go after successful login
                .defaultSuccessUrl("/dashboard", true)
                
                // Where to go if login fails
                .failureUrl("/login?error=true")
            )
            
            // Logout configuration
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
            );
            
        return http.build();
    }
}
```

### What Each Part Does

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   authorizeHttpRequests:                                                     │
│   → Defines which URLs need login                                            │
│   → "/" and "/public/**" = anyone can access                                 │
│   → Everything else = must be logged in                                      │
│                                                                              │
│   oauth2Login:                                                               │
│   → Enables "Login with Google"                                              │
│   → Handles Steps 2-5 automatically!                                         │
│   → Creates the /oauth2/authorization/google endpoint                        │
│   → Creates the /login/oauth2/code/google callback handler                   │
│                                                                              │
│   logout:                                                                    │
│   → Clears session when user logs out                                        │
│   → Redirects to home page                                                   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Step 4: Controller (Using the User Info)

```java
package com.oauth.client_app.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class UserController {

    // Get basic user status
    @GetMapping("/api/user-status")
    public Map<String, Object> getUserStatus(@AuthenticationPrincipal OidcUser user) {
        Map<String, Object> response = new HashMap<>();
        
        if (user != null) {
            response.put("isLoggedIn", true);
            response.put("name", user.getFullName());        // From id_token
            response.put("email", user.getEmail());          // From id_token
            response.put("picture", user.getPicture());      // From id_token
            response.put("sub", user.getSubject());          // Unique user ID
        } else {
            response.put("isLoggedIn", false);
        }
        
        return response;
    }
    
    // Get the actual id_token (to send to Resource Server)
    @GetMapping("/api/id-token")
    public Map<String, String> getIdToken(@AuthenticationPrincipal OidcUser user) {
        Map<String, String> response = new HashMap<>();
        
        if (user != null && user.getIdToken() != null) {
            response.put("idToken", user.getIdToken().getTokenValue());
        }
        
        return response;
    }
    
    // Get all claims from id_token
    @GetMapping("/api/claims")
    public Map<String, Object> getClaims(@AuthenticationPrincipal OidcUser user) {
        if (user != null) {
            return user.getClaims();  // All JWT claims as Map
        }
        return Collections.emptyMap();
    }
}
```

### The Magic: @AuthenticationPrincipal OidcUser

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    @AuthenticationPrincipal OidcUser                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   This annotation:                                                           │
│   → Injects the currently logged-in user                                     │
│   → User info from id_token is automatically available                       │
│   → If not logged in, user is null                                           │
│                                                                              │
│   OidcUser methods:                                                          │
│   ┌─────────────────────────┬────────────────────────────────────────────┐   │
│   │ Method                  │ Returns                                    │   │
│   ├─────────────────────────┼────────────────────────────────────────────┤   │
│   │ getSubject()            │ Unique user ID (sub claim)                 │   │
│   │ getEmail()              │ User's email address                       │   │
│   │ getFullName()           │ User's full name                           │   │
│   │ getPicture()            │ Profile picture URL                        │   │
│   │ getIdToken()            │ The actual JWT token object                │   │
│   │ getClaims()             │ All claims as Map<String, Object>          │   │
│   └─────────────────────────┴────────────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Step 5: Running the App

```bash
# Set environment variables
export GOOGLE_CLIENT_ID=your-client-id
export GOOGLE_CLIENT_SECRET=your-client-secret

# Run the app
mvn spring-boot:run

# Or with inline variables
GOOGLE_CLIENT_ID=xxx GOOGLE_CLIENT_SECRET=yyy mvn spring-boot:run
```

---

## 🌐 Step 6: The Flow in Action

```
1. User visits: http://localhost:8080/dashboard
   → Not logged in, redirected to /login

2. User clicks: "Login with Google"
   → Goes to /oauth2/authorization/google (Spring endpoint)

3. Spring redirects to Google:
   → accounts.google.com/...?client_id=xxx&scope=openid+email+profile

4. User logs in at Google → Clicks Allow

5. Google redirects back:
   → /login/oauth2/code/google?code=xxx&state=yyy

6. Spring exchanges code for tokens (BACK CHANNEL)

7. Spring creates OidcUser, stores in session

8. User redirected to /dashboard (now logged in!)
```

---

## 🤔 Beginner Check

1. What dependency enables OAuth2 login?
2. What happens if you don't set client-secret?
3. How do you get the user's email in a controller?
4. Which annotation injects the logged-in user?

Answers:

1. `spring-boot-starter-oauth2-client`
2. Token exchange (Step 5) fails - you get 401 from Google
3. `user.getEmail()` where user is `@AuthenticationPrincipal OidcUser`
4. `@AuthenticationPrincipal`

---

**Next:** [12_Common_Confusions.md](./12_Common_Confusions.md)
