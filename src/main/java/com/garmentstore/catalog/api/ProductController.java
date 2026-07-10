package com.garmentstore.catalog.api;

import com.garmentstore.catalog.application.CatalogService;
import com.garmentstore.catalog.dto.*;
import com.garmentstore.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final CatalogService s;

    @GetMapping
    public ApiResponse<PageResponse<ProductSummaryResponse>> all(@RequestParam(required = false) Long categoryId, @RequestParam(required = false) String q, @RequestParam(defaultValue = "newest") String sort, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("Products fetched successfully", s.getProducts(categoryId, q, page, size, sort));
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
}