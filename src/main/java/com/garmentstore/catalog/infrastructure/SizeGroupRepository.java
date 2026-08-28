package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.SizeGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SizeGroupRepository extends JpaRepository<SizeGroup, Long> {
    List<SizeGroup> findByActiveTrueOrderByNameAsc();
    boolean existsByName(String name);
}
