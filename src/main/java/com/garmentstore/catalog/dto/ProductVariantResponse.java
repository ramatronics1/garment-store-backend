package com.garmentstore.catalog.dto;

/**
 * Per-variant response including real-time stock quantity.
 * stockQuantity = 0 means out of stock for this size.
 */
public record ProductVariantResponse(Long id, String sizeCode, String skuCode, boolean active, int stockQuantity) {}