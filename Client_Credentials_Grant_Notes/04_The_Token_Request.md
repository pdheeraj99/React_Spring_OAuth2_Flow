# 04 - The Token Request: Deep Dive

> 📌 Understanding every part of the simple token request!

---

## 📋 The Complete Request

```http
POST https://auth-server.com/oauth/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic b3JkZXItc2VydmljZTpzZWNyZXQtMTIz

grant_type=client_credentials
&scope=read:inventory write:orders
```

---

## 🔑 Authentication Methods

There are TWO ways to send credentials:

### Method 1: Basic Authentication (Preferred)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    BASIC AUTH IN HEADER                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Authorization: Basic <base64(client_id:client_secret)>                     │
│                                                                              │
│   Example:                                                                   │
│   client_id = order-service                                                  │
│   client_secret = secret-123                                                 │
│                                                                              │
│   Combine: "order-service:secret-123"                                        │
│   Base64 encode: "b3JkZXItc2VydmljZTpzZWNyZXQtMTIz"                          │
│                                                                              │
│   Header: Authorization: Basic b3JkZXItc2VydmljZTpzZWNyZXQtMTIz              │
│                                                                              │
│   ⭐ This is the OAuth 2.0 recommended method!                               │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Method 2: Body Parameters

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    CREDENTIALS IN BODY                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   POST /oauth/token                                                          │
│   Content-Type: application/x-www-form-urlencoded                            │
│                                                                              │
│   grant_type=client_credentials                                              │
│   &client_id=order-service                                                   │
│   &client_secret=secret-123                                                  │
│   &scope=read:inventory                                                      │
│                                                                              │
│   ⚠️ Some auth servers support this, but Basic Auth is preferred!           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Request Parameters Explained

| Parameter | Required | Value | Description |
|-----------|----------|-------|-------------|
| `grant_type` | ✅ Yes | `client_credentials` | Tells auth server which flow |
| `scope` | ⚡ Optional | `read:inventory` | Permissions requested |
| `client_id` | ✅ Yes* | `order-service` | *In header or body |
| `client_secret` | ✅ Yes* | `secret-123` | *In header or body |

---

## 📬 Response Explained

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2F1dGgubXlhcHAuY29tIiwic3ViIjoib3JkZXItc2VydmljZSIsImF1ZCI6ImludmVudG9yeS1zZXJ2aWNlIiwic2NvcGUiOiJyZWFkOmludmVudG9yeSIsImV4cCI6MTcwMjI5MzM5OCwiaWF0IjoxNzAyMjg5Nzk4fQ.signature",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "read:inventory write:orders"
}
```

### Response Fields

| Field | Description | Example |
|-------|-------------|---------|
| `access_token` | JWT token for API calls | `eyJhbG...` |
| `token_type` | How to use the token | `Bearer` |
| `expires_in` | Seconds until expiry | `3600` (1 hour) |
| `scope` | Granted permissions | `read:inventory` |

---

## 🧩 Decoding the Access Token (JWT)

```json
// HEADER
{
  "alg": "RS256",
  "typ": "JWT"
}

// PAYLOAD
{
  "iss": "https://auth.myapp.com",      // Issuer (auth server)
  "sub": "order-service",                // Subject (the app!)
  "aud": "inventory-service",            // Audience (who can use this)
  "scope": "read:inventory",             // Permissions
  "iat": 1702289798,                     // Issued at
  "exp": 1702293398                      // Expires at
}

// SIGNATURE
// Signed by auth server's private key
```

### Key Difference from Authorization Code Grant

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   Authorization Code Grant token (has USER info):                            │
│   {                                                                          │
│     "sub": "112416036337094439562",  ← User's ID                             │
│     "email": "dheeraj@gmail.com",     ← User's email                         │
│     ...                                                                      │
│   }                                                                          │
│                                                                              │
│   Client Credentials token (NO user info):                                   │
│   {                                                                          │
│     "sub": "order-service",           ← App's ID                             │
│     "client_id": "order-service",                                            │
│     // NO email, NO user info!                                               │
│   }                                                                          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## ⚠️ Error Responses

### Invalid Credentials

```json
{
  "error": "invalid_client",
  "error_description": "Client authentication failed"
}
```

### Invalid Scope

```json
{
  "error": "invalid_scope",
  "error_description": "The requested scope 'admin:everything' is not allowed"
}
```

### Common Errors

| Error | Meaning | Fix |
|-------|---------|-----|
| `invalid_client` | Wrong client_id or secret | Check credentials |
| `invalid_scope` | Scope not allowed for this client | Request allowed scopes only |
| `unauthorized_client` | Client can't use this grant type | Enable client_credentials for this client |

---

## 💻 Code Examples

### Java (RestTemplate)

```java
// Token request
RestTemplate restTemplate = new RestTemplate();

HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
headers.setBasicAuth("order-service", "secret-123");

MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
body.add("grant_type", "client_credentials");
body.add("scope", "read:inventory");

HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
    "https://auth-server.com/oauth/token",
    request,
    TokenResponse.class
);

String accessToken = response.getBody().getAccessToken();
```

### cURL

```bash
curl -X POST https://auth-server.com/oauth/token \
  -u "order-service:secret-123" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "scope=read:inventory"
```

---

## 🤔 Beginner Check

1. What are the two ways to send client_id and client_secret?
2. Is scope required in the request?
3. What's the `sub` claim in a Client Credentials token?
4. What error means "wrong password"?

Answers:

1. Basic Auth header OR body parameters
2. Optional (but recommended to limit permissions)
3. The app's ID (e.g., "order-service"), NOT a user
4. `invalid_client`

---

**Next:** [05_Spring_Boot_Implementation.md](./05_Spring_Boot_Implementation.md)
