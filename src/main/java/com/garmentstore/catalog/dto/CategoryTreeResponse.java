package com.garmentstore.catalog.dto;

import java.util.List;

/**
 * Hierarchical category response — each node carries its children inline.
 * Root categories have {@code parentCategoryId == null}.
 * Used by {@code GET /api/v1/categories/tree}.
 */
public record CategoryTreeResponse(
        Long id,
        String name,
        String slug,
        Long parentCategoryId,
        int displayOrder,
        boolean active,
        String thumbnailUrl,
        String bannerUrl,
        String bannerAlt,
        String description,
        String iconUrl,
        List<CategoryTreeResponse> children
) {}
