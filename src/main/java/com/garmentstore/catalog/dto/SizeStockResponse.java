package com.garmentstore.catalog.dto;

public record SizeStockResponse(
        Long variantId,
        String sizeCode,
        int stockQuantity
) {}
