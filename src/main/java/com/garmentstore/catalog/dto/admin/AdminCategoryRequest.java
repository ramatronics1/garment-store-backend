package com.garmentstore.catalog.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCategoryRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 180) String slug,
        Long parentCategoryId,
        Integer displayOrder,
        Boolean active,
        @Size(max = 700) String thumbnailUrl,
        @Size(max = 700) String bannerUrl,
        @Size(max = 300) String bannerAlt,
        String description,
        @Size(max = 700) String iconUrl
) {}