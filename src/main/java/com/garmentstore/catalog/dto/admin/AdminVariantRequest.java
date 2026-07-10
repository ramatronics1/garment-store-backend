package com.garmentstore.catalog.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminVariantRequest(@NotBlank@Size(max=30)String sizeCode, @NotBlank@Size(max=120)String skuCode, Boolean active){}