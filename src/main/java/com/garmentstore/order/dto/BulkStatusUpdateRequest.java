package com.garmentstore.order.dto;

import com.garmentstore.order.domain.OrderStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** Request body for PATCH /admin/orders/bulk-status */
@Data
public class BulkStatusUpdateRequest {

    @NotEmpty(message = "Order IDs must not be empty")
    private List<Long> orderIds;

    @NotNull(message = "Status must not be null")
    private OrderStatus status;

    /** Optional note appended to status history for every updated order. */
    private String note;
}
