package com.garmentstore.catalog.dto.admin;

public record AdminColorResponse(
        Long id,
        String name,
        String code,
        String hexCode,
        int displayOrder,
        boolean active
) {}
