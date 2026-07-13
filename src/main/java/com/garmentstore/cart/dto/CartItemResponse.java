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
        String sizeCode,            // e.g. "M", "L", "XL"
        String skuCode,
        int stockQuantity,          // current available stock for this size
        int quantity,               // how many in cart
        BigDecimal sellingPrice,    // server-read price (never from client)
        BigDecimal mrp,
        int discountPercent
) {}
