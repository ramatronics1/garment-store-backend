package com.garmentstore.catalog.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminSizeRequest(
        @NotNull Long sizeGroupId,
        @NotBlank @Size(max = 50)  String name,
        @NotBlank @Size(max = 30)  String sizeCode,
        Integer sortOrder,
        Boolean active
) {}
