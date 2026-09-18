package com.garmentstore.catalog.dto;

public record StoreMediaResponse(
        Long id,
        String placementKey,
        String mediaUrl,
        String altText,
        String linkUrl,
        String title,
        String subtitle,
        String badgeText,
        boolean active,
        int displayOrder
) {}
