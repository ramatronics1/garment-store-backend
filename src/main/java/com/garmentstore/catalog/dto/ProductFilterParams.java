package com.garmentstore.catalog.dto;

import com.garmentstore.catalog.domain.GenderTag;

import java.math.BigDecimal;
import java.util.List;

/**
 * Encapsulates all product listing filter & pagination params.
 * Passed from controller → service to keep method signatures clean.
 *
 * Mirrors UI filter state (useFilters.js):
 *  gender, category, sizes, colors, minPrice, maxPrice, minDiscount, q, sort, page, size
 */
public record ProductFilterParams(
        GenderTag gender,          // null = all genders
        Long categoryId,           // null = all categories (resolved from category name)
        String keyword,            // null or blank = no text search
        BigDecimal minPrice,       // null = no lower bound
        BigDecimal maxPrice,       // null = no upper bound
        Integer minDiscount,       // null = no discount filter (value 0-100)
        List<String> sizes,        // empty = all sizes (e.g. ["S", "M", "L"])
        List<String> colors,       // empty = all colors (e.g. ["black", "blue"])
        int page,                  // 0-based (converted from UI's 1-based)
        int size,                  // items per page, capped at 100
        String sort                // relevant | price_asc | price_desc | newest | discount
) {}
