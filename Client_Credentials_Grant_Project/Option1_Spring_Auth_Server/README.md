# Client Credentials Grant Demo - Spring Authorization Server

## Architecture

```
┌─────────────────┐              ┌─────────────────┐              ┌─────────────────┐
│   Order Service │              │   Auth Server   │              │Inventory Service│
│     (8080)      │              │     (9000)      │              │     (8081)      │
│                 │              │                 │              │                 │
│  OAuth2 Client  │─────────────▶│  Spring Auth    │◀─────────────│Resource Server  │
│                 │ Get Token    │      Server     │ Validate JWT │                 │
└────────┬────────┘              └─────────────────┘              └────────▲────────┘
         │                                                                 │
         │                        Call with Token                          │
         └─────────────────────────────────────────────────────────────────┘
```

## Quick Start

### 1. Start Auth Server (Port 9000)

```bash
cd auth-server
./mvnw spring-boot:run
```

### 2. Start Inventory Service (Port 8081)

```bash
cd inventory-service
./mvnw spring-boot:run
```

### 3. Start Order Service (Port 8080)

```bash
cd order-service
./mvnw spring-boot:run
```

### 4. Test the Flow

```bash
# This triggers:
# 1. Order Service gets token from Auth Server using Client Credentials
# 2. Order Service calls Inventory with token
# 3. Inventory validates token and returns stock

curl http://localhost:8080/api/orders/check-stock/laptop-001
```

## Expected Flow

1. You call `/api/orders/check-stock/laptop-001` on Order Service
2. Order Service needs to call Inventory Service
3. WebClient automatically requests token from Auth Server:

   ```
   POST http://localhost:9000/oauth2/token
   grant_type=client_credentials
   client_id=order-service
   client_secret=order-service-secret
   ```

4. Auth Server returns access_token
5. WebClient attaches token to Inventory call
6. Inventory validates token and returns stock data

## Key Files

| Service | Key Config |
|---------|------------|
| auth-server | `AuthorizationServerConfig.java` - Registers order-service client |
| order-service | `WebClientConfig.java` - Enables client_credentials |
| inventory-service | `SecurityConfig.java` - Validates JWT, checks scope |

## Registered Clients

| Client ID | Secret | Scopes |
|-----------|--------|--------|
| order-service | order-service-secret | read:inventory |
| notification-service | notification-service-secret | send:email, send:sms |
