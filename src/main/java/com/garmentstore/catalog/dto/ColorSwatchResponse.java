package com.garmentstore.catalog.dto;

/**
 * A color swatch used in product summary/detail responses.
 * Enough for the frontend to render a color picker without fetching extra data.
 */
public record ColorSwatchResponse(
        Long id,
        String name,
        String code,
        String hexCode
) {}
