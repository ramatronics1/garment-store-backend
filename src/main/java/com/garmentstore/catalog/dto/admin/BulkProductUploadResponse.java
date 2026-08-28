package com.garmentstore.catalog.dto.admin;

import java.util.List;

public record BulkProductUploadResponse(
        int totalRowsProcessed,
        int productsCreated,
        int variantsCreated,
        int failedRowsCount,
        List<BulkUploadRowError> errors
) {}
