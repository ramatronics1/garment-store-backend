package com.garmentstore.catalog.dto;

import com.garmentstore.catalog.domain.GenderTag;
import com.garmentstore.catalog.domain.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailResponse(Long id, String name, String slug, CategoryResponse category, GenderTag genderTag, BigDecimal mrp, BigDecimal sellingPrice, int discountPercent, String color, String description, String fabricDetails, String careInstructions, String countryOfOrigin, boolean returnPolicyEnabled, String metaTitle, String metaDescription, ProductStatus status, List<ProductImageResponse> images, List<ProductVariantResponse> variants){}