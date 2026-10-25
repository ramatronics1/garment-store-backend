package com.garmentstore.inventory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record AdminInventoryVariantResponse(
        Long id,
        @JsonProperty("product_id") Long productId,
        @JsonProperty("product_name") String productName,
        @JsonProperty("product_slug") String productSlug,
        @JsonProperty("sku_code") String skuCode,
        @JsonProperty("color_id") Long colorId,
        @JsonProperty("color_name") String colorName,
        @JsonProperty("color_hex") String colorHex,
        @JsonProperty("size_id") Long sizeId,
        @JsonProperty("size_code") String sizeCode,
        @JsonProperty("category_id") Long categoryId,
        @JsonProperty("category_name") String categoryName,
        @JsonProperty("gender_tag") String genderTag,
        BigDecimal mrp,
        @JsonProperty("selling_price") BigDecimal sellingPrice,
        @JsonProperty("cost_price") BigDecimal costPrice,
        @JsonProperty("stock_quantity") int stockQuantity,
        String status, // "HEALTHY", "LOW_STOCK", "OUT_OF_STOCK"
        @JsonProperty("thumbnail_url") String thumbnailUrl,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {}
