package com.garmentstore.order.dto;

import com.garmentstore.customer.dto.AddressResponse;
import com.garmentstore.order.domain.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal grandTotal;
    private String paymentMethod;
    private Instant createdAt;
    private AddressResponse shippingAddress;
    private List<OrderItemResponse> items;
}
