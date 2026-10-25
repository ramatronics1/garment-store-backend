package com.garmentstore.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AdminBulkStockAdjustmentRequest(
        @NotEmpty(message = "items cannot be empty")
        @Valid
        List<AdminBulkStockAdjustmentItem> items
) {}
