package com.garmentstore.customer.infrastructure;

import com.garmentstore.auth.domain.AccountStatus;
import com.garmentstore.auth.domain.User;
import com.garmentstore.auth.domain.UserType;
import com.garmentstore.customer.domain.CustomerProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {

    Optional<CustomerProfile> findByUserId(Long userId);

    // ── Admin: paginated list of all CUSTOMER users with optional search + status filter ──

    /**
     * Returns all Users of type CUSTOMER, optionally filtered by:
     *   q      — case-insensitive match on name, email, or mobile
     *   status — exact AccountStatus match (null = all)
     */
    @Query("""
        SELECT u FROM User u
        WHERE u.userType = :type
          AND (:status IS NULL OR u.accountStatus = :status)
          AND (:q IS NULL
               OR LOWER(u.name)   LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(u.email)  LIKE LOWER(CONCAT('%', :q, '%'))
               OR u.mobile        LIKE CONCAT('%', :q, '%'))
    """)
    Page<User> findAdminCustomers(
            @Param("type")   UserType type,
            @Param("status") AccountStatus status,
            @Param("q")      String q,
            Pageable pageable
    );

    /**
     * Admin Customer List with database-level aggregate sorting & pagination.
     * Returns Page<Object[]> where each row is:
     *   row[0] = User entity
     *   row[1] = Long totalOrders
     *   row[2] = BigDecimal totalSpent
     *   row[3] = Instant lastOrderDate
     */
    @Query(value = """
        SELECT u AS user,
               COUNT(o.id) AS totalOrders,
               COALESCE(SUM(CASE WHEN o.status != com.garmentstore.order.domain.OrderStatus.CANCELLED THEN o.grandTotal ELSE 0 END), 0) AS totalSpent,
               MAX(o.createdAt) AS lastOrderDate
        FROM User u
        LEFT JOIN Order o ON o.user = u
        WHERE u.userType = :type
          AND (:status IS NULL OR u.accountStatus = :status)
          AND (:q IS NULL
               OR LOWER(u.name)   LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(u.email)  LIKE LOWER(CONCAT('%', :q, '%'))
               OR u.mobile        LIKE CONCAT('%', :q, '%'))
        GROUP BY u
    """,
    countQuery = """
        SELECT COUNT(u) FROM User u
        WHERE u.userType = :type
          AND (:status IS NULL OR u.accountStatus = :status)
          AND (:q IS NULL
               OR LOWER(u.name)   LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(u.email)  LIKE LOWER(CONCAT('%', :q, '%'))
               OR u.mobile        LIKE CONCAT('%', :q, '%'))
    """)
    Page<Object[]> findAdminCustomerSummaries(
            @Param("type")   UserType type,
            @Param("status") AccountStatus status,
            @Param("q")      String q,
            Pageable pageable
    );

    /**
     * Count of all CUSTOMER users for overview KPI (regardless of current filter).
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :type")
    long countByUserType(@Param("type") UserType type);

    /**
     * Count of CUSTOMER users whose accountStatus = ACTIVE for the overview KPI.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :type AND u.accountStatus = :status")
    long countByUserTypeAndStatus(@Param("type") UserType type, @Param("status") AccountStatus status);
}
