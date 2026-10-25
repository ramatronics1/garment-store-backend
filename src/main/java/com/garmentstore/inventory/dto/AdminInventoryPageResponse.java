package com.garmentstore.inventory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record AdminInventoryPageResponse(
        AdminInventoryOverviewResponse overview,
        List<AdminInventoryCategoryStat> categories,
        @JsonProperty("most_sold") List<AdminInventoryTopSold> mostSold,
        List<AdminInventoryVariantResponse> variants,
        long total,
        int page,
        int size,
        int totalPages
) {}
