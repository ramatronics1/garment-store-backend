package com.garmentstore.inventory.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.garmentstore.inventory.domain.InventoryTransactionType;
import lombok.Builder;

import java.time.Instant;

@Builder
public record AdminInventoryTransactionResponse(
        Long id,
        @JsonProperty("variant_id") Long variantId,
        @JsonProperty("product_id") Long productId,
        @JsonProperty("product_name") String productName,
        String sku,
        @JsonProperty("previous_quantity") int previousQuantity,
        @JsonProperty("quantity_change") int quantityChange,
        @JsonProperty("new_quantity") int newQuantity,
        @JsonProperty("transaction_type") InventoryTransactionType transactionType,
        String reason,
        @JsonProperty("reference_id") String referenceId,
        @JsonProperty("actor_name") String actorName,
        @JsonProperty("created_at") Instant createdAt
) {}
