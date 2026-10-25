package com.garmentstore.inventory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AdminInventoryTopSold(
        Long id,
        @JsonProperty("product_id") Long productId,
        @JsonProperty("product_name") String productName,
        @JsonProperty("sku_code") String skuCode,
        @JsonProperty("size_code") String sizeCode,
        @JsonProperty("thumbnail_url") String thumbnailUrl,
        @JsonProperty("stock_quantity") int stockQuantity,
        @JsonProperty("sold_last_30_days") long soldLast30Days
) {}
