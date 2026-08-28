package com.garmentstore.catalog.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminColorRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 50)  String code,
        @Size(max = 10)            String hexCode,
        Integer displayOrder,
        Boolean active
) {}
