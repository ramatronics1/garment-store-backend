package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductIdAndActiveTrueOrderBySizeCodeAsc(Long productId);

    /**
     * Returns product IDs that have at least one active variant matching any of the given size codes.
     * Used for the sizes filter — checks which products among a set of candidate IDs
     * actually have the requested sizes available.
     *
     * @param productIds  candidate product IDs to check
     * @param sizeCodes   set of size codes to match (e.g. {"S", "M", "L"})
     * @return product IDs that have at least one matching active size
     */
    @Query("""
            SELECT DISTINCT v.product.id FROM ProductVariant v
            WHERE v.product.id IN :productIds
              AND v.active = true
              AND v.sizeCode IN :sizeCodes
            """)
    List<Long> findProductIdsHavingSizes(
            @Param("productIds") List<Long> productIds,
            @Param("sizeCodes") List<String> sizeCodes
    );
}