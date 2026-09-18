package com.garmentstore.catalog.dto.admin;

public record AdminAttributeValueResponse(
        Long id,
        Long attributeId,
        String value,
        int displayOrder
) {}
