package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.RelatedProductMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelatedProductMappingRepository extends JpaRepository<RelatedProductMapping,Long>{List<RelatedProductMapping> findByProductIdOrderByIdAsc(Long productId);}