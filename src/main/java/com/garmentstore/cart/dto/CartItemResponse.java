package com.garmentstore.cart.dto;

import java.math.BigDecimal;

/**
 * Cart item as returned by GET /api/v1/cart.
 * Contains enriched product + variant data so the UI can render without extra fetches.
 */
public record CartItemResponse(
        Long id,                    // cart_items.id — used for DELETE /cart/items/{id}
        Long productId,
        String productName,
        String thumbnailUrl,
        String category,
        String gender,
        Long variantId,
        String sizeName,            // e.g. "M", "L", "XL" — from Size entity
        String sku,                 // globally unique SKU on ProductVariant
        int stockQuantity,          // current available stock for this variant
        int quantity,               // how many in cart
        BigDecimal sellingPrice,    // from ProductVariant (never from client)
        BigDecimal mrp,             // from ProductVariant
        int discountPercent         // from ProductVariant
) {}
