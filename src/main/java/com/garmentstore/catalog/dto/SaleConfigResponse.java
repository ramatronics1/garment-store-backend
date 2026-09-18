package com.garmentstore.catalog.dto;

import java.time.LocalDateTime;

public record SaleConfigResponse(
        Long id,
        boolean active,
        String title,
        String subtitle,
        LocalDateTime endsAt,
        String badgeText,
        String linkUrl,
        String mediaUrl
) {}
