package com.garmentstore.order.dto;

import com.garmentstore.order.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Request body for PATCH /admin/orders/{id}/status */
@Data
public class OrderStatusUpdateRequest {

    @NotNull(message = "Status must not be null")
    private OrderStatus status;

    /** Optional note visible on the order timeline. */
    private String note;
}
