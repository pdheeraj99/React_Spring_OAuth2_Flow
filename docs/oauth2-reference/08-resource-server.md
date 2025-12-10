# Resource Server Flow

> 🎓 **JWT Validation and Protected APIs**

---

## 🏗️ Architecture Overview

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   React     │────►│ Client-App  │────►│  Resource   │
│   (5174)    │     │   (8080)    │     │  Server     │
│             │     │   BFF       │     │  (8081)     │
└─────────────┘     └─────────────┘     └─────────────┘
                          │
                    Gets JWT from
                    session, sends
                    to Resource Server
```

---

## 📍 Step-by-Step: GET /api/photos

### STEP 1: React Calls BFF

```javascript
// React App
fetch('http://localhost:8080/api/photos', {
    credentials: 'include'  // Send cookies
})
```

```http
GET /api/photos HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=440421F81FB0CD37C26E757DADE4CBAB
```

From logs:
```
🚀 REQUEST #11 - GET /api/photos
════════════════════════════════════════════════════════════════════════════════

📥 STEP 1: REQUEST VACHINDI (Incoming Request)
────────────────────────────────────────────────────────────────────────────────
   🌐 URI: /api/photos
   📝 Method: GET

📦 STEP 2: SESSION CHECK (Server-side storage)
────────────────────────────────────────────────────────────────────────────────
   ✅ Session EXISTS
   🆔 Session ID: 440421F8...

⚙️ STEP 4: EE REQUEST NI EVARU HANDLE CHESTARU?
────────────────────────────────────────────────────────────────────────────────
   📸 /api/photos - SPECIAL FLOW:
      1️⃣ Session nundi ID Token (JWT) extract chestaru
      2️⃣ Resource Server (8081) ki call chestaru with JWT
      3️⃣ Header: Authorization: Bearer <JWT>
```

---

### STEP 2: BFF Gets JWT from Session

```java
@GetMapping("/api/photos")
public String getPhotos(@AuthenticationPrincipal OidcUser user) {
    
    // 1. Get ID Token (JWT) from session
    String jwt = user.getIdToken().getTokenValue();
    // "eyJhbGciOiJSUzI1NiIsImtpZCI6ImQ1NDNl..."
    
    // 2. Create request to Resource Server
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(jwt);  // Authorization: Bearer <JWT>
    
    HttpEntity<String> entity = new HttpEntity<>(headers);
    
    // 3. Call Resource Server
    ResponseEntity<String> response = restTemplate.exchange(
        "http://localhost:8081/photos",
        HttpMethod.GET,
        entity,
        String.class
    );
    
    return response.getBody();
}
```

From logs:
```
🔑 CALLING RESOURCE SERVER with ID Token (JWT)...
Token (first 50 chars): eyJhbGciOiJSUzI1NiIsImtpZCI6ImQ1NDNlMjFhMDI3M2VmYz...

DEBUG RestTemplate : HTTP GET http://localhost:8081/photos
DEBUG RestTemplate : Accept=[text/plain, application/json, application/*+json, */*]
```

---

### STEP 3: BFF → Resource Server Request

```http
GET /photos HTTP/1.1
Host: localhost:8081
Authorization: Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6ImQ1NDNlMjFhMDI3M2VmYz...
Accept: text/plain, application/json, */*
```

---

### STEP 4: Resource Server Validates JWT

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    RESOURCE SERVER (Port 8081)                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Filter: BearerTokenAuthenticationFilter                                    │
│  ─────────────────────────────────────────                                   │
│                                                                              │
│  1. Extract JWT from Authorization header                                   │
│     Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6ImQ1NDNl...                         │
│                                                                              │
│  2. Decode JWT header                                                        │
│     {                                                                        │
│       "alg": "RS256",                                                        │
│       "kid": "d543e21a0273efc..."  ← KEY ID                                │
│     }                                                                        │
│                                                                              │
│  3. Fetch Google's public keys (JWKS)                                       │
│     GET https://www.googleapis.com/oauth2/v3/certs                         │
│                                                                              │
│     Response:                                                                │
│     {                                                                        │
│       "keys": [                                                              │
│         { "kid": "d543e21a...", "n": "...", "e": "..." },                   │
│         { "kid": "abc123...", "n": "...", "e": "..." }                      │
│       ]                                                                      │
│     }                                                                        │
│                                                                              │
│  4. Find matching key by 'kid'                                              │
│     publicKey = keys.find(k => k.kid === "d543e21a...")                    │
│                                                                              │
│  5. Verify signature                                                         │
│     verify(header + "." + payload, signature, publicKey)                   │
│     ✅ VALID! Token was signed by Google                                   │
│                                                                              │
│  6. Validate claims                                                          │
│     ├── exp > now? ✅ Not expired                                          │
│     ├── iss == "https://accounts.google.com"? ✅ Correct issuer            │
│     └── aud == our-client-id? ✅ Token for us                              │
│                                                                              │
│  7. Create Authentication                                                    │
│     JwtAuthenticationToken {                                                │
│       principal: { sub: "112416036...", email: "dheeraj@...", ... }        │
│       authenticated: true                                                   │
│     }                                                                        │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### STEP 5: Resource Server Controller

```java
@GetMapping("/photos")
public String getPhotos(@AuthenticationPrincipal Jwt jwt) {
    
    // Extract user info from JWT claims
    String userId = jwt.getSubject();           // "112416036337094439562"
    String email = jwt.getClaim("email");       // "dheerajp0299@gmail.com"
    String name = jwt.getClaim("name");         // "Dheeraj"
    
    // Return personalized photos
    return """
        <h1>Welcome %s!</h1>
        <p>Email: %s</p>
        <p>Your secret photos:</p>
        <img src="photo1.jpg" />
        <img src="photo2.jpg" />
    """.formatted(name, email);
}
```

---

### STEP 6: Response Back to BFF

```http
HTTP/1.1 200 OK
Content-Type: text/html;charset=UTF-8

<h1>Welcome Dheeraj!</h1>
<p>Email: dheerajp0299@gmail.com</p>
...
```

From logs:
```
DEBUG RestTemplate : Response 200 OK
DEBUG RestTemplate : Reading to [java.lang.String] as "text/plain;charset=UTF-8"
🔑 RESOURCE SERVER RESPONSE: 200 OK

📤 STEP 5: RESPONSE READY (Filter chain complete)
────────────────────────────────────────────────────────────────────────────────
   📊 Status Code: 200
   ✅ SUCCESS - Data returned!
```

---

## 📊 Resource Server Configuration

```yaml
# resource-server/application.yaml
server:
  port: 8081

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # Where to get Google's public keys
          jwk-set-uri: https://www.googleapis.com/oauth2/v3/certs
          # Expected issuer
          issuer-uri: https://accounts.google.com
```

---

## 📊 Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           BROWSER (React)                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│  1. Click "Get My Secret Photos"                                            │
│  2. Send: GET /api/photos with Cookie: JSESSIONID=...                       │
└───────────────────────────────────┬─────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      CLIENT BACKEND (BFF - 8080)                             │
├─────────────────────────────────────────────────────────────────────────────┤
│  3. Load OidcUser from session                                              │
│  4. Extract ID Token (JWT): user.getIdToken().getTokenValue()               │
│  5. Call Resource Server with: Authorization: Bearer <JWT>                  │
└───────────────────────────────────┬─────────────────────────────────────────┘
                                    │
                          GET /photos
                          Authorization: Bearer eyJhbGci...
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      RESOURCE SERVER (8081)                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│  6. Extract JWT from Authorization header                                   │
│  7. Fetch Google's public keys (JWKS)                                       │
│  8. Verify JWT signature                                                     │
│  9. Validate claims (iss, aud, exp)                                         │
│  10. Extract user info from JWT                                             │
│  11. Return personalized photos                                             │
└───────────────────────────────────┬─────────────────────────────────────────┘
                                    │
                          200 OK + Photos HTML
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      CLIENT BACKEND (BFF - 8080)                             │
├─────────────────────────────────────────────────────────────────────────────┤
│  12. Receive photos from Resource Server                                    │
│  13. Return to browser                                                       │
└───────────────────────────────────┬─────────────────────────────────────────┘
                                    │
                          200 OK + Photos HTML
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           BROWSER (React)                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│  14. Display photos 🎉                                                       │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔐 Security Key Points

| Point | Description |
|-------|-------------|
| JWT never in browser | Browser only has JSESSIONID cookie |
| BFF extracts JWT | Gets from session, sends to RS |
| RS validates signature | Uses Google's public key |
| RS checks issuer | Must be accounts.google.com |
| RS checks audience | Token must be for our app |
| RS checks expiry | Token must not be expired |

---

## 📋 Summary

| Step | Who | What |
|------|-----|------|
| 1-2 | Browser → BFF | Request with JSESSIONID cookie |
| 3-5 | BFF | Extract JWT from session, call RS |
| 6-10 | Resource Server | Validate JWT using JWKS |
| 11 | Resource Server | Return protected data |
| 12-14 | BFF → Browser | Return data to user |

---

> 📖 **Next:** [09-complete-flow.md](./09-complete-flow.md) - End-to-End with All Diagrams
