package com.garmentstore.order.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** Paginated response for GET /admin/orders */
@Data
@Builder
public class AdminOrderPageResponse {
    private List<AdminOrderSummaryResponse> orders;
    private Map<String, Long> statusCounts;   // e.g. {"PENDING":5,"SHIPPED":8,...}
    private long total;
    private int page;
    private int size;
    private int totalPages;
}
