package com.example.orderservice.dto;

/**
 * Stock information returned by Inventory Service
 */
public record StockInfo(
        String productId,
        int quantity,
        String status) {
}
