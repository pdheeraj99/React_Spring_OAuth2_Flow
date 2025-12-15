package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.StockInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private static final Logger log = LoggerFactory.getLogger(StockController.class);

    // Simulated inventory data
    private static final Map<String, StockInfo> INVENTORY = Map.of(
            "laptop-001", new StockInfo("laptop-001", 50, "In Stock"),
            "phone-002", new StockInfo("phone-002", 100, "In Stock"),
            "tablet-003", new StockInfo("tablet-003", 0, "Out of Stock"));

    /**
     * Get stock for a product
     * 
     * This endpoint requires:
     * 1. Valid JWT token
     * 2. Token must have scope "read:inventory"
     */
    @GetMapping("/{productId}")
    public StockInfo getStock(
            @PathVariable String productId,
            @AuthenticationPrincipal Jwt jwt) {

        log.info("==================================================");
        log.info("INVENTORY SERVICE: Stock request received");
        log.info("==================================================");

        String subject = jwt.getSubject();
        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "unknown";
        Object scopes = jwt.getClaim("scope");

        log.info("Token subject (who called): " + subject);
        log.info("Token issuer: " + issuer);
        log.info("Token scopes: " + scopes);
        log.info("Product requested: " + productId);

        StockInfo stock = INVENTORY.getOrDefault(
                productId,
                new StockInfo(productId, 0, "Product Not Found"));

        log.info("Returning stock: " + stock);
        return stock;
    }

    /**
     * Debug endpoint to see token info
     */
    @GetMapping("/debug/token")
    public Map<String, Object> debugToken(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("subject", jwt.getSubject());
        tokenInfo.put("issuer", jwt.getIssuer() != null ? jwt.getIssuer().toString() : "unknown");
        tokenInfo.put("scope", jwt.getClaim("scope"));
        tokenInfo.put("issuedAt", jwt.getIssuedAt());
        tokenInfo.put("expiresAt", jwt.getExpiresAt());
        tokenInfo.put("allClaims", jwt.getClaims());
        return tokenInfo;
    }
}
