package com.learn.userapi.controller;

import com.learn.userapi.dto.response.*;
import com.learn.userapi.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // GET /api/analytics/users/order-summary
    @GetMapping("/users/order-summary")
    public ResponseEntity<ApiResponse<List<UserOrderSummary>>> getUserOrderSummaries() {
        return ResponseEntity.ok(ApiResponse.success(
                "User order summaries retrieved",
                analyticsService.getUserOrderSummaries()));
    }

    // GET /api/analytics/users/top-spenders?minSpend=100
    @GetMapping("/users/top-spenders")
    public ResponseEntity<ApiResponse<List<UserOrderSummary>>> getTopSpenders(
            @RequestParam(defaultValue = "0") BigDecimal minSpend) {
        return ResponseEntity.ok(ApiResponse.success(
                "Top spending users retrieved",
                analyticsService.getTopSpendingUsers(minSpend)));
    }

    // GET /api/analytics/users/1/total-spent
    @GetMapping("/users/{userId}/total-spent")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalSpent(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Total spent retrieved",
                analyticsService.getTotalSpentByUser(userId)));
    }

    // GET /api/analytics/products/low-stock?threshold=10
    @GetMapping("/products/low-stock")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStock(
            @RequestParam(defaultValue = "10") Integer threshold) {
        return ResponseEntity.ok(ApiResponse.success(
                "Low stock products retrieved",
                analyticsService.getLowStockProducts(threshold)));
    }

    // GET /api/analytics/products/1/purchasers
    @GetMapping("/products/{productId}/purchasers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getProductPurchasers(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product purchasers retrieved",
                analyticsService.getUsersWhoPurchasedProduct(productId)));
    }
}