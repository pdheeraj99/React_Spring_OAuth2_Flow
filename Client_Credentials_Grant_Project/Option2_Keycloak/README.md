# Client Credentials Grant Demo - Keycloak

## Architecture

```
┌─────────────────┐              ┌─────────────────┐              ┌─────────────────┐
│   Order Service │              │    Keycloak     │              │Inventory Service│
│     (8080)      │              │     (9000)      │              │     (8081)      │
│                 │              │                 │              │                 │
│  OAuth2 Client  │─────────────▶│  Authorization  │◀─────────────│Resource Server  │
│                 │ Get Token    │     Server      │ Validate JWT │                 │
└────────┬────────┘              └─────────────────┘              └────────▲────────┘
         │                        Call with Token                          │
         └─────────────────────────────────────────────────────────────────┘
```

## Quick Start

### 1. Start Keycloak

```bash
cd Option2_Keycloak
docker-compose up -d
```

### 2. Configure Keycloak (One-time setup)

1. Open <http://localhost:9000>
2. Login with `admin` / `admin`
3. Create a new realm called `microservices`
4. Create a client:
   - Client ID: `order-service`
   - Client authentication: ON
   - Service accounts roles: ON (enables client_credentials)
   - Copy the client secret from Credentials tab

### 3. Update configuration

Update `order-service/src/main/resources/application.yaml` with your client secret.

### 4. Start Services

```bash
# Terminal 1:
cd inventory-service
./mvnw spring-boot:run

# Terminal 2:
cd order-service
./mvnw spring-boot:run
```

### 5. Test

```bash
curl http://localhost:8080/api/orders/check-stock/laptop-001
```

## Key Differences from Option 1

| Aspect | Option 1 (Spring Auth Server) | Option 2 (Keycloak) |
|--------|------------------------------|---------------------|
| Auth Server | Spring Boot app | Docker container |
| Configuration | Code-based | UI-based |
| Production Ready | Needs more work | Production-ready |
| Features | Basic OAuth2 | Full IdP (SSO, LDAP, etc.) |
