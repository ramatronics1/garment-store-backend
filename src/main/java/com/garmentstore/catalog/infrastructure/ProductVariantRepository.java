package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.ProductVariant;
import com.garmentstore.catalog.domain.VariantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductIdOrderByColorDisplayOrderAscSizeSortOrderAsc(Long productId);

    List<ProductVariant> findByProductIdAndStatusOrderByColorDisplayOrderAscSizeSortOrderAsc(Long productId, VariantStatus status);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByProductIdAndCombinationKey(Long productId, String combinationKey);

    boolean existsByProductIdAndCombinationKeyAndIdNot(Long productId, String combinationKey, Long id);

    int countByProductId(Long productId);
}