package com.garmentstore.catalog.dto;

public record SizeStockResponse(
        String sizeCode,
        int stockQuantity
) {}
