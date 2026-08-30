package com.garmentstore.catalog.dto;

import com.garmentstore.catalog.domain.GenderTag;
import com.garmentstore.catalog.domain.ProductStatus;

import java.util.List;

/**
 * Full product detail response for PDP (Product Detail Page).
 * Pricing lives on each variant — the frontend picks the default variant to display.
 */
public record ProductDetailResponse(
        Long id,
        String productCode,
        String name,
        String slug,
        CategoryResponse category,
        GenderTag genderTag,
        String brand,
        String description,
        String fabricDetails,
        String fit,
        String season,
        String careInstructions,
        String countryOfOrigin,
        boolean returnPolicyEnabled,
        String metaTitle,
        String metaDescription,
        ProductStatus status,
        int orderCount,
        List<ProductImageResponse> images,
        List<ProductVariantResponse> variants
) {}