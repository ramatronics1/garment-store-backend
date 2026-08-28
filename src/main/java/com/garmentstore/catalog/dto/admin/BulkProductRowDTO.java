package com.garmentstore.catalog.dto.admin;

import java.math.BigDecimal;

public record BulkProductRowDTO(
        int rowNumber,
        String productCode,
        String productName,
        String categoryName,
        String genderTag,
        String brand,
        String description,
        String fabricDetails,
        String fit,
        String season,
        String careInstructions,
        String countryOfOrigin,
        String variantSku,
        String barcode,
        String colorName,
        String sizeCode,
        BigDecimal mrp,
        BigDecimal sellingPrice,
        BigDecimal costPrice,
        Integer stockQuantity,
        Integer weightGrams,
        String imageUrls
) {}
