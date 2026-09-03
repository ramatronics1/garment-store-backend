package com.garmentstore.catalog.dto;

import java.math.BigDecimal;

public record SizeStockResponse(
        Long variantId,
        String sizeCode,
        int stockQuantity,
        BigDecimal sellingPrice,
        BigDecimal mrp
) {}
