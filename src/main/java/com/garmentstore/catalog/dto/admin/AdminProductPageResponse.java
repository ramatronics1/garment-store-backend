package com.garmentstore.catalog.dto.admin;

import java.util.List;

public record AdminProductPageResponse(
        List<AdminProductListResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}
