package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.FeaturedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeaturedProductRepository extends JpaRepository<FeaturedProduct,Long>{List<FeaturedProduct> findByActiveTrueOrderByDisplayOrderAscIdAsc();}