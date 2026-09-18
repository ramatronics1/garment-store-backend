package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.FeaturedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeaturedProductRepository extends JpaRepository<FeaturedProduct, Long> {

    /** Public-facing: only active entries, ordered for display. */
    List<FeaturedProduct> findByActiveTrueOrderByDisplayOrderAscIdAsc();

    /** Admin-facing: all entries (active + inactive) for management. */
    List<FeaturedProduct> findAllByOrderByDisplayOrderAscIdAsc();

    /** Duplication guard — prevents adding the same product twice. */
    boolean existsByProductId(Long productId);
}