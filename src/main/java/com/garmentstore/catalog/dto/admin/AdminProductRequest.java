package com.garmentstore.catalog.dto.admin;

import com.garmentstore.catalog.domain.GenderTag;
import com.garmentstore.catalog.domain.ProductStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AdminProductRequest(@NotBlank@Size(max=200)String name, @Size(max=220)String slug, @NotNull Long categoryId, @NotNull GenderTag genderTag, @NotNull@DecimalMin("0.0")BigDecimal mrp, @NotNull@DecimalMin("0.0")BigDecimal sellingPrice, @Min(0)@Max(100)Integer discountPercent, String color, String description, String fabricDetails, String careInstructions, String countryOfOrigin, Boolean returnPolicyEnabled, String metaTitle, String metaDescription, ProductStatus status){}