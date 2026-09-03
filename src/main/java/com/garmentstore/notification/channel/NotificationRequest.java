package com.garmentstore.notification.channel;

import com.garmentstore.notification.domain.NotificationType;
import lombok.Builder;
import lombok.Getter;

/**
 * Internal DTO passed from NotificationService to each NotificationChannel impl.
 * Carries everything a channel needs to deliver the notification.
 */
@Getter
@Builder
public class NotificationRequest {

    /** Recipient identifier: email address for EMAIL, E.164 phone for WHATSAPP, ignored for IN_APP. */
    private final String recipient;

    /** Subject line (for email) or message template name (for WhatsApp). */
    private final String subject;

    /** Full rendered message body. */
    private final String body;

    /** The event type — used for logging and routing. */
    private final NotificationType type;

    /** DB user id of recipient (for logging). */
    private final Long userId;

    /** Associated order id, if any. */
    private final Long orderId;
}
