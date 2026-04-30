package com.learn.userapi.service;

import com.learn.userapi.dto.response.ProductResponse;
import com.learn.userapi.dto.response.UserOrderSummary;
import com.learn.userapi.dto.response.UserResponse;
import com.learn.userapi.model.OrderStatus;
import com.learn.userapi.repository.OrderRepository;
import com.learn.userapi.repository.ProductRepository;
import com.learn.userapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public AnalyticsService(UserRepository userRepository,
                            OrderRepository orderRepository,
                            ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    // maps Object[] rows to UserOrderSummary DTOs
    public List<UserOrderSummary> getUserOrderSummaries() {
        log.debug("Fetching user order summaries");
        return userRepository.findUsersWithOrderCount()
                .stream()
                .map(row -> new UserOrderSummary(
                        (Long) row[0],                // u.id
                        (String) row[1],              // u.name
                        (String) row[2],              // u.email
                        (Long) row[3],                // COUNT(o)
                        BigDecimal.ZERO               // no spend data in this query
                ))
                .toList();
    }

    public List<UserOrderSummary> getTopSpendingUsers(BigDecimal minSpend) {
        log.debug("Fetching top spending users above: {}", minSpend);
        return userRepository.findTopSpendingUsers(minSpend)
                .stream()
                .map(row -> new UserOrderSummary(
                        (Long) row[0],                // u.id
                        (String) row[1],              // u.name
                        (String) row[2],              // u.email
                        null,                         // count not in this query
                        (BigDecimal) row[3]           // SUM(o.totalAmount)
                ))
                .toList();
    }

    public BigDecimal getTotalSpentByUser(Long userId) {
        log.debug("Fetching total spent for user id: {}", userId);
        return orderRepository.getTotalSpentByUser(userId);
    }

    public List<ProductResponse> getLowStockProducts(Integer threshold) {
        log.debug("Fetching products with stock <= {}", threshold);
        return productRepository.findLowStockProducts(threshold)
                .stream()
                .map(ProductResponse::fromProduct)
                .toList();
    }

    public List<UserResponse> getUsersWhoPurchasedProduct(Long productId) {
        log.debug("Fetching users who purchased product id: {}", productId);
        return userRepository.findUsersWhoPurchasedProduct(productId)
                .stream()
                .map(UserResponse::fromUser)
                .toList();
    }
}