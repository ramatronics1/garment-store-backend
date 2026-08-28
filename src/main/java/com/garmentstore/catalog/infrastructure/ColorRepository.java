package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.Color;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ColorRepository extends JpaRepository<Color, Long> {
    List<Color> findByActiveTrueOrderByDisplayOrderAscNameAsc();
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
    Optional<Color> findByCode(String code);
}
