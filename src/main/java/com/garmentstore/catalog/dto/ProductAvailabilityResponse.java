package com.garmentstore.catalog.dto;

import java.util.List;

/**
 * Richer availability response including per-variant stock quantities.
 * Used by the UI to show disabled size buttons and OOS banners.
 */
public record ProductAvailabilityResponse(
        Long productId,
        boolean inStock,                          // true if any variant has stock > 0
        List<VariantStock> variants,              // per-size stock
        String inventoryStatus                    // informational note
) {
    public record VariantStock(String sizeCode, int stockQuantity, boolean available) {}
}