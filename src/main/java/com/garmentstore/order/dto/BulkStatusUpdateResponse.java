package com.garmentstore.order.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Response for PATCH /admin/orders/bulk-status */
@Data
@Builder
public class BulkStatusUpdateResponse {
    private int updatedCount;
    private List<Long> failedIds;
    private String message;
}
