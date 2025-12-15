package com.example.orderservice.controller;

import com.example.orderservice.dto.StockInfo;
import com.example.orderservice.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final InventoryService inventoryService;

    public OrderController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Demo endpoint to trigger Client Credentials flow
     * 
     * When you call this:
     * 1. Order Service needs to check stock
     * 2. It calls Inventory Service
     * 3. WebClient automatically gets token from Auth Server
     * 4. Token is attached and request goes to Inventory
     */
    @GetMapping("/check-stock/{productId}")
    public Map<String, Object> checkStock(@PathVariable String productId) {
        log.info("==================================================");
        log.info("📦 ORDER SERVICE: Checking stock for product: {}", productId);
        log.info("==================================================");

        try {
            StockInfo stock = inventoryService.getStock(productId);

            return Map.of(
                    "success", true,
                    "message", "Stock checked successfully using Client Credentials!",
                    "productId", productId,
                    "stock", stock);
        } catch (Exception e) {
            log.error("Failed to check stock", e);
            return Map.of(
                    "success", false,
                    "error", e.getMessage());
        }
    }

    /**
     * Simple health check
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "service", "order-service",
                "status", "UP",
                "port", "8080");
    }
}
