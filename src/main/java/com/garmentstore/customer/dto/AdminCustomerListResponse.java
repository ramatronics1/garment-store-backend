package com.garmentstore.customer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * AdminCustomerListResponse — paginated response for GET /api/v1/admin/customers.
 *
 * Wraps an overview block plus the paged list of customer summaries.
 */
public record AdminCustomerListResponse(

        AdminCustomerOverview overview,

        List<AdminCustomerSummary> customers,

        long total,

        int page,

        int size,

        @JsonProperty("totalPages")
        int totalPages
) {}
