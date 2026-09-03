package com.garmentstore.notification.application;

import com.garmentstore.auth.application.event.UserRegisteredEvent;
import com.garmentstore.auth.domain.User;
import com.garmentstore.auth.infrastructure.UserRepository;
import com.garmentstore.notification.application.event.LowStockEvent;
import com.garmentstore.notification.application.event.OrderPlacedEvent;
import com.garmentstore.notification.application.event.OrderStatusChangedEvent;
import com.garmentstore.notification.domain.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Listens to Spring ApplicationEvents and triggers appropriate notifications.
 *
 * Design: This is the ONLY component that knows which events map to which notifications.
 * Decoupling: OrderService / AdminOrderService / AuthRegistrationService publish events
 * and have zero knowledge of the notification module.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // ── 1. New Order Placed ────────────────────────────────────────────────────

    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("[NotifListener] OrderPlaced event for order #{}", event.orderNumber());

        // Customer: ORDER_PLACED notification
        notificationService.notifyCustomerOrderEvent(
                event.customerId(), event.customerEmail(), event.customerMobile(),
                event.customerName(), event.orderId(), event.orderNumber(),
                event.grandTotal(), NotificationType.ORDER_PLACED);

        // Admin: NEW_ORDER notification (get admin user id from DB)
        resolveAdminUserId().ifPresent(adminId ->
                notificationService.notifyAdminNewOrder(
                        adminId, event.customerName(),
                        event.orderId(), event.orderNumber(), event.grandTotal()));
    }

    // ── 2. Order Status Changed ────────────────────────────────────────────────

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("[NotifListener] OrderStatusChanged event: order #{} -> {}",
                event.orderNumber(), event.newStatus());

        NotificationType type = event.newStatus() == null ? null : switch (event.newStatus().name()) {
            case "CONFIRMED" -> NotificationType.ORDER_CONFIRMED;
            case "SHIPPED"   -> NotificationType.ORDER_SHIPPED;
            case "DELIVERED" -> NotificationType.ORDER_DELIVERED;
            case "CANCELLED" -> NotificationType.ORDER_CANCELLED;
            default -> null;
        };

        if (type != null) {
            notificationService.notifyCustomerOrderEvent(
                    event.customerId(), event.customerEmail(), event.customerMobile(),
                    event.customerName(), event.orderId(), event.orderNumber(),
                    event.grandTotal(), type);
        }
    }

    // ── 3. Welcome on Registration ─────────────────────────────────────────────

    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("[NotifListener] UserRegistered event for userId={}", event.userId());

        // Fetch user details to get the name
        userRepository.findById(event.userId()).ifPresent(user ->
                notificationService.notifyWelcome(
                        user.getId(),
                        user.getName() != null ? user.getName() : "Valued Customer",
                        user.getEmail(),
                        user.getMobile()
                )
        );
    }

    // ── 4. Low Stock Alert ─────────────────────────────────────────────────────

    @EventListener
    public void onLowStock(LowStockEvent event) {
        log.info("[NotifListener] LowStock event: {} (SKU: {}) = {} units remaining",
                event.productName(), event.sku(), event.stockRemaining());

        resolveAdminUserId().ifPresent(adminId ->
                notificationService.notifyAdminLowStock(
                        adminId, event.productName(), event.sku(), event.stockRemaining()));
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    /**
     * Finds the first admin user in the system for logging admin notifications.
     * In a real multi-admin setup this could be per-product-manager assignment.
     */
    private Optional<Long> resolveAdminUserId() {
        return userRepository.findFirstAdmin()
                .map(User::getId);
    }
}
