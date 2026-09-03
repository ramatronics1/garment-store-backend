package com.garmentstore.notification.application;

import com.garmentstore.notification.channel.NotificationChannel;
import com.garmentstore.notification.channel.NotificationRequest;
import com.garmentstore.notification.config.NotificationProperties;
import com.garmentstore.notification.domain.*;
import com.garmentstore.notification.infrastructure.NotificationLogRepository;
import com.garmentstore.notification.infrastructure.NotificationPreferencesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Core notification orchestrator.
 *
 * Responsibilities:
 *  1. Decides WHAT notifications to send and to WHOM.
 *  2. Checks user notification preferences (opt-out support).
 *  3. Persists a NotificationLog row for every notification.
 *  4. Dispatches to the correct NotificationChannel implementation.
 *  5. Saves IN_APP notifications for the UI bell.
 *
 * All external sends (Email, WhatsApp) are @Async — they never block the main request thread.
 */
@Slf4j
@Service
public class NotificationService {

    private final Map<NotificationChannelType, NotificationChannel> channels;
    private final NotificationLogRepository logRepository;
    private final NotificationPreferencesRepository preferencesRepository;
    private final NotificationTemplateEngine templateEngine;
    private final NotificationProperties properties;

    /**
     * Spring auto-injects ALL NotificationChannel beans into the list.
     * We build a map keyed by channel type for O(1) lookup.
     * Adding a new channel = just implement NotificationChannel — nothing else to change here.
     */
    public NotificationService(
            List<NotificationChannel> channelList,
            NotificationLogRepository logRepository,
            NotificationPreferencesRepository preferencesRepository,
            NotificationTemplateEngine templateEngine,
            NotificationProperties properties) {
        this.channels = channelList.stream()
                .collect(Collectors.toMap(NotificationChannel::supports, Function.identity()));
        this.logRepository = logRepository;
        this.preferencesRepository = preferencesRepository;
        this.templateEngine = templateEngine;
        this.properties = properties;
        log.info("[Notification] Loaded channels: {}", this.channels.keySet());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PUBLIC API — called by NotificationEventListener
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Send customer order notification (Email + WhatsApp if enabled) + save IN_APP.
     */
    @Async
    public void notifyCustomerOrderEvent(
            Long userId, String email, String mobile,
            String customerName, Long orderId,
            String orderNumber, java.math.BigDecimal amount,
            NotificationType type) {

        NotificationPreferences prefs = getPrefs(userId);
        NotificationTemplateEngine.TemplateContext ctx =
                NotificationTemplateEngine.TemplateContext.forOrder(customerName, orderNumber, amount);

        // IN_APP (always, even if they opt out of external channels — they still see it in the bell)
        sendAndLog(userId, orderId, type, NotificationChannelType.IN_APP,
                email != null ? email : mobile, ctx);

        // Email
        if (email != null && prefs.isEmailEnabled() && prefs.isOrderUpdatesEnabled()) {
            sendAndLog(userId, orderId, type, NotificationChannelType.EMAIL, email, ctx);
        }

        // WhatsApp
        if (mobile != null && prefs.isWhatsappEnabled() && prefs.isOrderUpdatesEnabled()) {
            sendAndLog(userId, orderId, type, NotificationChannelType.WHATSAPP, mobile, ctx);
        }
    }

    /**
     * Send admin notification for new order (Email + WhatsApp + IN_APP).
     */
    @Async
    public void notifyAdminNewOrder(
            Long adminUserId, String customerName,
            Long orderId, String orderNumber, java.math.BigDecimal amount) {

        NotificationTemplateEngine.TemplateContext ctx =
                NotificationTemplateEngine.TemplateContext.forOrder(customerName, orderNumber, amount);

        sendAndLog(adminUserId, orderId, NotificationType.NEW_ORDER_ADMIN,
                NotificationChannelType.IN_APP, properties.getAdmin().getEmail(), ctx);

        if (properties.getAdmin().getEmail() != null && !properties.getAdmin().getEmail().isBlank()) {
            sendAndLog(adminUserId, orderId, NotificationType.NEW_ORDER_ADMIN,
                    NotificationChannelType.EMAIL, properties.getAdmin().getEmail(), ctx);
        }

        if (properties.getAdmin().getWhatsapp() != null && !properties.getAdmin().getWhatsapp().isBlank()) {
            sendAndLog(adminUserId, orderId, NotificationType.NEW_ORDER_ADMIN,
                    NotificationChannelType.WHATSAPP, properties.getAdmin().getWhatsapp(), ctx);
        }
    }

    /**
     * Send admin low-stock alert (Email + WhatsApp + IN_APP).
     */
    @Async
    public void notifyAdminLowStock(Long adminUserId, String productName, String sku, int stockRemaining) {
        NotificationTemplateEngine.TemplateContext ctx =
                NotificationTemplateEngine.TemplateContext.forStock(productName, sku, stockRemaining);

        sendAndLog(adminUserId, null, NotificationType.LOW_STOCK_ADMIN,
                NotificationChannelType.IN_APP, properties.getAdmin().getEmail(), ctx);

        if (properties.getAdmin().getEmail() != null && !properties.getAdmin().getEmail().isBlank()) {
            sendAndLog(adminUserId, null, NotificationType.LOW_STOCK_ADMIN,
                    NotificationChannelType.EMAIL, properties.getAdmin().getEmail(), ctx);
        }

        if (properties.getAdmin().getWhatsapp() != null && !properties.getAdmin().getWhatsapp().isBlank()) {
            sendAndLog(adminUserId, null, NotificationType.LOW_STOCK_ADMIN,
                    NotificationChannelType.WHATSAPP, properties.getAdmin().getWhatsapp(), ctx);
        }
    }

    /**
     * Send welcome notification on new user registration (Email only).
     */
    @Async
    public void notifyWelcome(Long userId, String customerName, String email, String mobile) {
        NotificationTemplateEngine.TemplateContext ctx =
                NotificationTemplateEngine.TemplateContext.forUser(customerName);

        sendAndLog(userId, null, NotificationType.WELCOME, NotificationChannelType.IN_APP,
                email != null ? email : mobile, ctx);

        if (email != null) {
            sendAndLog(userId, null, NotificationType.WELCOME, NotificationChannelType.EMAIL, email, ctx);
        }
    }

    // ── In-App inbox methods (called by REST controller) ──────────────────────

    @Transactional(readOnly = true)
    public Page<NotificationLog> getInAppNotifications(Long userId, int page, int size) {
        return logRepository.findByUserIdAndChannelOrderByCreatedAtDesc(
                userId, NotificationChannelType.IN_APP, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return logRepository.countByUserIdAndChannelAndReadAtIsNull(userId, NotificationChannelType.IN_APP);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        logRepository.markAsRead(notificationId, userId, Instant.now());
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        logRepository.markAllAsRead(userId, Instant.now());
    }

    // ── Preferences ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public NotificationPreferences getPreferences(Long userId) {
        return getPrefs(userId);
    }

    @Transactional
    public NotificationPreferences updatePreferences(Long userId, boolean emailEnabled,
                                                     boolean whatsappEnabled, boolean orderUpdatesEnabled) {
        NotificationPreferences prefs = getPrefs(userId);
        prefs.setEmailEnabled(emailEnabled);
        prefs.setWhatsappEnabled(whatsappEnabled);
        prefs.setOrderUpdatesEnabled(orderUpdatesEnabled);
        return preferencesRepository.save(prefs);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PRIVATE helpers
    // ══════════════════════════════════════════════════════════════════════════

    private void sendAndLog(Long userId, Long orderId, NotificationType type,
                            NotificationChannelType channelType, String recipient,
                            NotificationTemplateEngine.TemplateContext ctx) {

        NotificationTemplateEngine.NotificationContent content =
                templateEngine.build(type, channelType, ctx);

        // Persist log row first with PENDING status
        NotificationLog logEntry = NotificationLog.builder()
                .userId(userId)
                .orderId(orderId)
                .type(type)
                .channel(channelType)
                .recipient(recipient)
                .subject(content.subject())
                .body(content.body())
                .status(NotificationStatus.PENDING)
                .build();
        logEntry = logRepository.save(logEntry);

        // For IN_APP, we just save the row — the UI polls and reads it
        if (channelType == NotificationChannelType.IN_APP) {
            logEntry.setStatus(NotificationStatus.SENT);
            logEntry.setSentAt(Instant.now());
            logRepository.save(logEntry);
            return;
        }

        // For external channels, delegate to the channel implementation
        NotificationChannel channel = channels.get(channelType);
        if (channel == null) {
            log.warn("[Notification] No channel registered for: {}", channelType);
            logEntry.setStatus(NotificationStatus.FAILED);
            logEntry.setErrorMessage("No channel implementation registered for " + channelType);
            logRepository.save(logEntry);
            return;
        }

        try {
            channel.send(NotificationRequest.builder()
                    .recipient(recipient)
                    .subject(content.subject())
                    .body(content.body())
                    .type(type)
                    .userId(userId)
                    .orderId(orderId)
                    .build());

            logEntry.setStatus(NotificationStatus.SENT);
            logEntry.setSentAt(Instant.now());
            logRepository.save(logEntry);

        } catch (Exception e) {
            log.error("[Notification] {} via {} failed for user {}: {}", type, channelType, userId, e.getMessage());
            logEntry.setStatus(NotificationStatus.FAILED);
            logEntry.setErrorMessage(e.getMessage());
            logRepository.save(logEntry);
            // Never re-throw — notification failures must not affect the main flow
        }
    }

    /** Get or lazily create default preferences for a user. */
    private NotificationPreferences getPrefs(Long userId) {
        return preferencesRepository.findByUserId(userId)
                .orElse(NotificationPreferences.builder().userId(userId).build());
    }
}
