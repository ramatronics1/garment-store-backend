package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.ProductVariant;
import com.garmentstore.catalog.domain.VariantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductIdOrderByColorDisplayOrderAscSizeSortOrderAsc(Long productId);

    List<ProductVariant> findByProductIdAndStatusOrderByColorDisplayOrderAscSizeSortOrderAsc(Long productId, VariantStatus status);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByProductIdAndCombinationKey(Long productId, String combinationKey);

    boolean existsByProductIdAndCombinationKeyAndIdNot(Long productId, String combinationKey, Long id);

    int countByProductId(Long productId);

    /**
     * Returns distinct product IDs where at least one ACTIVE variant has a real discount
     * (selling_price strictly less than mrp) and the computed discount percentage >= minDiscountPct.
     *
     * Formula: (mrp - sellingPrice) / mrp * 100 >= minDiscountPct
     * Equivalent JPQL form:  (mrp - sellingPrice) * 100 >= minDiscountPct * mrp
     *
     * Used by:
     *   - {@code getProducts()} to implement the ?minDiscount filter (was silently ignored before)
     *   - {@code getSaleProducts()} to power the /products/sale endpoint
     */
    @Query("""
            select distinct v.product.id
            from ProductVariant v
            where v.status = 'ACTIVE'
              and v.mrp is not null
              and v.sellingPrice is not null
              and v.mrp > v.sellingPrice
              and (v.mrp - v.sellingPrice) * 100 >= :minDiscountPct * v.mrp
            """)
    List<Long> findProductIdsWithDiscountAtLeast(@Param("minDiscountPct") BigDecimal minDiscountPct);

    long countByStatusAndStockQuantity(VariantStatus status, int stockQuantity);

    @Query("""
        SELECT v FROM ProductVariant v
        JOIN FETCH v.product p
        JOIN FETCH v.size s
        JOIN FETCH v.color c
        WHERE v.status = 'ACTIVE'
          AND v.stockQuantity <= :threshold
        ORDER BY v.stockQuantity ASC, v.id ASC
    """)
    List<ProductVariant> findLowStockVariants(@Param("threshold") int threshold, org.springframework.data.domain.Pageable pageable);
}