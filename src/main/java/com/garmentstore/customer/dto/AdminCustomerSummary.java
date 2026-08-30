package com.garmentstore.customer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * AdminCustomerSummary — per-row DTO for the admin customers list.
 *
 * Returned as part of {@link AdminCustomerListResponse}.
 */
public record AdminCustomerSummary(

        @JsonProperty("user_id")
        Long userId,

        String name,
        String email,
        String mobile,

        @JsonProperty("account_status")
        String accountStatus,

        @JsonProperty("email_verified")
        boolean emailVerified,

        @JsonProperty("mobile_verified")
        boolean mobileVerified,

        @JsonProperty("created_at")
        Instant createdAt,

        @JsonProperty("last_login_at")
        Instant lastLoginAt,

        @JsonProperty("total_orders")
        long totalOrders,

        @JsonProperty("total_spent")
        BigDecimal totalSpent,

        @JsonProperty("last_order_date")
        Instant lastOrderDate,

        @JsonProperty("favorite_category")
        String favoriteCategory
) {}
