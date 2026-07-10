package com.garmentstore.catalog.dto.admin;

import jakarta.validation.constraints.NotNull;

public record AdminFeaturedRequest(@NotNull Long productId, Integer displayOrder, Boolean active){}