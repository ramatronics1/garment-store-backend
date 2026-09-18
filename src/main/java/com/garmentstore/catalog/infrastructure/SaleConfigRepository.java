package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.SaleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SaleConfigRepository extends JpaRepository<SaleConfig, Long> {

    /** Fetch the single active sale configuration. */
    Optional<SaleConfig> findFirstByOrderByIdAsc();
}
