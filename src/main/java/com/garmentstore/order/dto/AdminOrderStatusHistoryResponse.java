package com.garmentstore.order.dto;

import com.garmentstore.order.domain.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/** Single entry in the order status timeline. */
@Data
@Builder
public class AdminOrderStatusHistoryResponse {
    private Long id;
    private OrderStatus status;
    private String changedByType;   // ADMIN | SYSTEM | CUSTOMER
    private Long changedById;
    private String note;
    private Instant createdAt;
    private boolean isCurrent;      // true for the most-recent entry
}
