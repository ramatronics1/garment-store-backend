package com.garmentstore.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for adding or updating a cart item.
 * The server resolves product/variant from the IDs and re-reads the price from the DB.
 * The client NEVER sends price — it cannot be trusted.
 */
public record CartItemRequest(
        @NotNull(message = "productId is required")
        Long productId,

        @NotNull(message = "variantId is required")
        Long variantId,

        @Min(value = 1, message = "quantity must be at least 1")
        int quantity
) {}
