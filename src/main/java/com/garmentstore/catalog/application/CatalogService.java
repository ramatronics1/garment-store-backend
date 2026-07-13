package com.garmentstore.catalog.application;

import com.garmentstore.catalog.domain.*;
import com.garmentstore.catalog.dto.*;
import com.garmentstore.catalog.infrastructure.*;
import com.garmentstore.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CategoryRepository categories;
    private final ProductRepository products;
    private final ProductImageRepository images;
    private final ProductVariantRepository variants;
    private final FeaturedProductRepository featured;
    private final RelatedProductMappingRepository related;

    // ── Category ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categories.findByActiveTrueOrderByDisplayOrderAscNameAsc()
                .stream().map(this::cat).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        return categories.findById(id)
                .filter(Category::isActive)
                .map(this::cat)
                .orElseThrow(() -> new BusinessException(
                        "CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND));
    }

    // ── Product Listing (with full filter stack) ──────────────────────────────

    /**
     * Fetches a paginated, filtered, sorted product listing.
     *
     * Filter strategy:
     *  1. Scalar filters (gender, category, price, discount, keyword) — applied in DB via JPQL
     *  2. Size filter — applied post-DB via ProductVariantRepository.findProductIdsHavingSizes
     *  3. Color filter — applied post-DB via Product.color field (exact match, case-insensitive)
     *
     * The two-pass approach (sizes/colors post-DB) avoids N+1 issues from complex
     * JOIN + IN queries in JPA, while keeping DB-level filtering for the heavy cases.
     *
     * @param params All filter, sort, and pagination params from the UI
     * @return UI-aligned page response with products, total, page, perPage, totalPages
     */
    @Transactional(readOnly = true)
    public ProductPageResponse getProducts(ProductFilterParams params) {
        boolean hasSizeFilter  = params.sizes()  != null && !params.sizes().isEmpty();
        boolean hasColorFilter = params.colors() != null && !params.colors().isEmpty();

        if (hasSizeFilter || hasColorFilter) {
            return getProductsWithVariantFilters(params, hasSizeFilter, hasColorFilter);
        }

        // Fast path: all filters handled by DB — single paginated query
        Page<Product> page = products.searchWithFilters(
                ProductStatus.ACTIVE,
                params.gender(),
                params.categoryId(),
                blankToNull(params.keyword()),
                params.minPrice(),
                params.maxPrice(),
                params.minDiscount(),
                PageRequest.of(params.page(), params.size(), sort(params.sort()))
        );

        return new ProductPageResponse(
                page.getContent().stream().map(this::summary).toList(),
                page.getTotalElements(),
                params.page() + 1,       // convert back to 1-based for UI
                params.size(),
                page.getTotalPages()
        );
    }

    /**
     * Handles size and/or color filtering using a two-pass approach:
     *  Pass 1: Fetch all matching product IDs from DB (with scalar filters applied)
     *  Pass 2: Filter IDs by sizes (via variant lookup) and colors (in-memory)
     *  Pass 3: Manual pagination on the filtered+sorted ID list
     */
    private ProductPageResponse getProductsWithVariantFilters(
            ProductFilterParams params, boolean hasSizeFilter, boolean hasColorFilter) {

        // Pass 1: Get all matching IDs (scalar filters in DB, ordered by sort)
        List<Long> allIds = products.findIdsWithFilters(
                ProductStatus.ACTIVE,
                params.gender(),
                params.categoryId(),
                blankToNull(params.keyword()),
                params.minPrice(),
                params.maxPrice(),
                params.minDiscount(),
                params.sort() == null ? "newest" : params.sort()
        );

        if (allIds.isEmpty()) {
            return new ProductPageResponse(List.of(), 0, params.page() + 1, params.size(), 0);
        }

        // Pass 2a: Filter by sizes
        List<Long> filteredIds = new ArrayList<>(allIds);
        if (hasSizeFilter) {
            List<Long> sizeMatchIds = variants.findProductIdsHavingSizes(
                    filteredIds, params.sizes()
            );
            Set<Long> sizeSet = new HashSet<>(sizeMatchIds);
            filteredIds = filteredIds.stream().filter(sizeSet::contains).toList();
        }

        // Pass 2b: Filter by colors (in-memory — products already fetched as IDs)
        if (hasColorFilter && !filteredIds.isEmpty()) {
            Set<String> colorSet = params.colors().stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            // Fetch full product data for the remaining IDs to check color
            List<Product> colorCandidates = products.findAllByIdIn(filteredIds);
            // Preserve the sort order from allIds
            Map<Long, Product> productMap = colorCandidates.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));
            filteredIds = filteredIds.stream()
                    .filter(id -> {
                        Product p = productMap.get(id);
                        return p != null && p.getColor() != null &&
                               colorSet.contains(p.getColor().toLowerCase());
                    })
                    .toList();
        }

        // Pass 3: Manual pagination
        long totalElements = filteredIds.size();
        int totalPages = (int) Math.ceil((double) totalElements / params.size());
        int fromIndex = Math.min(params.page() * params.size(), filteredIds.size());
        int toIndex   = Math.min(fromIndex + params.size(), filteredIds.size());
        List<Long> pageIds = filteredIds.subList(fromIndex, toIndex);

        if (pageIds.isEmpty()) {
            return new ProductPageResponse(List.of(), totalElements, params.page() + 1, params.size(), totalPages);
        }

        // Fetch full product data for the final page
        List<Product> pageProducts = products.findAllByIdIn(pageIds);
        // Restore sort order (findAllByIdIn doesn't guarantee order)
        Map<Long, Product> pageMap = pageProducts.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        List<ProductSummaryResponse> summaries = pageIds.stream()
                .map(pageMap::get)
                .filter(Objects::nonNull)
                .map(this::summary)
                .toList();

        return new ProductPageResponse(summaries, totalElements, params.page() + 1, params.size(), totalPages);
    }

    // ── Legacy method — kept for CategoryController compatibility ─────────────

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> getProductsLegacy(Long cid, String q, int page, int size, String sort) {
        Page<Product> r = products.searchPublic(
                ProductStatus.ACTIVE, cid,
                q == null || q.isBlank() ? null : q.trim(),
                PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), sort(sort))
        );
        return new PageResponse<>(
                r.getContent().stream().map(this::summary).toList(),
                r.getNumber(), r.getSize(), r.getTotalElements(), r.getTotalPages(), r.isLast()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> getCategoryProducts(Long id, int p, int s) {
        getCategory(id);
        return getProductsLegacy(id, null, p, s, "newest");
    }

    // ── Product Detail ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ProductDetailResponse getProduct(Long id) {
        return detail(products.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        return detail(products.findBySlugAndStatus(slug, ProductStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND)));
    }

    // ── Featured & Related ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getFeaturedProducts() {
        return featured.findByActiveTrueOrderByDisplayOrderAscIdAsc().stream()
                .map(FeaturedProduct::getProduct)
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .map(this::summary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getRelatedProducts(Long id) {
        getProduct(id);
        return related.findByProductIdOrderByIdAsc(id).stream()
                .map(RelatedProductMapping::getRelatedProduct)
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .map(this::summary)
                .toList();
    }

    // ── Size Guide & Availability ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SizeGuideResponse getSizeGuide(Long id) {
        getProduct(id);
        var s = variants.findByProductIdAndActiveTrueOrderBySizeCodeAsc(id)
                .stream().map(ProductVariant::getSizeCode).distinct().toList();
        return new SizeGuideResponse(id, s, "Detailed size chart can be managed in Admin Product Module.");
    }

    @Transactional(readOnly = true)
    public ProductAvailabilityResponse getAvailability(Long id) {
        getProduct(id);
        var s = variants.findByProductIdAndActiveTrueOrderBySizeCodeAsc(id)
                .stream().map(ProductVariant::getSizeCode).distinct().toList();
        return new ProductAvailabilityResponse(id, !s.isEmpty(), s, "VARIANT_ACTIVE_STATUS_ONLY_UNTIL_INVENTORY_MODULE");
    }

    // ── Mapper helpers ────────────────────────────────────────────────────────

    public CategoryResponse cat(Category c) {
        return new CategoryResponse(
                c.getId(), c.getName(), c.getSlug(),
                c.getParentCategory() == null ? null : c.getParentCategory().getId(),
                c.getDisplayOrder(), c.isActive()
        );
    }

    public ProductSummaryResponse summary(Product p) {
        String th = images.findFirstByProductIdAndThumbnailTrueOrderByDisplayOrderAscIdAsc(p.getId())
                .or(() -> images.findByProductIdOrderByDisplayOrderAscIdAsc(p.getId()).stream().findFirst())
                .map(ProductImage::getMediaUrl)
                .orElse(null);
        return new ProductSummaryResponse(
                p.getId(), p.getName(), p.getSlug(),
                p.getCategory().getId(), p.getCategory().getName(),
                p.getGenderTag(), p.getMrp(), p.getSellingPrice(),
                p.getDiscountPercent(), p.getColor(), th
        );
    }

    public ProductDetailResponse detail(Product p) {
        return new ProductDetailResponse(
                p.getId(), p.getName(), p.getSlug(),
                cat(p.getCategory()), p.getGenderTag(),
                p.getMrp(), p.getSellingPrice(), p.getDiscountPercent(),
                p.getColor(), p.getDescription(), p.getFabricDetails(),
                p.getCareInstructions(), p.getCountryOfOrigin(),
                p.isReturnPolicyEnabled(), p.getMetaTitle(), p.getMetaDescription(),
                p.getStatus(),
                images.findByProductIdOrderByDisplayOrderAscIdAsc(p.getId()).stream()
                        .map(i -> new ProductImageResponse(i.getId(), i.getMediaUrl(), i.getDisplayOrder(), i.isThumbnail()))
                        .toList(),
                variants.findByProductIdAndActiveTrueOrderBySizeCodeAsc(p.getId()).stream()
                        .map(v -> new ProductVariantResponse(v.getId(), v.getSizeCode(), v.getSkuCode(), v.isActive()))
                        .toList()
        );
    }

    /**
     * Converts UI sort string to Spring Sort.
     *
     * Supported values (matching UI PLPToolbar.jsx SORT_OPTIONS):
     *  relevant  → newest (createdAt DESC) — default/fallback
     *  price_asc → sellingPrice ASC
     *  price_desc→ sellingPrice DESC
     *  newest    → createdAt DESC (id DESC as tiebreaker)
     *  discount  → discountPercent DESC
     */
    private Sort sort(String s) {
        return switch (s == null ? "relevant" : s) {
            case "price_asc"  -> Sort.by("sellingPrice").ascending();
            case "price_desc" -> Sort.by("sellingPrice").descending();
            case "discount"   -> Sort.by(Sort.Order.desc("discountPercent"), Sort.Order.desc("id"));
            case "newest"     -> Sort.by(Sort.Order.desc("id"));
            default           -> Sort.by(Sort.Order.desc("id")); // relevant → newest
        };
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}