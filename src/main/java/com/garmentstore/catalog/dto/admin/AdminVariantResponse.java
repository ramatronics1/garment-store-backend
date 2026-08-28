package com.garmentstore.catalog.dto.admin;

import com.garmentstore.catalog.domain.VariantStatus;

import java.math.BigDecimal;

public record AdminVariantResponse(
        Long id,
        Long colorId,
        String colorName,
        String colorCode,
        String colorHex,
        Long sizeId,
        String sizeName,
        String sizeCode,
        String sku,
        String barcode,
        BigDecimal mrp,
        BigDecimal sellingPrice,
        BigDecimal costPrice,
        int discountPercent,
        int stockQuantity,
        Integer weightGrams,
        String combinationKey,
        VariantStatus status,
        boolean isActive
) {}

