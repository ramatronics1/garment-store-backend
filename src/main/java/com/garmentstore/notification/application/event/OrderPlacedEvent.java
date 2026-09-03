package com.garmentstore.notification.application.event;

import java.math.BigDecimal;

/**
 * Published by OrderService after a new order is successfully saved.
 * NotificationEventListener consumes this to trigger customer + admin notifications.
 */
public record OrderPlacedEvent(
        Long orderId,
        String orderNumber,
        BigDecimal grandTotal,
        Long customerId,
        String customerName,
        String customerEmail,
        String customerMobile
) {}
