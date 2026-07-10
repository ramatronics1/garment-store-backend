package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Long>{List<Category> findByActiveTrueOrderByDisplayOrderAscNameAsc();boolean existsBySlug(String slug);}