package com.garmentstore.catalog.dto.admin;

import jakarta.validation.constraints.NotNull;

public record AdminRelatedProductRequest(@NotNull Long relatedProductId){}