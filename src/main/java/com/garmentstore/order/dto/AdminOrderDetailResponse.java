package com.garmentstore.order.dto;

import com.garmentstore.order.domain.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Full order detail response for the Admin Per-Order page. */
@Data
@Builder
public class AdminOrderDetailResponse {
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal grandTotal;
    private Integer itemCount;
    private String paymentMethod;
    private Instant createdAt;
    private Instant updatedAt;

    private AdminOrderSummaryResponse.AdminCustomerSummary customer;
    private AdminDeliveryAddressResponse deliveryAddress;
    private List<AdminOrderItemResponse> items;
    private List<AdminOrderStatusHistoryResponse> statusHistory;
}
