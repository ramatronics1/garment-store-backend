package com.garmentstore.catalog.dto.admin;

import java.util.List;
import java.util.Map;

public record AdminProductPageResponse(
        List<AdminProductListResponse> products,
        int page,
        int size,
        long total,
        int totalPages,
        boolean last,
        Map<String, Long> statusCounts
) {}
