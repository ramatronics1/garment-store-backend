package com.garmentstore.catalog.api;

import com.garmentstore.catalog.application.AdminCatalogService;
import com.garmentstore.catalog.dto.CategoryResponse;
import com.garmentstore.catalog.dto.ProductDetailResponse;
import com.garmentstore.catalog.dto.ProductImageResponse;
import com.garmentstore.catalog.dto.ProductVariantResponse;
import com.garmentstore.catalog.dto.admin.*;
import com.garmentstore.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/catalog")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminCatalogController {
    private final AdminCatalogService s;

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody AdminCategoryRequest r) {
        return ApiResponse.success("Category created", s.createCategory(r));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody AdminCategoryRequest r) {
        return ApiResponse.success("Category updated", s.updateCategory(id, r));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        s.deleteCategory(id);
        return ApiResponse.success("Category deactivated", null);
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductDetailResponse> createProduct(@Valid @RequestBody AdminProductRequest r) {
        return ApiResponse.success("Product created", s.createProduct(r));
    }

    @PutMapping("/products/{id}")
    public ApiResponse<ProductDetailResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody AdminProductRequest r) {
        return ApiResponse.success("Product updated", s.updateProduct(id, r));
    }

    @PatchMapping("/products/{id}/status")
    public ApiResponse<ProductDetailResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody AdminProductStatusRequest r) {
        return ApiResponse.success("Product status updated", s.updateStatus(id, r));
    }

    @DeleteMapping("/products/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        s.deleteProduct(id);
        return ApiResponse.success("Product deleted", null);
    }

    @PostMapping("/products/{id}/variants")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductVariantResponse> addVariant(@PathVariable Long id, @Valid @RequestBody AdminVariantRequest r) {
        return ApiResponse.success("Variant added", s.addVariant(id, r));
    }

    @PutMapping("/variants/{id}")
    public ApiResponse<ProductVariantResponse> updateVariant(@PathVariable Long id, @Valid @RequestBody AdminVariantRequest r) {
        return ApiResponse.success("Variant updated", s.updateVariant(id, r));
    }

    @DeleteMapping("/variants/{id}")
    public ApiResponse<Void> deleteVariant(@PathVariable Long id) {
        s.deleteVariant(id);
        return ApiResponse.success("Variant deleted", null);
    }

    @PostMapping("/products/{id}/images")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductImageResponse> addImage(@PathVariable Long id, @Valid @RequestBody AdminImageRequest r) {
        return ApiResponse.success("Image added", s.addImage(id, r));
    }

    @PutMapping("/images/{id}")
    public ApiResponse<ProductImageResponse> updateImage(@PathVariable Long id, @Valid @RequestBody AdminImageRequest r) {
        return ApiResponse.success("Image updated", s.updateImage(id, r));
    }

    @DeleteMapping("/images/{id}")
    public ApiResponse<Void> deleteImage(@PathVariable Long id) {
        s.deleteImage(id);
        return ApiResponse.success("Image deleted", null);
    }

    @PostMapping("/featured")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> addFeatured(@Valid @RequestBody AdminFeaturedRequest r) {
        return ApiResponse.success("Featured product added", s.addFeatured(r));
    }

    @DeleteMapping("/featured/{id}")
    public ApiResponse<Void> deleteFeatured(@PathVariable Long id) {
        s.deleteFeatured(id);
        return ApiResponse.success("Featured product removed", null);
    }

    @PostMapping("/products/{id}/related")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> addRelated(@PathVariable Long id, @Valid @RequestBody AdminRelatedProductRequest r) {
        return ApiResponse.success("Related product added", s.addRelated(id, r));
    }

    @DeleteMapping("/related/{id}")
    public ApiResponse<Void> deleteRelated(@PathVariable Long id) {
        s.deleteRelated(id);
        return ApiResponse.success("Related product removed", null);
    }
}