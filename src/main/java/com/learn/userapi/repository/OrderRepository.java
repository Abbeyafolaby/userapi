package com.learn.userapi.repository;

import com.learn.userapi.model.Order;
import com.learn.userapi.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<Order> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Optional<Order> findById(Long id);

    long countByUserId(Long userId);

    // ── JPQL Query 1: orders within a date range
    // demonstrates: date comparison, named params, JOIN FETCH
    @Query("""
            SELECT o FROM Order o
            JOIN FETCH o.user u
            WHERE u.id = :userId
              AND o.createdAt BETWEEN :from AND :to
            ORDER BY o.createdAt DESC
            """)
    List<Order> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // ── JPQL Query 2: orders above a minimum total
    // demonstrates: comparison operators, multiple conditions
    @Query("""
            SELECT o FROM Order o
            JOIN FETCH o.user u
            WHERE u.id = :userId
              AND o.totalAmount >= :minAmount
              AND o.status NOT IN :excludedStatuses
            ORDER BY o.totalAmount DESC
            """)
    List<Order> findByUserIdAndMinAmount(
            @Param("userId") Long userId,
            @Param("minAmount") BigDecimal minAmount,
            @Param("excludedStatuses") List<OrderStatus> excludedStatuses);

    // ── JPQL Query 3: aggregate — total spent by a user
    // demonstrates: SUM aggregate, returns a scalar value
    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0)
            FROM Order o
            WHERE o.user.id = :userId
              AND o.status NOT IN ('CANCELLED')
            """)
    BigDecimal getTotalSpentByUser(@Param("userId") Long userId);

    // ── JPQL Query 4: count orders grouped by status for a user
    // demonstrates: GROUP BY, constructor expression, projection
    @Query("""
            SELECT o.status, COUNT(o)
            FROM Order o
            WHERE o.user.id = :userId
            GROUP BY o.status
            """)
    List<Object[]> countOrdersByStatusForUser(@Param("userId") Long userId);

    // ── JPQL Query 5: paginated with status filter
    // demonstrates: pagination + filtering combined
    // countQuery needed when JOIN FETCH is used with pagination
    @Query(
            value = """
            SELECT o FROM Order o
            JOIN FETCH o.user u
            WHERE u.id = :userId
              AND (:status IS NULL OR o.status = :status)
            """,
            countQuery = """
            SELECT COUNT(o) FROM Order o
            WHERE o.user.id = :userId
              AND (:status IS NULL OR o.status = :status)
            """
    )
    Page<Order> findByUserIdFilteredPaged(
            @Param("userId") Long userId,
            @Param("status") OrderStatus status,
            Pageable pageable);
}