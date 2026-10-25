package com.garmentstore.inventory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AdminInventoryOverviewResponse(
        @JsonProperty("total_items") long totalItems,
        @JsonProperty("total_variants") long totalVariants,
        @JsonProperty("low_stock") long lowStock,
        @JsonProperty("out_of_stock") long outOfStock,
        @JsonProperty("total_stock_units") long totalStockUnits,
        @JsonProperty("total_valuation_mrp") BigDecimal totalValuationMrp,
        @JsonProperty("total_valuation_cost") BigDecimal totalValuationCost
) {}
