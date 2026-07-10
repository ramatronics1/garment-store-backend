package com.garmentstore.catalog.api;

import com.garmentstore.catalog.application.CatalogService;
import com.garmentstore.catalog.dto.CategoryResponse;
import com.garmentstore.catalog.dto.PageResponse;
import com.garmentstore.catalog.dto.ProductSummaryResponse;
import com.garmentstore.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CatalogService s;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> all() {
        return ApiResponse.success("Categories fetched successfully", s.getCategories());
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> one(@PathVariable Long id) {
        return ApiResponse.success("Category fetched successfully", s.getCategory(id));
    }

    @GetMapping("/{id}/products")
    public ApiResponse<PageResponse<ProductSummaryResponse>> products(@PathVariable Long id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("Category products fetched successfully", s.getCategoryProducts(id, page, size));
    }
}