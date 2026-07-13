package com.garmentstore.cart.dto;

import java.util.List;

/**
 * Pre-checkout cart validation response.
 * Returns a per-item breakdown of stock availability.
 * The UI blocks the "Place Order" button if valid=false.
 */
public record CartValidationResponse(
        boolean valid,                      // true only if ALL items have enough stock
        List<ItemValidation> items          // per-item breakdown
) {
    public record ItemValidation(
            Long variantId,
            String sizeCode,
            String productName,
            int requestedQty,
            int availableQty,
            boolean sufficient,             // requestedQty <= availableQty
            String message                  // user-facing note (e.g. "Only 2 left!")
    ) {}
}
