# 🔄 03. Request Flow - Complete Step-by-Step

## 📍 Overview: 4 Main Requests

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                     │
│  Req1: User → Order-Service           (Just wants stock info)      │
│  Req2: Order-Service → Auth-Server    (Get token)                  │
│  Res1: Auth-Server → Order-Service    (Here's your token)          │
│  Req3: Order-Service → Inventory      (Get stock, here's my token) │
│  Res3: Inventory → Order-Service      (Here's stock data)          │
│  Res:  Order-Service → User           (Final response)             │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📨 REQUEST 1: User → Order-Service

### User just stock check cheyyadam istadu

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                     │
│  Browser/Postman:                                                   │
│                                                                     │
│  REQUEST:                                                           │
│  ─────────                                                          │
│  Method: GET                                                        │
│  URL:    http://localhost:8080/api/orders/check-stock/laptop-001    │
│                                                                     │
│  HEADERS:                                                           │
│  ─────────                                                          │
│  Accept: application/json                                           │
│  (No Authorization header! User ki token avasaram ledu!)            │
│                                                                     │
│  BODY: Empty (GET request)                                          │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### What Happens Inside Order-Service?

```java
// OrderController.java
@GetMapping("/check-stock/{productId}")
public Mono<String> checkStock(@PathVariable String productId) {
    // User request vachindi...
    // Ippudu Inventory-Service ki call cheyyali
    // BUT! Token kavali! Let me get it first...
    
    return webClient
        .get()
        .uri(inventoryUrl + "/api/stock/" + productId)
        .retrieve()  // ← Ee step lo internally token teeskuntundi!
        .bodyToMono(String.class);
}
```

---

## 📨 REQUEST 2: Order-Service → Auth-Server (Token Request)

### Order-Service internally token teeskuntundi

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                     │
│  AUTOMATIC (Spring does this!):                                     │
│                                                                     │
│  REQUEST:                                                           │
│  ─────────                                                          │
│  Method: POST                                                       │
│  URL:    http://localhost:9000/oauth2/token                         │
│                                                                     │
│  HEADERS:                                                           │
│  ─────────                                                          │
│  Content-Type: application/x-www-form-urlencoded                    │
│  Authorization: Basic b3JkZXItc2VydmljZTpvcmRlci1zZXJ2aWNlLXNlY3JldA== │
│                       ↑                                             │
│                       Base64("order-service:order-service-secret")  │
│                                                                     │
│  BODY (form data):                                                  │
│  ──────────────────                                                 │
│  grant_type=client_credentials                                      │
│  scope=read:inventory                                               │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 🔍 Authorization Header Breakdown

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  CLIENT_SECRET_BASIC Authentication:                               ║
║  ───────────────────────────────────                               ║
║                                                                    ║
║  Step 1: Combine credentials                                       ║
║          "order-service" + ":" + "order-service-secret"            ║
║          = "order-service:order-service-secret"                    ║
║                                                                    ║
║  Step 2: Base64 encode                                             ║
║          = "b3JkZXItc2VydmljZTpvcmRlci1zZXJ2aWNlLXNlY3JldA=="       ║
║                                                                    ║
║  Step 3: Add to header                                             ║
║          Authorization: Basic b3JkZXItc2VydmljZTpvcmRlci...        ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 📨 RESPONSE 1: Auth-Server → Order-Service (Token Response)

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                     │
│  Response from Auth-Server:                                         │
│                                                                     │
│  STATUS: 200 OK                                                     │
│                                                                     │
│  HEADERS:                                                           │
│  ─────────                                                          │
│  Content-Type: application/json                                     │
│                                                                     │
│  BODY:                                                              │
│  ─────                                                              │
│  {                                                                  │
│    "access_token": "eyJraWQiOiI4YjM3...",    ← JWT TOKEN!          │
│    "token_type": "Bearer",                                          │
│    "expires_in": 3600,                       ← 1 hour valid        │
│    "scope": "read:inventory"                 ← Permissions          │
│  }                                                                  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### What Auth-Server Did Internally

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  AUTH-SERVER Processing:                                           ║
║  ───────────────────────                                           ║
║                                                                    ║
║  1. Read Authorization header                                      ║
║  2. Base64 decode → "order-service:order-service-secret"           ║
║  3. Check RegisteredClientRepository:                              ║
║     ✓ client_id = "order-service" → Found!                         ║
║     ✓ client_secret = "order-service-secret" → Correct!            ║
║     ✓ grant_type = "client_credentials" → Allowed!                 ║
║     ✓ scope = "read:inventory" → Allowed!                          ║
║                                                                    ║
║  4. Create JWT payload:                                            ║
║     {                                                              ║
║       "sub": "order-service",                                      ║
║       "scope": "read:inventory",                                   ║
║       "iss": "http://localhost:9000",                              ║
║       "exp": 1702677059                                            ║
║     }                                                              ║
║                                                                    ║
║  5. Sign with PRIVATE KEY → Signature created                      ║
║  6. Combine: Header.Payload.Signature → JWT Token!                 ║
║  7. Return to client                                               ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 📨 REQUEST 3: Order-Service → Inventory-Service

### Token attach chesi call

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                     │
│  REQUEST:                                                           │
│  ─────────                                                          │
│  Method: GET                                                        │
│  URL:    http://localhost:8081/api/stock/laptop-001                 │
│                                                                     │
│  HEADERS:                                                           │
│  ─────────                                                          │
│  Authorization: Bearer eyJraWQiOiI4YjM3NjEzOS1hYmNkLTRlZjUtOGE3... │
│                 ↑       ↑                                           │
│                 │       └── The JWT Token (from Res1)               │
│                 └── Token type                                      │
│                                                                     │
│  Accept: application/json                                           │
│                                                                     │
│  BODY: Empty (GET request)                                          │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔐 Inventory-Service: Token Validation Process

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  INVENTORY-SERVICE Validation Steps:                               ║
║  ───────────────────────────────────                               ║
║                                                                    ║
║  STEP 1: Extract Token                                             ║
║  ─────────────────────                                             ║
║  Authorization header → "Bearer eyJ..."                            ║
║  Extract: "eyJ..." (the JWT)                                       ║
║                                                                    ║
║  STEP 2: Split JWT                                                 ║
║  ───────────────────                                               ║
║  eyJhbGci...  .  eyJzdWIi...  .  SflKxw...                         ║
║  [HEADER]        [PAYLOAD]       [SIGNATURE]                       ║
║                                                                    ║
║  STEP 3: Verify Signature                                          ║
║  ─────────────────────────                                         ║
║  • Get public key from Auth Server (cached at startup)             ║
║  • Use public key + signature to verify                            ║
║  • "Was this signed by Auth Server's private key?"                 ║
║  • ✓ YES → Continue                                                ║
║  • ✗ NO → 401 Unauthorized                                         ║
║                                                                    ║
║  STEP 4: Check Issuer                                              ║
║  ─────────────────────                                             ║
║  Token's "iss" == "http://localhost:9000"?                         ║
║  (Must match our configured issuer-uri!)                           ║
║                                                                    ║
║  STEP 5: Check Expiry                                              ║
║  ─────────────────────                                             ║
║  Token's "exp" > current time?                                     ║
║  • ✓ Not expired → Continue                                        ║
║  • ✗ Expired → 401 Unauthorized                                    ║
║                                                                    ║
║  STEP 6: Check Scope (SecurityConfig.java)                         ║
║  ───────────────────────────────────────────                       ║
║  hasAuthority("SCOPE_read:inventory")                              ║
║  Token lo "read:inventory" scope unda?                             ║
║  • ✓ YES → Allow access!                                           ║
║  • ✗ NO → 403 Forbidden                                            ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 📨 RESPONSE 3: Inventory → Order-Service

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                     │
│  Response (Token Valid!):                                           │
│                                                                     │
│  STATUS: 200 OK                                                     │
│                                                                     │
│  BODY:                                                              │
│  {                                                                  │
│    "productId": "laptop-001",                                       │
│    "productName": "Dell Laptop",                                    │
│    "quantity": 50,                                                  │
│    "available": true,                                               │
│    "warehouse": "Hyderabad-WH-01"                                   │
│  }                                                                  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Complete Timeline

```
TIME    ACTION
─────   ──────────────────────────────────────────────────────────────

0ms     User: GET /api/orders/check-stock/laptop-001
        │
        ▼
10ms    Order-Service: "Token ledu, teeskovalsi untundi..."
        │
        ▼
20ms    Order-Service → Auth-Server: POST /oauth2/token
        (client_id + secret + grant_type)
        │
        ▼
50ms    Auth-Server: Validates credentials, creates JWT
        │
        ▼
60ms    Auth-Server → Order-Service: Returns JWT token
        │
        ▼
70ms    Order-Service: Stores token, attaches to request
        │
        ▼
80ms    Order-Service → Inventory: GET /api/stock/laptop-001
        (with Bearer token)
        │
        ▼
100ms   Inventory-Service: Validates JWT, checks scope
        │
        ▼
120ms   Inventory-Service: Returns stock data
        │
        ▼
130ms   Order-Service → User: Final response!

TOTAL: ~130ms
```

---

## 🎯 Key Observations

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  1. User ki token knowledge avasaram ledu!                         ║
║     All happens behind the scenes.                                 ║
║                                                                    ║
║  2. Token oka sari ostundi, next requests ki reuse!                ║
║     Spring caches the token until it expires.                      ║
║                                                                    ║
║  3. Auth Server is NOT in data path!                               ║
║     Only needed for token issuance.                                ║
║     Data flows: Order ↔ Inventory directly.                        ║
║                                                                    ║
║  4. Inventory contacts Auth Server ONCE (at startup)               ║
║     Just to get public keys.                                       ║
║     After that, validates locally!                                 ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

**Next:** [04_JWT_Deep_Dive.md](./04_JWT_Deep_Dive.md) - JWT structure lo deep dive
