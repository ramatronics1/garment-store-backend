package com.garmentstore.catalog.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request for auto-generating all Color × Size variant combinations for a product.
 * The backend generates len(colorIds) × len(sizeIds) variants, skipping any
 * combination that already exists for this product.
 */
public record AdminVariantGenerateRequest(
        @NotEmpty List<@NotNull Long> colorIds,
        @NotEmpty List<@NotNull Long> sizeIds,

        /** Default MRP applied to all generated variants */
        @NotNull java.math.BigDecimal mrp,

        /** Default selling price applied to all generated variants */
        @NotNull java.math.BigDecimal sellingPrice,

        /** Default starting stock quantity (0 if omitted) */
        Integer stockQuantity
) {}
