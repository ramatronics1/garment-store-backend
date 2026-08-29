package com.garmentstore.order.infrastructure;

import com.garmentstore.order.domain.Order;
import com.garmentstore.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ── Customer-facing (existing) ─────────────────────────────────────────────
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // ── Admin: fetch a single order, eagerly loading everything needed ─────────
    @Query("""
        SELECT o FROM Order o
        LEFT JOIN FETCH o.user
        LEFT JOIN FETCH o.address
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.product
        LEFT JOIN FETCH i.variant
        WHERE o.id = :id
    """)
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    // ── Admin: paginated list with optional filters ────────────────────────────
    @Query(value = """
        SELECT o FROM Order o
        LEFT JOIN FETCH o.user u
        WHERE (:status IS NULL OR o.status = :status)
          AND (:from IS NULL OR o.createdAt >= :from)
          AND (:to IS NULL OR o.createdAt <= :to)
          AND (:q IS NULL OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%',:q,'%'))
              OR LOWER(u.name) LIKE LOWER(CONCAT('%',:q,'%'))
              OR LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%')))
    """,
    countQuery = """
        SELECT COUNT(o) FROM Order o
        LEFT JOIN o.user u
        WHERE (:status IS NULL OR o.status = :status)
          AND (:from IS NULL OR o.createdAt >= :from)
          AND (:to IS NULL OR o.createdAt <= :to)
          AND (:q IS NULL OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%',:q,'%'))
              OR LOWER(u.name) LIKE LOWER(CONCAT('%',:q,'%'))
              OR LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%')))
    """)
    Page<Order> findAdminOrders(
            @Param("status") OrderStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("q") String q,
            Pageable pageable
    );

    // ── Admin: status counts for the chips (unfiltered) ───────────────────────
    @Query("""
        SELECT o.status, COUNT(o)
        FROM Order o
        GROUP BY o.status
    """)
    List<Object[]> countByStatus();

    // ── Admin: bulk update — returns count of rows updated ────────────────────
    @Query("""
        UPDATE Order o SET o.status = :status, o.updatedAt = :now
        WHERE o.id IN :ids
    """)
    @org.springframework.data.jpa.repository.Modifying
    int bulkUpdateStatus(
            @Param("ids") List<Long> ids,
            @Param("status") OrderStatus status,
            @Param("now") Instant now
    );
}
