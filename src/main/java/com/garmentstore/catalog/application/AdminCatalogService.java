package com.garmentstore.catalog.application;

import com.garmentstore.catalog.domain.*;
import com.garmentstore.catalog.dto.CategoryResponse;
import com.garmentstore.catalog.dto.ProductDetailResponse;
import com.garmentstore.catalog.dto.ProductImageResponse;
import com.garmentstore.catalog.dto.admin.*;
import com.garmentstore.catalog.infrastructure.*;
import com.garmentstore.common.exception.BusinessException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class AdminCatalogService {

    private final CategoryRepository categories;
    private final ProductRepository products;
    private final ProductImageRepository images;
    private final ProductVariantRepository variants;
    private final FeaturedProductRepository featured;
    private final RelatedProductMappingRepository related;
    private final ColorRepository colors;
    private final SizeRepository sizes;
    private final SizeGroupRepository sizeGroups;
    private final AttributeRepository attributes;
    private final AttributeValueRepository attributeValues;
    private final CatalogService mapper;

    // =========================================================================
    // Admin product list & detail
    // =========================================================================

    private static final Set<String> ALLOWED_PRODUCT_SORT_PROPERTIES = Set.of(
            "id", "productCode", "name", "slug", "brand", "status", "createdAt", "updatedAt"
    );

    @Transactional(readOnly = true)
    public AdminProductPageResponse getAdminProducts(String status, String categoryParam,
            String gender, String q, int page, int size, String sort, String dir) {

        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String rawSort = (sort == null || sort.isBlank()) ? "createdAt" : sort.trim();

        String normalizedSort = switch (rawSort) {
            case "created_at" -> "createdAt";
            case "updated_at" -> "updatedAt";
            case "product_code" -> "productCode";
            case "selling_price" -> "sellingPrice";
            case "discount_percent" -> "discountPercent";
            case "total_stock" -> "totalStock";
            default -> rawSort;
        };

        String dbSortProperty = ALLOWED_PRODUCT_SORT_PROPERTIES.contains(normalizedSort) ? normalizedSort : "createdAt";
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, dbSortProperty));

        ProductStatus productStatus = null;
        if (status != null && !status.isBlank()) {
            try { productStatus = ProductStatus.valueOf(status.toUpperCase()); } catch (Exception ignored) {}
        }
        GenderTag genderTag = null;
        if (gender != null && !gender.isBlank()) {
            try { genderTag = GenderTag.valueOf(gender.toUpperCase()); } catch (Exception ignored) {}
        }

        ProductStatus finalStatus = productStatus;
        GenderTag finalGender = genderTag;

        Long categoryId = null;
        String categoryText = null;
        if (categoryParam != null && !categoryParam.isBlank()) {
            try { categoryId = Long.parseLong(categoryParam.trim()); }
            catch (NumberFormatException e) { categoryText = categoryParam.trim().toLowerCase(); }
        }
        Long finalCategoryId = categoryId;
        String finalCategoryText = categoryText;

        Page<Product> paged = products.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (finalStatus != null) {
                predicates.add(cb.equal(root.get("status"), finalStatus));
            } else {
                predicates.add(cb.notEqual(root.get("status"), ProductStatus.DELETED));
            }
            if (finalCategoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), finalCategoryId));
            } else if (finalCategoryText != null) {
                predicates.add(cb.or(
                        cb.equal(cb.lower(root.get("category").get("name")), finalCategoryText),
                        cb.equal(cb.lower(root.get("category").get("slug")), finalCategoryText)));
            }
            if (finalGender != null) {
                predicates.add(cb.equal(root.get("genderTag"), finalGender));
            }
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("slug")), pattern)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        List<AdminProductListResponse> list = new ArrayList<>(paged.getContent().stream().map(p -> {
            List<ProductVariant> vars = variants.findByProductIdOrderByColorDisplayOrderAscSizeSortOrderAsc(p.getId());
            int totalStock = vars.stream().mapToInt(ProductVariant::getStockQuantity).sum();

            BigDecimal minMrp = vars.stream()
                    .map(ProductVariant::getMrp)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            BigDecimal minSellingPrice = vars.stream()
                    .map(ProductVariant::getSellingPrice)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            int maxDiscount = vars.stream()
                    .mapToInt(ProductVariant::getDiscountPercent)
                    .max()
                    .orElse(0);

            String thumb = images.findByProductIdOrderByDisplayOrderAscIdAsc(p.getId()).stream()
                    .filter(ProductImage::isThumbnail).map(ProductImage::getMediaUrl).findFirst().orElse(null);

            return new AdminProductListResponse(
                    p.getId(), p.getProductCode(), p.getName(), p.getSlug(),
                    p.getCategory() != null ? p.getCategory().getName() : null,
                    p.getCategory() != null ? p.getCategory().getId() : null,
                    p.getGenderTag(), p.getBrand(),
                    minMrp, minSellingPrice, maxDiscount,
                    p.getStatus(), totalStock, vars.size(), thumb,
                    p.getCreatedAt(), p.getUpdatedAt());
        }).toList());

        if (!ALLOWED_PRODUCT_SORT_PROPERTIES.contains(normalizedSort)) {
            Comparator<AdminProductListResponse> comparator = switch (normalizedSort) {
                case "sellingPrice" -> Comparator.comparing(AdminProductListResponse::sellingPrice, Comparator.nullsFirst(Comparator.naturalOrder()));
                case "discountPercent" -> Comparator.comparingInt(AdminProductListResponse::discountPercent);
                case "totalStock" -> Comparator.comparingInt(AdminProductListResponse::totalStock);
                case "mrp" -> Comparator.comparing(AdminProductListResponse::mrp, Comparator.nullsFirst(Comparator.naturalOrder()));
                default -> null;
            };
            if (comparator != null) {
                if (direction == Sort.Direction.DESC) {
                    comparator = comparator.reversed();
                }
                list.sort(comparator);
            }
        }

        Map<String, Long> statusCounts = Map.of(
                "ALL",      products.countByStatusNot(ProductStatus.DELETED),
                "ACTIVE",   products.countByStatus(ProductStatus.ACTIVE),
                "INACTIVE", products.countByStatus(ProductStatus.INACTIVE),
                "DRAFT",    products.countByStatus(ProductStatus.DRAFT));

        return new AdminProductPageResponse(list, paged.getNumber(), paged.getSize(),
                paged.getTotalElements(), paged.getTotalPages(), paged.isLast(), statusCounts);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getAdminProductDetail(Long id) {
        return mapper.detail(prod(id));
    }

    // =========================================================================
    // Category CRUD
    // =========================================================================

    @Transactional
    public CategoryResponse createCategory(AdminCategoryRequest r) {
        String slug = slug(r.slug() == null || r.slug().isBlank() ? r.name() : r.slug());
        if (categories.existsBySlug(slug))
            throw new BusinessException("CATEGORY_SLUG_EXISTS", "Category slug already exists", HttpStatus.CONFLICT);
        Category parent = r.parentCategoryId() == null ? null :
                categories.findById(r.parentCategoryId()).orElseThrow(() ->
                        new BusinessException("PARENT_CATEGORY_NOT_FOUND", "Parent category not found", HttpStatus.NOT_FOUND));
        Category c = categories.save(Category.builder()
                .name(r.name().trim()).slug(slug).parentCategory(parent)
                .displayOrder(r.displayOrder() == null ? 0 : r.displayOrder())
                .active(r.active() == null || r.active()).build());
        return mapper.cat(c);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, AdminCategoryRequest r) {
        Category c = cat(id);
        c.setName(r.name().trim());
        c.setSlug(slug(r.slug() == null || r.slug().isBlank() ? r.name() : r.slug()));
        c.setParentCategory(r.parentCategoryId() == null ? null :
                categories.findById(r.parentCategoryId()).orElseThrow(() ->
                        new BusinessException("PARENT_CATEGORY_NOT_FOUND", "Parent category not found", HttpStatus.NOT_FOUND)));
        c.setDisplayOrder(r.displayOrder() == null ? 0 : r.displayOrder());
        c.setActive(r.active() == null || r.active());
        return mapper.cat(categories.save(c));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category c = cat(id);
        c.setActive(false);
        categories.save(c);
    }

    // =========================================================================
    // Product CRUD
    // =========================================================================

    @Transactional
    public ProductDetailResponse createProduct(AdminProductRequest r) {
        // Validate SKU uniqueness within the request
        if (r.variants() != null && !r.variants().isEmpty()) {
            Set<String> skusInReq = new HashSet<>();
            for (AdminVariantRequest vr : r.variants()) {
                if (vr.sku() == null || vr.sku().isBlank()) continue;
                String sku = vr.sku().trim().toUpperCase();
                if (!skusInReq.add(sku))
                    throw new BusinessException("DUPLICATE_SKU", "Duplicate SKU '" + sku + "' in request", HttpStatus.CONFLICT);
                if (variants.existsBySku(sku))
                    throw new BusinessException("DUPLICATE_SKU", "SKU '" + sku + "' already exists", HttpStatus.CONFLICT);
            }
        }

        String baseSlug = slug(r.slug() == null || r.slug().isBlank() ? r.name() : r.slug());
        String finalSlug = baseSlug;
        if (r.slug() != null && !r.slug().isBlank()) {
            if (products.existsBySlug(finalSlug))
                throw new BusinessException("PRODUCT_SLUG_EXISTS",
                        "Product slug '" + finalSlug + "' already exists", HttpStatus.CONFLICT);
        } else {
            int counter = 1;
            while (products.existsBySlug(finalSlug)) finalSlug = baseSlug + "-" + counter++;
        }

        Product p = fill(Product.builder().build(), r);
        p.setSlug(finalSlug);
        p.setProductCode(generateProductCode(p.getName()));
        p = products.save(p);

        if (r.images() != null) {
            for (AdminImageRequest ir : r.images()) {
                if (ir.mediaUrl() != null && !ir.mediaUrl().isBlank()) {
                    images.save(ProductImage.builder().product(p)
                            .mediaUrl(ir.mediaUrl().trim())
                            .displayOrder(ir.displayOrder() == null ? 0 : ir.displayOrder())
                            .thumbnail(Boolean.TRUE.equals(ir.thumbnail())).build());
                }
            }
        }

        if (r.variants() != null) {
            for (AdminVariantRequest vr : r.variants()) {
                saveVariant(p, vr);
            }
        }

        return mapper.detail(p);
    }

    @Transactional
    public ProductDetailResponse updateProduct(Long id, AdminProductRequest r) {
        Product p = prod(id);
        p = fill(p, r);
        if (r.slug() != null && !r.slug().isBlank()) {
            String newSlug = slug(r.slug());
            if (products.existsBySlugAndIdNot(newSlug, id))
                throw new BusinessException("PRODUCT_SLUG_EXISTS",
                        "Product slug '" + newSlug + "' already exists", HttpStatus.CONFLICT);
            p.setSlug(newSlug);
        }
        return mapper.detail(products.save(p));
    }

    @Transactional
    public ProductDetailResponse updateStatus(Long id, AdminProductStatusRequest r) {
        Product p = prod(id);
        p.setStatus(r.status());
        if (r.status() == ProductStatus.DELETED) p.setDeletedAt(Instant.now());
        return mapper.detail(products.save(p));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product p = prod(id);
        p.setStatus(ProductStatus.DELETED);
        p.setDeletedAt(Instant.now());
        products.save(p);
    }

    // =========================================================================
    // Variant CRUD
    // =========================================================================

    @Transactional
    public AdminVariantResponse addVariant(Long productId, AdminVariantRequest r) {
        Product p = prod(productId);
        ProductVariant v = saveVariant(p, r);
        return toVariantResponse(v);
    }

    @Transactional
    public AdminVariantResponse updateVariant(Long id, AdminVariantRequest r) {
        ProductVariant v = variants.findById(id).orElseThrow(() ->
                new BusinessException("VARIANT_NOT_FOUND", "Variant not found", HttpStatus.NOT_FOUND));

        Color color = color(r.colorId());
        Size size   = size(r.sizeId());
        String sku  = r.sku().trim().toUpperCase();
        String combinationKey = ProductVariant.buildCombinationKey(color.getId(), size.getId());

        if (variants.existsBySkuAndIdNot(sku, id))
            throw new BusinessException("DUPLICATE_SKU", "SKU '" + sku + "' already exists", HttpStatus.CONFLICT);
        if (variants.existsByProductIdAndCombinationKeyAndIdNot(v.getProduct().getId(), combinationKey, id))
            throw new BusinessException("DUPLICATE_COMBINATION",
                    "A variant with this color+size combination already exists", HttpStatus.CONFLICT);

        VariantStatus status = r.status();
        if (status == null && r.isActive() != null) {
            status = r.isActive() ? VariantStatus.ACTIVE : VariantStatus.INACTIVE;
        }
        if (status == null) {
            status = v.getStatus() != null ? v.getStatus() : VariantStatus.ACTIVE;
        }

        v.setColor(color);
        v.setSize(size);
        v.setSku(sku);
        v.setBarcode(r.barcode());
        v.setMrp(r.mrp());
        v.setSellingPrice(r.sellingPrice());
        v.setCostPrice(r.costPrice());
        v.setStockQuantity(r.stockQuantity() == null ? v.getStockQuantity() : r.stockQuantity());
        v.setWeightGrams(r.weightGrams());
        v.setCombinationKey(combinationKey);
        v.setStatus(status);
        return toVariantResponse(variants.save(v));
    }

    @Transactional
    public void deleteVariant(Long id) {
        variants.deleteById(id);
    }

    /**
     * Auto-generate all colorIds × sizeIds combinations for a product.
     * Combinations that already exist are silently skipped.
     * Returns the list of newly created variants.
     */
    @Transactional
    public List<AdminVariantResponse> generateVariants(Long productId, AdminVariantGenerateRequest r) {
        Product p = prod(productId);
        List<AdminVariantResponse> created = new ArrayList<>();

        for (Long colorId : r.colorIds()) {
            Color color = color(colorId);
            for (Long sizeId : r.sizeIds()) {
                Size size = size(sizeId);
                String combinationKey = ProductVariant.buildCombinationKey(colorId, sizeId);

                // Skip if this combination already exists
                if (variants.existsByProductIdAndCombinationKey(productId, combinationKey)) continue;

                String sku = autoSku(p, color, size);
                int stock = r.stockQuantity() == null ? 0 : r.stockQuantity();

                ProductVariant v = ProductVariant.builder()
                        .product(p)
                        .color(color)
                        .size(size)
                        .sku(sku)
                        .mrp(r.mrp())
                        .sellingPrice(r.sellingPrice())
                        .stockQuantity(stock)
                        .combinationKey(combinationKey)
                        .status(VariantStatus.ACTIVE)
                        .build();
                created.add(toVariantResponse(variants.save(v)));
            }
        }
        return created;
    }

    // =========================================================================
    // Image CRUD
    // =========================================================================

    @Transactional
    public ProductImageResponse addImage(Long pid, AdminImageRequest r) {
        Product p = prod(pid);
        ProductImage i = images.save(ProductImage.builder().product(p)
                .mediaUrl(r.mediaUrl().trim())
                .displayOrder(r.displayOrder() == null ? 0 : r.displayOrder())
                .thumbnail(Boolean.TRUE.equals(r.thumbnail())).build());
        return new ProductImageResponse(i.getId(), i.getMediaUrl(), i.getDisplayOrder(), i.isThumbnail());
    }

    @Transactional
    public ProductImageResponse updateImage(Long id, AdminImageRequest r) {
        ProductImage i = images.findById(id).orElseThrow(() ->
                new BusinessException("IMAGE_NOT_FOUND", "Image not found", HttpStatus.NOT_FOUND));
        i.setMediaUrl(r.mediaUrl().trim());
        i.setDisplayOrder(r.displayOrder() == null ? 0 : r.displayOrder());
        i.setThumbnail(Boolean.TRUE.equals(r.thumbnail()));
        i = images.save(i);
        return new ProductImageResponse(i.getId(), i.getMediaUrl(), i.getDisplayOrder(), i.isThumbnail());
    }

    @Transactional
    public void deleteImage(Long id) {
        images.deleteById(id);
    }

    // =========================================================================
    // Featured / Related
    // =========================================================================

    @Transactional
    public Long addFeatured(AdminFeaturedRequest r) {
        FeaturedProduct f = featured.save(FeaturedProduct.builder()
                .product(prod(r.productId()))
                .displayOrder(r.displayOrder() == null ? 0 : r.displayOrder())
                .active(r.active() == null || r.active()).build());
        return f.getId();
    }

    @Transactional
    public void deleteFeatured(Long id) { featured.deleteById(id); }

    @Transactional
    public Long addRelated(Long productId, AdminRelatedProductRequest r) {
        Product p  = prod(productId);
        Product rp = prod(r.relatedProductId());
        if (p.getId().equals(rp.getId()))
            throw new BusinessException("INVALID_RELATED_PRODUCT",
                    "Product cannot be related to itself", HttpStatus.BAD_REQUEST);
        return related.save(RelatedProductMapping.builder().product(p).relatedProduct(rp).build()).getId();
    }

    @Transactional
    public void deleteRelated(Long id) { related.deleteById(id); }

    // =========================================================================
    // Color CRUD
    // =========================================================================

    @Transactional(readOnly = true)
    public List<AdminColorResponse> listColors() {
        return colors.findByActiveTrueOrderByDisplayOrderAscNameAsc().stream()
                .map(this::toColorResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AdminColorResponse> listAllColors() {
        return colors.findAll(Sort.by("displayOrder", "name")).stream()
                .map(this::toColorResponse).toList();
    }

    @Transactional
    public AdminColorResponse createColor(AdminColorRequest r) {
        String code = r.code().trim().toUpperCase();
        if (colors.existsByCode(code))
            throw new BusinessException("COLOR_CODE_EXISTS", "Color code '" + code + "' already exists", HttpStatus.CONFLICT);
        Color c = Color.builder().name(r.name().trim()).code(code)
                .hexCode(r.hexCode()).displayOrder(r.displayOrder() == null ? 0 : r.displayOrder())
                .active(r.active() == null || r.active()).build();
        return toColorResponse(colors.save(c));
    }

    @Transactional
    public AdminColorResponse updateColor(Long id, AdminColorRequest r) {
        Color c = color(id);
        String code = r.code().trim().toUpperCase();
        if (colors.existsByCodeAndIdNot(code, id))
            throw new BusinessException("COLOR_CODE_EXISTS", "Color code '" + code + "' already exists", HttpStatus.CONFLICT);
        c.setName(r.name().trim());
        c.setCode(code);
        c.setHexCode(r.hexCode());
        c.setDisplayOrder(r.displayOrder() == null ? c.getDisplayOrder() : r.displayOrder());
        c.setActive(r.active() == null || r.active());
        return toColorResponse(colors.save(c));
    }

    @Transactional
    public void deleteColor(Long id) {
        Color c = color(id);
        c.setActive(false);
        colors.save(c);
    }

    // =========================================================================
    // Size Group CRUD
    // =========================================================================

    @Transactional(readOnly = true)
    public List<AdminSizeResponse> listSizes(Long sizeGroupId) {
        List<Size> sizeList = sizeGroupId != null
                ? sizes.findBySizeGroupIdAndActiveTrueOrderBySortOrderAsc(sizeGroupId)
                : sizes.findByActiveTrueOrderBySizeGroupIdAscSortOrderAsc();
        return sizeList.stream().map(this::toSizeResponse).toList();
    }

    @Transactional
    public AdminSizeResponse createSize(AdminSizeRequest r) {
        SizeGroup sg = sizeGroup(r.sizeGroupId());
        if (sizes.existsBySizeGroupIdAndSizeCode(r.sizeGroupId(), r.sizeCode().trim().toUpperCase()))
            throw new BusinessException("SIZE_CODE_EXISTS",
                    "Size code '" + r.sizeCode() + "' already exists in this group", HttpStatus.CONFLICT);
        Size s = Size.builder().sizeGroup(sg).name(r.name().trim())
                .sizeCode(r.sizeCode().trim().toUpperCase())
                .sortOrder(r.sortOrder() == null ? 0 : r.sortOrder())
                .active(r.active() == null || r.active()).build();
        return toSizeResponse(sizes.save(s));
    }

    @Transactional
    public AdminSizeResponse updateSize(Long id, AdminSizeRequest r) {
        Size s = size(id);
        s.setSizeGroup(sizeGroup(r.sizeGroupId()));
        s.setName(r.name().trim());
        s.setSizeCode(r.sizeCode().trim().toUpperCase());
        s.setSortOrder(r.sortOrder() == null ? s.getSortOrder() : r.sortOrder());
        s.setActive(r.active() == null || r.active());
        return toSizeResponse(sizes.save(s));
    }

    @Transactional
    public void deleteSize(Long id) {
        Size s = size(id);
        s.setActive(false);
        sizes.save(s);
    }

    // =========================================================================
    // Attribute CRUD
    // =========================================================================

    @Transactional(readOnly = true)
    public List<Attribute> listAttributes() {
        return attributes.findByActiveTrueOrderByNameAsc();
    }

    @Transactional
    public Attribute createAttribute(AdminAttributeRequest r) {
        if (attributes.existsByName(r.name().trim()))
            throw new BusinessException("ATTRIBUTE_EXISTS", "Attribute '" + r.name() + "' already exists", HttpStatus.CONFLICT);
        return attributes.save(Attribute.builder().name(r.name().trim())
                .scope(r.scope() == null ? Attribute.Scope.PRODUCT : r.scope())
                .active(r.active() == null || r.active()).build());
    }

    @Transactional
    public Attribute updateAttribute(Long id, AdminAttributeRequest r) {
        Attribute a = attribute(id);
        if (attributes.existsByNameAndIdNot(r.name().trim(), id))
            throw new BusinessException("ATTRIBUTE_EXISTS", "Attribute '" + r.name() + "' already exists", HttpStatus.CONFLICT);
        a.setName(r.name().trim());
        a.setScope(r.scope() == null ? a.getScope() : r.scope());
        a.setActive(r.active() == null || r.active());
        return attributes.save(a);
    }

    @Transactional
    public AttributeValue createAttributeValue(AdminAttributeValueRequest r) {
        Attribute attr = attribute(r.attributeId());
        if (attributeValues.existsByAttributeIdAndValue(r.attributeId(), r.value().trim()))
            throw new BusinessException("ATTR_VALUE_EXISTS",
                    "Value '" + r.value() + "' already exists for attribute", HttpStatus.CONFLICT);
        return attributeValues.save(AttributeValue.builder().attribute(attr).value(r.value().trim())
                .displayOrder(r.displayOrder() == null ? 0 : r.displayOrder()).build());
    }

    @Transactional
    public List<AttributeValue> listAttributeValues(Long attributeId) {
        return attributeValues.findByAttributeIdOrderByDisplayOrderAsc(attributeId);
    }

    @Transactional
    public void deleteAttributeValue(Long id) {
        attributeValues.deleteById(id);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private Product fill(Product p, AdminProductRequest r) {
        p.setName(r.name().trim());
        p.setCategory(cat(r.categoryId()));
        p.setGenderTag(r.genderTag());
        p.setBrand(r.brand());
        p.setDescription(r.description());
        p.setFabricDetails(r.fabricDetails());
        p.setFit(r.fit());
        p.setSeason(r.season());
        p.setCareInstructions(r.careInstructions());
        p.setCountryOfOrigin(r.countryOfOrigin());
        p.setReturnPolicyEnabled(r.returnPolicyEnabled() == null || r.returnPolicyEnabled());
        p.setMetaTitle(r.metaTitle());
        p.setMetaDescription(r.metaDescription());
        p.setStatus(r.status() == null ? ProductStatus.DRAFT : r.status());
        return p;
    }

    private ProductVariant saveVariant(Product p, AdminVariantRequest vr) {
        Color color = color(vr.colorId());
        Size  size  = size(vr.sizeId());
        String sku  = vr.sku().trim().toUpperCase();
        String combinationKey = ProductVariant.buildCombinationKey(color.getId(), size.getId());

        if (variants.existsBySku(sku))
            throw new BusinessException("DUPLICATE_SKU", "SKU '" + sku + "' already exists", HttpStatus.CONFLICT);
        if (variants.existsByProductIdAndCombinationKey(p.getId(), combinationKey))
            throw new BusinessException("DUPLICATE_COMBINATION",
                    "A variant with color '" + color.getName() + "' + size '" + size.getSizeCode() + "' already exists for this product",
                    HttpStatus.CONFLICT);

        VariantStatus status = vr.status();
        if (status == null && vr.isActive() != null) {
            status = vr.isActive() ? VariantStatus.ACTIVE : VariantStatus.INACTIVE;
        }
        if (status == null) {
            status = VariantStatus.ACTIVE;
        }

        return variants.save(ProductVariant.builder()
                .product(p).color(color).size(size).sku(sku)
                .barcode(vr.barcode())
                .mrp(vr.mrp()).sellingPrice(vr.sellingPrice()).costPrice(vr.costPrice())
                .stockQuantity(vr.stockQuantity() == null ? 0 : vr.stockQuantity())
                .weightGrams(vr.weightGrams())
                .combinationKey(combinationKey)
                .status(status)
                .build());
    }

    /** Auto-generates a SKU like SHRT001-BLK-M from product code + color code + size code */
    private String autoSku(Product p, Color color, Size size) {
        String base = (p.getProductCode() != null ? p.getProductCode() : "PROD")
                + "-" + color.getCode()
                + "-" + size.getSizeCode();
        if (!variants.existsBySku(base)) return base;
        // Handle rare collision by appending a counter
        for (int i = 2; i < 1000; i++) {
            String candidate = base + "-" + i;
            if (!variants.existsBySku(candidate)) return candidate;
        }
        throw new BusinessException("SKU_GENERATION_FAILED", "Could not auto-generate unique SKU", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Generates a short immutable product code like SHRT001.
     * Prefix is derived from the first 4 letters of the product name.
     */
    private String generateProductCode(String name) {
        String prefix = name.replaceAll("[^A-Za-z]", "").toUpperCase();
        prefix = prefix.length() >= 4 ? prefix.substring(0, 4) : prefix;
        AtomicInteger counter = new AtomicInteger(1);
        String code;
        do {
            code = String.format("%s%03d", prefix, counter.getAndIncrement());
        } while (products.existsByProductCode(code) && counter.get() < 10000);
        return code;
    }


    private AdminVariantResponse toVariantResponse(ProductVariant v) {
        return new AdminVariantResponse(
                v.getId(),
                v.getColor().getId(), v.getColor().getName(), v.getColor().getCode(), v.getColor().getHexCode(),
                v.getSize().getId(), v.getSize().getName(), v.getSize().getSizeCode(),
                v.getSku(), v.getBarcode(),
                v.getMrp(), v.getSellingPrice(), v.getCostPrice(), v.getDiscountPercent(),
                v.getStockQuantity(), v.getWeightGrams(), v.getCombinationKey(), v.getStatus(), v.isActive());
    }

    private AdminColorResponse toColorResponse(Color c) {
        return new AdminColorResponse(c.getId(), c.getName(), c.getCode(), c.getHexCode(), c.getDisplayOrder(), c.isActive());
    }

    private AdminSizeResponse toSizeResponse(Size s) {
        return new AdminSizeResponse(s.getId(),
                s.getSizeGroup().getId(), s.getSizeGroup().getName(),
                s.getName(), s.getSizeCode(), s.getSortOrder(), s.isActive());
    }

    private Category cat(Long id) {
        return categories.findById(id).orElseThrow(() ->
                new BusinessException("CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND));
    }

    private Product prod(Long id) {
        return products.findById(id).orElseThrow(() ->
                new BusinessException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));
    }

    private Color color(Long id) {
        return colors.findById(id).orElseThrow(() ->
                new BusinessException("COLOR_NOT_FOUND", "Color not found", HttpStatus.NOT_FOUND));
    }

    private Size size(Long id) {
        return sizes.findById(id).orElseThrow(() ->
                new BusinessException("SIZE_NOT_FOUND", "Size not found", HttpStatus.NOT_FOUND));
    }

    private SizeGroup sizeGroup(Long id) {
        return sizeGroups.findById(id).orElseThrow(() ->
                new BusinessException("SIZE_GROUP_NOT_FOUND", "Size group not found", HttpStatus.NOT_FOUND));
    }

    private Attribute attribute(Long id) {
        return attributes.findById(id).orElseThrow(() ->
                new BusinessException("ATTRIBUTE_NOT_FOUND", "Attribute not found", HttpStatus.NOT_FOUND));
    }

    private String slug(String s) {
        return s.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}