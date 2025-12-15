# 06 - Common Use Cases

> 📌 Real-world scenarios where Client Credentials Grant shines!

---

## 🎯 Use Case 1: Microservices Communication

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    MICROSERVICES TALKING TO EACH OTHER                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Scenario:                                                                  │
│   User places an order on e-commerce site.                                   │
│   Order Service needs to check inventory.                                    │
│                                                                              │
│   ┌─────────────┐    access_token     ┌──────────────────┐                   │
│   │   Order     │ ─────────────────► │   Inventory      │                   │
│   │   Service   │                     │   Service        │                   │
│   │             │ ◄───────────────── │                  │                   │
│   │             │    stock: 50        │                  │                   │
│   └─────────────┘                     └──────────────────┘                   │
│                                                                              │
│   Why Client Credentials?                                                    │
│   • Order Service calls Inventory Service on its own                         │
│   • No user is directly making this call                                     │
│   • Service-to-service = Client Credentials!                                 │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Code Example

```java
@Service
public class OrderService {
    
    private final WebClient webClient;
    
    public Order createOrder(OrderRequest request) {
        // Check inventory (service-to-service call)
        StockInfo stock = webClient.get()
            .uri("http://inventory-service/api/stock/{sku}", request.getSku())
            .retrieve()
            .bodyToMono(StockInfo.class)
            .block();
        
        if (stock.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException();
        }
        
        // Create order...
    }
}
```

---

## 🎯 Use Case 2: Scheduled Jobs / Cron Tasks

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    SCHEDULED BACKGROUND JOBS                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Scenario:                                                                  │
│   Every night at 2 AM, backup service exports data to cloud storage.        │
│                                                                              │
│   ┌───────────────────┐                                                      │
│   │  Backup Service   │  Runs at 2:00 AM                                     │
│   │  (cron job)       │  No user is awake!                                   │
│   └─────────┬─────────┘                                                      │
│             │                                                                │
│             │  1. Get token (client_credentials)                             │
│             ▼                                                                │
│   ┌───────────────────┐                                                      │
│   │   Auth Server     │                                                      │
│   └───────────────────┘                                                      │
│             │                                                                │
│             │  2. Call API with token                                        │
│             ▼                                                                │
│   ┌───────────────────┐                                                      │
│   │  Storage API      │                                                      │
│   │  (protected)      │                                                      │
│   └───────────────────┘                                                      │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Code Example

```java
@Component
public class NightlyBackupJob {
    
    private final WebClient webClient;
    private final DataRepository dataRepository;
    
    @Scheduled(cron = "0 0 2 * * *")  // Every day at 2 AM
    public void runDailyBackup() {
        List<Data> todaysData = dataRepository.findByDate(LocalDate.now());
        
        // Upload to cloud storage (token automatically attached!)
        webClient.post()
            .uri("http://storage-service/api/backup")
            .bodyValue(todaysData)
            .retrieve()
            .toBodilessEntity()
            .block();
        
        log.info("Nightly backup completed!");
    }
}
```

---

## 🎯 Use Case 3: Third-Party Integrations

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    EXTERNAL SYSTEM INTEGRATION                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Scenario:                                                                  │
│   Your CRM system needs to sync customer data with your main API.            │
│                                                                              │
│   ┌───────────────────┐                                                      │
│   │  Salesforce CRM   │  (Third-party)                                       │
│   │  client_id: sf001 │                                                      │
│   └─────────┬─────────┘                                                      │
│             │                                                                │
│             │  POST /oauth/token                                             │
│             │  grant_type=client_credentials                                 │
│             ▼                                                                │
│   ┌───────────────────┐                                                      │
│   │  Your Auth Server │                                                      │
│   └─────────┬─────────┘                                                      │
│             │                                                                │
│             │  access_token (scope: read:customers)                          │
│             ▼                                                                │
│   ┌───────────────────┐                                                      │
│   │  Your Customer    │                                                      │
│   │  API              │                                                      │
│   └───────────────────┘                                                      │
│                                                                              │
│   Third-party authenticates as ITSELF (not as a user!)                       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Use Case 4: Webhooks

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    WEBHOOK SECURITY                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Scenario:                                                                  │
│   Payment provider (Stripe) needs to notify your app of payments.            │
│                                                                              │
│   Option 1: Stripe calls YOUR API                                            │
│   ┌─────────────┐                     ┌───────────────┐                      │
│   │   Stripe    │ ──────────────────► │  Your API     │                      │
│   │             │  Bearer token        │  (validates)  │                      │
│   └─────────────┘                     └───────────────┘                      │
│                                                                              │
│   Stripe uses client_credentials to get a token from YOUR auth server        │
│   Then calls your webhook endpoint with that token.                          │
│   Your API validates: "Is this really Stripe?"                               │
│                                                                              │
│   Option 2: YOU call Stripe's API                                            │
│   ┌─────────────┐                     ┌───────────────┐                      │
│   │  Your App   │ ──────────────────► │  Stripe API   │                      │
│   │             │  Bearer token        │  (validates)  │                      │
│   └─────────────┘                     └───────────────┘                      │
│                                                                              │
│   You get token from Stripe's auth (using your Stripe API credentials)       │
│   Then call Stripe's API to verify payment status.                           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Use Case 5: CLI Tools and Scripts

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ADMIN SCRIPTS AND CLI TOOLS                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Scenario:                                                                  │
│   DevOps runs a script to clean up old database records.                     │
│                                                                              │
│   $ ./cleanup-script.sh                                                      │
│                                                                              │
│   Script internally:                                                         │
│   1. Uses client_credentials to get token                                    │
│   2. Calls admin API to delete old records                                   │
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐    │
│   │  #!/bin/bash                                                        │    │
│   │                                                                     │    │
│   │  # Get token                                                        │    │
│   │  TOKEN=$(curl -s -X POST https://auth.myapp.com/oauth/token \       │    │
│   │    -u "cleanup-script:secret" \                                     │    │
│   │    -d "grant_type=client_credentials" \                             │    │
│   │    -d "scope=admin:cleanup" | jq -r '.access_token')                │    │
│   │                                                                     │    │
│   │  # Use token to call API                                            │    │
│   │  curl -X DELETE https://api.myapp.com/admin/cleanup \               │    │
│   │    -H "Authorization: Bearer $TOKEN"                                │    │
│   │                                                                     │    │
│   └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
│   Script authenticates as ITSELF, not as a human admin!                      │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Summary: When to Use What

| Scenario | Grant Type | Why |
|----------|------------|-----|
| User clicks "Login with Google" | Auth Code | User involved |
| Service A → Service B | Client Credentials | No user |
| Nightly cron job | Client Credentials | No user at 2 AM |
| Third-party webhook | Client Credentials | App-to-app |
| Admin CLI script | Client Credentials | Script, not person |
| User views their photos | Auth Code | User's data |
| Microservice health check | Client Credentials | Automated |

---

## 🤔 Beginner Check

1. Your microservice needs to call another microservice. Which grant?
2. A scheduled job runs at midnight. Which grant?
3. Stripe needs to call your webhook. Which grant?
4. User logs into your app. Which grant?

Answers:

1. Client Credentials (service-to-service)
2. Client Credentials (no user at midnight)
3. Client Credentials (Stripe authenticates itself)
4. Authorization Code (user is logging in!)

---

**Next:** [07_Security_Considerations.md](./07_Security_Considerations.md)
