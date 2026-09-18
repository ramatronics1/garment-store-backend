package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByActiveTrueOrderByDisplayOrderAscNameAsc();

    /** All active root (top-level) categories — used as entry points for tree building. */
    List<Category> findByParentCategoryIsNullAndActiveTrueOrderByDisplayOrderAscNameAsc();

    /** All active children of a given parent — used for recursive tree construction. */
    List<Category> findByParentCategoryIdAndActiveTrueOrderByDisplayOrderAscNameAsc(Long parentId);

    /** Fetch all active categories whose ID is in the given set — used for subcategory filtering. */
    List<Category> findByIdInAndActiveTrue(List<Long> ids);

    boolean existsBySlug(String slug);

    /**
     * Find a category by name (case-insensitive).
     * Used by ProductController to resolve category name → ID from UI query params.
     * Example: "Shirts" or "shirts" or "SHIRTS" all resolve to the same category.
     */
    Optional<Category> findByNameIgnoreCase(String name);
}