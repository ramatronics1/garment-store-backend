package com.garmentstore.catalog.application;

import com.garmentstore.catalog.domain.*;
import com.garmentstore.catalog.dto.CategoryResponse;
import com.garmentstore.catalog.dto.ProductDetailResponse;
import com.garmentstore.catalog.dto.ProductImageResponse;
import com.garmentstore.catalog.dto.ProductVariantResponse;
import com.garmentstore.catalog.dto.admin.*;
import com.garmentstore.catalog.infrastructure.*;
import com.garmentstore.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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
        String slug = slug(r.slug() == null || r.slug().isBlank() ? r.name() : r.slug());
        if (products.existsBySlug(slug))
            throw new BusinessException("PRODUCT_SLUG_EXISTS", "Product slug already exists", HttpStatus.CONFLICT);
        Product p = fill(Product.builder().build(), r);
        p.setSlug(slug);
        return mapper.detail(products.save(p));
    }

    @Transactional
    public ProductDetailResponse updateProduct(Long id, AdminProductRequest r) {
        Product p = prod(id);
        p = fill(p, r);
        if (r.slug() != null && !r.slug().isBlank()) p.setSlug(slug(r.slug()));
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
        int stock = r.stockQuantity() == null ? 0 : r.stockQuantity();
        ProductVariant v = variants.save(ProductVariant.builder()
                .product(p)
                .sizeCode(r.sizeCode().trim())
                .skuCode(r.skuCode().trim())
                .active(r.active() == null || r.active())
                .stockQuantity(stock)
                .build());
        return new ProductVariantResponse(v.getId(), v.getSizeCode(), v.getSkuCode(), v.isActive(), v.getStockQuantity());
    }

    @Transactional
    public ProductVariantResponse updateVariant(Long id, AdminVariantRequest r) {
        ProductVariant v = variants.findById(id).orElseThrow(() -> new BusinessException("VARIANT_NOT_FOUND", "Variant not found", HttpStatus.NOT_FOUND));
        v.setSizeCode(r.sizeCode().trim());
        v.setSkuCode(r.skuCode().trim());
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