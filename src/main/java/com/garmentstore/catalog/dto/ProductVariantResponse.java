package com.garmentstore.catalog.dto;

public record ProductVariantResponse(Long id, String sizeCode, String skuCode, boolean active, int stockQuantity) {}