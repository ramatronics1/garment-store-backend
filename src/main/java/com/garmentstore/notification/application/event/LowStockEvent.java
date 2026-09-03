package com.garmentstore.notification.application.event;

/**
 * Published by OrderService when a product variant's stock drops to or below
 * the configured low-stock threshold (default 5).
 *
 * NotificationEventListener consumes this to send an admin alert via
 * Email, WhatsApp, and In-App bell — all asynchronously.
 */
public record LowStockEvent(
        String productName,
        String sku,
        int stockRemaining
) {}