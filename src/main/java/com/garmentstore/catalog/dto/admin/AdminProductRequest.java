package com.garmentstore.catalog.dto.admin;

import com.garmentstore.catalog.domain.GenderTag;
import com.garmentstore.catalog.domain.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminProductRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 220) String slug,
        @NotNull Long categoryId,
        @NotNull GenderTag genderTag,
        @Size(max = 100) String brand,
        String description,
        String fabricDetails,
        @Size(max = 80) String fit,
        @Size(max = 80) String season,
        String careInstructions,
        String countryOfOrigin,
        Boolean returnPolicyEnabled,
        String metaTitle,
        String metaDescription,
        ProductStatus status,
        List<@Valid AdminImageRequest> images,
        List<@Valid AdminVariantRequest> variants
) {}