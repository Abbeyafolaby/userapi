package com.learn.userapi.repository;

import com.learn.userapi.model.Order;
import com.learn.userapi.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // find all orders for a specific user
    List<Order> findByUserId(Long userId);

    // find orders for a user filtered by status
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    // count orders for a user
    long countByUserId(Long userId);
}