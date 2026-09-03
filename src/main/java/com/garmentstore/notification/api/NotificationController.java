package com.garmentstore.notification.api;

import com.garmentstore.notification.application.NotificationService;
import com.garmentstore.notification.domain.NotificationLog;
import com.garmentstore.notification.domain.NotificationPreferences;
import com.garmentstore.notification.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the notification module.
 *
 * Customer Endpoints:
 *  GET  /api/v1/notifications/my              — Fetch in-app notifications
 *  GET  /api/v1/notifications/unread-count    — Poll for bell badge count
 *  PUT  /api/v1/notifications/{id}/read       — Mark single as read
 *  PUT  /api/v1/notifications/read-all        — Mark all as read
 *  GET  /api/v1/notifications/preferences     — Get notification preferences
 *  PUT  /api/v1/notifications/preferences     — Update notification preferences
 *
 * Admin Endpoints:
 *  GET  /api/v1/admin/notifications           — Fetch admin in-app notifications
 *  GET  /api/v1/admin/notifications/unread-count
 *  PUT  /api/v1/admin/notifications/read-all
 */
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ── Customer Endpoints ────────────────────────────────────────────────────

    @GetMapping("/api/v1/notifications/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = Long.valueOf(authentication.getName());
        Page<NotificationLog> logs = notificationService.getInAppNotifications(userId, page, Math.min(size, 50));
        return ResponseEntity.ok(logs.stream().map(this::toResponse).toList());
    }

    @GetMapping("/api/v1/notifications/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(new UnreadCountResponse(notificationService.getUnreadCount(userId)));
    }

    @PutMapping("/api/v1/notifications/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        notificationService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/v1/notifications/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/notifications/preferences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationPreferencesResponse> getPreferences(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(toPrefsResponse(userId, notificationService.getPreferences(userId)));
    }

    @PutMapping("/api/v1/notifications/preferences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationPreferencesResponse> updatePreferences(
            Authentication authentication,
            @RequestBody UpdatePreferencesRequest request) {

        Long userId = Long.valueOf(authentication.getName());
        NotificationPreferences prefs = notificationService.updatePreferences(
                userId, request.emailEnabled(), request.whatsappEnabled(), request.orderUpdatesEnabled());
        return ResponseEntity.ok(toPrefsResponse(userId, prefs));
    }

    // ── Admin Endpoints ───────────────────────────────────────────────────────

    @GetMapping("/api/v1/admin/notifications")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getAdminNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = Long.valueOf(authentication.getName());
        Page<NotificationLog> logs = notificationService.getInAppNotifications(userId, page, Math.min(size, 50));
        return ResponseEntity.ok(logs.stream().map(this::toResponse).toList());
    }

    @GetMapping("/api/v1/admin/notifications/unread-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UnreadCountResponse> getAdminUnreadCount(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(new UnreadCountResponse(notificationService.getUnreadCount(userId)));
    }

    @PutMapping("/api/v1/admin/notifications/read-all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> adminMarkAllAsRead(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private NotificationResponse toResponse(NotificationLog log) {
        return NotificationResponse.builder()
                .id(log.getId())
                .type(log.getType())
                .channel(log.getChannel())
                .subject(log.getSubject())
                .body(log.getBody())
                .status(log.getStatus())
                .read(log.getReadAt() != null)
                .createdAt(log.getCreatedAt())
                .orderId(log.getOrderId())
                .displayTitle(resolveTitle(log))
                .displayIcon(resolveIcon(log))
                .build();
    }

    private NotificationPreferencesResponse toPrefsResponse(Long userId, NotificationPreferences prefs) {
        return NotificationPreferencesResponse.builder()
                .userId(userId)
                .emailEnabled(prefs.isEmailEnabled())
                .whatsappEnabled(prefs.isWhatsappEnabled())
                .orderUpdatesEnabled(prefs.isOrderUpdatesEnabled())
                .promotionsEnabled(prefs.isPromotionsEnabled())
                .build();
    }

    private String resolveTitle(NotificationLog log) {
        if (log.getType() == null) return "Notification";
        return switch (log.getType().name()) {
            case "ORDER_PLACED"    -> "Order Placed";
            case "ORDER_CONFIRMED" -> "Order Confirmed";
            case "ORDER_SHIPPED"   -> "Order Shipped";
            case "ORDER_DELIVERED" -> "Order Delivered";
            case "ORDER_CANCELLED" -> "Order Cancelled";
            case "NEW_ORDER_ADMIN" -> "New Order Received";
            case "LOW_STOCK_ADMIN" -> "Low Stock Alert";
            case "WELCOME"         -> "Welcome to Vastra!";
            default                -> "Notification";
        };
    }

    private String resolveIcon(NotificationLog log) {
        if (log.getType() == null) return "🔔";
        return switch (log.getType().name()) {
            case "ORDER_PLACED"    -> "🛍️";
            case "ORDER_CONFIRMED" -> "✅";
            case "ORDER_SHIPPED"   -> "🚚";
            case "ORDER_DELIVERED" -> "📦";
            case "ORDER_CANCELLED" -> "❌";
            case "NEW_ORDER_ADMIN" -> "🛒";
            case "LOW_STOCK_ADMIN" -> "⚠️";
            case "WELCOME"         -> "🎉";
            default                -> "🔔";
        };
    }
}
