package com.garmentstore.catalog.dto;

/**
 * Delivery availability response for a given pincode.
 * Business rule: Delivery is available only within Karnataka state.
 */
public record DeliveryCheckResponse(
        String pincode,
        boolean available,
        boolean cod,            // Cash on Delivery available
        String eta,             // e.g. "Mon, 18 Jul" — null if unavailable
        String state,           // Resolved state name from postal API
        String message          // User-facing message
) {}
