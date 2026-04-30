package com.learn.userapi.repository;

import com.learn.userapi.model.OrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @EntityGraph(attributePaths = {"order", "product"})
    List<OrderItem> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = {"order", "product"})
    Optional<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);

    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN FETCH oi.order o " +
            "JOIN FETCH oi.product p " +
            "WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderIdWithFetch(@Param("orderId") Long orderId);
}