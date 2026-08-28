package com.garmentstore.catalog.dto;

import com.garmentstore.catalog.domain.GenderTag;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lightweight product summary for listing pages.
 * priceRange = {min, max} sellingPrice across all ACTIVE variants.
 * colorSwatches = distinct colors available across variants.
 */
public record ProductSummaryResponse(
        Long id,
        String productCode,
        String name,
        String slug,
        Long categoryId,
        String categoryName,
        GenderTag genderTag,
        String brand,
        BigDecimal minSellingPrice,
        BigDecimal maxSellingPrice,
        BigDecimal minMrp,
        List<ColorSwatchResponse> colorSwatches,
        String thumbnailUrl,
        boolean inStock,
        List<SizeStockResponse> sizes
) {}