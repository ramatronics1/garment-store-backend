package com.garmentstore.catalog.dto.admin;

import java.time.Instant;

/**
 * Admin view of a single featured-product entry.
 * Returned by {@code GET /api/v1/admin/catalog/featured}.
 */
public record AdminFeaturedListResponse(
        Long id,
        Long productId,
        String productCode,
        String productName,
        String slug,
        String thumbnailUrl,
        int displayOrder,
        boolean active,
        Instant createdAt
) {}
