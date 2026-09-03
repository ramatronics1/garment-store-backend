package com.garmentstore.notification.domain;

/**
 * All notification event types in the system.
 * Customer-facing types trigger customer notifications.
 * ADMIN types trigger admin notifications.
 */
public enum NotificationType {
    // Customer-facing order lifecycle
    ORDER_PLACED,
    ORDER_CONFIRMED,
    ORDER_SHIPPED,
    ORDER_DELIVERED,
    ORDER_CANCELLED,

    // Admin-facing alerts
    NEW_ORDER_ADMIN,
    LOW_STOCK_ADMIN,

    // Onboarding
    WELCOME
}
