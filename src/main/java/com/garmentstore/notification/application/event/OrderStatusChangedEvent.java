package com.garmentstore.notification.application.event;

import com.garmentstore.order.domain.OrderStatus;
import java.math.BigDecimal;

/**
 * Published by AdminOrderService after a status update.
 * NotificationEventListener consumes this to notify the customer of the change.
 */
public record OrderStatusChangedEvent(
        Long orderId,
        String orderNumber,
        BigDecimal grandTotal,
        OrderStatus newStatus,
        Long customerId,
        String customerName,
        String customerEmail,
        String customerMobile
) {}
