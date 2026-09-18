package com.garmentstore.catalog.dto;

public record CategoryResponse(
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
        String iconUrl
) {}