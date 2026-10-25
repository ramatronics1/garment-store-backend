package com.garmentstore.inventory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.garmentstore.inventory.domain.InventoryTransactionType;
import jakarta.validation.constraints.NotNull;

public record AdminStockAdjustmentRequest(
        /** "SET" to directly set stockQuantity, or "DELTA" to add/subtract (+5, -2) */
        @JsonProperty("mode") String mode,
        @NotNull(message = "quantity is required") Integer quantity,
        @JsonProperty("transaction_type") InventoryTransactionType transactionType,
        String reason,
        @JsonProperty("reference_id") String referenceId
) {}
