package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant,Long>{List<ProductVariant> findByProductIdAndActiveTrueOrderBySizeCodeAsc(Long productId);}