package com.garmentstore.catalog.api;

import com.garmentstore.catalog.application.CatalogService;
import com.garmentstore.catalog.domain.GenderTag;
import com.garmentstore.catalog.dto.*;
import com.garmentstore.catalog.infrastructure.CategoryRepository;
import com.garmentstore.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Public product catalog API — all endpoints are open (no authentication required).
 *
 * GET /api/v1/products — paginated product listing with full filter stack
 * GET /api/v1/products/{id} — full product detail
 * GET /api/v1/products/slug/{slug} — product detail by slug (SEO-friendly)
 * GET /api/v1/products/{id}/related — related products
 * GET /api/v1/products/featured — featured products for the home page
 * GET /api/v1/products/{id}/size-guide — size chart
 * GET /api/v1/products/{id}/availability — variant availability
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final CatalogService s;
    private final CategoryRepository categoryRepository;

    /**
     * GET /api/v1/products
     *
     * Full-featured product listing with server-side pagination, filtering & sorting.
     *
     * Query parameters (all optional):
     *  @param gender      Gender tag — "MEN", "WOMEN", "KIDS" (case-insensitive)
     *  @param category    Category name — "Shirts", "T-Shirts", "Jeans", etc. (resolved to ID internally)
     *  @param sizes       Comma-separated size codes — "S,M,L" or "XS,S"
     *  @param colors      Comma-separated color names — "black,blue,red"
     *  @param minPrice    Minimum selling price (inclusive)
     *  @param maxPrice    Maximum selling price (inclusive)
     *  @param minDiscount Minimum discount percentage (10, 20, 30, 50)
     *  @param q           Full-text search across product name and description
     *  @param sort        Sort order: relevant | price_asc | price_desc | newest | discount
     *  @param page        Page number, 1-based (UI sends 1, maps to 0-based internally)
     *  @param size        Items per page (default 12, max 100)
     *
     * Response shape (matches useProductListing.js exactly):
     *  { products: [...], total: 85, page: 1, perPage: 12, totalPages: 8 }
     */
    @GetMapping
    public ApiResponse<ProductPageResponse> all(
            @RequestParam(required = false)                    String gender,
            @RequestParam(required = false)                    String category,
            @RequestParam(required = false)                    String sizes,
            @RequestParam(required = false)                    String colors,
            @RequestParam(required = false)                    BigDecimal minPrice,
            @RequestParam(required = false)                    BigDecimal maxPrice,
            @RequestParam(required = false)                    Integer minDiscount,
            @RequestParam(required = false)                    String q,
            @RequestParam(defaultValue = "relevant")           String sort,
            @RequestParam(defaultValue = "1")                  int page,
            @RequestParam(defaultValue = "12")                 int size
    ) {
        // Resolve gender string → GenderTag enum (case-insensitive, null if not provided or invalid)
        GenderTag genderTag = resolveGender(gender);

        // Resolve category name → categoryId (null if not provided or not found)
        Long categoryId = resolveCategory(category);

        // Parse comma-separated lists
        List<String> sizeList  = parseCsvParam(sizes);
        List<String> colorList = parseCsvParam(colors);

        // Clamp pagination: UI sends 1-based page, backend uses 0-based; cap size at 100
        int pageIndex = Math.max(0, page - 1);
        int pageSize  = Math.min(Math.max(1, size), 100);

        ProductFilterParams params = new ProductFilterParams(
                genderTag, categoryId, q,
                minPrice, maxPrice, minDiscount,
                sizeList, colorList,
                pageIndex, pageSize, sort
        );

        return ApiResponse.success("Products fetched successfully", s.getProducts(params));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> one(@PathVariable Long id) {
        return ApiResponse.success("Product fetched successfully", s.getProduct(id));
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<ProductDetailResponse> slug(@PathVariable String slug) {
        return ApiResponse.success("Product fetched successfully", s.getProductBySlug(slug));
    }

    @GetMapping("/{id}/related")
    public ApiResponse<List<ProductSummaryResponse>> related(@PathVariable Long id) {
        return ApiResponse.success("Related products fetched successfully", s.getRelatedProducts(id));
    }

    @GetMapping("/featured")
    public ApiResponse<List<ProductSummaryResponse>> featured() {
        return ApiResponse.success("Featured products fetched successfully", s.getFeaturedProducts());
    }

    @GetMapping("/{id}/size-guide")
    public ApiResponse<SizeGuideResponse> size(@PathVariable Long id) {
        return ApiResponse.success("Size guide fetched successfully", s.getSizeGuide(id));
    }

    @GetMapping("/{id}/availability")
    public ApiResponse<ProductAvailabilityResponse> av(@PathVariable Long id) {
        return ApiResponse.success("Product availability fetched successfully", s.getAvailability(id));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Resolves gender string to GenderTag enum.
     * Case-insensitive: "men", "Men", "MEN" → GenderTag.MEN
     * Returns null for null/blank/unrecognized values (means "all genders").
     */
    private GenderTag resolveGender(String gender) {
        if (gender == null || gender.isBlank()) return null;
        try {
            return GenderTag.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // unrecognized gender → no filter
        }
    }

    /**
     * Resolves category name to categoryId.
     * "Shirts" → looks up Category by name (case-insensitive) → returns id
     * "All" or null/blank → returns null (no category filter)
     */
    private Long resolveCategory(String category) {
        if (category == null || category.isBlank() || "All".equalsIgnoreCase(category)) return null;
        return categoryRepository.findByNameIgnoreCase(category)
                .filter(cat -> cat.isActive())
                .map(cat -> cat.getId())
                .orElse(null); // unrecognized category name → no filter
    }

    /**
     * Parses a comma-separated parameter string into a list.
     * "S,M,L" → ["S", "M", "L"]
     * null or blank → empty list
     */
    private List<String> parseCsvParam(String param) {
        if (param == null || param.isBlank()) return List.of();
        return Arrays.stream(param.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}