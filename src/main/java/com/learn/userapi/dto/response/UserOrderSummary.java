package com.learn.userapi.dto.response;

import java.math.BigDecimal;

public class UserOrderSummary {

    private Long userId;
    private String name;
    private String email;
    private Long orderCount;
    private BigDecimal totalSpent;

    public UserOrderSummary(Long userId, String name, String email,
                            Long orderCount, BigDecimal totalSpent) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.orderCount = orderCount;
        this.totalSpent = totalSpent;
    }


    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Long getUserId() {
        return userId;
    }
}