package com.garmentstore.catalog.application;

import com.garmentstore.catalog.domain.*;
import com.garmentstore.catalog.dto.*;
import com.garmentstore.catalog.infrastructure.*;
import com.garmentstore.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final ProductImageRepository images;
    private final ProductVariantRepository variants;
    private final FeaturedProductRepository featured;
    private final RelatedProductMappingRepository related;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categories.findByActiveTrueOrderByDisplayOrderAscNameAsc().stream().map(this::cat).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        return categories.findById(id).filter(Category::isActive).map(this::cat).orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> getProducts(Long cid, String q, int page, int size, String sort) {
        Page<Product> r = products.searchPublic(ProductStatus.ACTIVE, cid, q == null || q.isBlank() ? null : q.trim(), PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), sort(sort)));
        return new PageResponse<>(r.getContent().stream().map(this::summary).toList(), r.getNumber(), r.getSize(), r.getTotalElements(), r.getTotalPages(), r.isLast());
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> getCategoryProducts(Long id, int p, int s) {
        getCategory(id);
        return getProducts(id, null, p, s, "newest");
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProduct(Long id) {
        return detail(products.findByIdAndStatus(id, ProductStatus.ACTIVE).orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        return detail(products.findBySlugAndStatus(slug, ProductStatus.ACTIVE).orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getFeaturedProducts() {
        return featured.findByActiveTrueOrderByDisplayOrderAscIdAsc().stream().map(FeaturedProduct::getProduct).filter(p -> p.getStatus() == ProductStatus.ACTIVE).map(this::summary).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getRelatedProducts(Long id) {
        getProduct(id);
        return related.findByProductIdOrderByIdAsc(id).stream().map(RelatedProductMapping::getRelatedProduct).filter(p -> p.getStatus() == ProductStatus.ACTIVE).map(this::summary).toList();
    }

    @Transactional(readOnly = true)
    public SizeGuideResponse getSizeGuide(Long id) {
        getProduct(id);
        var s = variants.findByProductIdAndActiveTrueOrderBySizeCodeAsc(id).stream().map(ProductVariant::getSizeCode).distinct().toList();
        return new SizeGuideResponse(id, s, "Detailed size chart can be managed in Admin Product Module.");
    }

    @Transactional(readOnly = true)
    public ProductAvailabilityResponse getAvailability(Long id) {
        getProduct(id);
        var s = variants.findByProductIdAndActiveTrueOrderBySizeCodeAsc(id).stream().map(ProductVariant::getSizeCode).distinct().toList();
        return new ProductAvailabilityResponse(id, !s.isEmpty(), s, "VARIANT_ACTIVE_STATUS_ONLY_UNTIL_INVENTORY_MODULE");
    }

    public CategoryResponse cat(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getParentCategory() == null ? null : c.getParentCategory().getId(), c.getDisplayOrder(), c.isActive());
    }

    public ProductSummaryResponse summary(Product p) {
        String th = images.findFirstByProductIdAndThumbnailTrueOrderByDisplayOrderAscIdAsc(p.getId()).or(() -> images.findByProductIdOrderByDisplayOrderAscIdAsc(p.getId()).stream().findFirst()).map(ProductImage::getMediaUrl).orElse(null);
        return new ProductSummaryResponse(p.getId(), p.getName(), p.getSlug(), p.getCategory().getId(), p.getCategory().getName(), p.getGenderTag(), p.getMrp(), p.getSellingPrice(), p.getDiscountPercent(), p.getColor(), th);
    }

    public ProductDetailResponse detail(Product p) {
        return new ProductDetailResponse(p.getId(), p.getName(), p.getSlug(), cat(p.getCategory()), p.getGenderTag(), p.getMrp(), p.getSellingPrice(), p.getDiscountPercent(), p.getColor(), p.getDescription(), p.getFabricDetails(), p.getCareInstructions(), p.getCountryOfOrigin(), p.isReturnPolicyEnabled(), p.getMetaTitle(), p.getMetaDescription(), p.getStatus(), images.findByProductIdOrderByDisplayOrderAscIdAsc(p.getId()).stream().map(i -> new ProductImageResponse(i.getId(), i.getMediaUrl(), i.getDisplayOrder(), i.isThumbnail())).toList(), variants.findByProductIdAndActiveTrueOrderBySizeCodeAsc(p.getId()).stream().map(v -> new ProductVariantResponse(v.getId(), v.getSizeCode(), v.getSkuCode(), v.isActive(), v.getStockQuantity())).toList());
    }

    private Sort sort(String s) {
        return switch (s == null ? "newest" : s) {
            case "price_asc" -> Sort.by("sellingPrice").ascending();
            case "price_desc" -> Sort.by("sellingPrice").descending();
            case "name_asc" -> Sort.by("name").ascending();
            default -> Sort.by("createdAt").descending();
        };
    }
}