package com.garmentstore.catalog.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminAttributeValueRequest(
        @NotNull Long attributeId,
        @NotBlank @Size(max = 200) String value,
        Integer displayOrder
) {}
