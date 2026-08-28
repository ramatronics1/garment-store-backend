package com.garmentstore.catalog.dto.admin;

import com.garmentstore.catalog.domain.Attribute;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminAttributeRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull Attribute.Scope scope,
        Boolean active
) {}
