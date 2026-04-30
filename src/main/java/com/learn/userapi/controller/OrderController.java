package com.learn.userapi.controller;

import com.learn.userapi.dto.request.OrderCreateRequest;
import com.learn.userapi.dto.response.ApiResponse;
import com.learn.userapi.dto.response.OrderResponse;
import com.learn.userapi.model.OrderStatus;
import com.learn.userapi.service.OrderService;
import com.learn.userapi.dto.request.AddOrderItemRequest;
import com.learn.userapi.dto.response.OrderItemResponse;
import com.learn.userapi.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.learn.userapi.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    // GET /api/users/1/orders/1/items
    @GetMapping("/{orderId}/items")
    public ResponseEntity<ApiResponse<List<OrderItemResponse>>> getOrderItems(
            @PathVariable Long userId,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order items retrieved successfully",
                orderService.getOrderItems(userId, orderId)));
    }

    // POST /api/users/1/orders/1/items
    @PostMapping("/{orderId}/items")
    public ResponseEntity<ApiResponse<OrderItemResponse>> addItemToOrder(
            @PathVariable Long userId,
            @PathVariable Long orderId,
            @Valid @RequestBody AddOrderItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to order",
                        orderService.addItemToOrder(userId, orderId, request)));
    }

    // DELETE /api/users/1/orders/1/items/5
    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeItemFromOrder(
            @PathVariable Long userId,
            @PathVariable Long orderId,
            @PathVariable Long itemId) {
        orderService.removeItemFromOrder(userId, orderId, itemId);
        return ResponseEntity.ok(ApiResponse.success(
                "Item removed from order", null));
    }

    // GET /api/users/1/orders/paged?page=0&size=5&sort=createdAt,desc
    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getOrdersPaged(
            @PathVariable Long userId,
            @PageableDefault(size = 5, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Orders retrieved successfully",
                orderService.getOrdersByUserIdPaged(userId, pageable)));
    }

    // GET /api/users/1/orders/search?from=2026-01-01T00:00:00&to=2026-12-31T23:59:59
    @GetMapping("/search/date-range")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByDateRange(
            @PathVariable Long userId,
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(
                "Orders retrieved",
                orderService.getOrdersByDateRange(userId, from, to)));
    }

    // GET /api/users/1/orders/search?minAmount=50
    @GetMapping("/search/min-amount")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersAboveAmount(
            @PathVariable Long userId,
            @RequestParam BigDecimal minAmount) {
        return ResponseEntity.ok(ApiResponse.success(
                "Orders retrieved",
                orderService.getOrdersAboveAmount(userId, minAmount)));
    }

    // GET /api/users/1/orders/filtered?status=PENDING&page=0&size=5
    @GetMapping("/filtered")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getOrdersFiltered(
            @PathVariable Long userId,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 5, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Orders retrieved",
                orderService.getOrdersFiltered(userId, status, pageable)));
    }
}