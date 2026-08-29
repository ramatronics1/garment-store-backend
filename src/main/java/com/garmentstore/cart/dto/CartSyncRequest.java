package com.garmentstore.cart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Bulk sync request — sent from the frontend after login to merge the
 * guest localStorage cart with the server cart.
 *
 * Strategy (server-side):
 *   For each item:
 *     - If variant already in server cart → take the MAX of server qty and client qty
 *     - If variant not in server cart → add it (stock-permitting)
 */
public record CartSyncRequest(
        @NotNull
        @Valid
        List<CartItemRequest> items
) {}
