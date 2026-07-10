package com.garmentstore.catalog.dto;

import com.garmentstore.catalog.domain.GenderTag;

import java.math.BigDecimal;

public record ProductSummaryResponse(Long id, String name, String slug, Long categoryId, String categoryName, GenderTag genderTag, BigDecimal mrp, BigDecimal sellingPrice, int discountPercent, String color, String thumbnailUrl){}