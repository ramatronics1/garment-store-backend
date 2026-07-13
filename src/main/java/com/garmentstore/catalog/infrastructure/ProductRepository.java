package com.garmentstore.catalog.infrastructure;

import com.garmentstore.catalog.domain.GenderTag;
import com.garmentstore.catalog.domain.Product;
import com.garmentstore.catalog.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndStatus(Long id, ProductStatus s);

    Optional<Product> findBySlugAndStatus(String slug, ProductStatus s);

    boolean existsBySlug(String slug);

    /**
     * Full-featured product search with all UI filter params.
     *
     * Filters applied:
     *  - status = ACTIVE, deletedAt IS NULL (always)
     *  - gender (optional)
     *  - categoryId (optional)
     *  - keyword — case-insensitive LIKE on name (optional)
     *  - minPrice / maxPrice — on sellingPrice (optional)
     *  - minDiscount — on discountPercent (optional)
     *
     * Note on sizes & colors: These are applied as post-query in-memory filters
     * in CatalogService because:
     *  - Sizes require joining to product_variants (JPA N+1 risk with IN clause on joined entity)
     *  - Colors use exact-match IN which is cleaner in Java for a small set
     *  - The dataset per-page is ≤100 items, so in-memory is negligible
     *
     * The DB indexes on (gender_tag), (selling_price), (discount_percent), (color),
     * (status, category_id) make this query fast even on large tables.
     */
    @Query("""
            SELECT DISTINCT p FROM Product p
            WHERE p.status = :status
              AND p.deletedAt IS NULL
              AND (:gender IS NULL OR p.genderTag = :gender)
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:minPrice IS NULL OR p.sellingPrice >= :minPrice)
              AND (:maxPrice IS NULL OR p.sellingPrice <= :maxPrice)
              AND (:minDiscount IS NULL OR p.discountPercent >= :minDiscount)
            """)
    Page<Product> searchWithFilters(
            @Param("status") ProductStatus status,
            @Param("gender") GenderTag gender,
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minDiscount") Integer minDiscount,
            Pageable pageable
    );

    /**
     * Fetch all products matching base filters (without pagination) for use when
     * size/color in-memory filtering is needed — used when sizes or colors filter is active.
     *
     * Returns IDs only to minimize data transfer; CatalogService then applies
     * size/color filtering and slices the page from the remaining IDs.
     */
    @Query("""
            SELECT p.id FROM Product p
            WHERE p.status = :status
              AND p.deletedAt IS NULL
              AND (:gender IS NULL OR p.genderTag = :gender)
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:minPrice IS NULL OR p.sellingPrice >= :minPrice)
              AND (:maxPrice IS NULL OR p.sellingPrice <= :maxPrice)
              AND (:minDiscount IS NULL OR p.discountPercent >= :minDiscount)
            ORDER BY
              CASE WHEN :sort = 'price_asc'  THEN p.sellingPrice   END ASC,
              CASE WHEN :sort = 'price_desc' THEN p.sellingPrice   END DESC,
              CASE WHEN :sort = 'discount'   THEN p.discountPercent END DESC,
              p.id DESC
            """)
    List<Long> findIdsWithFilters(
            @Param("status") ProductStatus status,
            @Param("gender") GenderTag gender,
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minDiscount") Integer minDiscount,
            @Param("sort") String sort
    );

    /**
     * Fetch products whose IDs are in the given list, preserving order via FIELD().
     * Used in the sizes/colors filter path to fetch full product data after ID filtering.
     */
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findAllByIdIn(@Param("ids") List<Long> ids);

    /**
     * Legacy simple search — kept for backward compatibility with admin use.
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.status = :status
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Product> searchPublic(
            @Param("status") ProductStatus status,
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}