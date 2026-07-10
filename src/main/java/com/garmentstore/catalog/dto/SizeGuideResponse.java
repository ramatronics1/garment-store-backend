package com.garmentstore.catalog.dto;

import java.util.List;

public record SizeGuideResponse(Long productId, List<String> availableSizes, String note){}