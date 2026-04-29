package com.learn.userapi.controller;

import com.learn.userapi.dto.request.OrderCreateRequest;
import com.learn.userapi.dto.response.ApiResponse;
import com.learn.userapi.dto.response.OrderResponse;
import com.learn.userapi.model.OrderStatus;
import com.learn.userapi.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/orders")   // nested under users — expresses ownership
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // GET /api/users/1/orders
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Orders retrieved successfully",
                orderService.getOrdersByUserId(userId)));
    }

    // GET /api/users/1/orders/2
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long userId,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order retrieved successfully",
                orderService.getOrderById(userId, orderId)));
    }

    // POST /api/users/1/orders
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @PathVariable Long userId,
            @Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Order created successfully",
                        orderService.createOrder(userId, request)));
    }

    // PATCH /api/users/1/orders/2/status
    // PATCH because we're partially updating — only the status field
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long userId,
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order status updated successfully",
                orderService.updateOrderStatus(userId, orderId, status)));
    }

    // DELETE /api/users/1/orders/2
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
            @PathVariable Long userId,
            @PathVariable Long orderId) {
        orderService.deleteOrder(userId, orderId);
        return ResponseEntity.ok(ApiResponse.success("Order deleted successfully", null));
    }
}