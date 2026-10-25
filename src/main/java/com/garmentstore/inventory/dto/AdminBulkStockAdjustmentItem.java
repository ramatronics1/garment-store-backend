package com.garmentstore.inventory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.garmentstore.inventory.domain.InventoryTransactionType;
import jakarta.validation.constraints.NotNull;

public record AdminBulkStockAdjustmentItem(
        @NotNull(message = "variant_id is required")
        @JsonProperty("variant_id") Long variantId,
        String mode,
        @NotNull(message = "quantity is required")
        Integer quantity,
        @JsonProperty("transaction_type") InventoryTransactionType transactionType,
        String reason,
        @JsonProperty("reference_id") String referenceId
) {}
