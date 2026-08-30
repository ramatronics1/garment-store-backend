package com.garmentstore.catalog.dto.admin;

import com.garmentstore.catalog.domain.GenderTag;
import com.garmentstore.catalog.domain.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record AdminProductListResponse(
        Long id,
        String productCode,
        String name,
        String slug,
        String categoryName,
        Long categoryId,
        GenderTag genderTag,
        String brand,
        BigDecimal mrp,
        BigDecimal sellingPrice,
        int discountPercent,
        ProductStatus status,
        int totalStock,
        int variantCount,
        int orderCount,
        String thumbnailUrl,
        Instant createdAt,
        Instant updatedAt
) {}

