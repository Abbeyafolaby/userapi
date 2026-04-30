package com.learn.userapi.repository;

import com.learn.userapi.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    boolean existsByName(String name);

    // ── JPQL Query 6: price range search with pagination
    // demonstrates: BETWEEN, pagination on JPQL query
    @Query("""
            SELECT p FROM Product p
            WHERE p.price BETWEEN :minPrice AND :maxPrice
              AND p.stockQuantity > 0
            ORDER BY p.price ASC
            """)
    Page<Product> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    // ── JPQL Query 7: low stock alert
    // demonstrates: comparison, ORDER BY
    @Query("""
            SELECT p FROM Product p
            WHERE p.stockQuantity <= :threshold
            ORDER BY p.stockQuantity ASC
            """)
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    // ── JPQL Query 8: full text search across name and description
    // demonstrates: LOWER() function, OR condition, LIKE
    @Query("""
            SELECT p FROM Product p
            WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY p.name ASC
            """)
    Page<Product> searchByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable);

    // ── JPQL Query 9: bulk stock update using @Modifying
    // demonstrates: UPDATE in JPQL, @Modifying annotation
    @Modifying                      // required for INSERT/UPDATE/DELETE in JPQL
    @Query("""
            UPDATE Product p
            SET p.stockQuantity = p.stockQuantity + :quantity
            WHERE p.id = :productId
            """)
    int incrementStock(
            @Param("productId") Long productId,
            @Param("quantity") Integer quantity);
}