package com.learn.userapi.repository;

import com.learn.userapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // ── JPQL Query 10: users with their order count
    // demonstrates: LEFT JOIN (include users with zero orders),
    //               constructor expression mapping to DTO
    @Query("""
            SELECT u.id, u.name, u.email, COUNT(o)
            FROM User u
            LEFT JOIN u.orders o
            GROUP BY u.id, u.name, u.email
            ORDER BY COUNT(o) DESC
            """)
    List<Object[]> findUsersWithOrderCount();

    // ── JPQL Query 11: top spending users
    // demonstrates: SUM across join, HAVING clause, subquery-free ranking
    @Query("""
            SELECT u.id, u.name, u.email, SUM(o.totalAmount)
            FROM User u
            JOIN u.orders o
            WHERE o.status <> 'CANCELLED'
            GROUP BY u.id, u.name, u.email
            HAVING SUM(o.totalAmount) > :minSpend
            ORDER BY SUM(o.totalAmount) DESC
            """)
    List<Object[]> findTopSpendingUsers(@Param("minSpend") java.math.BigDecimal minSpend);

    // ── JPQL Query 12: users who ordered a specific product
    // demonstrates: multi-level join traversal across 3 entities
    @Query("""
            SELECT DISTINCT u
            FROM User u
            JOIN u.orders o
            JOIN o.orderItems oi
            WHERE oi.product.id = :productId
            """)
    List<User> findUsersWhoPurchasedProduct(@Param("productId") Long productId);
}