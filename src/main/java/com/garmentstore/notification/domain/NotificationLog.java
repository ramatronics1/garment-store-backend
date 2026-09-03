package com.garmentstore.notification.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Persists every notification dispatched by the system.
 *
 * Dual purpose:
 *  1. Audit trail for EMAIL and WHATSAPP sends.
 *  2. In-App notification inbox (channel = IN_APP, read_at = null means unread).
 */
@Entity
@Table(name = "notification_log",
    indexes = {
        @Index(name = "idx_notif_log_user_channel", columnList = "user_id, channel"),
        @Index(name = "idx_notif_log_user_unread",  columnList = "user_id, channel, read_at"),
        @Index(name = "idx_notif_log_created_at",   columnList = "created_at")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Recipient user (could be customer or admin). */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Order this notification is about (nullable for WELCOME, LOW_STOCK, etc.). */
    @Column(name = "order_id")
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannelType channel;

    /** Email address or E.164 phone number. */
    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;

    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /** For IN_APP channel: null = unread, non-null = timestamp when user read it. */
    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
