package com.garmentstore.inventory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AdminInventoryCategoryStat(
        String name,
        @JsonProperty("gender_tag") String genderTag,
        @JsonProperty("total_stock") long totalStock,
        @JsonProperty("total_products") long totalProducts,
        @JsonProperty("total_variants") long totalVariants
) {}
