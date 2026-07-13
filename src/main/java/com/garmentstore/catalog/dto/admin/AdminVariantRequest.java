package com.garmentstore.catalog.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Admin request to create or update a product variant.
 * stockQuantity can be updated directly by admin (replaces full inventory module for now).
 */
public record AdminVariantRequest(
        @NotBlank @Size(max = 30)  String sizeCode,
        @NotBlank @Size(max = 120) String skuCode,
        Boolean active,
        @Min(0) Integer stockQuantity   // defaults to 0 if not provided
) {}