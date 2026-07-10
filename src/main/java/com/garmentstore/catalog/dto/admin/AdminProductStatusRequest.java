package com.garmentstore.catalog.dto.admin;

import com.garmentstore.catalog.domain.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record AdminProductStatusRequest(@NotNull ProductStatus status){}