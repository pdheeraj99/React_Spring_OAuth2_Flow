package com.example.orderservice.service;

import com.example.orderservice.dto.StockInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final WebClient webClient;
    private final String inventoryServiceUrl;

    public InventoryService(
            WebClient webClient,
            @Value("${inventory.service.url}") String inventoryServiceUrl) {
        this.webClient = webClient;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }

    /**
     * Call Inventory Service to get stock info
     * 
     * ⭐ MAGIC HAPPENS HERE:
     * 1. WebClient sees we need to call inventory-service
     * 2. It checks: "Do I have a valid token?"
     * 3. If NO: It calls Auth Server with client_id + client_secret
     * 4. Auth Server returns access_token
     * 5. WebClient caches the token
     * 6. WebClient adds "Authorization: Bearer xxx" header
     * 7. Request goes to Inventory Service!
     * 
     * ALL AUTOMATIC! You just call webClient.get()!
     */
    public StockInfo getStock(String productId) {
        log.info("🔍 Calling Inventory Service for product: {}", productId);
        log.info("📍 URL: {}/api/stock/{}", inventoryServiceUrl, productId);

        StockInfo stock = webClient.get()
                .uri(inventoryServiceUrl + "/api/stock/{productId}", productId)
                .retrieve()
                .bodyToMono(StockInfo.class)
                .doOnSubscribe(s -> log.info("🚀 Request started - token will be fetched automatically!"))
                .doOnSuccess(s -> log.info("✅ Response received from Inventory Service"))
                .doOnError(e -> log.error("❌ Error calling Inventory Service: {}", e.getMessage()))
                .block();

        log.info("📦 Stock info: {}", stock);
        return stock;
    }
}
