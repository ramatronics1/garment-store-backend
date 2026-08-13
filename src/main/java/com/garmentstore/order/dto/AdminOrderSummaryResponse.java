package com.garmentstore.order.dto;

import com.garmentstore.order.domain.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/** Lightweight row used in the Admin Orders list table. */
@Data
@Builder
public class AdminOrderSummaryResponse {
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal grandTotal;
    private Integer itemCount;
    private String paymentMethod;
    private Instant createdAt;
    private Instant updatedAt;
    private AdminCustomerSummary customer;

    @Data
    @Builder
    public static class AdminCustomerSummary {
        private Long userId;
        private String name;
        private String email;
        private String mobile;
    }
}
