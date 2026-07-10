package com.garmentstore.catalog.dto;

import java.util.List;

public record ProductAvailabilityResponse(Long productId, boolean hasActiveVariants, List<String> activeSizes, String inventoryStatus){}