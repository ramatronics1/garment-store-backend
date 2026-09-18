package com.garmentstore.catalog.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminStoreMediaRequest(
        @NotBlank @Size(max = 700) String mediaUrl,
        @Size(max = 300) String altText,
        @Size(max = 500) String linkUrl,
        @Size(max = 200) String title,
        @Size(max = 400) String subtitle,
        @Size(max = 80)  String badgeText,
        Boolean active,
        Integer displayOrder
) {}
