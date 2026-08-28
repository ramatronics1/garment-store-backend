package com.garmentstore.catalog.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminSizeGroupRequest(
        @NotBlank @Size(max = 100) String name,
        Boolean active
) {}
