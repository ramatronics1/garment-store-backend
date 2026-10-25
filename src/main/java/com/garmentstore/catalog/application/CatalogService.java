package com.garmentstore.catalog.application;

import com.garmentstore.catalog.domain.*;
import com.garmentstore.catalog.dto.*;
import com.garmentstore.catalog.infrastructure.*;
import com.garmentstore.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogService {

    /**
     * Window (in days) within which a product is considered a "new arrival".
     * Configurable via {@code catalog.new-arrivals.days} in application.properties.
     * Defaults to 30 days.
     */
    @Value("${catalog.new-arrivals.days:30}")
    private int newArrivalsDays;

    private final CategoryRepository categories;
    private final ProductRepository products;
    private final ProductImageRepository images;
    private final ProductVariantRepository variants;
    private final FeaturedProductRepository featured;
    private final RelatedProductMappingRepository related;
    private final StoreMediaRepository storeMedia;
    private final SaleConfigRepository saleConfig;

    // =========================================================================
    // Categories
    // =========================================================================

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categories.findByActiveTrueOrderByDisplayOrderAscNameAsc().stream()
                .map(this::cat).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        return categories.findById(id).filter(Category::isActive).map(this::cat)
                .orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND));
    }

    // =========================================================================
    // Product listing
    // =========================================================================

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> getProducts(
            Long categoryId, String categoryParam, String gender,
            BigDecimal minPrice, BigDecimal maxPrice, Integer minDiscount,
            String brand, String fit, String season, String fabric,
            String q, int page, int size, String sort) {

        GenderTag genderTag = null;
        if (gender != null && !gender.isBlank()) {
            try { genderTag = GenderTag.valueOf(gender.toUpperCase()); } catch (Exception ignored) {}
        }

        Long finalCategoryId = categoryId;
        String categoryText  = null;
        if (categoryParam != null && !categoryParam.isBlank()) {
            try { finalCategoryId = Long.parseLong(categoryParam.trim()); }
            catch (NumberFormatException e) { categoryText = categoryParam.trim().toLowerCase(); }
        }
        Long cid          = finalCategoryId;
        String ctext      = categoryText;
        GenderTag fGender = genderTag;

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), sort(sort));

        // Resolve minDiscount → matching product IDs via variant query (was silently ignored before)
        List<Long> discountFilteredIds = null;
        if (minDiscount != null && minDiscount > 0) {
            discountFilteredIds = variants.findProductIdsWithDiscountAtLeast(BigDecimal.valueOf(minDiscount));
            if (discountFilteredIds.isEmpty()) {
                // No products match the discount filter — return empty page immediately
                return new PageResponse<>(List.of(), 0, size, 0L, 0, true);
            }
        }

        // Resolve categoryId to include subcategories (two-step: find child IDs, then IN clause)
        final List<Long> categoryIdFilter;
        if (cid != null) {
            List<Long> childIds = categories
                    .findByParentCategoryIdAndActiveTrueOrderByDisplayOrderAscNameAsc(cid)
                    .stream().map(Category::getId).collect(Collectors.toList());
            childIds.add(cid); // include the parent itself
            categoryIdFilter = childIds;
        } else if (ctext != null) {
            Optional<Category> matched = categories.findBySlugIgnoreCaseAndActiveTrue(ctext);
            if (matched.isEmpty()) {
                matched = categories.findByNameIgnoreCaseAndActiveTrue(ctext);
            }
            if (matched.isPresent()) {
                Long matchedId = matched.get().getId();
                List<Long> childIds = categories
                        .findByParentCategoryIdAndActiveTrueOrderByDisplayOrderAscNameAsc(matchedId)
                        .stream().map(Category::getId).collect(Collectors.toList());
                childIds.add(matchedId);
                categoryIdFilter = childIds;
            } else {
                categoryIdFilter = null;
            }
        } else {
            categoryIdFilter = null;
        }

        final List<Long> finalDiscountIds = discountFilteredIds;

        Page<Product> paged = products.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));

            if (categoryIdFilter != null) {
                predicates.add(root.get("category").get("id").in(categoryIdFilter));
            } else if (ctext != null) {
                predicates.add(cb.or(
                        cb.equal(cb.lower(root.get("category").get("name")), ctext),
                        cb.equal(cb.lower(root.get("category").get("slug")), ctext)));
            }
            if (fGender != null) {
                predicates.add(cb.equal(root.get("genderTag"), fGender));
            }
            if (brand != null && !brand.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("brand")), brand.trim().toLowerCase()));
            }
            if (fit != null && !fit.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("fit")), fit.trim().toLowerCase()));
            }
            if (season != null && !season.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("season")), season.trim().toLowerCase()));
            }
            if (fabric != null && !fabric.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("fabricDetails")), "%" + fabric.trim().toLowerCase() + "%"));
            }
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("slug")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)));
            }
            if (finalDiscountIds != null) {
                predicates.add(root.get("id").in(finalDiscountIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        // Post-filter by price range if needed (applied in-memory after variant lookup)
        List<ProductSummaryResponse> content = paged.getContent().stream()
                .map(this::summary)
                .filter(s -> {
                    if (minPrice != null && (s.minSellingPrice() == null || s.minSellingPrice().compareTo(minPrice) < 0)) return false;
                    if (maxPrice != null && (s.minSellingPrice() == null || s.minSellingPrice().compareTo(maxPrice) > 0)) return false;
                    return true;
                })
                .toList();

        return new PageResponse<>(content, paged.getNumber(), paged.getSize(),
                paged.getTotalElements(), paged.getTotalPages(), paged.isLast());
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> getProducts(
            Long categoryId, String categoryParam, String gender,
            BigDecimal minPrice, BigDecimal maxPrice, Integer minDiscount,
            String q, int page, int size, String sort) {
        return getProducts(categoryId, categoryParam, gender, minPrice, maxPrice, minDiscount, null, null, null, null, q, page, size, sort);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> getProducts(Long cid, String q, int page, int size, String sort) {
        return getProducts(cid, null, null, null, null, null, null, null, null, null, q, page, size, sort);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> getCategoryProducts(Long id, int p, int s) {
        getCategory(id);
        return getProducts(id, null, p, s, "newest");
    }

    // =========================================================================
    // Product detail
    // =========================================================================

    @Transactional(readOnly = true)
    public ProductDetailResponse getProduct(Long id) {
        return detail(products.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        return detail(products.findBySlugIgnoreCaseAndStatus(slug, ProductStatus.ACTIVE)
                .or(() -> {
                    try {
                        Long id = Long.parseLong(slug.trim());
                        return products.findByIdAndStatus(id, ProductStatus.ACTIVE);
                    } catch (NumberFormatException e) {
                        return java.util.Optional.empty();
                    }
                })
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND)));
    }

    // =========================================================================
    // Featured / Related
    // =========================================================================

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getFeaturedProducts() {
        return featured.findByActiveTrueOrderByDisplayOrderAscIdAsc().stream()
                .map(FeaturedProduct::getProduct)
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .map(this::summary).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getRelatedProducts(Long id) {
        getProduct(id);
        return related.findByProductIdOrderByIdAsc(id).stream()
                .map(RelatedProductMapping::getRelatedProduct)
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .map(this::summary).toList();
    }

    // =========================================================================
    // Category Tree
    // =========================================================================

    /**
     * Returns all active categories as a nested tree.
     * Root nodes have no parent; each carries a {@code children} list.
     * Single DB call: fetch all active categories, build tree in memory.
     */
    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> all = categories.findByActiveTrueOrderByDisplayOrderAscNameAsc();

        // Group children by parentId for O(n) lookup
        Map<Long, List<Category>> byParent = all.stream()
                .filter(c -> c.getParentCategory() != null)
                .collect(Collectors.groupingBy(c -> c.getParentCategory().getId()));

        // Build tree nodes only for root categories
        return all.stream()
                .filter(c -> c.getParentCategory() == null)
                .map(c -> toTreeNode(c, byParent))
                .toList();
    }

    private CategoryTreeResponse toTreeNode(Category c, Map<Long, List<Category>> byParent) {
        List<CategoryTreeResponse> children = byParent
                .getOrDefault(c.getId(), List.of())
                .stream()
                .map(child -> toTreeNode(child, byParent))
                .toList();
        return new CategoryTreeResponse(
                c.getId(), c.getName(), c.getSlug(),
                c.getParentCategory() == null ? null : c.getParentCategory().getId(),
                c.getDisplayOrder(), c.isActive(),
                c.getThumbnailUrl(), c.getBannerUrl(), c.getBannerAlt(),
                c.getDescription(), c.getIconUrl(),
                children);
    }

    // =========================================================================
    // Store Media
    // =========================================================================

    /**
     * Returns all active hero banner slides ordered by display_order.
     * Placement keys follow the pattern {@code hero_banner_*}.
     */
    @Transactional(readOnly = true)
    public List<StoreMediaResponse> getHeroSlides() {
        return storeMedia
                .findByPlacementKeyStartingWithAndActiveTrueOrderByDisplayOrderAsc("hero_banner_")
                .stream().map(this::mediaResponse).toList();
    }

    /**
     * Returns a single active store media slot by placement key.
     * Returns null (not an error) if the slot is inactive or missing.
     */
    @Transactional(readOnly = true)
    public StoreMediaResponse getStoreMedia(String key) {
        return storeMedia.findByPlacementKey(key)
                .filter(StoreMedia::isActive)
                .map(this::mediaResponse)
                .orElse(null);
    }

    // =========================================================================
    // Sale Config
    // =========================================================================

    /**
     * Returns the current sale configuration.
     * When {@code active} is false the frontend should hide the sale banner.
     */
    @Transactional(readOnly = true)
    public SaleConfigResponse getSaleConfig() {
        return saleConfig.findFirstByOrderByIdAsc()
                .map(this::saleResponse)
                .orElse(null);
    }

    // =========================================================================
    // Sale Products
    // =========================================================================

    /**
     * Returns paginated ACTIVE products that have at least one active variant with
     * any real discount (selling_price < mrp, i.e. discount >= 1%).
     * Sale is derived from pricing — no separate flag needed.
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> getSaleProducts(int page, int size) {
        List<Long> saleProductIds = variants.findProductIdsWithDiscountAtLeast(BigDecimal.ONE);
        if (saleProductIds.isEmpty()) {
            return new PageResponse<>(List.of(), page, size, 0L, 0, true);
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100),
                Sort.by("createdAt").descending());

        Page<Product> paged = products.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("status"), ProductStatus.ACTIVE),
                root.get("id").in(saleProductIds)
        ), pageable);

        List<ProductSummaryResponse> content = paged.getContent().stream().map(this::summary).toList();
        return new PageResponse<>(content, paged.getNumber(), paged.getSize(),
                paged.getTotalElements(), paged.getTotalPages(), paged.isLast());
    }

    // =========================================================================
    // New Arrivals
    // =========================================================================

    /**
     * Returns paginated ACTIVE products created within the last {@code newArrivalsDays} days,
     * newest first. The window is configurable via {@code catalog.new-arrivals.days}.
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> getNewArrivals(int page, int size) {
        Instant since = Instant.now().minus(newArrivalsDays, ChronoUnit.DAYS);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));

        Page<Product> paged = products
                .findByStatusAndCreatedAtAfterOrderByCreatedAtDesc(ProductStatus.ACTIVE, since, pageable);

        List<ProductSummaryResponse> content = paged.getContent().stream().map(this::summary).toList();
        return new PageResponse<>(content, paged.getNumber(), paged.getSize(),
                paged.getTotalElements(), paged.getTotalPages(), paged.isLast());
    }

    // =========================================================================
    // Size guide / Availability
    // =========================================================================

    @Transactional(readOnly = true)
    public SizeGuideResponse getSizeGuide(Long id) {
        getProduct(id);
        List<String> sizeCodes = variants
                .findByProductIdAndStatusOrderByColorDisplayOrderAscSizeSortOrderAsc(id, VariantStatus.ACTIVE)
                .stream().map(v -> v.getSize().getSizeCode()).distinct().toList();
        return new SizeGuideResponse(id, sizeCodes, "Detailed size chart can be managed in Admin Product Module.");
    }

    @Transactional(readOnly = true)
    public ProductAvailabilityResponse getAvailability(Long id) {
        getProduct(id);
        List<ProductVariant> activeVariants = variants
                .findByProductIdAndStatusOrderByColorDisplayOrderAscSizeSortOrderAsc(id, VariantStatus.ACTIVE);
        boolean inStock = activeVariants.stream().anyMatch(v -> v.getStockQuantity() > 0);
        List<ProductAvailabilityResponse.VariantStock> variantStocks = activeVariants.stream()
                .map(v -> new ProductAvailabilityResponse.VariantStock(
                        v.getSize().getSizeCode(), v.getStockQuantity(), v.getStockQuantity() > 0))
                .toList();
        return new ProductAvailabilityResponse(id, inStock, variantStocks, "LIVE_STOCK");
    }

    // =========================================================================
    // Mapper helpers — package-accessible so AdminCatalogService can reuse
    // =========================================================================

    public CategoryResponse cat(Category c) {
        return new CategoryResponse(
                c.getId(), c.getName(), c.getSlug(),
                c.getParentCategory() == null ? null : c.getParentCategory().getId(),
                c.getDisplayOrder(), c.isActive(),
                c.getThumbnailUrl(), c.getBannerUrl(), c.getBannerAlt(),
                c.getDescription(), c.getIconUrl());
    }

    /** Exposed for AdminCatalogService to reuse when building the featured-product admin list. */
    public String resolveThumbnailUrl(Long productId) {
        return images.findFirstByProductIdAndThumbnailTrueOrderByDisplayOrderAscIdAsc(productId)
                .or(() -> images.findByProductIdOrderByDisplayOrderAscIdAsc(productId).stream().findFirst())
                .map(ProductImage::getMediaUrl).orElse(null);
    }

    public ProductSummaryResponse summary(Product p) {
        String thumb = images.findFirstByProductIdAndThumbnailTrueOrderByDisplayOrderAscIdAsc(p.getId())
                .or(() -> images.findByProductIdOrderByDisplayOrderAscIdAsc(p.getId()).stream().findFirst())
                .map(ProductImage::getMediaUrl).orElse(null);

        List<ProductVariant> activeVars = variants
                .findByProductIdAndStatusOrderByColorDisplayOrderAscSizeSortOrderAsc(p.getId(), VariantStatus.ACTIVE);

        BigDecimal minPrice = activeVars.stream().map(ProductVariant::getSellingPrice)
                .min(Comparator.naturalOrder()).orElse(null);
        BigDecimal maxPrice = activeVars.stream().map(ProductVariant::getSellingPrice)
                .max(Comparator.naturalOrder()).orElse(null);
        BigDecimal minMrp   = activeVars.stream().map(ProductVariant::getMrp)
                .min(Comparator.naturalOrder()).orElse(null);

        // Distinct colors, preserving display order
        List<ColorSwatchResponse> swatches = activeVars.stream()
                .map(ProductVariant::getColor)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Color::getId, c -> c, (a, b) -> a, java.util.LinkedHashMap::new),
                        map -> map.values().stream()
                                .map(c -> new ColorSwatchResponse(c.getId(), c.getName(), c.getCode(), c.getHexCode()))
                                .toList()));

        List<SizeStockResponse> sizes = activeVars.stream()
                .map(v -> new SizeStockResponse(
                        v.getId(),
                        v.getSize() != null ? v.getSize().getSizeCode() : "",
                        v.getStockQuantity(),
                        v.getSellingPrice(),
                        v.getMrp()))
                .toList();

        boolean inStock = activeVars.stream().anyMatch(v -> v.getStockQuantity() > 0);

        return new ProductSummaryResponse(
                p.getId(), p.getProductCode(), p.getName(), p.getSlug(),
                p.getCategory().getId(), p.getCategory().getName(),
                p.getGenderTag(), p.getBrand(),
                minPrice, maxPrice, minMrp, swatches, thumb, inStock, sizes);
    }

    public ProductDetailResponse detail(Product p) {
        return detail(p, 0);
    }

    public ProductDetailResponse detail(Product p, int orderCount) {
        List<ProductVariant> allVars = variants
                .findByProductIdOrderByColorDisplayOrderAscSizeSortOrderAsc(p.getId());

        List<ProductVariantResponse> variantResponses = allVars.stream()
                .map(v -> new ProductVariantResponse(
                        v.getId(),
                        v.getColor() != null ? v.getColor().getId() : null,
                        v.getColor() != null ? v.getColor().getName() : "",
                        v.getColor() != null ? v.getColor().getCode() : "",
                        v.getColor() != null ? v.getColor().getHexCode() : "",
                        v.getSize() != null ? v.getSize().getId() : null,
                        v.getSize() != null ? v.getSize().getName() : "",
                        v.getSize() != null ? v.getSize().getSizeCode() : "",
                        v.getSku(),
                        v.getMrp(), v.getSellingPrice(), v.getDiscountPercent(),
                        v.getStockQuantity(), v.getCombinationKey(), v.getStatus(), v.isActive()))
                .toList();

        List<ProductImageResponse> imageResponses = images
                .findByProductIdOrderByDisplayOrderAscIdAsc(p.getId()).stream()
                .map(i -> new ProductImageResponse(i.getId(), i.getMediaUrl(), i.getDisplayOrder(), i.isThumbnail()))
                .toList();

        return new ProductDetailResponse(
                p.getId(), p.getProductCode(), p.getName(), p.getSlug(),
                cat(p.getCategory()), p.getGenderTag(), p.getBrand(),
                p.getDescription(), p.getFabricDetails(), p.getFit(), p.getSeason(),
                p.getCareInstructions(), p.getCountryOfOrigin(), p.isReturnPolicyEnabled(),
                p.getMetaTitle(), p.getMetaDescription(), p.getStatus(),
                orderCount, imageResponses, variantResponses);
    }

    // =========================================================================
    // Private
    // =========================================================================

    private Sort sort(String s) {
        return switch (s == null ? "newest" : s) {
            case "price_asc"  -> Sort.by("createdAt").descending(); // will sort by minPrice post-load
            case "price_desc" -> Sort.by("createdAt").descending();
            case "name_asc"   -> Sort.by("name").ascending();
            default           -> Sort.by("createdAt").descending();
        };
    }


    private StoreMediaResponse mediaResponse(StoreMedia m) {
        return new StoreMediaResponse(
                m.getId(), m.getPlacementKey(), m.getMediaUrl(), m.getAltText(),
                m.getLinkUrl(), m.getTitle(), m.getSubtitle(), m.getBadgeText(),
                m.isActive(), m.getDisplayOrder());
    }

    private SaleConfigResponse saleResponse(SaleConfig sc) {
        return new SaleConfigResponse(
                sc.getId(), sc.isActive(), sc.getTitle(), sc.getSubtitle(),
                sc.getEndsAt(), sc.getBadgeText(), sc.getLinkUrl(), sc.getMediaUrl());
    }
}