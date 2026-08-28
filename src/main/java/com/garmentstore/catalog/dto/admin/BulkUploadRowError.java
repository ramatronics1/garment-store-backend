package com.garmentstore.catalog.dto.admin;

public record BulkUploadRowError(
        int rowNumber,
        String productCode,
        String sku,
        String field,
        String message
) {}
