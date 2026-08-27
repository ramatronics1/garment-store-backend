package com.garmentstore.catalog.application;

import com.garmentstore.catalog.domain.*;
import com.garmentstore.catalog.dto.CategoryResponse;
import com.garmentstore.catalog.dto.ProductDetailResponse;
import com.garmentstore.catalog.dto.ProductImageResponse;
import com.garmentstore.catalog.dto.ProductVariantResponse;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCatalogService {
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final ProductImageRepository images;
    private final ProductVariantRepository variants;
    private final FeaturedProductRepository featured;
    private final RelatedProductMappingRepository related;
    private final CatalogService mapper;

    @Transactional(readOnly = true)
    public AdminProductPageResponse getAdminProducts(String status, String categoryParam, String gender, String q, int page, int size, String sort, String dir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortProperty = (sort == null || sort.isBlank()) ? "createdAt" : sort;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));

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
            try {
                categoryId = Long.parseLong(categoryParam.trim());
            } catch (NumberFormatException e) {
                categoryText = categoryParam.trim().toLowerCase();
            }
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
                        cb.equal(cb.lower(root.get("category").get("slug")), finalCategoryText)
                ));
            }
            if (finalGender != null) {
                predicates.add(cb.equal(root.get("genderTag"), finalGender));
            }
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("slug")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        List<AdminProductListResponse> list = paged.getContent().stream().map(p -> {
            List<ProductVariant> vars = variants.findByProductIdAndActiveTrueOrderBySizeCodeAsc(p.getId());
            int totalStock = vars.stream().mapToInt(ProductVariant::getStockQuantity).sum();
            String thumb = images.findByProductIdOrderByDisplayOrderAscIdAsc(p.getId()).stream()
                    .filter(ProductImage::isThumbnail).map(ProductImage::getMediaUrl).findFirst().orElse(null);
            return new AdminProductListResponse(
                    p.getId(), p.getName(), p.getSlug(),
                    p.getCategory() != null ? p.getCategory().getName() : null,
                    p.getCategory() != null ? p.getCategory().getId() : null,
                    p.getGenderTag(), p.getMrp(), p.getSellingPrice(), p.getDiscountPercent(),
                    p.getStatus(), totalStock, vars.size(), thumb, p.getColor(),
                    p.getCreatedAt(), p.getUpdatedAt()
            );
        }).toList();

        java.util.Map<String, Long> statusCounts = java.util.Map.of(
                "ALL", products.countByStatusNot(ProductStatus.DELETED),
                "ACTIVE", products.countByStatus(ProductStatus.ACTIVE),
                "INACTIVE", products.countByStatus(ProductStatus.INACTIVE),
                "DRAFT", products.countByStatus(ProductStatus.DRAFT)
        );

        return new AdminProductPageResponse(list, paged.getNumber(), paged.getSize(), paged.getTotalElements(), paged.getTotalPages(), paged.isLast(), statusCounts);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getAdminProductDetail(Long id) {
        Product p = prod(id);
        return mapper.detail(p);
    }

    @Transactional
    public CategoryResponse createCategory(AdminCategoryRequest r) {
        String slug = slug(r.slug() == null || r.slug().isBlank() ? r.name() : r.slug());
        if (categories.existsBySlug(slug))
            throw new BusinessException("CATEGORY_SLUG_EXISTS", "Category slug already exists", HttpStatus.CONFLICT);
        Category p = r.parentCategoryId() == null ? null : categories.findById(r.parentCategoryId()).orElseThrow(() -> new BusinessException("PARENT_CATEGORY_NOT_FOUND", "Parent category not found", HttpStatus.NOT_FOUND));
        Category c = categories.save(Category.builder().name(r.name().trim()).slug(slug).parentCategory(p).displayOrder(r.displayOrder() == null ? 0 : r.displayOrder()).active(r.active() == null || r.active()).build());
        return mapper.cat(c);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, AdminCategoryRequest r) {
        Category c = cat(id);
        c.setName(r.name().trim());
        c.setSlug(slug(r.slug() == null || r.slug().isBlank() ? r.name() : r.slug()));
        c.setParentCategory(r.parentCategoryId() == null ? null : categories.findById(r.parentCategoryId()).orElseThrow(() -> new BusinessException("PARENT_CATEGORY_NOT_FOUND", "Parent category not found", HttpStatus.NOT_FOUND)));
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

    @Transactional
    public ProductDetailResponse createProduct(AdminProductRequest r) {
        // 1. Validate variant SKU uniqueness within request and against database
        if (r.variants() != null && !r.variants().isEmpty()) {
            java.util.Set<String> skusInReq = new java.util.HashSet<>();
            for (AdminVariantRequest vr : r.variants()) {
                if (vr.skuCode() == null || vr.skuCode().isBlank()) continue;
                String sku = vr.skuCode().trim();
                if (!skusInReq.add(sku.toUpperCase())) {
                    throw new BusinessException("DUPLICATE_SKU_CODE", "Duplicate SKU code '" + sku + "' in request. Each variant must have a unique SKU code.", HttpStatus.CONFLICT);
                }
                if (variants.existsBySkuCode(sku)) {
                    throw new BusinessException("DUPLICATE_SKU_CODE", "SKU code '" + sku + "' already exists. Please use a unique SKU code.", HttpStatus.CONFLICT);
                }
            }
        }

        String baseSlug = slug(r.slug() == null || r.slug().isBlank() ? r.name() : r.slug());
        String finalSlug = baseSlug;
        if (r.slug() != null && !r.slug().isBlank()) {
            if (products.existsBySlug(finalSlug)) {
                throw new BusinessException("PRODUCT_SLUG_EXISTS", "Product slug '" + finalSlug + "' already exists. Please choose a different slug.", HttpStatus.CONFLICT);
            }
        } else {
            // Auto-generate unique slug if same product name already exists
            int counter = 1;
            while (products.existsBySlug(finalSlug)) {
                finalSlug = baseSlug + "-" + counter++;
            }
        }
        Product p = fill(Product.builder().build(), r);
        p.setSlug(finalSlug);
        p = products.save(p);

        // 2. Persist images if provided
        if (r.images() != null && !r.images().isEmpty()) {
            for (AdminImageRequest ir : r.images()) {
                if (ir.mediaUrl() != null && !ir.mediaUrl().isBlank()) {
                    images.save(ProductImage.builder()
                            .product(p)
                            .mediaUrl(ir.mediaUrl().trim())
                            .displayOrder(ir.displayOrder() == null ? 0 : ir.displayOrder())
                            .thumbnail(Boolean.TRUE.equals(ir.thumbnail()))
                            .build());
                }
            }
        }

        // 3. Persist variants if provided
        if (r.variants() != null && !r.variants().isEmpty()) {
            for (AdminVariantRequest vr : r.variants()) {
                if (vr.sizeCode() == null || vr.sizeCode().isBlank() || vr.skuCode() == null || vr.skuCode().isBlank()) {
                    continue;
                }
                String sku = vr.skuCode().trim();
                int stock = vr.stockQuantity() == null ? 0 : vr.stockQuantity();
                variants.save(ProductVariant.builder()
                        .product(p)
                        .sizeCode(vr.sizeCode().trim())
                        .skuCode(sku)
                        .active(vr.active() == null || vr.active())
                        .stockQuantity(stock)
                        .build());
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
            if (products.existsBySlugAndIdNot(newSlug, id)) {
                throw new BusinessException("PRODUCT_SLUG_EXISTS", "Product slug '" + newSlug + "' already exists. Please choose a different slug.", HttpStatus.CONFLICT);
            }
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

    @Transactional
    public ProductVariantResponse addVariant(Long productId, AdminVariantRequest r) {
        Product p = prod(productId);
        String sku = r.skuCode().trim();
        if (variants.existsBySkuCode(sku)) {
            throw new BusinessException("DUPLICATE_SKU_CODE", "SKU code '" + sku + "' already exists. Please use a unique SKU code.", HttpStatus.CONFLICT);
        }
        int stock = r.stockQuantity() == null ? 0 : r.stockQuantity();
        ProductVariant v = variants.save(ProductVariant.builder()
                .product(p)
                .sizeCode(r.sizeCode().trim())
                .skuCode(sku)
                .active(r.active() == null || r.active())
                .stockQuantity(stock)
                .build());
        return new ProductVariantResponse(v.getId(), v.getSizeCode(), v.getSkuCode(), v.isActive(), v.getStockQuantity());
    }

    @Transactional
    public ProductVariantResponse updateVariant(Long id, AdminVariantRequest r) {
        ProductVariant v = variants.findById(id).orElseThrow(() -> new BusinessException("VARIANT_NOT_FOUND", "Variant not found", HttpStatus.NOT_FOUND));
        String sku = r.skuCode().trim();
        if (variants.existsBySkuCodeAndIdNot(sku, id)) {
            throw new BusinessException("DUPLICATE_SKU_CODE", "SKU code '" + sku + "' already exists. Please use a unique SKU code.", HttpStatus.CONFLICT);
        }
        v.setSizeCode(r.sizeCode().trim());
        v.setSkuCode(sku);
        v.setActive(r.active() == null || r.active());
        if (r.stockQuantity() != null) v.setStockQuantity(r.stockQuantity());
        v = variants.save(v);
        return new ProductVariantResponse(v.getId(), v.getSizeCode(), v.getSkuCode(), v.isActive(), v.getStockQuantity());
    }

    @Transactional
    public void deleteVariant(Long id) {
        variants.deleteById(id);
    }

    @Transactional
    public ProductImageResponse addImage(Long pid, AdminImageRequest r) {
        Product p = prod(pid);
        ProductImage i = images.save(ProductImage.builder().product(p).mediaUrl(r.mediaUrl().trim()).displayOrder(r.displayOrder() == null ? 0 : r.displayOrder()).thumbnail(Boolean.TRUE.equals(r.thumbnail())).build());
        return new ProductImageResponse(i.getId(), i.getMediaUrl(), i.getDisplayOrder(), i.isThumbnail());
    }

    @Transactional
    public ProductImageResponse updateImage(Long id, AdminImageRequest r) {
        ProductImage i = images.findById(id).orElseThrow(() -> new BusinessException("IMAGE_NOT_FOUND", "Image not found", HttpStatus.NOT_FOUND));
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

    @Transactional
    public Long addFeatured(AdminFeaturedRequest r) {
        FeaturedProduct f = featured.save(FeaturedProduct.builder().product(prod(r.productId())).displayOrder(r.displayOrder() == null ? 0 : r.displayOrder()).active(r.active() == null || r.active()).build());
        return f.getId();
    }

    @Transactional
    public void deleteFeatured(Long id) {
        featured.deleteById(id);
    }

    @Transactional
    public Long addRelated(Long productId, AdminRelatedProductRequest r) {
        Product p = prod(productId);
        Product rp = prod(r.relatedProductId());
        if (p.getId().equals(rp.getId()))
            throw new BusinessException("INVALID_RELATED_PRODUCT", "Product cannot be related to itself", HttpStatus.BAD_REQUEST);
        return related.save(RelatedProductMapping.builder().product(p).relatedProduct(rp).build()).getId();
    }

    @Transactional
    public void deleteRelated(Long id) {
        related.deleteById(id);
    }

    private Product fill(Product p, AdminProductRequest r) {
        p.setName(r.name().trim());
        p.setCategory(cat(r.categoryId()));
        p.setGenderTag(r.genderTag());
        p.setMrp(r.mrp());
        p.setSellingPrice(r.sellingPrice());
        p.setDiscountPercent(r.discountPercent() == null ? 0 : r.discountPercent());
        p.setColor(r.color());
        p.setDescription(r.description());
        p.setFabricDetails(r.fabricDetails());
        p.setCareInstructions(r.careInstructions());
        p.setCountryOfOrigin(r.countryOfOrigin());
        p.setReturnPolicyEnabled(r.returnPolicyEnabled() == null || r.returnPolicyEnabled());
        p.setMetaTitle(r.metaTitle());
        p.setMetaDescription(r.metaDescription());
        p.setStatus(r.status() == null ? ProductStatus.DRAFT : r.status());
        return p;
    }

    private Category cat(Long id) {
        return categories.findById(id).orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND));
    }

    private Product prod(Long id) {
        return products.findById(id).orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));
    }

    private String slug(String s) {
        return s.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}