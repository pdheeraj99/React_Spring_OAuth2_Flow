package com.example.inventoryservice.dto;

/**
 * Stock information
 */
public record StockInfo(
        String productId,
        int quantity,
        String status) {
}
