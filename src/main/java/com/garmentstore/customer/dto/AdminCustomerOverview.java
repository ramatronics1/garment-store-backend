package com.garmentstore.customer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * AdminCustomerOverview — KPI card data for the admin customers page header.
 */
public record AdminCustomerOverview(

        @JsonProperty("total_customers")
        long totalCustomers,

        @JsonProperty("active_customers")
        long activeCustomers,

        @JsonProperty("total_revenue")
        BigDecimal totalRevenue,

        @JsonProperty("avg_ltv")
        BigDecimal avgLtv
) {}
