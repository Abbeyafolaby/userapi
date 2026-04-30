package com.learn.userapi.dto.response;

import com.learn.userapi.model.OrderItem;
import java.math.BigDecimal;

public class OrderItemResponse {

    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;     // denormalised for convenience — saves a round trip
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    private OrderItemResponse(Long id, Long orderId, Long productId,
                              String productName, Integer quantity,
                              BigDecimal unitPrice, BigDecimal lineTotal) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public static OrderItemResponse fromOrderItem(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getOrder().getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),    // safe — Product is loaded with item
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getProductName() {
        return productName;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getOrderId() {
        return orderId;
    }
}