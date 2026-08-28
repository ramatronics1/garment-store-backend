package com.garmentstore.catalog.dto.admin;

public record AdminSizeResponse(
        Long id,
        Long sizeGroupId,
        String sizeGroupName,
        String name,
        String sizeCode,
        int sortOrder,
        boolean active
) {}
