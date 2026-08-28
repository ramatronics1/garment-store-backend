package com.garmentstore.catalog.dto.admin;

import com.garmentstore.catalog.domain.VariantStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AdminVariantRequest(
        @NotNull Long colorId,
        @NotNull Long sizeId,
        @NotBlank @Size(max = 120) String sku,
        @Size(max = 100) String barcode,
        @NotNull @DecimalMin("0.0") BigDecimal mrp,
        @NotNull @DecimalMin("0.0") BigDecimal sellingPrice,
        @DecimalMin("0.0") BigDecimal costPrice,
        Integer weightGrams,
        Integer stockQuantity,
        VariantStatus status,
        Boolean isActive
) {}